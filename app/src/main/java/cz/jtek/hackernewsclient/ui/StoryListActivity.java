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

package cz.jtek.hackernewsclient.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.model.Item;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils.AsyncTaskResult;

public class StoryListActivity extends AppCompatActivity
        implements StoryListFragment.OnStoryClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListActivity.class.getSimpleName();

    // Bundle arguments
    public static final String BUNDLE_STORY_TYPE = "story-type";
    public static final String BUNDLE_ITEM_ID = "item-id";


    private HashMap<String, long[]> mStoriesMap = new HashMap<>();
    public LongSparseArray<Item> mItemCache = new LongSparseArray<>();

    private Context mContext;

    private ViewPager mViewPager;
    public StoryTypeTabsAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        mContext = this;

        TabLayout tabLayout = findViewById(R.id.tablayout_story_type);
        mViewPager = findViewById(R.id.viewpager_story_type);
        AppBarLayout appbarLayout = findViewById(R.id.appbar_stories_list);

        mPagerAdapter = new StoryTypeTabsAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        // Get all available tab story types
        String[] storyTypes = getResources().getStringArray(R.array.story_type_strings);

        // Start loaders for all available story types
        for (int i = 0; i < storyTypes.length; i++) {
            Bundle loaderBundle = new Bundle();
            loaderBundle.putString(BUNDLE_STORY_TYPE, storyTypes[i]);
            getSupportLoaderManager().initLoader(i, loaderBundle, new StoryListLoaderListener());
        }

    }

    @Override
    public void onStorySelected(int position) {
        // TODO Start comments activity
    }

    /**
     *
     */
    private class StoryTypeTabsAdapter extends FragmentStatePagerAdapter {

        String[] mTabTitleArray;
        String[] mStoryTypeArray;
        int mTabCount;

        StoryTypeTabsAdapter(FragmentManager fm, Context context) {
            super(fm);
            mTabTitleArray = context.getResources().getStringArray(R.array.story_type_titles);
            mStoryTypeArray = context.getResources().getStringArray(R.array.story_type_strings);
            mTabCount = mTabTitleArray.length;
        }

        @Override
        public int getCount() {
            return mTabCount;
        }


        @Override
        public Fragment getItem(int i) {

            if (mStoriesMap.get(mStoryTypeArray[i]) == null) {
                long[] dummyArray = new long[] {};
                mStoriesMap.put(mStoryTypeArray[i], dummyArray);
            }

            Fragment fragment = StoryListFragment.newInstance(mStoryTypeArray[i], mStoriesMap.get(mStoryTypeArray[i]));
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitleArray[position];
        }

        @Override
        public int getItemPosition(Object object) {
            //Log.d(TAG, "*** getItemPosition: ");
            return POSITION_NONE;
        }
    }

    /**
     *
     */
    private class StoryListLoaderListener implements LoaderManager.LoaderCallbacks<AsyncTaskResult<long[]>> {
        private Bundle mArgs;

        @NonNull
        @Override
        public Loader<AsyncTaskResult<long[]>> onCreateLoader(int id, @Nullable Bundle args) {
            //mLoadingIndicator.setVisibility(View.VISIBLE);
            Log.d(TAG, "*** onCreateLoader: " + args.getString(BUNDLE_STORY_TYPE));
            mArgs = args;
            return new StoryListLoader(mContext, args);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AsyncTaskResult<long[]>> loader, AsyncTaskResult<long[]> data) {
            //mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (data.hasException()) {
                // There was an error during data loading
                Exception ex = data.getException();
                //showErrorMessage(getResources().getString(R.string.error_msg_no_data));
            } else {
                // Valid results received
                String storyType = mArgs.getString(BUNDLE_STORY_TYPE);
                Log.d(TAG, "*** onLoadFinished: loaded stories for: " + storyType + ", " + data.getResult()[1]);
                mStoriesMap.put(storyType, data.getResult());

                mPagerAdapter.notifyDataSetChanged();

                // Destroy this loader, otherwise is gets called again during onResume
                //getLoaderManager().destroyLoader(LOADER_ID_STORY_LIST);
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<AsyncTaskResult<long[]>> loader) {
            // Not implemented
        }

    }

    /**
     * Story list async task loader implementation
     */
    public static class StoryListLoader
            extends AsyncTaskLoader<AsyncTaskResult<long[]>> {

        final PackageManager mPackageManager;
        AsyncTaskResult<long[]> mResult;

        private final String mStoryType;
        private final boolean mUseMockData;

        private StoryListLoader(Context context, Bundle args) {
            super(context);
            mPackageManager = context.getPackageManager();
            mStoryType = args.getString(BUNDLE_STORY_TYPE);
            mUseMockData = true;
        }

        @Override
        protected void onStartLoading() {
            if (mResult != null && (mResult.hasResult() || mResult.hasException())) {
                // If there are already data available, deliver them
                deliverResult(mResult);
            } else {
                // Start loader
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        public AsyncTaskResult<long[]> loadInBackground() {
            String jsonStoryList;

            try {
                // Load story list JSON
                URL storiesUrl = HackerNewsApi.buildStoriesUrl(mStoryType);

                if (mUseMockData) {
                    // Mock request used for debugging to avoid sending network queries
                    jsonStoryList = MockDataUtils.getMockStoriesJson(getContext(), mStoryType);
                } else {
                    jsonStoryList = NetworkUtils.getResponseFromHttpUrl(storiesUrl);
                }

                HackerNewsApi.HackerNewsJsonResult<long[]> storyListResult =
                        HackerNewsApi.getStoriesFromJson(jsonStoryList);

                mResult = new AsyncTaskResult<>(storyListResult.getResult(), storyListResult.getException());
            } catch (IOException iex) {
                Log.e(TAG, String.format("IOException when fetching API story data: %s", iex.getMessage()));
                mResult = new AsyncTaskResult<>(null, iex);
            }

            return mResult;
        }
    }

    /**
     *
     * @param itemId
     */
    public void startItemLoader(long itemId) {
        // Create temporary item placeholder in cache to prevent multiple loading
        Item tempItem = new Item();
        tempItem.setTitle("...");
        mItemCache.put(itemId, tempItem);

        Bundle loaderBundle = new Bundle();
        loaderBundle.putLong(BUNDLE_ITEM_ID, itemId);

        // Init item loader
        // Quick hack to get unique loader id. Should be OK until HN story id > MAX_INT.
        getSupportLoaderManager().initLoader((int) itemId, loaderBundle, new ItemLoaderListener());
    }

    /**
     *
     */
    private class ItemLoaderListener
            implements LoaderManager.LoaderCallbacks<NetworkUtils.AsyncTaskResult<Item>> {

        private long mItemId;

        @NonNull
        @Override
        public Loader<NetworkUtils.AsyncTaskResult<Item>> onCreateLoader(int id, Bundle bundle) {
            mItemId = bundle.getLong(BUNDLE_ITEM_ID);
            //Log.d(TAG, "onCreateLoader: " + mItemId);
            return new ItemLoader(mContext, bundle);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<NetworkUtils.AsyncTaskResult<Item>> loader,
                                   NetworkUtils.AsyncTaskResult<Item> itemAsyncTaskResult) {
            //long itemId = mArgs.getLong(BUNDLE_ITEM_ID);

            if (itemAsyncTaskResult.hasException()) {
                // There was an error during data loading
                Exception ex = itemAsyncTaskResult.getException();
                Log.d(TAG, "*** onLoadFinished: Exception for item: " + mItemId + ", msg: " + ex.getMessage());
                //showErrorMessage(getResources().getString(R.string.error_msg_no_data));
                mPagerAdapter.notifyDataSetChanged();
            }
            else {
                // Valid results received
                Item item = itemAsyncTaskResult.getResult();
                mItemCache.put(mItemId, item);

                // Refresh pager contents
                mPagerAdapter.notifyDataSetChanged();

                // Destroy this loader, otherwise is gets called again during onResume
                //getLoaderManager().destroyLoader(LOADER_ID_STORY_LIST);
            }

        }

        @Override
        public void onLoaderReset(@NonNull Loader<NetworkUtils.AsyncTaskResult<Item>> loader) {
            // Not implemented
        }
    }

    private static class ItemLoader extends AsyncTaskLoader<NetworkUtils.AsyncTaskResult<Item>> {

        private NetworkUtils.AsyncTaskResult<Item> mResult;
        private final long mItemId;
        private final boolean mUseMockData;

        ItemLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            mItemId = bundle.getLong(BUNDLE_ITEM_ID);
            mUseMockData = true;
        }

        @Override
        protected void onStartLoading() {
            Log.d(TAG, "***onStartLoading: item " + mItemId);
            if (mResult != null && (mResult.hasResult() || mResult.hasException())) {
                // If there are already data available, deliver them
                deliverResult(mResult);
            }
            else {
                // Start loader
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Nullable
        @Override
        public NetworkUtils.AsyncTaskResult<Item> loadInBackground() {
            String jsonItem;

            try {
                if (mUseMockData) {
                    // Mock request used for debugging to avoid sending network queries
                    jsonItem = MockDataUtils.getMockItemJson(getContext(), mItemId);
                }
                else {
                    URL itemUrl = HackerNewsApi.buildItemUrl(mItemId);
                    jsonItem = NetworkUtils.getResponseFromHttpUrl(itemUrl);
                }

                HackerNewsApi.HackerNewsJsonResult<Item> itemResult =
                        HackerNewsApi.getItemFromJson(jsonItem);
                mResult = new AsyncTaskResult<>(itemResult.getResult(), itemResult.getException());
            }
            catch (IOException iex) {
                Log.e(TAG, String.format("IOException when fetching API item data: %s", iex.getMessage()));
                mResult = new AsyncTaskResult<>(null, iex);
            }

            return mResult;
        }
    }

}
