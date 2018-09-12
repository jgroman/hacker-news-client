package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.List;

import cz.jtek.hackernewsclient.HackerNewsClientApplication;
import cz.jtek.hackernewsclient.data.DataRepository;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.data.StoryList;

public class StoryListViewModel extends AndroidViewModel {

    @SuppressWarnings("unused")
    static final String TAG = StoryListViewModel.class.getSimpleName();

    private DataRepository mRepository;

    private final MediatorLiveData<List<StoryList>> mObservableStoryLists;

    private MutableLiveData<LongSparseArray<Item>> storyItems;

    public StoryListViewModel(Application application) {
        super(application);

        mObservableStoryLists = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableStoryLists.setValue(null);

        mRepository = ((HackerNewsClientApplication) application).getRepository();

        LiveData<List<StoryList>> storyLists = mRepository.getAllStoryLists();

        // observe the changes of the story lists from the database and forward them
        mObservableStoryLists.addSource(storyLists, new Observer<List<StoryList>>() {
            @Override
            public void onChanged(@Nullable List<StoryList> value) {
                mObservableStoryLists.setValue(value);
            }
        });
    }

    /**
     * Expose the LiveData Stories query so the UI can observe it.
     */
    public LiveData<List<StoryList>> getAllStoryLists() {
        return mObservableStoryLists;
    }

    /**
     *
     * @param storyType
     * @return
     */
    public StoryList getStoryList(String storyType) {
        return mRepository.getStoryList(storyType);
    }

}
