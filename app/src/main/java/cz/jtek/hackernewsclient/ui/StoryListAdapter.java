package cz.jtek.hackernewsclient.ui;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.databinding.ItemStoryBinding;
import cz.jtek.hackernewsclient.model.ItemViewModel;

public class StoryListAdapter extends ListAdapter<Long, StoryListAdapter.StoryViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListAdapter.class.getSimpleName();

    public interface StoryListOnClickListener {
        void onClick(long itemId);
    }

    private final StoryListOnClickListener mClickListener;

    private ItemViewModel mItemModel;



    protected StoryListAdapter(Activity activity, StoryListOnClickListener clickListener) {
        super(DIFF_CALLBACK);

        Log.d(TAG, "StoryListAdapter: construct");
        mItemModel = ViewModelProviders.of((StoryListActivity) activity).get(ItemViewModel.class);
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
            long itemId = getItem(itemPos);
            mClickListener.onClick(itemId);
        }
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Log.d(TAG, "*** onBindViewHolder: binding " + position + " to " + getItem(position));
        holder.bind(mItemModel.getItem(getItem(position)));
    }

    public static final DiffUtil.ItemCallback<Long> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Long>() {
                @Override
                public boolean areItemsTheSame(@NonNull Long oldItem, @NonNull Long newItem) {
                    // Item properties may have changed if reloaded from the DB, but ID is fixed
                    return oldItem.equals(newItem);
                }
                @Override
                public boolean areContentsTheSame(@NonNull Long oldItem, @NonNull Long newItem) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldItem.equals(newItem);
                }
            };

}
