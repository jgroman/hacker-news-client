package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import cz.jtek.hackernewsclient.HackerNewsClientApplication;
import cz.jtek.hackernewsclient.data.DataRepository;
import cz.jtek.hackernewsclient.data.StoryList;

public class StoryListViewModel extends AndroidViewModel {

    @SuppressWarnings("unused")
    static final String TAG = StoryListViewModel.class.getSimpleName();

    private DataRepository mRepository;

    private final MediatorLiveData<List<StoryList>> mObservableStoryLists;

    private final LiveData<ArrayList<Long>> mObservableTypedStoryList;

    // This simplified constructor is used by StoryListActivity and CommentListActivity
    // Used via StoryListViewModelFactory
    @SuppressWarnings("unused")
    public StoryListViewModel(Application application) {
        this(application, "any");
    }

    public StoryListViewModel(Application application, String storyType) {
        super(application);

        // Get repository singleton instance
        mRepository = ((HackerNewsClientApplication) application).getRepository();

        mObservableStoryLists = new MediatorLiveData<>();
        // By default set to null until we get data from the database
        mObservableStoryLists.setValue(null);

        // Observe changes of the story lists from the database and forward them to observers
        LiveData<List<StoryList>> storyLists = mRepository.getAllStoryLists();
        mObservableStoryLists.addSource(storyLists, mObservableStoryLists::setValue);

        mObservableTypedStoryList = Transformations.switchMap(mObservableStoryLists,
                (List<StoryList> stories) -> mRepository.getTypedStoryList(stories, storyType));
    }

    /**
     * Expose the LiveData Stories query so the UI can observe it.
     */
    public LiveData<List<StoryList>> getAllStoryLists() {
        return mObservableStoryLists;
    }

    public LiveData<ArrayList<Long>> getTypedStoryList() { return mObservableTypedStoryList; }



}
