package zubayer.docsites.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import zubayer.docsites.R;
import zubayer.docsites.activity.Reply;

public class FirebaseForegroundMessage extends FirebaseMessagingService {
    public static final String TAG = "Mytag";
    Intent intent;
    PendingIntent pendingIntent;
    HashMap<String,String> dataPlayLoad=new HashMap<>();
    String imageUrl;
    private Bitmap bitmap;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        imageUrl=remoteMessage.getData().get("icon");
        if (remoteMessage.getData().size() > 0) {
            dataPlayLoad.putAll(remoteMessage.getData());
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        }

        if (remoteMessage.getNotification() != null) {

            if(imageUrl!=null)
                notification("firebase", "firebase_channel",
                        remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), notifyID(),getBitmapFromUrl(imageUrl));
        else notification("firebase", "firebase_channel",
                    remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), notifyID(),BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground));
        }
    }

    private int notifyID() {
        Random random=new Random();
        random.nextInt();
        return random.nextInt();
    }

    private void notification(String channel_id, String channel_name, String title, String text, int notify_id,Bitmap bitmap) {
        intent=new Intent(this, Reply.class);
        intent.putExtra("postID",dataPlayLoad.get("postID"));
        intent.putExtra("intent",dataPlayLoad.get("intent"));
        pendingIntent = PendingIntent.getActivity(this, notifyID(), intent, 0);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_HIGH);
            channel.shouldShowLights();
            channel.shouldVibrate();
            channel.canShowBadge();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            Notification notification = new NotificationCompat.Builder(FirebaseForegroundMessage.this,channel_id)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setColor(0xff990000)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setVibrate(new long[]{0, 300, 300, 300})
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setChannelId(channel_id).build();

            if (notificationManager != null) {
                notificationManager.notify(notify_id, notification);
            }
        } else {
            notification2(title, text, notify_id,channel_id,bitmap);
        }
    }

    private void notification2(String title, String text, int id, String channel_id,Bitmap bitmap) {
        intent=new Intent(this, Reply.class);
        intent.putExtra("postID",dataPlayLoad.get("postID"));
        intent.putExtra("intent",dataPlayLoad.get("intent"));
        pendingIntent = PendingIntent.getActivity(this, notifyID(), intent, 0);
        NotificationCompat.BigTextStyle bigTextStyle;
        bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(text);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,channel_id)
                .setContentTitle(title)
                .setContentText(text)
                .setColor(0xff990000)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 300, 300, 300})
                .setLights(Color.GREEN, 1000, 1000)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(bigTextStyle)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(bitmap);

        if (notificationManager != null) {
            notificationManager.notify(id, notificationBuilder.build());
        }
    }
    private Bitmap getBitmapFromUrl(String imageUrl) {

        InputStream in;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
