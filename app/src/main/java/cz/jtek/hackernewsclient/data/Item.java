/*
 * Copyright 2018 Jaroslav Groman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.jtek.hackernewsclient.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.jtek.hackernewsclient.BR;

@Entity(tableName = Item.TABLE_NAME)
public class Item extends BaseObservable implements Parcelable {

    @SuppressWarnings("unused")
    private static final String TAG = Item.class.getSimpleName();

    private static final int DEFAULT_NESTING_LEVEL = 1;

    public static final String TABLE_NAME = "items";

    // JSON field string ids, also Room column names
    static final String ID = "id";
    static final String DELETED = "deleted";
    static final String TYPE = "type";
    static final String BY = "by";
    static final String TEXT = "text";
    static final String DEAD = "dead";
    static final String PARENT = "parent";
    static final String POLL = "poll";
    static final String KIDS = "kids";
    static final String URL = "url";
    static final String SCORE = "score";
    static final String TITLE = "title";
    static final String PARTS = "parts";
    static final String DESCENDANTS = "descendants";
    static final String NEST_LEVEL = "nest-level";
    static final String IS_LOADED = "is-loaded";

    // Members
    @PrimaryKey
    @ColumnInfo(index = true, name = ID)
    private long id;

    @ColumnInfo(name = DELETED)
    private Boolean deleted;

    @NonNull
    @ColumnInfo(name = TYPE)
    private String type;

    @ColumnInfo(name = BY)
    private String by;

    @ColumnInfo(name = TEXT)
    private String text;

    @ColumnInfo(name = DEAD)
    private Boolean dead;

    @ColumnInfo(name = PARENT)
    private long parent;

    @ColumnInfo(name = POLL)
    private long poll;

    @ColumnInfo(name = KIDS)
    private ArrayList<Long> kids;

    @ColumnInfo(name = URL)
    private String url;

    @ColumnInfo(name = SCORE)
    private int score;

    @ColumnInfo(name = TITLE)
    private String title;

    @ColumnInfo(name = PARTS)
    private ArrayList<Long> parts;

    @ColumnInfo(name = DESCENDANTS)
    private int descendants;

    @ColumnInfo(name = NEST_LEVEL)
    private int nestLevel;

    @ColumnInfo(name = IS_LOADED)
    private Boolean isLoaded;

    // Getters & Setters
    @Bindable
    public long getId() { return id; }
    public void setId(long id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
        notifyPropertyChanged(BR.deleted);
    }

    @NonNull
    @Bindable
    public String getType() { return type; }
    public void setType(String type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
    }

    @Bindable
    public String getBy() { return by; }
    public void setBy(String by) {
        this.by = by;
        notifyPropertyChanged(BR.by);
    }

    @Bindable
    public String getText() { return text; }
    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public Boolean getDead() { return dead; }
    public void setDead(Boolean dead) {
        this.dead = dead;
        notifyPropertyChanged(BR.dead);
    }

    @Bindable
    public long getParent() { return parent; }
    public void setParent(long parent) {
        this.parent = parent;
        notifyPropertyChanged(BR.parent);
    }

    @Bindable
    public long getPoll() { return poll; }
    public void setPoll(long poll) {
        this.poll = poll;
        notifyPropertyChanged(BR.poll);
    }

    @Bindable
    public ArrayList<Long> getKids() { return kids; }
    public void setKids(ArrayList<Long> kids) {
        this.kids = kids;
        notifyPropertyChanged(BR.kids);
    }

    @Bindable
    public String getUrl() { return url; }
    public void setUrl(String url) {
        this.url = url;
        notifyPropertyChanged(BR.url);
    }

    @Bindable
    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = score;
        notifyPropertyChanged(BR.score);
    }

    @Bindable
    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public ArrayList<Long> getParts() { return parts; }
    public void setParts(ArrayList<Long> parts) {
        this.parts = parts;
        notifyPropertyChanged(BR.parts);
    }

    @Bindable
    public int getDescendants() { return descendants; }
    public void setDescendants(int descendants) {
        this.descendants = descendants;
        notifyPropertyChanged(BR.descendants);
    }

    @Bindable
    public int getNestLevel() { return nestLevel; }
    public void setNestLevel(int nestLevel) {
        this.nestLevel = nestLevel;
        notifyPropertyChanged(BR.nestLevel);
    }

    public Boolean getIsLoaded() { return isLoaded; }
    public void setIsLoaded(Boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    // Default constructor
    public Item() {
        this.id = 0;
        this.deleted = false;
        this.type = "";
        this.by = "";
        this.text = "";
        this.dead = false;
        this.parent = 0;
        this.poll = 0;
        this.kids = new ArrayList<>();
        this.url = "";
        this.score = 0;
        this.title = "";
        this.parts = new ArrayList<>();
        this.descendants = 0;
        this.nestLevel = DEFAULT_NESTING_LEVEL;
        this.isLoaded = false;
    }

    // Constructor converting JSON object to instance of this class
    public static Item fromJson(JSONObject jo) {
        Item item = new Item();

        item.setId(jo.optLong(ID));
        item.setDeleted(jo.optBoolean(DELETED));
        item.setType(jo.optString(TYPE, ""));
        item.setBy(jo.optString(BY, ""));
        item.setText(jo.optString(TEXT, ""));
        item.setDead(jo.optBoolean(DEAD));
        item.setParent(jo.optLong(PARENT));
        item.setPoll(jo.optLong(POLL));

        JSONArray ja = jo.optJSONArray(KIDS);
        if (ja != null) {
            ArrayList<Long> arrKids = new ArrayList<>();
            for (int i = 0; i < ja.length(); ++i) {
                arrKids.add(ja.optLong(i));
            }
            item.setKids(arrKids);
        }

        item.setUrl(jo.optString(URL, ""));
        item.setScore(jo.optInt(SCORE));
        item.setTitle(jo.optString(TITLE, ""));

        ja = jo.optJSONArray(PARTS);
        if (ja != null) {
            ArrayList<Long> arrParts = new ArrayList<>();
            for (int i = 0; i < ja.length(); ++i) {
                arrParts.add(ja.optLong(i));
            }
            item.setParts(arrParts);
        }

        item.setDescendants(jo.optInt(DESCENDANTS));

        // Nesting level is not contained in JSON, it is calculated during comment tree unpacking
        item.setNestLevel(DEFAULT_NESTING_LEVEL);

        // Loaded status is not contained in JSON
        item.setIsLoaded(true);

        return item;
    }

    // Factory method for converting JSON object array to a list of object instances
    public static ArrayList<Item> fromJson(JSONArray ja) throws JSONException {
        JSONObject itemJson;

        int objectCount = ja.length();
        ArrayList<Item> items = new ArrayList<>(objectCount);

        for (int i = 0; i < objectCount; i++) {
            itemJson = ja.getJSONObject(i);
            Item item = Item.fromJson(itemJson);
            if (item != null) { items.add(item); }
        }
        return items;
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        // No file descriptors in class members, returning 0
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(this.id);
        parcel.writeInt(this.deleted ? 1 : 0);
        parcel.writeString(this.type);
        parcel.writeString(this.by);
        parcel.writeString(this.text);
        parcel.writeInt(this.dead ? 1 : 0);
        parcel.writeLong(this.parent);
        parcel.writeLong(this.poll);
        parcel.writeList(this.kids);
        parcel.writeString(this.url);
        parcel.writeInt(this.score);
        parcel.writeString(this.title);
        parcel.writeList(this.parts);
        parcel.writeInt(this.descendants);
        parcel.writeInt(this.nestLevel);
        parcel.writeInt(this.isLoaded ? 1 : 0);
    }

    // Constructor from incoming Parcel
    private Item(Parcel in) {
        this.id = in.readLong();
        this.deleted = in.readInt() != 0;
        this.type = in.readString();
        this.by = in.readString();
        this.text = in.readString();
        this.dead = in.readInt() != 0;
        this.parent = in.readLong();
        this.poll = in.readLong();
        if (this.kids == null) { this.kids = new ArrayList<>(); }
        in.readList(this.kids, null);
        this.url = in.readString();
        this.score = in.readInt();
        this.title = in.readString();
        if (this.parts == null) { this.parts = new ArrayList<>(); }
        in.readList(this.parts, null);
        this.descendants = in.readInt();
        this.nestLevel = in.readInt();
        this.isLoaded = in.readInt() != 0;
    }

    // Parcelable creator
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) { return new Item(in); }
        public Item[] newArray(int size) { return new Item[size]; }
    };

    /**
     * Search list of items for item with given id
     *
     * @param itemList
     * @param itemId
     * @return
     */
    public static Item findItemInList(List<Item> itemList, long itemId) {
        if (itemList == null) return null;
        for(Item item : itemList) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }

    /**
     * Diff callback
     * Used by ListAdapter
     *
     */
    public static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Item>() {
                @Override
                public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
                    // Item properties may have changed if reloaded from the DB, but ID is fixed
                    return oldItem.getId() == newItem.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.

                    ArrayList<Long> oldList, newList;

                    if (oldItem.getId() != newItem.getId()) return false;
                    if (oldItem.getDeleted() != newItem.getDeleted()) return false;
                    if (!oldItem.getType().equals(newItem.getType())) return false;
                    if (!oldItem.getBy().equals(newItem.getBy())) return false;
                    if (!oldItem.getText().equals(newItem.getText())) return false;
                    if (oldItem.getDead() != newItem.getDead()) return false;
                    if (oldItem.getParent() != newItem.getParent()) return false;
                    if (oldItem.getPoll() != newItem.getPoll()) return false;

                    oldList = oldItem.getKids();
                    newList = newItem.getKids();
                    if (!oldList.containsAll(newList) || !newList.containsAll(oldList))
                        return false;

                    if (!oldItem.getUrl().equals(newItem.getUrl())) return false;
                    if (oldItem.getScore() != newItem.getScore()) return false;
                    if (!oldItem.getTitle().equals(newItem.getTitle())) return false;

                    oldList = oldItem.getParts();
                    newList = newItem.getParts();
                    if (!oldList.containsAll(newList) || !newList.containsAll(oldList))
                        return false;

                    if (oldItem.getDescendants() != newItem.getDescendants()) return false;
                    //if (oldItem.getNestLevel() != newItem.getNestLevel()) return true;
                    if (oldItem.getIsLoaded() != newItem.getIsLoaded()) return false;

                    return true;
                }
            };

    static Item newEmptyItem(long itemId) {
        Item item = new Item();
        item.setId(itemId);
        item.setText(".....");
        item.setTitle("....");
        return item;
    }

    public static Item newLoadingItem(long itemId) {
        Item item = new Item();
        item.setId(itemId);
        item.setTitle("Loading " + Long.toString(itemId));
        item.setText(Long.toString(itemId));
        return item;
    }

    static Item newFailedItem(long itemId) {
        Item item = new Item();
        item.setId(itemId);
        item.setTitle("Loading " + Long.toString(itemId) + " failed");
        item.setText(Long.toString(itemId) + " failed");
        return item;
    }

}
