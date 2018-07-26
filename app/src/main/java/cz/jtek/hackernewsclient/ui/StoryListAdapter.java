package cz.jtek.hackernewsclient.ui;

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
    private LoaderManager mLoaderManager;

    private LongSparseArray<Item> mItemCache = new LongSparseArray<>();

    StoryListAdapter(Context context, long[] storyList, StoryListOnClickListener clickListener, LoaderManager lm) {
        mContext = context;
        mStoryList = storyList;
        mClickListener = clickListener;
        mLoaderManager = lm;
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
        Log.d(TAG, "*** onBindViewHolder: " + mStoryList[position]);
        holder.mStoryTitleTextView.setText(Long.toString(mStoryList[position]));

        if (mItemCache.get(mStoryList[position]) != null) {
            // Populate holder from cache
        }
        else {
            // Start item loader
            Bundle loaderBundle = new Bundle();
            loaderBundle.putLong(BUNDLE_ITEM_ID, mStoryList[position]);
            mLoaderManager.initLoader(123, loaderBundle, new ItemLoaderListener());
        }

    }

    @Override
    public int getItemCount() {
        if (mStoryList == null) { return 0; }
        return mStoryList.length;
    }

    /**
     *
     */
    private class ItemLoaderListener implements LoaderManager.LoaderCallbacks<NetworkUtils.AsyncTaskResult<Item>> {
        private Bundle mArgs;

        @NonNull
        @Override
        public Loader<NetworkUtils.AsyncTaskResult<Item>> onCreateLoader(int id, Bundle bundle) {
            mArgs = bundle;
            return new ItemLoader(mContext, bundle);
        }

        @Override
        public void onLoadFinished(Loader<NetworkUtils.AsyncTaskResult<Item>> loader, NetworkUtils.AsyncTaskResult<Item> itemAsyncTaskResult) {
            if (itemAsyncTaskResult.hasException()) {
                // There was an error during data loading
                Exception ex = itemAsyncTaskResult.getException();
                //showErrorMessage(getResources().getString(R.string.error_msg_no_data));
            }
            else {
                // Valid results received
                long itemId = mArgs.getLong(BUNDLE_ITEM_ID);
                Log.d(TAG, "*** onLoadFinished: loaded for: " + itemId);
                mItemCache.put(itemId, itemAsyncTaskResult.getResult());

                notifyDataSetChanged();

                // Destroy this loader, otherwise is gets called again during onResume
                //getLoaderManager().destroyLoader(LOADER_ID_STORY_LIST);
            }

        }

        @Override
        public void onLoaderReset(Loader<NetworkUtils.AsyncTaskResult<Item>> loader) {
            // Not implemented
        }
    }

    private static class ItemLoader extends AsyncTaskLoader<NetworkUtils.AsyncTaskResult<Item>> {

        private NetworkUtils.AsyncTaskResult<Item> mResult;
        private final long mItemId;
        private final boolean mUseMockData;

        public ItemLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            mItemId = bundle.getLong(BUNDLE_ITEM_ID);
            mUseMockData = true;
        }

        @Override
        protected void onStartLoading() {
            if (mResult != null && (mResult.hasResult() || mResult.hasException())) {
                // If there are already data available, deliver them
                deliverResult(mResult);
            }
            else {
                // Start loader
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Nullable
        @Override
        public NetworkUtils.AsyncTaskResult<Item> loadInBackground() {
            String jsonItem;

            try {
                URL itemUrl = HackerNewsApi.buildItemUrl(mItemId);

                if (mUseMockData) {
                    // Mock request used for debugging to avoid sending network queries
                    jsonItem = MockDataUtils.getMockItemJson(getContext(), mItemId);
                }
                else {
                    jsonItem = NetworkUtils.getResponseFromHttpUrl(itemUrl);
                }

                HackerNewsApi.HackerNewsJsonResult<Item> itemResult = HackerNewsApi.getItemFromJson(jsonItem);

                mResult = new AsyncTaskResult<>(itemResult.getResult(), itemResult.getException());
            }
            catch (IOException iex) {
                Log.e(TAG, String.format("IOException when fetching API item data: %s", iex.getMessage()));
                mResult = new AsyncTaskResult<>(null, iex);
            }

            return mResult;
        }
    }
}
