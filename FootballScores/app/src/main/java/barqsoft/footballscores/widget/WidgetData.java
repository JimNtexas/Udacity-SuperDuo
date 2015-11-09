package barqsoft.footballscores.widget;

import android.database.Cursor;

/**
 * Created by Jim on 11/9/2015.
 */
public class WidgetData {
    public String home;
    public String home_goals;
    public String time;
    public String away;
    public String away_goals;

    public WidgetData(Cursor cursor) {
        int i = cursor.getColumnIndex("home");
        home = cursor.getString(i);
        i = cursor.getColumnIndex("home_goals");
        home_goals = cursor.getString(i);
        i = cursor.getColumnIndex("time");
        time = cursor.getString(i);
        i = cursor.getColumnIndex("away");
        away = cursor.getString(i);
        i = cursor.getColumnIndex("away_goals");
        away_goals = cursor.getString(i);
    }

    public String getDisplayString() {
        String result = "";
        if( Integer.parseInt(home_goals) < 0 || Integer.parseInt(away_goals) < 0 ) {
            result = result.format("%s  -  %s  - %s", home,time,away);
        } else {
            result = result.format("%s : %s  -  %s : %s", home, home_goals,away,away_goals);
        }
        return result;
    }

}
