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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

public class StoryListActivity extends AppCompatActivity
        implements StoryListFragment.OnStoryClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListActivity.class.getSimpleName();

    private ViewPager mViewPager;
    public StoryTypeTabsAdapter mPagerAdapter;

    public StoryListViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        TabLayout tabLayout = findViewById(R.id.tablayout_story_type);
        AppBarLayout appbarLayout = findViewById(R.id.appbar_stories_list);

        mModel = ViewModelProviders.of(this).get(StoryListViewModel.class);
        // Create the observer for story ids which updates the UI
        final Observer<HashMap<String, long[]>> storiesObserver = new Observer<HashMap<String, long[]>>() {
            @Override
            public void onChanged(@Nullable final HashMap<String, long[]> storyIds) {
                //Log.d(TAG, "onChanged: stories");
                mPagerAdapter.notifyDataSetChanged();
            }
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        mModel.getStoryIds().observe(this, storiesObserver);

        mViewPager = findViewById(R.id.viewpager_story_type);
        mPagerAdapter = new StoryTypeTabsAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStorySelected(int position) {
        // TODO Start comments activity
    }

    /**
     *
     */
    private class StoryTypeTabsAdapter extends FragmentPagerAdapter {

        String[] mTabTitleArray;
        ArrayList<String> mStoryTypes;
        int mTabCount;

        StoryTypeTabsAdapter(FragmentManager fm, Context context) {
            super(fm);
            Resources res = context.getResources();
            mTabTitleArray = res.getStringArray(R.array.story_type_titles);
            mStoryTypes = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.story_type_strings)));
            mTabCount = mTabTitleArray.length;
        }

        @Override
        public int getCount() {
            return mTabCount;
        }


        @Override
        public Fragment getItem(int i) {
            return StoryListFragment.newInstance(mStoryTypes.get(i));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitleArray[position];
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            StoryListFragment fragment = (StoryListFragment) object;
            String storyType = fragment.getStoryType();
            int position = mStoryTypes.indexOf(storyType);

            if (position >= 0) {
                return position;
            }
            else {
                return POSITION_NONE;
            }
        }
    }

}
