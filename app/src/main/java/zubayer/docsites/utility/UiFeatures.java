package zubayer.docsites.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Pattern;

public class UiFeatures {
    public static boolean dataconnected(Context context) {
        boolean wifiIsAvailable=false, mobileDataIsAvailable=false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (networkInfo != null) {
                wifiIsAvailable = networkInfo.isConnected();

            }
            NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileNetworkInfo != null) {
                mobileDataIsAvailable = mobileNetworkInfo.isConnected();

            }
        }
        return (wifiIsAvailable || mobileDataIsAvailable);
    }
    public static boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
