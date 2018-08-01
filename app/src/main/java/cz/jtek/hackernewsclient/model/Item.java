package cz.jtek.hackernewsclient.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Item implements Parcelable {

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
    private long mId;
    private Boolean mDeleted;
    private String mType;
    private String mBy;
    private String mText;
    private Boolean mDead;
    private long mParent;
    private long mPoll;
    private long[] mKids;
    private String mUrl;
    private int mScore;
    private String mTitle;
    private long[] mParts;
    private int mDescendants;

    // Getters & Setters
    public long getId() { return mId; }
    public void setId(long id) { mId = id; }

    public Boolean getDeleted() { return mDeleted; }
    public void setDeleted(Boolean deleted) { mDeleted = deleted; }

    public String getType() { return mType; }
    public void setType(String type) { mType = type; }

    public String getBy() { return mBy; }
    public void setBy(String by) { mBy = by; }

    public String getText() { return mText; }
    public void setText(String text) { mText = text; }

    public Boolean getDead() { return mDead; }
    public void setDead(Boolean dead) { this.mDead = dead; }

    public long getParent() { return mParent; }
    public void setParent(long parent) { this.mParent = parent; }

    public long getPoll() { return mPoll; }
    public void setPoll(long poll) { this.mPoll = poll; }

    public long[] getKids() { return mKids; }
    public void setKids(long[] kids) { this.mKids = kids; }

    public String getURL() { return mUrl; }
    public void setUrl(String url) { this.mUrl = url; }

    public int getScore() { return mScore; }
    public void setScore(int score) { this.mScore = score; }

    public String getTitle() { return mTitle; }
    public void setTitle(String title) { this.mTitle = title; }

    public long[] getParts() { return mParts; }
    public void setParts(long[] parts) { this.mParts = parts; }

    public int getDescendants() { return mDescendants; }
    public void setmDescendants(int descendants) { this.mDescendants = descendants; }

    // Default constructor
    public Item() {
        mId = 0;
        mDeleted = false;
        mType = null;
        mBy = null;
        mText = null;
        mDead = false;
        mParent = 0;
        mPoll = 0;
        mKids = null;
        mUrl = null;
        mScore = 0;
        mTitle = null;
        mParts = null;
        mDescendants = 0;
    }

    // Constructor converting JSON object to instance of this class
    public static Item fromJson(JSONObject jo) {
        Item item = new Item();

        item.mId = jo.optLong(ITEM_ID);
        item.mDeleted = jo.optBoolean(DELETED);
        item.mType = jo.optString(TYPE, null);
        item.mBy = jo.optString(BY, null);
        item.mText = jo.optString(TEXT, null);
        item.mDead = jo.optBoolean(DEAD);
        item.mParent = jo.optLong(PARENT);
        item.mPoll = jo.optLong(POLL);

        JSONArray ja = jo.optJSONArray(KIDS);
        if (ja != null) {
            item.mKids = new long[ja.length()];
            for (int i = 0; i < ja.length(); ++i) {
                item.mKids[i] = ja.optLong(i);
            }
        }

        item.mUrl = jo.optString(URL, null);
        item.mScore = jo.optInt(SCORE);
        item.mTitle = jo.optString(TITLE, null);

        ja = jo.optJSONArray(PARTS);
        if (ja != null) {
            item.mParts = new long[ja.length()];
            for (int i = 0; i < ja.length(); ++i) {
                item.mParts[i] = ja.optLong(i);
            }
        }

        item.mDescendants = jo.optInt(DESCENDANTS);

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
        parcel.writeLong(mId);
        parcel.writeInt(mDeleted ? 1 : 0);
        parcel.writeString(mType);
        parcel.writeString(mBy);
        parcel.writeString(mText);
        parcel.writeInt(mDead ? 1 : 0);
        parcel.writeLong(mParent);
        parcel.writeLong(mPoll);
        parcel.writeLongArray(mKids);
        parcel.writeString(mUrl);
        parcel.writeInt(mScore);
        parcel.writeString(mTitle);
        parcel.writeLongArray(mParts);
        parcel.writeInt(mDescendants);
    }

    // Constructor from incoming Parcel
    private Item(Parcel in) {
        mId = in.readLong();
        mDeleted = in.readInt() != 0;
        mType = in.readString();
        mBy = in.readString();
        mText = in.readString();
        mDead = in.readInt() != 0;
        mParent = in.readLong();
        mPoll = in.readLong();
        if (mKids == null) { mKids = new long[1]; }
        in.readLongArray(mKids);
        mUrl = in.readString();
        mScore = in.readInt();
        mTitle = in.readString();
        if (mParts == null) { mParts = new long[1]; }
        in.readLongArray(mParts);
        mDescendants = in.readInt();
    }

    // Parcelable creator
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) { return new Item(in); }
        public Item[] newArray(int size) { return new Item[size]; }
    };
}


