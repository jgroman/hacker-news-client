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
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

import cz.jtek.hackernewsclient.databinding.ItemStoryBinding;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.StoryViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListAdapter.class.getSimpleName();

    public interface StoryListOnClickListener {
        void onClick(long itemId);
    }

    private final StoryListOnClickListener mClickListener;

    private StoryListViewModel mModel;
    private long[] mStoryList;

    StoryListAdapter(Activity activity, String storyType, StoryListOnClickListener clickListener) {
        mModel = ViewModelProviders.of((StoryListActivity) activity).get(StoryListViewModel.class);
        mStoryList = mModel.getStoryIds().getValue().get(storyType);
        mClickListener = clickListener;
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Binding class name is generated from layout filename: item_story.xml
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

        void bind(Item item) {
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
            long itemId = mStoryList[itemPos];
            mClickListener.onClick(itemId);
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
        Item item = mModel.getItem(mStoryList[position], true);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        if (mStoryList == null) { return 0; }
        return mStoryList.length;
    }

}
