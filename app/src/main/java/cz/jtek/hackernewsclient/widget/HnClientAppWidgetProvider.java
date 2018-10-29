package cz.jtek.hackernewsclient.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import cz.jtek.hackernewsclient.R;
import cz.jtek.hackernewsclient.ui.StoryListActivity;


public class HnClientAppWidgetProvider extends AppWidgetProvider {

    // HOWTO: https://developer.android.com/guide/topics/appwidgets/

    @SuppressWarnings("unused")
    private static final String TAG = HnClientAppWidgetProvider.class.getSimpleName();

    /**
     * This is called to update the App Widget at intervals defined by the updatePeriodMillis
     * attribute in the AppWidgetProviderInfo. This method is also called when the user adds
     * the App Widget, so it should perform the essential setup, such as define event handlers
     * for Views and start a temporary Service, if necessary.
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * This is called when an instance the App Widget is created for the first time. For example,
     * if the user adds two instances of your App Widget, this is only called the first time.
     * If you need to open a new database or perform other setup that only needs to occur once
     * for all App Widget instances, then this is a good place to do it.
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * This is called when the last instance of your App Widget is deleted from the App Widget host.
     * This is where you should clean up any work done in onEnabled(Context), such as delete
     * a temporary database.
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {

        RemoteViews rvs = new RemoteViews(context.getPackageName(), R.layout.widget_story_list);

        String widgetTitle = context.getResources().getString(R.string.widget_title_story_list);

        /*
        // Obtain currently selected recipe name and servings
        Cursor cursor;
        Uri RECIPE_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE).build();

        cursor = context.getContentResolver().query(RECIPE_URI,
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(RecipeContract.RecipeEntry.COL_NAME);
            int indexServings = cursor.getColumnIndex(RecipeContract.RecipeEntry.COL_SERVINGS);

            String name = cursor.getString(indexName);
            int servings = cursor.getInt(indexServings);

            if (name != null && name.length() > 0) {
                // Temporarily disabled for inconsistent behavior
                // widgetTitle = name + " " + context.getResources().getString(R.string.widget_title_for) + " " + Integer.toString(servings);
            }

            cursor.close();
        }
        */

        // Set widget title
        rvs.setTextViewText(R.id.widget_tv_story_list_title, widgetTitle);

        Intent intent = new Intent(context, StoryListService.class);
        rvs.setRemoteAdapter(R.id.widget_lv_story_list, intent);

        // Set the StoryListActivity intent to launch when clicked
        Intent appIntent = new Intent(context, StoryListActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rvs.setPendingIntentTemplate(R.id.widget_lv_story_list, appPendingIntent);
        // Handle empty story list ListView
        rvs.setEmptyView(R.id.widget_lv_story_list, R.id.widget_tv_no_stories);

        appWidgetManager.updateAppWidget(appWidgetId, rvs);
    }
}
