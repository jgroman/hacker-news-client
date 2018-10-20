package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

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

    private MutableLiveData<String> mObservableStoryType;
    private final LiveData<StoryList> mObservableTypedStoryList;

    public StoryListViewModel(Application application) {
        super(application);

        // Get repository singleton instance
        mRepository = ((HackerNewsClientApplication) application).getRepository();

        mObservableStoryLists = new MediatorLiveData<>();
        // By default set to null until we get data from the database
        mObservableStoryLists.setValue(null);

        // Observe changes of the story lists from the database and forward them to observers
        LiveData<List<StoryList>> storyLists = mRepository.getAllStoryLists();
        mObservableStoryLists.addSource(storyLists, mObservableStoryLists::setValue);

        // On mObservableStoryType change set new source for mObservableTypedStoryList
        // mObservableStoryType is changed using setStoryType()
        mObservableStoryType = new MutableLiveData<>();
        mObservableStoryType.setValue(null);
        mObservableTypedStoryList = Transformations.switchMap(
                mObservableStoryType,
                storyType -> mRepository.getTypedStoryList(storyType)
        );
    }

    /**
     * Expose the LiveData Stories query so the UI can observe it.
     */
    public LiveData<List<StoryList>> getAllStoryLists() {
        return mObservableStoryLists;
    }

    /**
     * Sets the story type for getTypedStoryList()
     *
     * @param storyType Story type string
     */
    public void setStoryType(String storyType) {
        mObservableStoryType.setValue(storyType);
    }

    /**
     *
     * @return
     */
    public LiveData<StoryList> getTypedStoryList() {
        return mObservableTypedStoryList;
    }

}
