package cz.jtek.hackernewsclient.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
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
    private static final String TAG = ItemViewModel.class.getSimpleName();

    private DataRepository mRepository;

    private final MutableLiveData<Long> mObservableItemId;
    private final LiveData<Item> mObservableItem;
    private final LiveData<List<Long>> mObservableItemKidsList;

    private MutableLiveData<List<Long>> mListedItemIds;
    private final LiveData<List<Item>> mObservableSortedItemList;

    public ItemViewModel(Application application) {
        super(application);

        // Get repository singleton instance
        mRepository = ((HackerNewsClientApplication) application).getRepository();

        // On mObservableItemId change update source for observableItem
        mObservableItemId = new MutableLiveData<>();
        mObservableItemId.setValue(null);
        mObservableItem = Transformations.switchMap(
                mObservableItemId,
                itemId -> mRepository.getItem(itemId)
        );

        // Accessible by getItemKidsList()
        // On mObservableSortedItemList change update source for mObservableItemKidsList
        // Chained to
        mObservableItemKidsList = Transformations.switchMap(
                mRepository.getAllItems(),
                allItemList -> {
                    ArrayList<Long> resultKidList = new ArrayList<>();

                    if (allItemList != null) {
                        Log.d(TAG, "ItemViewModel: all items size " + allItemList.size());
                    }
                    else {
                        Log.d(TAG, "ItemViewModel: all items size 0");
                    }

                    if (mObservableItemId.getValue() == null) {
                        return null;
                    }

                    Item parentItem = Item.findItemInList(allItemList, mObservableItemId.getValue());
                    if (parentItem != null) {
                        Log.d(TAG, "**** ItemViewModel: getting kids of " + parentItem.getId());
                        resultKidList = parentItem.getKids();
                        Log.d(TAG, "**** ItemViewModel: basic kid list size " + resultKidList.size());

                        // TODO Traversing comment tree

                        if (resultKidList.size() > 0) {
                            Item workItem;
                            ArrayList<Long> workKidList;
                            // All items are set to nest level 1 by default when loading

                            int currentKidIndex = 0;
                            int totalKidsAdded = 0;
                            // Traversing comment tree, using cached items only
                            do {
                                //Log.d(TAG, "getItemKidsList: processing kid list position " + currentKidIndex);
                                // Check if current kid item is cached
                                workItem = Item.findItemInList(allItemList, resultKidList.get(currentKidIndex));
                                if (workItem != null) {
                                    //currentNestingLevel = workItem.getNestLevel();
                                    //Log.d(TAG, "getItemKidsList: adding kids from " + workItem.getId() + " at level " + currentNestingLevel);
                                    // Get kids of current kid
                                    workKidList = workItem.getKids();
                                    if (workKidList != null && workKidList.size() > 0) {
                                        totalKidsAdded += workKidList.size();
                                        Log.d(TAG, "*** getItemKidsList: Added kids " + workKidList.size() + " for item " + workItem.getId() + " at level " + mRepository.getItemNestLevel(allItemList, workItem.getId()));
                                        //updateKidNestLevel(itemList, workKidList, currentNestingLevel + 1);
                                        // Add current kid's kids right after it to kid list
                                        resultKidList.addAll(currentKidIndex + 1, workKidList);
                                    }
                                }
                                // Move to next item in kid list
                                // This might be recently added kid's kid
                                currentKidIndex++;
                            }
                            while (currentKidIndex < resultKidList.size() /*|| currentKidIndex < 20*/);
                        }

                        //mListedItemIds.setValue(resultKidList);
                    }
                    Log.d(TAG, "**** ItemViewModel: reworked kid list size " + resultKidList.size());
                    MutableLiveData<List<Long>> resultLD = new MutableLiveData<>();
                    resultLD.setValue(resultKidList);
                    return resultLD;
                }
        );

        // On mListedItemIds change update source for mObservableListedItems
        // mListedItemIds is changed using setObservableListIds()
        mListedItemIds = new MutableLiveData<>();
        mListedItemIds.setValue(new ArrayList<>());
        LiveData<List<Item>> observableUnsortedItems = Transformations.switchMap(
                mListedItemIds,
                itemIds -> {
                    Log.d(TAG, "*** ItemViewModel: transform 1, set item list source " + itemIds.size());
                    return mRepository.getItemList(itemIds);
                }
        );

        // On mObservableListedItems change sort result according to mListedItemIds
        // Chained to observableUnsortedItems transformation
        mObservableSortedItemList = Transformations.switchMap(
                observableUnsortedItems,
                unsortedItemList -> {
                    Log.d(TAG, "*** ItemViewModel: transform 2, source items " + unsortedItemList.size());

                    Item workItem;
                    List<Item> sortedItems = new ArrayList<>();
                    List<Long> sortedIds = mListedItemIds.getValue();

                    if (sortedIds != null) {
                        ArrayList<Item> itemsToInsert = new ArrayList<>();

                        for (Long id : sortedIds) {
                            workItem = Item.findItemInList(unsortedItemList, id);

                            if (workItem == null) {
                                // itemsToInsert will be filled with Items requested by list but
                                // missing in db
                                itemsToInsert.add(Item.newLoadingItem(id));
                            }
                            else {
                                // sortedItems list will be filled with Items according
                                // to mListedItemIds order
                                sortedItems.add(workItem);
                            }
                        }

                        if (itemsToInsert.size() > 0) {
                            // Insert missing items to db
                            Log.d(TAG, "*** ItemViewModel: inserting items to db " + itemsToInsert.size());
                            Item[] itemArray = new Item[itemsToInsert.size()];
                            itemsToInsert.toArray(itemArray);
                            mRepository.insertIgnoreItems(itemArray);
                        }
                    }

                    MutableLiveData<List<Item>> sortedListLD = new MutableLiveData<>();
                    sortedListLD.setValue(sortedItems);
                    return sortedListLD;
                }
        );


    }

    /**
     * Fetch Item from repository
     *
     * @param itemId Id of item to be fetched
     * @return Item
     */
    public Item fetchItem(long itemId) {
        return mRepository.fetchItem(itemId);
    }

    /**
     * Set source item id for getItemKidsList()
     * @param itemId Source item id
     */
    public void setObservableItemId(Long itemId) {
        Log.d(TAG, "setObservableItemId: " + itemId);
        mObservableItemId.setValue(itemId);
    }

    public LiveData<List<Long>> getItemKidsList() {
        return mObservableItemKidsList;
    }


    /**
     * Set list of item ids to be included in getListedItems() LiveData
     *
     * @param itemIds Item ids
     */
    public void setObservableListIds(List<Long> itemIds) {
        mListedItemIds.setValue(itemIds);
    }

    public LiveData<List<Item>> getSortedListedItems() {
        return mObservableSortedItemList;
    }


}
