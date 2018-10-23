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

import java.util.List;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.data.StoryList;
import cz.jtek.hackernewsclient.model.ItemViewModel;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

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

    /**
     * Create new instance of story fragment for stories of given type
     *
     * @param storyType Story type string
     * @return Fragment instance
     */
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

        // Story list ViewModel is scoped for this fragment instance only because story type is unique
        mStoryListModel = ViewModelProviders.of(this).get(StoryListViewModel.class);

        // Item ViewModel  is scoped for this fragment instance only because item list is unique
        mItemModel = ViewModelProviders.of(this).get(ItemViewModel.class);
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

        // Configure mStoryListModel getTypedStoryList() to provide story list of given type
        mStoryListModel.setStoryType(mStoryType);

        mStoryListRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_story_list,
                container, false);

        mLayoutManager = new LinearLayoutManager(mContext);
        mStoryListRecyclerView.setLayoutManager(mLayoutManager);
        mStoryListRecyclerView.setHasFixedSize(true);

        final StoryListAdapter adapter = new StoryListAdapter(mActivity, this);

        // Story item list observer updates source list for mItemModel.getListedItems()
        final Observer<StoryList> itemIdsObserver = itemIdList -> {
            Log.d(TAG, "*** onChanged: itemIdList, notifying list transformation - " + mStoryType);
            if (itemIdList != null) {
                mItemModel.setObservableListIds(itemIdList.getStories());
            }
        };
        mStoryListModel.getTypedStoryList().observe(this, itemIdsObserver);

        // Item list observer updates UI
        final Observer<List<Item>> itemsObserver = itemList -> {
            Log.d(TAG, "*** adapter story items livedata updated - " + mStoryType + " items " + itemList.size());
            adapter.submitList(itemList);
        };
        mItemModel.getSortedListedItems().observe(this, itemsObserver);

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
