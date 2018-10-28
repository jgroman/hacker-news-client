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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
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
        implements StoryListFragment.OnStoryClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{

    @SuppressWarnings("unused")
    private static final String TAG = StoryListActivity.class.getSimpleName();

    private ViewPager mViewPager;
    public StoryTypeTabsAdapter mPagerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public StoryListViewModel mStoryListModel;

    // Shared preferences
    private SharedPreferences mPrefs;
    private static boolean sPrefsUpdatedFlag = false;
    private static final String PREF_KEY_CUSTOM_TABS = "pref_key_custom_tabs";
    private Boolean mPrefUseCustomTabs;


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

        // Observer for story lists updates the UI on list change
        final Observer<List<StoryList>> storiesObserver = storyLists -> {
            Log.d(TAG, "*** onChanged: stories, updating pager adapter");
            mSwipeRefreshLayout.setRefreshing(false);
            mPagerAdapter.notifyDataSetChanged();
        };
        // -- Observe all story list LiveData, on change update pager adapter
        mStoryListModel.getAllStoryLists().observe(this, storiesObserver);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.srl_story_list);
        mSwipeRefreshLayout.setOnRefreshListener(this::refreshLayout);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_story_list);
        toolbar.inflateMenu(R.menu.menu_story_list);
        // -- Menu
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_refresh:
                    // Reload stories from API
                    mSwipeRefreshLayout.setRefreshing(true);
                    refreshLayout();
                    return true;
                case R.id.menu_item_settings:
                    // Open Settings
                    Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                    startActivity(startSettingsActivity);
                    return true;
            }
            return false;
        });

        // Shared Preferences and preference change listener
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        // -- Obtain current custom tabs status from shared preferences
        mPrefUseCustomTabs = mPrefs.getBoolean(PREF_KEY_CUSTOM_TABS, true);

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

        Boolean useCustomTabs = mPrefs.getBoolean(PREF_KEY_CUSTOM_TABS, true);
        if (useCustomTabs) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            // set toolbar color and/or setting custom actions before invoking build()
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
            CustomTabsIntent customTabsIntent = builder.build();
            // and launch the desired Url with CustomTabsIntent.launchUrl()
            customTabsIntent.launchUrl(this, itemUri);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW, itemUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    /**
     * Shared preference change listener, nn preference change sets global flag
     *
     * @param sharedPreferences Shared preferences
     * @param key               Changed preference key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        sPrefsUpdatedFlag = true;
    }

    /**
     * Reloads story lists content from API
     * Observer will update the UI
     */
    private void refreshLayout() {
        mStoryListModel.loadStoryLists();
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
