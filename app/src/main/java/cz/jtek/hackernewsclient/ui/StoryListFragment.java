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
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.model.Item;
import cz.jtek.hackernewsclient.utils.NetworkUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils.AsyncTaskResult;

public class StoryListFragment extends Fragment
    implements StoryListAdapter.StoryListOnClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListFragment.class.getSimpleName();

    // Bundle arguments
    public static final String BUNDLE_STORY_TYPE = "story-type";

    // Instance state bundle keys
    private static final String KEY_STORY_TYPE = BUNDLE_STORY_TYPE;
    private static final String KEY_STORY_LIST = "story-list";

    private Context mContext;
    private String mStoryType;
    private ArrayList<Item> mStoryList;
    private RecyclerView mStoryListRecyclerView;

    // Custom OnStoryClickListener interface, must be implemented by container activity
    public interface OnStoryClickListener {
        void onStorySelected(int position);
    }

    // This is a callback to onStorySelected in container activity
    OnStoryClickListener mStoryClickListenerCallback;

    public static Fragment newInstance(@NonNull String storyType) {
        Log.d(TAG, "*** StoryListFragment newInstance " + storyType);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Activity activity = getActivity();
        if (null == activity) { return null; }

        mContext = activity.getApplicationContext();

        mStoryListRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_story_list, container, false);

        if (savedInstanceState != null) {
            // Restoring story type and list from saved instance state
            mStoryType = savedInstanceState.getString(KEY_STORY_TYPE);
            mStoryList = savedInstanceState.getParcelableArrayList(KEY_STORY_LIST);
        }
        else {
            // Get story type from passed arguments
            Bundle args = getArguments();
            if (args != null && args.containsKey(BUNDLE_STORY_TYPE)) {
                mStoryType = args.getString(BUNDLE_STORY_TYPE);
            }

            Log.d(TAG, "*** StoryListFragment onCreateView " + mStoryType);

            // TODO Get story list using loader
        }

        StoryListAdapter storyListAdapter = new StoryListAdapter(mContext, mStoryList, this );
        mStoryListRecyclerView.setAdapter(storyListAdapter);

        return mStoryListRecyclerView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Store story type
        outState.putString(KEY_STORY_TYPE, mStoryType);
        // Store story list
        outState.putParcelableArrayList(KEY_STORY_LIST, mStoryList);

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

    /**
     *
     */
    public static class StoryListLoader
        extends AsyncTaskLoader<AsyncTaskResult<long[]>> {

        String mStoryType;
        AsyncTaskResult<long[]> mResult;

        private StoryListLoader(Context context, String storyType) {
            super(context);
            mStoryType = storyType;
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
        public AsyncTaskResult<long[]> loadInBackground() {
            try {
                // Example mock request used for debugging to avoid sending network queries
                //String jsonStoryList = MockDataUtils.getMockJson(getContext(), "top");

                // Load story list JSON
                URL storiesUrl = HackerNewsApi.buildStoriesUrl(mStoryType);
                String jsonStoryList = NetworkUtils.getResponseFromHttpUrl(storiesUrl);

                HackerNewsApi.HackerNewsJsonResult<long[]> storyListResult =
                        HackerNewsApi.getStoriesFromJson(jsonStoryList);

                mResult = new AsyncTaskResult<>(storyListResult.getResult(), storyListResult.getException());
            }
            catch (IOException iex) {
                Log.e(TAG, String.format("IOException when fetching API data: %s", iex.getMessage()));
                mResult = new AsyncTaskResult<>(null, iex);
            }

            return mResult;
        }


    }
}
