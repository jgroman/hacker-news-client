package cz.jtek.hackernewsclient.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class StoryListScrollListener extends RecyclerView.OnScrollListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListScrollListener.class.getSimpleName();

    private int loadedItemCount = 0;
    private int oldLoadedItemCount = 0;

    private int itemsToPreload = 10;

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 3;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;

    RecyclerView.LayoutManager mLayoutManager;

    public StoryListScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        StoryListAdapter adapter = (StoryListAdapter) view.getAdapter();

        int lastVisibleItemPosition = 0;
        //int totalItemCount = mLayoutManager.getItemCount();

        lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        loadedItemCount = adapter.getLoadedItemCount(lastVisibleItemPosition + itemsToPreload);

        Log.d(TAG, "onScrolled: last visible " + lastVisibleItemPosition);

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (loadedItemCount < oldLoadedItemCount) {
            this.oldLoadedItemCount = loadedItemCount;
            if (loadedItemCount == 0) {
                loading = true;
            }
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (loadedItemCount >= oldLoadedItemCount + itemsToPreload)) {
            loading = false;
            oldLoadedItemCount = loadedItemCount;
            Log.d(TAG, " ++++ onScrolled: loaded " + itemsToPreload + " to " + loadedItemCount);
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > loadedItemCount) {
            loading = true;
            Log.d(TAG, "++++ onScrolled: need to load "+ itemsToPreload +" from " + loadedItemCount);
            onLoadMore(view, loadedItemCount, itemsToPreload);
        }
    }

    // Call this method whenever performing new searches
    public void resetState() {
        this.oldLoadedItemCount = 0;
        this.loading = true;
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(RecyclerView recyclerView, int loadFromPosition, int itemCount);
}
