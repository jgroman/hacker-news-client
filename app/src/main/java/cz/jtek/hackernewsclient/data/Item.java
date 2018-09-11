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
import android.arch.persistence.room.PrimaryKey;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.jtek.hackernewsclient.BR;

@Entity(tableName = Item.TABLE_NAME)
public class Item extends BaseObservable implements Parcelable {

    @SuppressWarnings("unused")
    private static final String TAG = Item.class.getSimpleName();

    public static final String TABLE_NAME = "items";

    // JSON field string ids, also Room column names
    public static final String ID = "id";
    public static final String DELETED = "deleted";
    public static final String TYPE = "type";
    public static final String BY = "by";
    public static final String TEXT = "text";
    public static final String DEAD = "dead";
    public static final String PARENT = "parent";
    public static final String POLL = "poll";
    public static final String KIDS = "kids";
    public static final String URL = "url";
    public static final String SCORE = "score";
    public static final String TITLE = "title";
    public static final String PARTS = "parts";
    public static final String DESCENDANTS = "descendants";

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

    // Default constructor
    public Item() {
        this.id = 0;
        this.deleted = false;
        this.type = null;
        this.by = null;
        this.text = null;
        this.dead = false;
        this.parent = 0;
        this.poll = 0;
        this.kids = null;
        this.url = null;
        this.score = 0;
        this.title = null;
        this.parts = null;
        this.descendants = 0;
    }

    // Constructor converting JSON object to instance of this class
    public static Item fromJson(JSONObject jo) {
        Item item = new Item();

        item.setId(jo.optLong(ID));
        item.setDeleted(jo.optBoolean(DELETED));
        item.setType(jo.optString(TYPE, null));
        item.setBy(jo.optString(BY, null));
        item.setText(jo.optString(TEXT, null));
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

        item.setUrl(jo.optString(URL, null));
        item.setScore(jo.optInt(SCORE));
        item.setTitle(jo.optString(TITLE, null));

        ja = jo.optJSONArray(PARTS);
        if (ja != null) {
            ArrayList<Long> arrParts = new ArrayList<>();
            for (int i = 0; i < ja.length(); ++i) {
                arrParts.add(ja.optLong(i));
            }
            item.setParts(arrParts);
        }

        item.setDescendants(jo.optInt(DESCENDANTS));

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
    }

    // Parcelable creator
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) { return new Item(in); }
        public Item[] newArray(int size) { return new Item[size]; }
    };
}

