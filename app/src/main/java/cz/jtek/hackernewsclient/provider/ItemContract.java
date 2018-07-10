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

package cz.jtek.hackernewsclient.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ItemContract {

    public static final String AUTHORITY = "cz.jtek.hackernewsclient";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_ITEM = "item";

    public static final class ItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ITEM)
                .build();

        public static final String TABLE_NAME = "item";
        public static final String COL_ITEM_ID = "item_id";
        public static final String COL_DELETED = "deleted";
        public static final String COL_TYPE = "type";
        public static final String COL_BY = "by";
        public static final String COL_TEXT = "text";
        public static final String COL_DEAD = "dead";
        public static final String COL_PARENT = "parent";

    }

}
