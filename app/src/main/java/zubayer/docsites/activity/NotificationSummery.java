package zubayer.docsites.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.internal.NavigationMenu;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import me.anwarshahriar.calligrapher.Calligrapher;
import zubayer.docsites.adapters.MyAdapter;
import zubayer.docsites.adapters.NotificationListAdapter;
import zubayer.docsites.R;
import zubayer.docsites.services.MyWorkManager;

import static zubayer.docsites.utility.UiFeatures.dataconnected;

import static android.widget.Toast.makeText;

public class NotificationSummery extends Activity {
    private AlertDialog Dialog, checkinternet;
    private AlertDialog.Builder builder;
    private ArrayList<String> dates, seens, headings, urls, texts, missedNotifications, falseurls;
    private View m;
    private ListView notificationList;
    private ProgressBar progressBar;
    private WorkManager mWorkManager;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_summery);

        initializer();
        setFont();
        readNotification();
        readMissedNotification();
        setListView();
        buildAlertDialogue();
        saveState();
        equilifyNotificationCount();


        FabSpeedDial fabspeed = findViewById(R.id.fabsummery);
        fabspeed.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.off:
                        Intent setting = new Intent(NotificationSummery.this, Settings.class);
                        startActivity(setting);
                        break;
                    case R.id.missed:
                        Dialog.show();
                        progressBar.setVisibility(View.GONE);
                        break;
                    case R.id.refresh:
                        readNotification();
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed() {

            }
        });
    }

    private void initializer() {
        mWorkManager = WorkManager.getInstance(this);
        headings = new ArrayList<>();
        dates = new ArrayList<>();
        texts = new ArrayList<>();
        urls = new ArrayList<>();
        seens = new ArrayList<>();
        missedNotifications = new ArrayList<>();
        falseurls = new ArrayList<>();
        notificationList = findViewById(R.id.notificationListView);
    }

    private void equilifyNotificationCount() {
        try {
            SharedPreferences finalsize = getSharedPreferences("finalNotificationCount", Context.MODE_PRIVATE);
            finalsize.edit().putInt("finalsize", 0).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readMissedNotification() {
        try {
            FileInputStream read = openFileInput("missed");
            ObjectInputStream readarray = new ObjectInputStream(read);
            missedNotifications = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readNotification() {
        try {
            FileInputStream read = openFileInput("notificationHeading");
            ObjectInputStream readarray = new ObjectInputStream(read);
            headings = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileInputStream read = openFileInput("notificationDate");
            ObjectInputStream readarray = new ObjectInputStream(read);
            dates = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileInputStream read = openFileInput("notificationText");
            ObjectInputStream readarray = new ObjectInputStream(read);
            texts = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileInputStream read = openFileInput("notificationUrl");
            ObjectInputStream readarray = new ObjectInputStream(read);
            urls = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileInputStream read = openFileInput("notificationColor");
            ObjectInputStream readarray = new ObjectInputStream(read);
            seens = (ArrayList<String>) readarray.readObject();
            readarray.close();
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        NotificationListAdapter adaptate = new NotificationListAdapter(this, headings, dates, texts, seens, urls);
        notificationList.setAdapter(adaptate);
        adaptate.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    public void saveState() {
        try {
            FileOutputStream write = openFileOutput("notificationHeading", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            limitArray(headings);
            arrayoutput.writeObject(headings);
            arrayoutput.close();
            write.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream write = openFileOutput("notificationDate", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            limitArray(dates);
            arrayoutput.writeObject(dates);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream write = openFileOutput("notificationText", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            limitArray(texts);
            arrayoutput.writeObject(texts);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream write = openFileOutput("notificationUrl", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            limitArray(urls);
            arrayoutput.writeObject(urls);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream write = openFileOutput("notificationColor", Context.MODE_PRIVATE);
            ObjectOutputStream arrayoutput = new ObjectOutputStream(write);
            arrayoutput.writeObject(seens);
            arrayoutput.close();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFont() {
        Calligrapher font = new Calligrapher(NotificationSummery.this);
        font.setFont(NotificationSummery.this, "kalpurush.ttf", true);
    }

    private void buildAlertDialogue() {
        builder = new AlertDialog.Builder(NotificationSummery.this);
        Dialog = builder.create();
        Dialog.setCancelable(true);
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"close", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {
            }
        });
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE,"Check Notificatins again", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {

                if (!dataconnected(NotificationSummery.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Turn on Wi-Fi or Mobile Data then try again.");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE,"Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                        }
                    });

                    checkinternet.show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    try {
//                        stopWorkManager();
                        startWorkManager();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    myToaster();
                    finish();
                }
            }
        });
        Dialog.setView(m);
    }

    @SuppressLint("InflateParams")
    private void setListView() {
        MyAdapter adapter = new MyAdapter(NotificationSummery.this, missedNotifications, falseurls);
        m = getLayoutInflater().inflate(R.layout.listview, null);
        ListView missedList = m.findViewById(R.id.ListView);
        missedList.setAdapter(adapter);
        progressBar =  m.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void myToaster() {
        Toast toast = makeText(NotificationSummery.this, "Checking Notifications", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void limitArray(ArrayList<String> arrayName) {
        if (arrayName.size() > 200) {
            arrayName.remove(200);
        }
    }

    private void stopWorkManager() {
        mWorkManager.cancelAllWork();
    }

    private void startWorkManager() {
        mWorkManager.enqueue(OneTimeWorkRequest.from(MyWorkManager.class));
    }
}