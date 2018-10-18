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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.data.StoryList;
import cz.jtek.hackernewsclient.model.ItemViewModel;
import cz.jtek.hackernewsclient.model.StoryListViewModel;
import cz.jtek.hackernewsclient.model.StoryListViewModelFactory;

public class StoryListFragment extends Fragment
    implements StoryListAdapter.StoryListOnClickListener {

    @SuppressWarnings("unused")
    static final String TAG = StoryListFragment.class.getSimpleName();

    // Bundle arguments
    private static final String BUNDLE_STORY_TYPE = "story-type";

    // Instance state bundle keys
    private static final String KEY_STORY_TYPE = BUNDLE_STORY_TYPE;

    private Context mContext;
    private String mStoryType;
    private RecyclerView mStoryListRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private StoryListActivity mActivity;

    private ItemViewModel mItemModel;
    public StoryListViewModel mStoryListModel;

    // Custom OnStoryClickListener interface, must be implemented by container activity
    public interface OnStoryClickListener {
        void onStorySelected(long itemId);
    }

    // This is a callback to onStorySelected in container activity
    OnStoryClickListener mStoryClickListenerCallback;

    public static Fragment newInstance(@NonNull String storyType) {
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (null == mActivity) { return null; }

        mContext = mActivity.getApplicationContext();

        if (savedInstanceState != null) {
            // Restoring story type from saved instance state
            mStoryType = savedInstanceState.getString(KEY_STORY_TYPE);
        }
        else {
            // Get story type from passed arguments
            Bundle args = getArguments();
            if (args != null) {

                if (args.containsKey(BUNDLE_STORY_TYPE)) {
                    mStoryType = args.getString(BUNDLE_STORY_TYPE);
                }
            }
        }

        mStoryListModel = ViewModelProviders.of(this,
                new StoryListViewModelFactory(mActivity.getApplication(), mStoryType))
                .get(StoryListViewModel.class);

        mItemModel = ViewModelProviders.of(mActivity).get(ItemViewModel.class);

        mStoryListRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_story_list,
                container, false);

        mLayoutManager = new LinearLayoutManager(mContext);
        mStoryListRecyclerView.setLayoutManager(mLayoutManager);
        mStoryListRecyclerView.setHasFixedSize(true);

        final StoryListAdapter adapter = new StoryListAdapter(mActivity, this);

        // Create the observer for story item list which updates the UI
        final Observer<ArrayList<Long>> itemListObserver = itemList -> {
            if (itemList != null && itemList.size() > 0)
                Log.d(TAG, "*** onChanged: itemList, updating list adapter: " + mStoryType);
            else
                Log.d(TAG, "*** onChanged: itemList, list null");
            adapter.submitList(itemList);
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        mStoryListModel.getTypedStoryList().observe(this, itemListObserver);

        // Create the observer for items which updates the UI
        final Observer<List<Item>> itemsObserver = item -> {
            Log.d(TAG, "*** adapter story items livedata updated ");
            adapter.notifyDataSetChanged();
            //adapter.submitList(itemList);
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer
        mItemModel.getAllItems().observe(this, itemsObserver);

        mStoryListRecyclerView.setAdapter(adapter);

        return mStoryListRecyclerView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Store story type
        outState.putString(KEY_STORY_TYPE, mStoryType);

        super.onSaveInstanceState(outState);
    }

    /**
     * StoryList list item click listener
     *
     * @param itemId Clicked item id
     */
    @Override
    public void onClick(long itemId) {
        // OnClick event is passed to activity
        mStoryClickListenerCallback.onStorySelected(itemId);
    }

    public String getStoryType() {
        return mStoryType;
    }

}
