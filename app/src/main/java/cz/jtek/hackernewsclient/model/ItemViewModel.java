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
import cz.jtek.hackernewsclient.data.Item;

public class ItemViewModel extends AndroidViewModel {

    @SuppressWarnings("unused")
    private static final String TAG = StoryListViewModel.class.getSimpleName();

    private DataRepository mRepository;

    private final MediatorLiveData<List<Item>> mObservableItems;
    private final MediatorLiveData<List<Item>> mObservableCommentItems;

    private final MutableLiveData<Integer> mObservableItemId;
    private final LiveData<ArrayList<Long>> mObservableItemKidsList;

    private MutableLiveData<List<Long>> mListedItemIds;
    private final LiveData<List<Item>> mObservableFullItemList;

    public ItemViewModel(Application application) {
        this(application, 0);
    }

    public ItemViewModel(Application application, long itemId) {
        super(application);

        // Get repository singleton instance
        mRepository = ((HackerNewsClientApplication) application).getRepository();

        // Observe changes of all items in the database and forward them to observers
        mObservableItems = new MediatorLiveData<>();
        mObservableItems.setValue(null);
        LiveData<List<Item>> items = mRepository.getAllItems();
        mObservableItems.addSource(items, mObservableItems::setValue);

        // Observe changes of comment items in the database and forward them to observers
        mObservableCommentItems = new MediatorLiveData<>();
        mObservableCommentItems.setValue(null);
        LiveData<List<Item>> commentItems = mRepository.getAllCommentItems();
        mObservableCommentItems.addSource(commentItems, mObservableCommentItems::setValue);

        mObservableItemId = new MutableLiveData<>();
        mObservableItemId.setValue(null);
        mObservableItemKidsList = Transformations.switchMap(
                mObservableItems,
                itemList -> mRepository.getItemKidsList(itemList, itemId)
        );

        // On mListedItemIds change set new source for mObservableListedItems
        // mListedItemIds is changed using setItemList()
        mListedItemIds = new MutableLiveData<>();
        mListedItemIds.setValue(new ArrayList<>());
        LiveData<List<Item>> observableUnsortedItems = Transformations.switchMap(
                mListedItemIds,
                itemIds -> {
                    // itemIds holds list of items to be obtained from db
                    LiveData<List<Item>> itemListLD =  mRepository.getItemList(itemIds);
                    List<Item> itemList = itemListLD.getValue();

                    if (itemList == null) {
                        itemList = new ArrayList<>();
                    }

                    List<Item> itemsToInsert = new ArrayList<>();

                    for (Long id : itemIds) {
                        if (Item.findItemInList(itemList, id) == null) {
                            Item item = new Item();
                            item.setId(id);
                            item.setTitle("Loading " + Long.toString(id));
                            item.setText(Long.toString(id));
                            itemsToInsert.add(item);
                        }
                    }

                    // Insert items present in list but missing in db
                    mRepository.insertItems(itemsToInsert);

                    return itemListLD;
                }
        );

        // On mObservableListedItems change sort result according to mListedItemIds
        // Chained to observableUnsortedItems transformation
        mObservableFullItemList = Transformations.switchMap(
                observableUnsortedItems,
                unsortedItemList -> {
                    Log.d(TAG, "ItemViewModel: transform 2, source items " + unsortedItemList.size());

                    Item workItem;
                    List<Item> sortedItems = new ArrayList<>();
                    List<Long> sortedIds = mListedItemIds.getValue();

                    // Sort item list according to mListedItemIds
                    if (sortedIds != null) {
                        for (Long id : sortedIds) {
                            workItem = Item.findItemInList(unsortedItemList, id);
                            if (workItem != null) {
                                sortedItems.add(workItem);
                            }
                        }
                    }

                    MutableLiveData<List<Item>> sortedListLD = new MutableLiveData<>();
                    sortedListLD.setValue(sortedItems);

                    return sortedListLD;
                }
        );
    }

    public LiveData<List<Item>> getAllItems() {
        return mObservableItems;
    }

    public LiveData<List<Item>> getAllCommentItems() {
        return mObservableCommentItems;
    }

    public void setIdItemKids(Integer itemId) { this.mObservableItemId.setValue(itemId); }
    public LiveData<ArrayList<Long>> getItemKidsList() { return mObservableItemKidsList; }

    public Item getItem(long itemId) {
        return mRepository.getItem(itemId);
    }

    /**
     * Set list of item ids to be included in getListedItems() LiveData
     *
     * @param itemIds Item ids
     */
    public void setItemList(List<Long> itemIds) {
        mListedItemIds.setValue(itemIds);
    }

    public LiveData<List<Item>> getSortedListedItems() {
        return mObservableFullItemList;
    }


}
