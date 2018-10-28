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
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.data.StoryList;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

public class StoryListActivity extends AppCompatActivity
        implements StoryListFragment.OnStoryClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListActivity.class.getSimpleName();

    private ViewPager mViewPager;
    public StoryTypeTabsAdapter mPagerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public StoryListViewModel mStoryListModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        TabLayout tabLayout = findViewById(R.id.tablayout_story_type);
        //AppBarLayout appbarLayout = findViewById(R.id.appbar_story_list);

        mViewPager = findViewById(R.id.viewpager_story_list);
        mPagerAdapter = new StoryTypeTabsAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        mStoryListModel = ViewModelProviders.of(this).get(StoryListViewModel.class);

        // Create the observer for stories which updates the UI
        final Observer<List<StoryList>> storiesObserver = storyLists -> {
            Log.d(TAG, "*** onChanged: stories, updating pager adapter");
            mPagerAdapter.notifyDataSetChanged();
        };
        // Observe all story list LiveData, on change update pager adapter
        mStoryListModel.getAllStoryLists().observe(this, storiesObserver);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.srl_story_list);
        mSwipeRefreshLayout.setOnRefreshListener(this::refreshLayout);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_story_list);
        toolbar.inflateMenu(R.menu.menu_story_list);

    }

    /**
     * Story item click handler
     *
     * @param itemId Id of clicked item
     */
    @Override
    public void onStorySelected(long itemId) {
        // Starting CommentListActivity
        Intent intent = CommentListActivity.newInstance(this, itemId);
        startActivity(intent);
    }

    /**
     * Story item long click handler
     *
     * @param item Long clicked item instance
     */
    @Override
    public void onStoryLongPressed(Item item) {
        // Open story URL
        Uri itemUri = Uri.parse(item.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, itemUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void refreshLayout() {
        // TODO
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This pager adapter displays each type of story in a separate tab
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
