package barqsoft.footballscores.widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by Jim on 11/13/2015.
 */

@SuppressLint("NewApi")
public class FootballWidgetRemoteViewService extends RemoteViewsService {
    private static final String TAG = "FootballWgtRmtViewSvc";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        Log.d(TAG,"onGetViewFactory");
        FootballWidgetDataProvider dataProvider = new FootballWidgetDataProvider(
                getApplicationContext(), intent);
        return dataProvider;
    }

}
