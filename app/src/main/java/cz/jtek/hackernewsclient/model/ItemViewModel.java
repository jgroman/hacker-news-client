package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import cz.jtek.hackernewsclient.HackerNewsClientApplication;
import cz.jtek.hackernewsclient.data.DataRepository;
import cz.jtek.hackernewsclient.data.Item;

public class ItemViewModel extends AndroidViewModel {

    @SuppressWarnings("unused")
    static final String TAG = StoryListViewModel.class.getSimpleName();

    private DataRepository mRepository;

    private final MediatorLiveData<List<Item>> mObservableItems;

    public ItemViewModel(Application application) {
        super(application);

        mObservableItems = new MediatorLiveData<>();
        // By default set to null until we get data from the database
        mObservableItems.setValue(null);

        mRepository = ((HackerNewsClientApplication) application).getRepository();

        LiveData<List<Item>> items = mRepository.getAllItems();

        // Observe changes of items in the database and forward them
        mObservableItems.addSource(items, mObservableItems::setValue);
    }


    public LiveData<List<Item>> getAllItems() {
        return mObservableItems;
    }

    public Item getItem(long itemId) {
        return mRepository.getItem(itemId);
    }
}
