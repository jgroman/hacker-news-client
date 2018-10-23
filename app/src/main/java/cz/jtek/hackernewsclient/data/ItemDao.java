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

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ItemDao {

    @Query("SELECT * FROM " + Item.TABLE_NAME)
    LiveData<List<Item>> getAllItems();

    @Query("SELECT * FROM " + Item.TABLE_NAME + " WHERE " + Item.TYPE + " = 'comment'")
    LiveData<List<Item>> getAllCommentItems();

    @Query("SELECT * FROM " + Item.TABLE_NAME + " WHERE " + Item.ID + " IN (:itemIds)")
    LiveData<List<Item>> getItemsByIds(List<Long> itemIds);

    @Query("SELECT * FROM " + Item.TABLE_NAME + " WHERE " + Item.ID + " = :itemId")
    LiveData<Item> getItem(Long itemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Item... item);

    @Update
    void updateItems(Item... items);

    @Query("DELETE FROM " + Item.TABLE_NAME)
    void deleteAll();

}
