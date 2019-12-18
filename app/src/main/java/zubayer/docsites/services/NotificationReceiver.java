package zubayer.docsites.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        WorkManager mWorkManager = WorkManager.getInstance(context);
//        mWorkManager.cancelAllWork();
        mWorkManager.enqueue(OneTimeWorkRequest.from(MyWorkManager.class));
    }
}
