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

package cz.jtek.hackernewsclient.model;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import cz.jtek.hackernewsclient.data.Item;

public class HackerNewsApi {

    // API docs: https://github.com/HackerNews/API

    @SuppressWarnings("unused")
    private static final String TAG = HackerNewsApi.class.getSimpleName();

    private static final String API_SCHEME = "https";
    private static final String API_AUTHORITY = "hacker-news.firebaseio.com";
    private static final String API_VERSION = "v0";

    private static final String API_PATH_STORIES_NEW  = "newstories";
    private static final String API_PATH_STORIES_TOP  = "topstories";
    private static final String API_PATH_STORIES_BEST = "beststories";
    private static final String API_PATH_STORIES_ASK  = "askstories";
    private static final String API_PATH_STORIES_SHOW = "showstories";
    private static final String API_PATH_STORIES_JOB  = "jobstories";

    private static final String API_PATH_USER  = "user";

    private static final String API_PATH_ITEM  = "item";

    /**
     *
     * @param storyType
     * @return
     */
    public static URL buildStoriesUrl(String storyType) {

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_VERSION);

        switch(storyType) {

            case("new"):
                uriBuilder.appendPath(API_PATH_STORIES_NEW);
                break;

            case("top"):
                uriBuilder.appendPath(API_PATH_STORIES_TOP);
                break;


            default:
                uriBuilder.appendPath(API_PATH_STORIES_NEW);
                break;
        }

        Uri uri = uriBuilder.build();

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param itemId
     * @return
     */
    public static URL buildItemUrl(long itemId) {

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_VERSION)
                .appendPath(API_PATH_ITEM)
                .appendPath(String.valueOf(itemId));

        Uri uri = uriBuilder.build();

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param storiesJsonString
     * @return
     */
    public static HackerNewsJsonResult<long[]> getStoriesFromJson(String storiesJsonString) {
        long[] stories;

        try {
            JSONArray ja = new JSONArray(storiesJsonString);
            stories = new long[ja.length()];
            for (int i = 0; i < ja.length(); ++i) {
                stories[i] = ja.optLong(i);
            }
            return new HackerNewsJsonResult<>(stories, null);
        }
        catch (JSONException jex) {
            Log.e(TAG, String.format("JSON Exception parsing Hacker News stories: %s", jex.getMessage()));
            return new HackerNewsJsonResult<>(null, jex);
        }
    }

    /**
     *
     * @param itemJsonString
     * @return
     */
    public static HackerNewsJsonResult<Item> getItemFromJson(String itemJsonString) {
        try {
            Item item = Item.fromJson(new JSONObject(itemJsonString));
            return new HackerNewsJsonResult<>(item, null);
        } catch (JSONException jex) {
            Log.e(TAG, String.format("JSON Exception parsing Hacker News item: %s", jex.getMessage()));
            return new HackerNewsJsonResult<>(null, jex);
        }
    }


    /**
     *
     * @param itemsJsonString
     * @return
     */
    public static HackerNewsJsonResult<ArrayList<Item>> getItemsFromJson(String itemsJsonString) {
        try {
            ArrayList<Item> items = Item.fromJson(new JSONArray(itemsJsonString));
            return new HackerNewsJsonResult<>(items, null);
        } catch (JSONException jex) {
            Log.e(TAG, String.format("JSON Exception parsing Hacker News item: %s", jex.getMessage()));
            return new HackerNewsJsonResult<>(null, jex);
        }
    }

    /**
     * JSON result wrapper
     * Allows returning either result or exception
     *
     * @param <T> Result type
     */
    public static class HackerNewsJsonResult<T> {
        private final T result;
        private final Exception exception;

        HackerNewsJsonResult(T result, Exception exception) {
            this.result = result;
            this.exception = exception;
        }

        public T getResult() { return result; }

        public Exception getException() { return exception; }

        // Checks whether instance contains an exception
        public boolean hasException() { return exception != null; }
    }
}
