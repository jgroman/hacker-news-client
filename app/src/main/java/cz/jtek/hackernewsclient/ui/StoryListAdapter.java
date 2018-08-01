package cz.jtek.hackernewsclient.ui;

import android.app.Activity;
import android.content.Context;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.model.HackerNewsApi;
import cz.jtek.hackernewsclient.model.Item;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils.AsyncTaskResult;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.ViewHolder> {

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

    StoryListAdapter(Context context, long[] storyList, StoryListOnClickListener clickListener, Activity activity) {
        mContext = context;
        mStoryList = storyList;
        mClickListener = clickListener;
        mActivity =  (StoryListActivity) activity;
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryListAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "*** onBindViewHolder: " + mStoryList[position]);
        holder.mStoryTitleTextView.setText(Long.toString(mStoryList[position]));

        if ( mActivity.mItemCache.get(mStoryList[position]) != null) {
            // Populate holder from loader cache
            String title = mActivity.mItemCache.get(mStoryList[position]).getTitle();
            if (title != null) {
                holder.mStoryTitleTextView.setText(title);
            }
        }
        else {
            // Start item loader
            mActivity.startItemLoader(mStoryList[position]);
        }
    }

    @Override
    public int getItemCount() {
        if (mStoryList == null) { return 0; }
        return mStoryList.length;
    }

}
