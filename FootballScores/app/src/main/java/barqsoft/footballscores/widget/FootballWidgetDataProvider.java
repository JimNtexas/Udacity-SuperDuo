package barqsoft.footballscores.widget;

import android.app.LauncherActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.myFetchService;

/**
 * Created by Jim on 11/14/2015.
 */
public class FootballWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "FootballWgtDataProvider";
    private ArrayList<String> dataList = new ArrayList<String>();
    private Context context = null;
    private int appWidgetId;

    public FootballWidgetDataProvider(Context context, Intent intent) {
        Log.d(TAG,"ctor");
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        getData();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG, "getViewAt: " + position + " data: " + dataList.get(position));
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_row);
        remoteView.setTextViewText(R.id.widget_row_string, dataList.get(position));

        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(myFetchService.ACTION_DATA_UPDATED);
        final Bundle bundle = new Bundle();
        fillInIntent.putExtras(bundle);
        remoteView.setOnClickFillInIntent(R.id.widget_row_string, fillInIntent);


        return remoteView;
    }

    private void getData() {

        final long token = Binder.clearCallingIdentity();
        try {
            Uri uri = Uri.parse("content://barqsoft.footballscores/date");
            String[] args = new String[1];
            Date systemDate = new Date(System.currentTimeMillis());
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd"); // To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale) with for example Locale.US for ASCII dates
            args[0] = mformat.format(systemDate);
            String result;
            Cursor cursor = context.getContentResolver().query(uri, null, null, args, null);
            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Database empty!");
                String noResults = context.getResources().getString(R.string.no_results);
                dataList.add(noResults);
            } else {
                int cnt = 0;
                do {

                    WidgetData data = new WidgetData(cursor);
                    Log.d(TAG, cnt + ": " + data.getDisplayString());
                    dataList.add(data.getDisplayString());
                    ++cnt;

                } while (cursor.moveToNext());

                Log.d(TAG, "data read");
            }
            cursor.close();
        } finally {
            Binder.restoreCallingIdentity((token));
        }
    }

        @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        getData();
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG,"onDataSetChanged");
        getData();
    }

    @Override
    public void onDestroy() {
    }

}
