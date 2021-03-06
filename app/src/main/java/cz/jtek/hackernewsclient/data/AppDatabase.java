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

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Item.class, StoryList.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    // Database file name
    private static final String DB_NAME = "hnc_db";

    /**
     * @return The DAO for the StoryList table.
     */
    public abstract StoryListDao storyListDao();

    /**
     * @return The DAO for the Item table.
     */
    public abstract ItemDao itemDao();

    /** Singleton instance */
    private static AppDatabase sInstance;

    /**
     * Gets the singleton instance of AppDatabase
     *
     * @param context The context.
     * @return The singleton instance of AppDatabase
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room
                    .databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .build();
        }
        return sInstance;
    }
}

