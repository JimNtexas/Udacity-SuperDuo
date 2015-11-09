package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;

/**
 * Created by Jim on 11/8/2015.
 */
public class FootballWidgetService extends IntentService  {

    private final String TAG= "FootballWidgetService";
    private ScoresProvider mProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Football Widget Service started");
        getData();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent");
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                FootballWidgetProvider.class));

        getData();

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_provider_layout;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            Log.d(TAG, "updating widget: " + appWidgetId);
           // views.setRemoteAdapter(appWidgetId,R.id.widgetCollectionList, data);
        }

    }

    private void getData() {

        Uri uri = Uri.parse("content://barqsoft.footballscores/date");
        String[] args = new String[1];
        Date systemDate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd"); // To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale) with for example Locale.US for ASCII dates
        args[0] = mformat.format(systemDate);
        String result;
        Cursor cursor =  getContentResolver().query(uri,null,null,args,null);
        if(!cursor.moveToFirst()) {
            Log.e(TAG, "Database empty!");
        } else {
            int cnt = 0;
            do {

                WidgetData data = new WidgetData(cursor);
                Log.d(TAG, cnt + ": " + data.getDisplayString());
                ++cnt;

            } while(cursor.moveToNext());

            Log.d(TAG, "data read");
        }
        cursor.close();
    }

    public FootballWidgetService() {
        super("FootballWidgetService");
    }


}
