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
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

public class CommentListFragment extends Fragment
    implements CommentListAdapter.CommentListOnClickListener {

    @SuppressWarnings("unused")
    static final String TAG = CommentListFragment.class.getSimpleName();

    // Bundle arguments
    private static final String BUNDLE_STORY_ID = "story-id";

    // Instance state bundle keys
    private static final String KEY_STORY_ID = BUNDLE_STORY_ID;

    private CommentListActivity mActivity;
    private StoryListViewModel mModel;
    private long mStoryId;
    private RecyclerView mCommentListRecyclerView;
    private CommentListAdapter mCommentListAdapter;


    // Custom OnCommentClickListener interface, must be implemented by container activity
    public interface OnCommentClickListener {
        void onCommentSelected(long itemId);
    }

    // This is a callback to onCommentSelected in container activity
    OnCommentClickListener mCommentClickListenerCallback;

    public static CommentListFragment newInstance(@NonNull long storyId) {
        Bundle arguments = new Bundle();
        arguments.putLong(BUNDLE_STORY_ID, storyId);
        CommentListFragment fragment = new CommentListFragment();
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
            mCommentClickListenerCallback = (OnCommentClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCommentClickListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (CommentListActivity) getActivity();
        if (null == mActivity) { return; }

        mModel = ViewModelProviders.of(getActivity()).get(StoryListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Context context = mActivity.getApplicationContext();

        if (savedInstanceState != null) {
            // Restoring story id from saved instance state
            mStoryId = savedInstanceState.getLong(KEY_STORY_ID);
        }
        else {
            // Get story id from passed arguments
            Bundle args = getArguments();
            if (args != null) {
                if (args.containsKey(BUNDLE_STORY_ID)) {
                    mStoryId = args.getLong(BUNDLE_STORY_ID);
                }
            }
        }

        mCommentListRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_comment_list,
                container, false);

        mCommentListAdapter = new CommentListAdapter(mActivity, mStoryId, this);
        mCommentListRecyclerView.setAdapter(mCommentListAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mCommentListRecyclerView.setLayoutManager(layoutManager);
        mCommentListRecyclerView.setHasFixedSize(true);

        // Observer for comment items
        final Observer<LongSparseArray<Item>> commentsObserver = new Observer<LongSparseArray<Item>>() {
            @Override
            public void onChanged(@Nullable LongSparseArray<Item> itemLongSparseArray) {
                // On comment changes update adapter contents
                mCommentListAdapter.notifyDataSetChanged();
            }
        };

        // Start observing comment LiveData
        mModel.getStoryItems().observe(this, commentsObserver);

        return mCommentListRecyclerView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Store commented story item id
        outState.putLong(KEY_STORY_ID, mStoryId);

        super.onSaveInstanceState(outState);
    }

    /**
     * Comment list item click listener
     *
     * @param itemId Clicked item id
     */
    @Override
    public void onClick(long itemId) {
        // OnClick event is passed to activity
        mCommentClickListenerCallback.onCommentSelected(itemId);
    }
}
