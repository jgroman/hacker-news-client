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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils.AsyncTaskResult;

public class StoryListActivity extends AppCompatActivity
        implements StoryListFragment.OnStoryClickListener, StoryListFragment.OnUpdateListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListActivity.class.getSimpleName();


    // Arguments bundle keys


    private Context mContext;
    private long[] mStoryList;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        mContext = this;

        TabLayout tabLayout = findViewById(R.id.tablayout_story_type);
        mViewPager = findViewById(R.id.viewpager_story_type);
        AppBarLayout appbarLayout = findViewById(R.id.appbar_stories_list);

        mViewPager.setAdapter(new StoryTypeTabsAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStorySelected(int position) {
        // TODO Start comments activity
    }

    @Override
    public void onUpdate() {
        Log.d(TAG, "***  onUpdate: ");
        if (mViewPager != null && mViewPager.getAdapter() != null) {
            mViewPager.getAdapter().notifyDataSetChanged();
        }
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

        @Override
        public int getItemPosition(Object object) {
            Log.d(TAG, "*** getItemPosition: ");
            return POSITION_NONE;
        }
    }


}
