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

import java.net.MalformedURLException;
import java.net.URL;

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

}
