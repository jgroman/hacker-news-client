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

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {

    /**
     * TypeConverter from JSON string to ArrayList<Long>
     *
     * @param jsonString  JSON string.
     * @return ArrayList<Long>.
     */
    @TypeConverter
    public static ArrayList<Long> fromString(String jsonString) {
        Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
        return new Gson().fromJson(jsonString, listType);
    }

    /**
     * TypeConverter from ArrayList<Long> to JSON string
     *
     * @param list  ArrayList<Long>.
     * @return JSON string.
     */
    @TypeConverter
    public static String fromArrayList(ArrayList<Long> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

}