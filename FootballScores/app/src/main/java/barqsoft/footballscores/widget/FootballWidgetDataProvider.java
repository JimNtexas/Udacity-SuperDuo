package barqsoft.footballscores.widget;

import android.app.LauncherActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import barqsoft.footballscores.R;

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

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG,"getViewAt: " + position + " data: " + dataList.get(position));
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_row);
        remoteView.setTextViewText(R.id.widget_row_string, dataList.get(position));
        return remoteView;
    }

    private void getData() {
        dataList.clear();
        for(int i=1; i<5; i++) {
            dataList.add("data line: " + i);
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
