package cafe.adriel.popularmovies.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.lang.reflect.Field;

import cafe.adriel.popularmovies.R;

public class Util {

    private static ConnectivityManager connectivityManager;

    public static DateTime toDate(String date){
        return DateTimeFormat.forPattern("yyyy-MM-dd")
                .parseDateTime(date);
    }

    public static String prettyDate(Context context, DateTime date){
        return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_YEAR);
    }

    public static void setupToolbar(Context context, Toolbar toolbar){
        TextView titleView = null;
        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleView = (TextView) f.get(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(titleView != null) {
            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/MarckScript-Regular.ttf");
            titleView.setTypeface(font);
            titleView.setTextSize(26);
            titleView.setPadding(20, 10, 0, 0);
        }
        toolbar.setLogo(new IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_movie)
                .sizeDp(24));
    }

    public static boolean isConnected(final Activity activity, boolean showToast){
        if(connectivityManager == null) {
            connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = (info != null && info.isConnected());
        if(!isConnected && showToast){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, R.string.connect_internet, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return isConnected;
    }

}