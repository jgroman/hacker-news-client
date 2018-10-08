/*
 * Copyright 2018 Jaroslav Groman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.jtek.hackernewsclient.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;

public class DataRepository {

    @SuppressWarnings("unused")
    static final String TAG = DataRepository.class.getSimpleName();

    private static final boolean USE_MOCK_DATA = true;

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private final Application mApplication;

    private StoryListDao mStoryListDao;
    private MediatorLiveData<List<StoryList>> mObservableStoryLists;

    private ItemDao mItemDao;
    private MediatorLiveData<List<Item>> mObservableItems;
    private MediatorLiveData<List<Item>> mObservableCommentItems;

    private DataRepository(Application application, AppDatabase db) {
        mApplication = application;
        mDatabase = db;
        mStoryListDao = mDatabase.storyListDao();
        mItemDao = mDatabase.itemDao();

        mObservableStoryLists = new MediatorLiveData<>();
        mObservableStoryLists.addSource(mStoryListDao.getAllStoryLists(),
                storyLists -> mObservableStoryLists.postValue(storyLists));

        mObservableItems = new MediatorLiveData<>();
        mObservableItems.addSource(mItemDao.getAllItems(),
                value -> mObservableItems.postValue(value));

        mObservableCommentItems = new MediatorLiveData<>();
        mObservableCommentItems.addSource(mItemDao.getAllCommentItems(),
                allComments -> mObservableCommentItems.postValue(allComments));

        // Start loading story lists
        new LoadStoryListsTask(mApplication, mStoryListDao).execute();
    }

    public static DataRepository getInstance(Application app, final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(app, database);
                }
            }
        }
        return sInstance;
    }

    /**
     *
     * @return
     */
    public LiveData<List<StoryList>> getAllStoryLists() {
        Log.d(TAG, "*** getAllStoryLists: ");
        return mObservableStoryLists;
    }


    /**
     * Gets list of all story ids belonging to specific story type
     *
     * @param storyLists
     * @param storyType
     * @return
     */
    public LiveData<ArrayList<Long>> getTypedStoryList(List<StoryList> storyLists, String storyType) {
        Log.d(TAG, "*** getTypedStoryList: " + storyType);
        if (storyLists == null) return null;

        for(StoryList storyList : storyLists) {
            if (storyList.getType().equals(storyType)) {
                MediatorLiveData<ArrayList<Long>> sl = new MediatorLiveData<>();
                sl.setValue(storyList.getStories());
                return sl;
            }
        }
        return null;
    }

    /**
     *
     */
    private static class LoadStoryListsTask extends AsyncTask<Void, Void, Void> {

        private Application app;
        private StoryListDao mStoryListDao;

        LoadStoryListsTask(Application application, StoryListDao dao) {
            app = application;
            mStoryListDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            StoryList resultStoryList;
            String jsonStoryList;

            // Get all available tab story types from resources
            String[] storyTypes = app.getResources().getStringArray(R.array.story_type_strings);

            // Load all available story types
            for (String storyType : storyTypes) {

                // Load story list JSON
                Log.d(TAG, "*** doInBackground: loading story list from API: " + storyType);
                URL storiesUrl = HackerNewsApi.buildStoriesUrl(storyType);

                try {
                    if (USE_MOCK_DATA) {
                        // Mock request used for debugging to avoid sending network queries
                        jsonStoryList = MockDataUtils.getMockStoriesJson(app.getResources(), app.getPackageName(), storyType);
                    } else {
                        // Network request for stories from HN API
                        jsonStoryList = NetworkUtils.getResponseFromHttpUrl(storiesUrl);
                    }

                    HackerNewsApi.HackerNewsJsonResult<ArrayList<Long>> storyListResult =
                            HackerNewsApi.getStoriesFromJson(jsonStoryList);

                    resultStoryList = new StoryList(storyType, storyListResult.getResult());
                    Log.d(TAG, "*** doInBackground: loaded items: " + resultStoryList.getStories().size());

                } catch (IOException iex) {
                    Log.e(TAG, String.format("IOException when fetching API story data: %s", iex.getMessage()));
                    resultStoryList = new StoryList(storyType, new ArrayList<Long>());
                }

                // Insert story to db
                mStoryListDao.insert(resultStoryList);
            }
            return null;
        }
    }


    public LiveData<List<Item>> getAllItems() {
        Log.d(TAG, "*** getAllItems: ");
        return mObservableItems;
    }

    public LiveData<List<Item>> getAllCommentItems() {
        Log.d(TAG, "*** getAllCommentItems: ");
        return mObservableCommentItems;
    }

    private Item findItemById(List<Item> itemList, long itemId) {
        if (itemList == null) return null;
        for(Item item : itemList) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public Item getItem(long itemId) {
        List<Item> itemList = mObservableItems.getValue();

        Item item = findItemById(itemList, itemId);
        if (item != null) {
            Log.d(TAG, "getItem: " + itemId + " found in cache");
            return item;
        }

        // Item is not cached yet, start loading from API
        // LiveData will propagate item data to cache automagically
        Log.d(TAG, "getItem: " + itemId + " asynctask loading");
        new LoadItemTask(mApplication, mItemDao).execute(itemId);
        return null;
    }

    private void updateKidNestLevel(List<Item> itemList, ArrayList<Long> kidList, int nestLevel) {
        if (itemList == null) return;

        if (kidList != null && kidList.size() > 0) {
            for (long kidId : kidList) {
                Item kidItem = findItemById(itemList, kidId);
                if (kidItem != null) {
                    kidItem.setNestLevel(nestLevel);
                    //Log.d(TAG, "updateKidNestLevel: " + kidId + ", level " + nestLevel);
                    new UpdateItemsTask(mApplication, mItemDao).execute(kidItem);
                }
            }
        }
    }

    /**
     * Get list of item kids (comments)
     *
     * @param itemList
     * @param itemId
     * @return
     */
    public LiveData<ArrayList<Long>> getItemKidsList(List<Item> itemList, long itemId) {
        if (itemList == null) return null;

        Item parentItem, workItem;
        ArrayList<Long> resultKidList, workKidList;
        int currentNestingLevel;

        resultKidList = new ArrayList<>();

        Log.d(TAG, "getItemKidsList: getting kids from " + itemId);
        parentItem = findItemById(itemList, itemId);
        if (parentItem != null) {
            resultKidList = parentItem.getKids();
            if (resultKidList != null && resultKidList.size() > 0) {
                // All items are set to nest level 1 by default when loading

                int currentKidIndex = 0;
                // Traversing comment tree, using cached items only
                do {
                    //Log.d(TAG, "getItemKidsList: processing kid list position " + currentKidIndex);
                    workItem = findItemById(itemList, resultKidList.get(currentKidIndex));
                    if (workItem != null) {
                        currentNestingLevel = workItem.getNestLevel();
                        //Log.d(TAG, "getItemKidsList: adding kids from " + workItem.getId() + " at level " + currentNestingLevel);
                        workKidList = workItem.getKids();
                        if (workKidList != null && workKidList.size() > 0) {
                            //updateKidNestLevel(itemList, workKidList, currentNestingLevel + 1);
                            resultKidList.addAll(currentKidIndex + 1, workKidList);
                        }
                    }
                    currentKidIndex++;
                } while (currentKidIndex < resultKidList.size());
            }
        }

        MediatorLiveData<ArrayList<Long>> result = new MediatorLiveData<>();
        result.setValue(resultKidList);
        return result;
    }


    private static class LoadItemTask extends AsyncTask<Long, Void, Void> {

        private Application app;
        private ItemDao mItemDao;

        LoadItemTask(Application application, ItemDao dao) {
            this.app = application;
            this.mItemDao = dao;
        }

        @Override
        protected Void doInBackground(Long... longs) {
            Item resultItem;
            String jsonItem;

            long itemId = longs[0];

            try {
                if (USE_MOCK_DATA) {
                    // Mock request used for debugging to avoid sending network queries
                    jsonItem = MockDataUtils.getMockItemJson(app.getResources(), app.getPackageName(), itemId);
                } else {
                    URL itemUrl = HackerNewsApi.buildItemUrl(itemId);
                    jsonItem = NetworkUtils.getResponseFromHttpUrl(itemUrl);
                }

                HackerNewsApi.HackerNewsJsonResult<Item> itemResult =
                        HackerNewsApi.getItemFromJson(jsonItem);

                resultItem = itemResult.getResult();
            } catch (IOException iex) {
                Log.e(TAG, String.format("IOException when fetching API item data: %s", iex.getMessage()));
                resultItem = new Item();
                resultItem.setId(itemId);
                resultItem.setTitle("Loading failed...");
                resultItem.setText("Loading failed...");
                resultItem.setType("comment");
            }

            mItemDao.insert(resultItem);
            return null;
        }
    }

    private static class UpdateItemsTask extends AsyncTask<Item, Void, Void> {
        private Application mApp;
        private ItemDao mItemDao;

        UpdateItemsTask(Application application, ItemDao itemDao) {
            mApp = application;
            mItemDao = itemDao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            mItemDao.updateItems(items);
            return null;
        }
    }
}
