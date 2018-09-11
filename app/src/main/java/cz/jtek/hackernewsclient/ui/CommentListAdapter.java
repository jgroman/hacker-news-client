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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.databinding.ItemStoryBinding;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.model.StoryListViewModel;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CommentViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = CommentListAdapter.class.getSimpleName();

    public interface CommentListOnClickListener {
        void onClick(long itemId);
    }

    private final CommentListOnClickListener mClickListener;

    private StoryListViewModel mModel;
    private long[] mCommentList;

    CommentListAdapter(Activity activity, long storyId, CommentListOnClickListener clickListener) {
        mModel = ViewModelProviders.of((CommentListActivity) activity).get(StoryListViewModel.class);
        // At his point an item is fully loaded in ViewModel, its kids are available
        Log.d(TAG, "CommentListAdapter: getting kids from " + storyId);
        mCommentList = mModel.getItem(storyId, false).getKids();
        mClickListener = clickListener;
    }


    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemStoryBinding binding;

        /**
         * View holder constructor
         * Sets member variables and attaches local OnClick listener when creating views
         *
         * @param view View to be held in holder
         */
        CommentViewHolder(View view) {
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
            long itemId = mCommentList[itemPos];
            mClickListener.onClick(itemId);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_story, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Item item = mModel.getItem(mCommentList[position], true);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        if (mCommentList == null) { return 0; }
        return mCommentList.length;
    }
}
