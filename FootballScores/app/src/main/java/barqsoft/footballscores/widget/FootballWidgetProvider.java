package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.R;
import barqsoft.footballscores.service.myFetchService;
import barqsoft.footballscores.widget.FootballWidgetRemoteViewService;

public class FootballWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "FootballWidgetProvider";
    public static final String EXTRA_STRING = "barqsoft.footballscores.widget.EXTRA_String";

    public FootballWidgetProvider(Context applicationContext, Intent intent) {
    }

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

        Log.d(TAG,"onUpdate");
        for (int widgetId : appWidgetIds) {
            RemoteViews mView = updateWidgetListView(context, appWidgetManager, widgetId);
            Log.i("wprovider", "widgetId: " + widgetId);
            final Intent onItemClick = new Intent(context, FootballWidgetProvider.class);

            // Adding collection list item handler
           // onItemClick.setAction(ACTION_TOAST);
            onItemClick.setData(Uri.parse(onItemClick
                    .toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent onClickPendingIntent = PendingIntent
                    .getBroadcast(context, 0, onItemClick,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            mView.setPendingIntentTemplate(R.id.widgetCollectionList,
                    onClickPendingIntent);

            appWidgetManager.updateAppWidget(widgetId, mView);
        }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called with " + intent.getAction());
        if(intent.getAction().equals(myFetchService.ACTION_DATA_UPDATED)) {
            Log.d(TAG,"UPDATE WIDGET DATA");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetCollectionList);

        }

        super.onReceive(context, intent);
    }

    private RemoteViews updateWidgetListView(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG,"updateWidgetListView");
		//which layout to show on widget
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_provider_layout);
		
		//RemoteViews Service needed to provide adapter for ListView
		Intent svcIntent = new Intent(context, FootballWidgetRemoteViewService.class);
		//passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		//setting a unique Uri to the intent
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		//setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(appWidgetId, R.id.widgetCollectionList, svcIntent);
		return remoteViews;
	}

    public FootballWidgetProvider() {
        super();
    }

}