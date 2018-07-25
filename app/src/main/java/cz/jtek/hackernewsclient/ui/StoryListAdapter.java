package cz.jtek.hackernewsclient.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.Item;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListAdapter.class.getSimpleName();

    public interface StoryListOnClickListener {
        void onClick(int position);
    }

    private final StoryListOnClickListener mClickListener;

    private Context mContext;
    private long[] mStoryList;

    StoryListAdapter(Context context, long[] storyList, StoryListOnClickListener clickListener) {
        mContext = context;
        mStoryList = storyList;
        mClickListener = clickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        final TextView mStoryTitleTextView;

        /**
         * View holder constructor
         * Sets member variables and attaches local OnClick listener when creating views
         *
         * @param view View to be held in holder
         */
        ViewHolder(View view) {
            super(view);
            mStoryTitleTextView = view.findViewById(R.id.tv_story_title);
            view.setOnClickListener(this);
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
    public StoryListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "*** onBindViewHolder: ");
        holder.mStoryTitleTextView.setText(Long.toString(mStoryList[position]));
    }

    @Override
    public int getItemCount() {
        if (mStoryList == null) { return 0; }
        return mStoryList.length;
    }
}
