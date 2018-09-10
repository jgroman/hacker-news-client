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

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

public class CommentListFragment extends Fragment {

    @SuppressWarnings("unused")
    static final String TAG = CommentListFragment.class.getSimpleName();

    // Bundle arguments
    public static final String BUNDLE_STORY_ID = "story-id";

    private CommentListActivity mActivity;
    private StoryListViewModel mModel;
    private RecyclerView mCommentListRecyclerView;


    // Custom OnCommentClickListener interface, must be implemented by container activity
    public interface OnCommentClickListener {
        void onCommentSelected(long itemId);
    }

    // This is a callback to onCommentSelected in container activity
    OnCommentClickListener mCommentClickListenerCallback;

    public static Fragment newInstance(@NonNull long storyId) {
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

        mCommentListRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_comment_list,
                container, false);

        return mCommentListRecyclerView;
    }
}
