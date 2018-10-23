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
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.databinding.ItemCommentBinding;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.model.ItemViewModel;

public class CommentListAdapter extends ListAdapter<Item, CommentListAdapter.CommentViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = CommentListAdapter.class.getSimpleName();

    public interface CommentListOnClickListener {
        void onClick(long itemId);
    }

    private final CommentListOnClickListener mClickListener;

    private ItemViewModel mItemModel;

    CommentListAdapter(Activity activity, CommentListOnClickListener clickListener) {
        super(Item.DIFF_CALLBACK);

        mItemModel = ViewModelProviders.of((CommentListActivity) activity).get(ItemViewModel.class);
        mClickListener = clickListener;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Binding class name is generated from layout filename: item_comment.xml
        private ItemCommentBinding binding;

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
            // Method name is derived from binding variable name
            binding.setComment(item);
        }

        /**
         * Local OnClick listener
         * Sends OnClick event of clicked item via interface up to registered click listener
         * @param view View that was clicked on
         */
        @Override
        public void onClick(View view) {
            int itemPos = getAdapterPosition();
            Item clickedItem = getItem(itemPos);
            mClickListener.onClick(clickedItem.getId());
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Item bindItem = getItem(position);

        if (!bindItem.getIsLoaded()) {
            // If item is not present in db yet, start loading from API
            mItemModel.fetchItem(bindItem.getId());
        }

        // Clean comment text
        String commentText = bindItem.getText();
        if (commentText != null && commentText.length() > 0) {
            Spanned htmlResult = Html.fromHtml(commentText, Html.FROM_HTML_MODE_LEGACY);
            if (htmlResult != null) {
                bindItem.setText(htmlResult.toString());
            }
        }

        holder.bind(bindItem);
    }
}
