package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.jtek.hackernewsclient.HackerNewsClientApplication;
import cz.jtek.hackernewsclient.data.DataRepository;
import cz.jtek.hackernewsclient.data.Item;

public class ItemViewModel extends AndroidViewModel {

    @SuppressWarnings("unused")
    static final String TAG = StoryListViewModel.class.getSimpleName();

    private DataRepository mRepository;

    private final MediatorLiveData<List<Item>> mObservableItems;
    private final MediatorLiveData<List<Item>> mObservableCommentItems;

    private final LiveData<ArrayList<Long>> mObservableItemKidsList;

    public ItemViewModel(Application application) {
        this(application, 0);
    }

    public ItemViewModel(Application application, long itemId) {
        super(application);

        // Get repository singleton instance
        mRepository = ((HackerNewsClientApplication) application).getRepository();

        mObservableItems = new MediatorLiveData<>();
        // By default set to null until we get data from the database
        mObservableItems.setValue(null);

        // Observe changes of all items in the database and forward them to observers
        LiveData<List<Item>> items = mRepository.getAllItems();
        mObservableItems.addSource(items, mObservableItems::setValue);

        // Observe changes of comment items in the database and forward them to observers
        mObservableCommentItems = new MediatorLiveData<>();
        mObservableCommentItems.setValue(null);
        LiveData<List<Item>> commentItems = mRepository.getAllCommentItems();
        mObservableCommentItems.addSource(commentItems, mObservableCommentItems::setValue);

        mObservableItemKidsList = Transformations.switchMap(mObservableItems,
                itemList -> mRepository.getItemKidsList(itemList, itemId));
    }


    public LiveData<List<Item>> getAllItems() {
        return mObservableItems;
    }

    public LiveData<List<Item>> getAllCommentItems() {
        return mObservableCommentItems;
    }

    public LiveData<ArrayList<Long>> getItemKidsList() { return mObservableItemKidsList; }

    public Item getItem(long itemId) {
        return mRepository.getItem(itemId);
    }
}
