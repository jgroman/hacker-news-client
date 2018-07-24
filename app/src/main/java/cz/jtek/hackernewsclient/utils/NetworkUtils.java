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

package cz.jtek.hackernewsclient.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {

    @SuppressWarnings("unused")
    private static final String TAG = NetworkUtils.class.getSimpleName();

    /**
     * This method tests for network availability
     *
     * @return true if network connection available
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }

        return false;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        return response.body().string();
    }

    /**
     * This method returns MIME type of file at given URL
     * @param url File URL
     * @return File MIME type
     */
    public static String getUrlMimeType(String url)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getMimeTypeFromExtension(extension);
        }
        return null;
    }

    /**
     * Class for returning results from AsyncTaskLoader
     * Result contains either result of desired type or an exception
     *
     * @param <T> Result type
     */
    public static class AsyncTaskResult<T> {
        private final T result;
        private final Exception exception;

        public AsyncTaskResult(T result, Exception exception) {
            this.result = result;
            this.exception = exception;
        }

        public Exception getException() { return exception; }
        public T getResult() { return result; }

        public boolean hasException() { return exception != null; }
        public boolean hasResult() { return result != null; }
    }
}

