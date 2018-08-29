package cz.jtek.hackernewsclient.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.jtek.hackernewsclient.BR;

public class Item extends BaseObservable implements Parcelable {

    @SuppressWarnings("unused")
    private static final String TAG = Item.class.getSimpleName();

    // JSON field string ids
    private static final String ITEM_ID = "id";
    private static final String DELETED = "deleted";
    private static final String TYPE = "type";
    private static final String BY = "by";
    private static final String TEXT = "text";
    private static final String DEAD = "dead";
    private static final String PARENT = "parent";
    private static final String POLL = "poll";
    private static final String KIDS = "kids";
    private static final String URL = "url";
    private static final String SCORE = "score";
    private static final String TITLE = "title";
    private static final String PARTS = "parts";
    private static final String DESCENDANTS = "descendants";

    // Members
    private long id;
    private Boolean deleted;
    private String type;
    private String by;
    private String text;
    private Boolean dead;
    private long parent;
    private long poll;
    private long[] kids;
    private String url;
    private int score;
    private String title;
    private long[] parts;
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
    public long[] getKids() { return kids; }
    public void setKids(long[] kids) {
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
    public long[] getParts() { return parts; }
    public void setParts(long[] parts) {
        this.parts = parts;
        notifyPropertyChanged(BR.parts);
    }

    @Bindable
    public int getDescendants() { return descendants; }
    public void setDescendants(int descendants) {
        this.descendants = descendants;
        notifyPropertyChanged(BR.descendants);
    }

    /*
    @BindingAdapter("bind:itemId")
    public static void loadItem(long itemId) {

    }
    */

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

        item.setId(jo.optLong(ITEM_ID));
        item.setDeleted(jo.optBoolean(DELETED));
        item.setType(jo.optString(TYPE, null));
        item.setBy(jo.optString(BY, null));
        item.setText(jo.optString(TEXT, null));
        item.setDead(jo.optBoolean(DEAD));
        item.setParent(jo.optLong(PARENT));
        item.setPoll(jo.optLong(POLL));

        JSONArray ja = jo.optJSONArray(KIDS);
        if (ja != null) {
            long[] arrKids = new long[ja.length()];
            for (int i = 0; i < ja.length(); ++i) {
                arrKids[i] = ja.optLong(i);
            }
            item.setKids(arrKids);
        }

        item.setUrl(jo.optString(URL, null));
        item.setScore(jo.optInt(SCORE));
        item.setTitle(jo.optString(TITLE, null));

        ja = jo.optJSONArray(PARTS);
        if (ja != null) {
            long[] arrParts = new long[ja.length()];
            for (int i = 0; i < ja.length(); ++i) {
                arrParts[i] = ja.optLong(i);
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
        parcel.writeLongArray(this.kids);
        parcel.writeString(this.url);
        parcel.writeInt(this.score);
        parcel.writeString(this.title);
        parcel.writeLongArray(this.parts);
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
        if (this.kids == null) { this.kids = new long[1]; }
        in.readLongArray(this.kids);
        this.url = in.readString();
        this.score = in.readInt();
        this.title = in.readString();
        if (this.parts == null) { this.parts = new long[1]; }
        in.readLongArray(this.parts);
        this.descendants = in.readInt();
    }

    // Parcelable creator
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) { return new Item(in); }
        public Item[] newArray(int size) { return new Item[size]; }
    };
}


