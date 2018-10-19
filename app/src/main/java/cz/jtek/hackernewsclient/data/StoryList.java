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
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;

@Entity(tableName = StoryList.TABLE_NAME)
public class StoryList {

    @SuppressWarnings("unused")
    private static final String TAG = StoryList.class.getSimpleName();

    static final String TABLE_NAME = "story_lists";
    static final String COL_TYPE = "type";
    static final String COL_STORIES = "stories";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = COL_TYPE)
    private String type;

    @ColumnInfo(name = COL_STORIES)
    private ArrayList<Long> stories;

    @NonNull
    public String getType() { return type; }
    public void setType(@NonNull String type) {
        this.type = type;
    }

    public ArrayList<Long> getStories() { return stories; }
    public void setStories(ArrayList<Long> stories) {
        this.stories = stories;
    }

    public StoryList(@NonNull String type, ArrayList<Long> stories) {
        this.type = type;
        this.stories = stories;
    }

    /**
     *
     */
    public static final DiffUtil.ItemCallback<StoryList> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<StoryList>() {

                @Override
                public boolean areItemsTheSame(@NonNull StoryList oldItem, @NonNull StoryList newItem) {
                    // Item properties may have changed if reloaded from the DB, but ID is fixed
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull StoryList oldItem, @NonNull StoryList newItem) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.

                    ArrayList<Long> oldList, newList;

                    if (!oldItem.getType().equals(newItem.getType())) return false;

                    oldList = oldItem.getStories();
                    newList = newItem.getStories();
                    if (!oldList.containsAll(newList) || !newList.containsAll(oldList))
                        return false;

                    return true;
                }
            };


}
