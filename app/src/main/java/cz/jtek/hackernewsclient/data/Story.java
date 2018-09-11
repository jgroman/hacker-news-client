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

import java.util.ArrayList;

@Entity(tableName = Story.TABLE_NAME)
public class Story {

    @SuppressWarnings("unused")
    private static final String TAG = Story.class.getSimpleName();

    public static final String TABLE_NAME = "stories";
    public static final String COL_TYPE = "type";
    public static final String COL_STORIES = "stories";

    @PrimaryKey
    @ColumnInfo(name = COL_TYPE)
    private String type;

    @ColumnInfo(name = COL_STORIES)
    private ArrayList<Long> stories;

}
