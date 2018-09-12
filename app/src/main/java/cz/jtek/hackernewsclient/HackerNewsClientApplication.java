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

package cz.jtek.hackernewsclient;

import android.app.Application;

import com.facebook.stetho.Stetho;

import cz.jtek.hackernewsclient.data.AppDatabase;
import cz.jtek.hackernewsclient.data.DataRepository;

public class HackerNewsClientApplication extends Application {

    public void onCreate() {
        super.onCreate();

        //if (BuildConfig.DEBUG) {
            // Initialize Stetho only for debug builds
            Stetho.initializeWithDefaults(this);
        //}
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(this, getDatabase());
    }
}
