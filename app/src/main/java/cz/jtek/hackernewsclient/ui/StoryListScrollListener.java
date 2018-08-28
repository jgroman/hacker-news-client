package cz.jtek.hackernewsclient.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class StoryListScrollListener extends RecyclerView.OnScrollListener {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListScrollListener.class.getSimpleName();

    private int cachedItemCount = 0;
    private int oldCachedItemCount = 0;

    private int itemsToPreload = 10;

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 5;

    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;

    private LinearLayoutManager mLayoutManager;

    public StoryListScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        StoryListAdapter adapter = (StoryListAdapter) view.getAdapter();

        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
        cachedItemCount = adapter.getCachedItemCount();

        Log.d(TAG, "onScrolled: last visible " + lastVisibleItemPosition);

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (cachedItemCount < oldCachedItemCount) {
            this.oldCachedItemCount = cachedItemCount;
            if (cachedItemCount == 0) {
                loading = true;
            }
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (cachedItemCount >= oldCachedItemCount + itemsToPreload)) {
            loading = false;
            oldCachedItemCount = cachedItemCount;
            Log.d(TAG, " ++++ onScrolled: loaded " + itemsToPreload + " to " + cachedItemCount);
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > cachedItemCount) {
            loading = true;
            Log.d(TAG, "++++ onScrolled: need to load "+ itemsToPreload +" from " + cachedItemCount);
            onLoadMore(cachedItemCount, itemsToPreload);
        }
    }

    // Call this method whenever performing new searches
    public void resetState() {
        this.oldCachedItemCount = 0;
        this.loading = true;
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int loadFromPosition, int itemCount);
}
