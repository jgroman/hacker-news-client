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

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.Item;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

public class StoryListFragment extends Fragment
    implements StoryListAdapter.StoryListOnClickListener {

    @SuppressWarnings("unused")
    static final String TAG = StoryListFragment.class.getSimpleName();

    // Bundle arguments
    public static final String BUNDLE_STORY_TYPE = "story-type";

    // Instance state bundle keys
    private static final String KEY_STORY_TYPE = BUNDLE_STORY_TYPE;

    private Context mContext;
    private String mStoryType;
    private long[] mStoryList;
    private RecyclerView mStoryListRecyclerView;
    private StoryListAdapter mStoryListAdapter;
    private LinearLayoutManager mLayoutManager;

    private StoryListActivity mActivity;
    private StoryListViewModel mModel;

    // Custom OnStoryClickListener interface, must be implemented by container activity
    public interface OnStoryClickListener {
        void onStorySelected(int position);
    }

    // This is a callback to onStorySelected in container activity
    OnStoryClickListener mStoryClickListenerCallback;

    public static Fragment newInstance(@NonNull String storyType) {
        //Log.d(TAG, "*** StoryListFragment newInstance " + storyType);
        Bundle arguments = new Bundle();
        arguments.putString(BUNDLE_STORY_TYPE, storyType);
        StoryListFragment fragment = new StoryListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    // Overriding onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mStoryClickListenerCallback = (OnStoryClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnStoryClickListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (StoryListActivity) getActivity();
        if (null == mActivity) { return; }

        mModel = ViewModelProviders.of(getActivity()).get(StoryListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mContext = mActivity.getApplicationContext();

        if (savedInstanceState != null) {
            // Restoring story type and list from saved instance state
            mStoryType = savedInstanceState.getString(KEY_STORY_TYPE);
            //Log.d(TAG, "*** StoryListFragment onCreateView restoring: " + mStoryType);
        }
        else {
            // Get story type and list from passed arguments
            Bundle args = getArguments();
            if (args != null) {

                if (args.containsKey(BUNDLE_STORY_TYPE)) {
                    mStoryType = args.getString(BUNDLE_STORY_TYPE);
                }
            }

            //Log.d(TAG, "*** StoryListFragment onCreateView full load: " + mStoryType);
        }

        mStoryList = mModel.getStoryIds().getValue().get(mStoryType);

        mStoryListRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_story_list, container, false);

        mStoryListAdapter = new StoryListAdapter(mActivity, mStoryType, this);
        mStoryListRecyclerView.setAdapter(mStoryListAdapter);

        mLayoutManager = new LinearLayoutManager(mContext);
        mStoryListRecyclerView.setLayoutManager(mLayoutManager);
        mStoryListRecyclerView.setHasFixedSize(true);

        // Create the observer for story items which updates the UI
        final Observer<LongSparseArray<Item>> itemsObserver = new Observer<LongSparseArray<Item>>() {
            @Override
            public void onChanged(@Nullable LongSparseArray<Item> itemLongSparseArray) {
                //Log.d(TAG, "onChanged: items");
                mStoryListAdapter.notifyDataSetChanged();
            }
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        mModel.getStoryItems().observe(this, itemsObserver);

        return mStoryListRecyclerView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Store story type
        outState.putString(KEY_STORY_TYPE, mStoryType);

        super.onSaveInstanceState(outState);
    }

    /**
     * Story list item click listener
     *
     * @param position Id of the clicked list item
     */
    @Override
    public void onClick(int position) {
        mStoryClickListenerCallback.onStorySelected(position);
    }

    public String getStoryType() {
        return mStoryType;
    }

}
