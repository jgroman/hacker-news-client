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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataRepository {

    private MutableLiveData<HashMap<String, ArrayList<Long>>> mStories;
    private StoryDao mStoryDao;

    private ItemDao mItemDao;
    private LiveData<List<Item>> mAllItems;

    DataRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        mItemDao = db.itemDao();
        mAllItems = mItemDao.getAllItems();
    }

    public LiveData<HashMap<String, ArrayList<Long>>> getStories() {
        if (mStories == null) {
            // Local cache not available, trying db first

        }

    }


    LiveData<List<Item>> getAllItems() {
        return mAllItems;
    }


    public void insert(Item item) {
        new insertAsyncTask(mItemDao).execute(item);
    }

    private static class insertAsyncTask extends AsyncTask<Item, Void, Void> {

        private ItemDao mAsyncTaskDao;

        insertAsyncTask(ItemDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Item... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

}
