package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
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

        Log.d(TAG, "onUpdate");
        for (int widgetId : appWidgetIds) {

            RemoteViews mView = updateWidgetListView(context, appWidgetManager, widgetId);
            Log.i("wprovider", "widgetId: " + widgetId);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            mView.setOnClickPendingIntent(R.id.widgetLayoutMain, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, mView);
            } else {
                setRemoteAdapterV11(context, mView);
            }

            Intent clickIntentTemplate = new Intent(context, MainActivity.class);
            clickIntentTemplate.addFlags(Intent.URI_INTENT_SCHEME);
            PendingIntent clickPendingIntentTemplate =TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mView.setPendingIntentTemplate(R.id.widgetCollectionList, clickPendingIntentTemplate);

            appWidgetManager.updateAppWidget(widgetId, mView);
        }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widgetCollectionList,
                new Intent(context, FootballWidgetRemoteViewService.class));
    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widgetCollectionList,
                new Intent(context, FootballWidgetRemoteViewService.class));
    }

}