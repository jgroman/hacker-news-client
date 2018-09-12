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

    private DataRepository(Application application, AppDatabase db) {
        mApplication = application;
        mDatabase = db;
        mStoryListDao = mDatabase.storyListDao();
        mItemDao = mDatabase.itemDao();

        mObservableStoryLists = new MediatorLiveData<>();
        mObservableStoryLists.addSource(mStoryListDao.getAllStoryLists(),
                new Observer<List<StoryList>>() {
                    @Override
                    public void onChanged(@Nullable List<StoryList> storyLists) {
                        mObservableStoryLists.postValue(storyLists);
                    }
                });

        mObservableItems = new MediatorLiveData<>();
        mObservableItems.addSource(mItemDao.getAllItems(),
                new Observer<List<Item>>() {
                    @Override
                    public void onChanged(@Nullable List<Item> value) {
                        mObservableItems.postValue(value);
                    }
                });

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
        return mObservableStoryLists;
    }

    /**
     *
     * @param storyType
     * @return
     */
    public StoryList getStoryList(String storyType) {
        List<StoryList> storyLists = mObservableStoryLists.getValue();
        if (storyLists == null) return null;

        for(StoryList storyList : storyLists) {
            if (storyList.getType().equals(storyType)) return storyList;
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
        return mObservableItems;
    }

    public Item getItem(long itemId) {
        Item item = mItemDao.getItem(itemId);
        if (item == null) {
            new LoadItemTask(mApplication, mItemDao).execute(itemId);
        }
        return item;
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

            Log.d(TAG, "doInBackground: loading item " + itemId);

            try {
                if (USE_MOCK_DATA) {
                    // Mock request used for debugging to avoid sending network queries
                    jsonItem = MockDataUtils.getMockItemJson(app.getResources(), app.getPackageName(), itemId);
                }
                else {
                    URL itemUrl = HackerNewsApi.buildItemUrl(itemId);
                    jsonItem = NetworkUtils.getResponseFromHttpUrl(itemUrl);
                }

                HackerNewsApi.HackerNewsJsonResult<Item> itemResult =
                        HackerNewsApi.getItemFromJson(jsonItem);

                resultItem = itemResult.getResult();
            }
            catch (IOException iex) {
                Log.e(TAG, String.format("IOException when fetching API item data: %s", iex.getMessage()));
                resultItem = new Item();
                resultItem.setId(itemId);
                resultItem.setTitle("Loading failed...");
            }

            mItemDao.insert(resultItem);
            return null;
        }
    }
}
