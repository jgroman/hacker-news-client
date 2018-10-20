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
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
    private static final String TAG = DataRepository.class.getSimpleName();

    private static final boolean USE_MOCK_DATA = true;

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private final Application mApplication;

    private StoryListDao mStoryListDao;
    private MediatorLiveData<List<StoryList>> mObservableStoryLists;

    private ItemDao mItemDao;
    private MediatorLiveData<List<Item>> mObservableItems;
    private MediatorLiveData<List<Item>> mObservableStoryItems;
    private MediatorLiveData<List<Item>> mObservableCommentItems;

    private MediatorLiveData<List<Item>> mObservableListedItems;


    private static List<Long> mItemsBeingLoaded;

    private static final Item EMPTY_ITEM = new Item();

    /**
     * Returns repository singleton instance
     *
     * @param app
     * @param database
     * @return
     */
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
     * Constructor
     *
     * @param application
     * @param db
     */
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

        mObservableStoryItems = new MediatorLiveData<>();
        mObservableStoryItems.addSource(mItemDao.getAllStoryItems(),
                allStories -> mObservableStoryItems.postValue(allStories));

        mObservableCommentItems = new MediatorLiveData<>();
        mObservableCommentItems.addSource(mItemDao.getAllCommentItems(),
                allComments -> mObservableCommentItems.postValue(allComments));

        mItemsBeingLoaded = new ArrayList<>();

        EMPTY_ITEM.setTitle("...");
        EMPTY_ITEM.setText(".....");

        // Start loading story lists
        new LoadStoryListsTask(mApplication, mStoryListDao).execute();
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
     * @param storyType Story type string
     * @return StoryList
     */
    public LiveData<StoryList> getTypedStoryList(String storyType) {
        Log.d(TAG, "*** getTypedStoryList: " + storyType);
        return mStoryListDao.getStoryList(storyType);
    }

    /**
     * Async loader for story lists
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

    public LiveData<List<Item>> getAllStoryItems() {
        Log.d(TAG, "*** getAllStoryItems: ");
        return mObservableStoryItems;
    }

    public LiveData<List<Item>> getAllCommentItems() {
        Log.d(TAG, "*** getAllCommentItems: ");
        return mObservableCommentItems;
    }

    public LiveData<List<Item>> getItemList(@NonNull List<Long> itemIds) {
        Log.d(TAG, "*** getItemList: ");
        return mItemDao.getItemsByIds(itemIds);
    }

    public Item findItemById(List<Item> itemList, long itemId) {
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
            // Item is already loaded in db
            Log.d(TAG, "*** getItem: " + itemId + " found in cache");
            mItemsBeingLoaded.remove(itemId);
            return item;
        }

        if (!mItemsBeingLoaded.contains(itemId)) {
            // Item is not cached or being loaded yet, start loading from API
            // LiveData will propagate item data to cache automagically
            Log.d(TAG, "*** getItem: asynctask loading " + itemId);
            mItemsBeingLoaded.add(itemId);
            new LoadItemTask(mApplication, mItemDao).execute(itemId);
        }
        else {
            Log.d(TAG, "*** getItem: in progress " + itemId);
        }

        // Return empty item
        return EMPTY_ITEM;
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
     * Calculates item nest level in comment tree
     *
     * @param itemList
     * @param itemId
     * @return
     */
    public int getItemNestLevel(List<Item> itemList, long itemId) {
        if (itemList == null) return -1;

        Item item = findItemById(itemList, itemId);
        if (item != null) {
            if (item.getParent() == 0) {
                return 1;
            }
            else {
                return getItemNestLevel(itemList, item.getParent()) + 1;
            }
        }
        return -1;
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

        Log.d(TAG, "*** getItemKidsList: Adding kids for parent " + itemId);

        resultKidList = new ArrayList<>();

        parentItem = findItemById(itemList, itemId);
        if (parentItem != null) {
            resultKidList = parentItem.getKids();
            if (resultKidList != null && resultKidList.size() > 0) {
                // All items are set to nest level 1 by default when loading

                int currentKidIndex = 0;
                int totalKidsAdded = 0;
                // Traversing comment tree, using cached items only
                do {
                    //Log.d(TAG, "getItemKidsList: processing kid list position " + currentKidIndex);
                    workItem = findItemById(itemList, resultKidList.get(currentKidIndex));
                    if (workItem != null) {
                        currentNestingLevel = workItem.getNestLevel();
                        //Log.d(TAG, "getItemKidsList: adding kids from " + workItem.getId() + " at level " + currentNestingLevel);
                        workKidList = workItem.getKids();
                        if (workKidList != null && workKidList.size() > 0) {
                            totalKidsAdded += workKidList.size();
                            Log.d(TAG, "*** getItemKidsList: Added kids " + workKidList.size()+ " for item " + workItem.getId() + " at level " + getItemNestLevel(itemList, workItem.getId()));
                            //updateKidNestLevel(itemList, workKidList, currentNestingLevel + 1);
                            resultKidList.addAll(currentKidIndex + 1, workKidList);
                        }
                    }
                    currentKidIndex++;
                } while (currentKidIndex < resultKidList.size() || currentKidIndex < 20);
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
                Log.d(TAG, "*** doInBackground: loading " + itemId);
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
                resultItem.setTitle("Loading " + Long.toString(itemId) + " failed...");
                resultItem.setText("Loading " + Long.toString(itemId) + " failed...");
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
