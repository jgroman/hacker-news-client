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

package cz.jtek.hackernewsclient.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.data.Item;
import cz.jtek.hackernewsclient.model.ItemViewModel;

public class CommentListActivity extends AppCompatActivity
        implements CommentListFragment.OnCommentClickListener{

    @SuppressWarnings("unused")
    private static final String TAG = CommentListActivity.class.getSimpleName();

    // Extras
    public static final String EXTRA_STORY_ID = "story-id";

    private CollapsingToolbarLayout mToolbarLayout;
    private Toolbar mToolbar;

    private ItemViewModel mItemModel;

    private long mStoryId;


    public static Intent newInstance(AppCompatActivity activity, long storyId) {
        Intent intent = new Intent(activity, CommentListActivity.class);
        intent.putExtra(EXTRA_STORY_ID, storyId);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);

        if (savedInstanceState == null) {
            // Get story id from intent extras
            Intent intent = getIntent();
            if (intent == null) { return; }

            if (intent.hasExtra(EXTRA_STORY_ID)) {
                mStoryId = intent.getLongExtra(EXTRA_STORY_ID, 0);
            }
        }
        else {
            // Retrieve story id from saved state
            mStoryId = savedInstanceState.getLong(EXTRA_STORY_ID);
        }

        mItemModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        Item parentStoryItem = mItemModel.getItem(mStoryId);

        mToolbarLayout = findViewById(R.id.ctl_story);
        mToolbar = findViewById(R.id.toolbar_story);

        // Back navigation
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(v -> finish());

        mToolbar.setTitle(parentStoryItem.getTitle());

        // Create comment list fragment
        CommentListFragment commentListFragment = CommentListFragment.newInstance(mStoryId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.comment_list_fragment_container, commentListFragment)
                .commit();
    }


    /**
     * Comment click listener
     * Gets events from underlying fragment
     *
     * @param itemId Comment item id
     */
    @Override
    public void onCommentSelected(long itemId) {

    }
}
