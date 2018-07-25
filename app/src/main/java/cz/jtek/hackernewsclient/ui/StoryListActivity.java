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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils.AsyncTaskResult;

public class StoryListActivity extends AppCompatActivity
        implements StoryListFragment.OnStoryClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListActivity.class.getSimpleName();

    // AsyncTaskLoader
    private static final int LOADER_ID_STORY_LIST = 0;

    // Arguments bundle keys
    public static final String BUNDLE_STORY_LIST = "story-list";


    private Context mContext;
    private long[] mStoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        mContext = this;

        TabLayout tabLayout = findViewById(R.id.tablayout_story_type);
        ViewPager viewPager = findViewById(R.id.viewpager_story_type);
        AppBarLayout appbarLayout = findViewById(R.id.appbar_stories_list);

        viewPager.setAdapter(new StoryTypeTabsAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onStorySelected(int position) {
        // TODO Start comments activity
    }

    /**
     *
     */
    private static class StoryTypeTabsAdapter extends FragmentPagerAdapter {
        String[] mTabTitleArray;
        String[] mTabStringArray;
        int mTabCount;

        private SparseArray<Fragment> fragmentMap = new SparseArray<>();

        StoryTypeTabsAdapter(FragmentManager fm, Context context) {
            super(fm);
            mTabTitleArray = context.getResources().getStringArray(R.array.story_type_titles);
            mTabStringArray = context.getResources().getStringArray(R.array.story_type_strings);
            mTabCount = mTabTitleArray.length;
        }

        @Override
        public int getCount() {
            return mTabCount;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public Fragment getItem(int i) {
            return StoryListFragment.newInstance(mTabStringArray[i]);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragmentMap.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitleArray[position];
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }
    }

    /**
     *
     */
    private LoaderManager.LoaderCallbacks<NetworkUtils.AsyncTaskResult<long[]>> storyListLoaderListener =
            new LoaderManager.LoaderCallbacks<NetworkUtils.AsyncTaskResult<long[]>>() {

                @NonNull
                @Override
                public Loader<NetworkUtils.AsyncTaskResult<long[]>> onCreateLoader(int id, @Nullable Bundle args) {
                    //mLoadingIndicator.setVisibility(View.VISIBLE);
                    return new StoryListLoader(mContext, args);
                }

                @Override
                public void onLoadFinished(@NonNull Loader<NetworkUtils.AsyncTaskResult<long[]>> loader, NetworkUtils.AsyncTaskResult<long[]> data) {
                    //mLoadingIndicator.setVisibility(View.INVISIBLE);

                    if (data.hasException()) {
                        // There was an error during data loading
                        Exception ex = data.getException();
                        //showErrorMessage(getResources().getString(R.string.error_msg_no_data));
                    } else {
                        // Valid results received
                        mStoryList = data.getResult();

                        // RecipeListFragment receives complete recipe list as an argument
                        Bundle fragmentBundle = new Bundle();
                        fragmentBundle.putParcelableArray(BUNDLE_STORY_LIST, mStoryList);

                        StoryListFragment storyListFragment = StoryListFragment.newInstance("top");

                        // Add recipe list fragment to main activity fragment container
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.recipe_list_fragment_container, storyListFragment)
                                .commit();

                        // Destroy this loader, otherwise is gets called again during onResume
                        getSupportLoaderManager().destroyLoader(LOADER_ID_STORY_LIST);
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<NetworkUtils.AsyncTaskResult<long[]>> loader) {
                    // Not implemented
                }
            };

    /**
     * Story list async task loader implementation
     */
    public static class StoryListLoader
            extends AsyncTaskLoader<AsyncTaskResult<long[]>> {

        final PackageManager mPackageManager;
        final Bundle mArgs;
        AsyncTaskResult<long[]> mResult;

        private StoryListLoader(Context context, Bundle args) {
            super(context);
            mPackageManager = context.getPackageManager();
            mArgs = args;

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
                Log.e(TAG, String.format("IOException when fetching API data: %s", iex.getMessage()));
                mResult = new AsyncTaskResult<>(null, iex);
            }

            return mResult;
        }
    }

}
