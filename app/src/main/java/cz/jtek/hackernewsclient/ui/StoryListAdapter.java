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
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.model.Item;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils.AsyncTaskResult;

import cz.jtek.hackernewsclient.databinding.ItemStoryBinding;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.StoryViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListAdapter.class.getSimpleName();

    // Bundle arguments
    public static final String BUNDLE_ITEM_ID = "item-id";

    //

    public interface StoryListOnClickListener {
        void onClick(int position);
    }

    private final StoryListOnClickListener mClickListener;

    private Context mContext;
    private long[] mStoryList;
    private StoryListActivity mActivity;
    private LayoutInflater layoutInflater;

    StoryListAdapter(Context context, long[] storyList, StoryListOnClickListener clickListener, Activity activity) {
        mContext = context;
        mStoryList = storyList;
        mClickListener = clickListener;
        mActivity =  (StoryListActivity) activity;
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemStoryBinding binding;

        /**
         * View holder constructor
         * Sets member variables and attaches local OnClick listener when creating views
         *
         * @param view View to be held in holder
         */
        StoryViewHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
        }

        public void bind(Item item) {
            binding.setItem(item);
        }

        /**
         * Local OnClick listener
         * Sends OnClick event of clicked item via interface up to registered click listener
         * @param view View that was clicked on
         */
        @Override
        public void onClick(View view) {
            int itemPos = getAdapterPosition();
            mClickListener.onClick(itemPos);
        }
    }

    @NonNull
    @Override
    public StoryListAdapter.StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryListAdapter.StoryViewHolder holder, int position) {
        Item item = mActivity.mItemCache.get(mStoryList[position]);

        if (item == null) {
            item = new Item();
            item.setId(mStoryList[position]);
            mActivity.startItemLoader(mStoryList[position]);
        }

        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        if (mStoryList == null) { return 0; }
        return mStoryList.length;
    }

    public int getCachedItemCount() {
        if (mStoryList == null) { return 0; }

        Item item;
        int itemCount = 0;

        for (int i = 0; i < mStoryList.length; i++) {
            item = mActivity.mItemCache.get(mStoryList[i]);
            if (item == null) {
                break;
            }

            if (item.getTitle().length() > 5) {
                //Log.d(TAG, "*** getCachedItemCount: adding " + item.getId());
                itemCount++;
            }
        }

        Log.d(TAG, "** getCachedItemCount: total " + itemCount);
        return itemCount;
    }


}
