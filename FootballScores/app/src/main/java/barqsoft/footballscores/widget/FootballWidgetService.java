package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import barqsoft.footballscores.R;

/**
 * Created by Jim on 11/8/2015.
 */
public class FootballWidgetService extends IntentService {

    private final String TAG= "FootballWidgetService";

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent");
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                FootballWidgetProvider.class));

       String[] data = intent.getExtras().getStringArray("data");
        for(String item : data) {
            Log.d(TAG, "data item: " +item );
        }

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_provider_layout;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            Log.d(TAG, "updating widget: " + appWidgetId);
           // views.setRemoteAdapter(appWidgetId,R.id.widgetCollectionList, data);
        }

    }


    public FootballWidgetService() {
        super("FootballWidgetService");
    }
}
