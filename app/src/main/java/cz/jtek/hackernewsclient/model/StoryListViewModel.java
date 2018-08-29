package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.utils.MockDataUtils;
import cz.jtek.hackernewsclient.utils.NetworkUtils;

public class StoryListViewModel extends AndroidViewModel {

    @SuppressWarnings("unused")
    static final String TAG = StoryListViewModel.class.getSimpleName();

    private MutableLiveData<HashMap<String, long[]>> stories;

    public StoryListViewModel(Application application) {
        super(application);
    }

    public LiveData<HashMap<String, long[]>> getStories() {
        if (stories == null) {

            stories = new MutableLiveData<>();
            HashMap<String, long[]> hm = new HashMap<>();
            stories.setValue(hm);

            new LoadStoriesTask(getApplication(), stories).execute();
        }
        return stories;
    }

    private static class LoadStoriesTask extends AsyncTask<Void, Void, HashMap<String, long[]>> {

        private static final boolean USE_MOCK_DATA = true;

        private Application app;
        private MutableLiveData<HashMap<String, long[]>> stories;

        LoadStoriesTask(Application application, MutableLiveData<HashMap<String, long[]>> stories) {
            this.app = application;
            this.stories = stories;
        }

        @Override
        protected HashMap<String, long[]> doInBackground(Void... voids) {
            String jsonStoryList;
            NetworkUtils.AsyncTaskResult<long[]> result;

            HashMap<String, long[]> hm = new HashMap<>();

            // Get all available tab story types
            String[] storyTypes = app.getResources().getStringArray(R.array.story_type_strings);

            // Load all available story types
            for (String storyType : storyTypes) {

                // Load story list JSON
                URL storiesUrl = HackerNewsApi.buildStoriesUrl(storyType);

                try {
                    if (USE_MOCK_DATA) {
                        // Mock request used for debugging to avoid sending network queries
                        jsonStoryList = MockDataUtils.getMockStoriesJson(app.getResources(), app.getPackageName(), storyType);
                    } else {
                        jsonStoryList = NetworkUtils.getResponseFromHttpUrl(storiesUrl);
                    }

                    HackerNewsApi.HackerNewsJsonResult<long[]> storyListResult =
                            HackerNewsApi.getStoriesFromJson(jsonStoryList);

                    result = new NetworkUtils.AsyncTaskResult<>(storyListResult.getResult(), storyListResult.getException());
                } catch (IOException iex) {
                    Log.e(TAG, String.format("IOException when fetching API story data: %s", iex.getMessage()));
                    result = new NetworkUtils.AsyncTaskResult<>(null, iex);
                }

                hm.put(storyType, result.getResult());
            }
            return hm;
        }

        @Override
        protected void onPostExecute(HashMap<String, long[]> result) {
            stories.setValue(result);
        }
    }

}
