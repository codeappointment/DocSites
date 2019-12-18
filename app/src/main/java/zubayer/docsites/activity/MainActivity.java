package zubayer.docsites.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import me.anwarshahriar.calligrapher.Calligrapher;
import zubayer.docsites.BuildConfig;
import zubayer.docsites.adapters.GridAdapter;
import zubayer.docsites.adapters.MyAdapter;
import zubayer.docsites.R;
import zubayer.docsites.services.MyWorkManager;
import zubayer.docsites.services.NotificationReceiver;

import static zubayer.docsites.utility.UiFeatures.dataconnected;

public class MainActivity extends Activity {
    private AlertDialog Dialog, checkinternet;
    private AlertDialog.Builder builder;
    private View m;
    private ProgressBar progressBar;
    private ListView list;
    private FirebaseDatabase database;
    private GridView gridView;
    private ArrayList<String> buttonTexts, urls, buttonHeadidng, buttonDescription, buttonHint,
            bsmmuOptions, bcpsOptions, dghsOptions, mohfwOptions, bpscOptions, gazetteOptions, bmdcOptions,
            resultOptions, dgfpOptions, queryID, queryname;
    private MyAdapter adapter;
    private bsmmuParser back;
    private BSMMU parsebsmmu;
    private BSMMU2 parsebsmmu2;
    private BpscParser bpscParser;
    private DghsParser dghsParser;
    private DghsParser2 dghsParser2;
    private DghsParser3 dghsParser3;
    private ServiceParser serviceParser;
    private CcdParser ccdParser;
    private CcdParser2 ccdParser2;
    private String btxt, url, paramUrl, paramTagForText, paramTagForLink, paramLink, pdfFilter, filterContent, queryNotification, error;
    private int textMin, textMax, versionCode, bsmmubegin, bsmmuend;
    private boolean bsmmuClicked, bcpsClicked, dghsClicked, mohfwClicked, bpscClicked, gazetteClicked, dgfpClicked, bmdcClicked, resultsClicked,
            newNotifications,serviceConcirmClicked;
    private SharedPreferences preferences;
    private FabSpeedDial fab;
    private ImageButton notificationSummery;
    private FloatingActionButton forum;
    private Button showNotificationNumber;
    private WorkManager mWorkManager;
    private TextView updateNotifier, forumhelpNotify, forumNotification, forumNotificationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeWidgetVariable();
        createGridView();
        setFont(this, this);
        checkApplaunched();
        manageSettings();
        loadButtonOptions();
        checkStoragePermission();
        startWorkManager();
        setListView();
        buildAlertDialogue();
        checkAppUpdates();
        newForumPost();
        readNotificationCount();
        forumSubscription();
        setAlarm();
        getFCMdataPlayLoad();
        loadNotificationCount();

        updateNotifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=zubayer.docsites")));

            }
        });
        fab = findViewById(R.id.fabs);
        fab.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.settings:
                        Intent setting = new Intent(MainActivity.this, Settings.class);
                        startActivity(setting);
                        break;
                    case R.id.about:
                        buttonTexts.add(getString(R.string.about));
                        Dialog.setTitle("App Developer:");
                        Dialog.show();
                        progressBar.setVisibility(View.GONE);
                        break;
                    case R.id.check:

                        if (!dataconnected(MainActivity.this)) {
                            checkinternet = builder.create();
                            checkinternet.setCancelable(false);
                            checkinternet.setMessage("Turn on Wi-Fi or Mobile Data then try again.");
                            checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int id) {
                                }
                            });

                            checkinternet.show();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            try {
                                startWorkManager();
                                Toast.makeText(MainActivity.this,"Checking Notifications",Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed() {
            }
        });

        forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (queryID.size() != 0) {
                    preferences.edit().putString("query", queryID.get(0)).apply();
                }
                startActivity(new Intent(MainActivity.this, Forum.class));
//                startActivity(new Intent(MainActivity.this, Forum.class).putExtra("postID","1538061004237"));
            }
        });
        forumhelpNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putString("query", queryID.get(0)).apply();
                startActivity(new Intent(MainActivity.this, Forum.class));
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                readNotificationCount();
                gridViewOptionLoader(position);
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                listViewOptionLoader(position);
            }
        });
        notificationSummery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences oldsize = getSharedPreferences("finalNotificationCount", Context.MODE_PRIVATE);
                oldsize.edit().putInt("finalsize", 0).apply();
                Intent summery = new Intent(MainActivity.this, NotificationSummery.class);
                startActivity(summery);
            }
        });
        showNotificationNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences oldsize = getSharedPreferences("finalNotificationCount", Context.MODE_PRIVATE);
                oldsize.edit().putInt("finalsize", 0).apply();
                Intent summery = new Intent(MainActivity.this, NotificationSummery.class);
                startActivity(summery);
            }
        });

        forumNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Forum.class).putExtra("forumNotification", "Y"));
            }
        });


        forumNotificationCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Forum.class).putExtra("forumNotification", "Y"));
            }
        });
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) {
            browser(appLinkData.toString());
        }

    }

    private void getFCMdataPlayLoad() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String postID = intent.getExtras().getString("postID");
            if (postID != null) {
                startActivity(new Intent(MainActivity.this, Forum.class).putExtra("postID", postID));
            }
        }
    }

    private void startWorkManager() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mWorkManager.enqueue(OneTimeWorkRequest.from(MyWorkManager.class));
            }
        },20000);

    }

    public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @SuppressWarnings("NullableProblems") String[] permissions, @SuppressWarnings("NullableProblems") int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            checkinternet = builder.create();
            checkinternet.setMessage("You need to allow permission to download files from internet");
            checkinternet.setCancelable(false);
            checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Allow permission", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (checkStoragePermission()) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }
            });
            checkinternet.show();
        }
    }

    private void loaddgfpOptions() {
        buttonTexts.addAll(dgfpOptions);
        Dialog.show();
    }

    private void loadResultOptions() {
        buttonTexts.addAll(resultOptions);
        Dialog.show();
    }

    private void loadBMDCOptions() {
        buttonTexts.addAll(bmdcOptions);
        Dialog.show();
    }

    private void loadGazetteOptions() {
        buttonTexts.addAll(gazetteOptions);
        Dialog.show();
    }

    private void loadBpscOptions() {
        buttonTexts.addAll(bpscOptions);
        Dialog.show();
    }

    private void loadMohfwOptions() {
        buttonTexts.addAll(mohfwOptions);
        Dialog.show();
    }

    private void loadDghsOptions() {
        buttonTexts.addAll(dghsOptions);
        Dialog.show();
    }

    private void loadBcpsOptions() {
        buttonTexts.addAll(bcpsOptions);
        Dialog.show();
    }

    private void loadBsmmuOptions() {
        buttonTexts.addAll(bsmmuOptions);
        Dialog.show();
    }

    public void ccdExecutableTag(String Url, String TagForText, String tagForLink,
                                 String Attr, int begin, int end) {
        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < end; i++) {
                Element link = links.get(i);
                if (!link.text().equals("")) {
                    btxt = link.text();
                    url = link.select(tagForLink).attr(Attr);
                }

                buttonTexts.add(btxt);
                urls.add(url);
            }
            buttonTexts.add(0, getString(R.string.homePage));
            urls.add(0, "http://www.badas-dlp.org/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ccdExecutableTag2(String Url, String TagForText, int begin, int end) {
        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < end; i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = "http://www.badas-dlp.org/";
                buttonTexts.add(btxt);
                urls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bpscTag(String Url, String TagForText, String tagForLink,
                        String Attr, int begin) {

        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select(tagForLink).attr(Attr);
                buttonTexts.add(btxt);
                urls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bsmmuTag(String Url, String TagForText, String Attr) {

        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);

            for (int i = 0; i < links.size() - 1; i++) {
                Element link = links.get(i);
                url = link.attr(Attr);
                urls.add(url);
                String date_only = link.select("h5").text().substring(0, 2) + " ";
                String month_year = link.select("h5").text().substring(2) + ": ";
                String bold_date = "<font color=#16B330>" + date_only + month_year + "</font>";
                String notice = "<font color=#123456>" + link.select("h6").text() + "</font>";
                btxt = bold_date + notice;
                buttonTexts.add(btxt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dghsTag(String Url, String TagForText,
                        String Attr, int begin) {
        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select("a").attr(Attr);
                buttonTexts.add(btxt);
                urls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dghsTag2(String Url, String TagForText, String tagForLink, String Attr) {

        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);
            for (int i = links.size() - 11; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select(tagForLink).attr(Attr);
                buttonTexts.add(btxt);
                urls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serviceConfirmTag(String TagForText, String attr) {
        String Url = "http://mohfw.gov.bd/index.php?option=com_content&view=article&id=111:bcs-health&catid=38:bcs-health&Itemid=&lang=en";

        try {
            Document doc = Jsoup.connect(Url).get();
            Element table = doc.select("table").get(18);
            Elements rows = table.select(TagForText);

            for (int i = 0; i < rows.size(); i++) {
                Element link = rows.get(i);
                if(filterContent.equals(getString(R.string.assistantProfessor))){
                    if (link.text().contains(filterContent)||link.text().contains("Per-1:")) {
                        btxt = link.outerHtml();
                        url = link.select("a").attr(attr);
                        buttonTexts.add(btxt);
                        urls.add(url);
                    }
                }else {
                    if (link.text().contains(filterContent)) {
                        btxt = link.outerHtml();
                        url = link.select("a").attr(attr);
                        buttonTexts.add(btxt);
                        urls.add(url);
                    }
                }

            }

        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    class CcdParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ccdExecutableTag(paramUrl, paramTagForText, paramTagForLink, paramLink, textMin, textMax);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    checkinternet.show();
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        ccdNotices2();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            ccdNotices1();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class CcdParser2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ccdExecutableTag2(paramUrl, paramTagForText, textMin, textMax);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            ccdNotices2();
                        }
                    });

                    checkinternet.show();
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class BpscParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            bpscTag(paramUrl, paramTagForText, paramTagForLink, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();

        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            loadBpscOptions();
                            list.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class bsmmuParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            bsmmuTag(paramUrl, paramTagForText, paramLink);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            bsmmuNotice();
                        }
                    });
                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class BSMMU extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Document doc = Jsoup.connect("http://bsmmu.edu.bd/").get();
                Elements links = doc.select("a");
                bsmmubegin = 0;
                bsmmuend = 0;
                for (int i = bsmmubegin; i < links.size(); i++) {
                    Element link = links.get(i);
                    btxt = link.text();
                    url = link.select("a").attr("abs:href");
                    if (btxt.contains("Residency/Non-Residency")) {
                        bsmmubegin = i + 1;
                    }
                    if (btxt.contains("Institute")) {
                        bsmmuend = i;
                        break;
                    }
                }

                for (int i = bsmmubegin; i < bsmmuend; i++) {
                    Element link = links.get(i);
                    btxt = link.text();
                    url = link.select("a").attr("abs:href");
                    buttonTexts.add(btxt);
                    urls.add(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void b) {

            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            residency();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(b);
        }
    }

    class BSMMU2 extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Document doc = Jsoup.connect("http://bsmmu.edu.bd/").get();
                Elements links = doc.select("a");
                bsmmubegin = 0;
                bsmmuend = 0;
                for (int i = bsmmubegin; i < links.size(); i++) {
                    Element link = links.get(i);
                    btxt = link.text();
                    url = link.select("a").attr("abs:href");
                    if (btxt.contains("Admission")) {
                        bsmmubegin = i + 1;
                        bsmmuend = bsmmubegin + 4;
                        break;
                    }

                }
                for (int i = bsmmubegin; i < bsmmuend; i++) {
                    Element link = links.get(i);
                    btxt = link.text();
                    url = link.select("a").attr("abs:href");
                    buttonTexts.add(btxt);
                    urls.add(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            admission();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DghsParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            dghsTag(paramUrl, paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dghsHomeLinks2();
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            dghsHomeLinks();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DghsParser2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            dghsTag(paramUrl, paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dghsHomeLinks3();
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            dghsHomeLinks2();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DghsParser3 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            dghsTag2(paramUrl, paramTagForText, paramTagForLink, paramLink);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (url != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not Responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            dghsHomeLinks3();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ServiceParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            serviceConfirmTag(paramTagForText, paramLink);
            return null;
        }

        @Override
        protected void onCancelled() {
            buttonTexts.clear();
            urls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            try {
                if (!dataconnected(MainActivity.this)) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Check your network connection");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                } else if (btxt != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("btxt", error);
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("Website is not responding");
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            Dialog.dismiss();
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            buttonTexts.clear();
                            urls.clear();
                            url = null;
                            executeService();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        readNotificationCount();
        checkinternet = builder.create();
        checkinternet.setMessage(getText(R.string.exit));
        checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Get Help", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "zubayer.developer@gmail.com"));
//                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + ": ");
//                    i.putExtra(Intent.EXTRA_TEXT, "Write here:" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "\n" + "Sent from: " + Build.MANUFACTURER + " " + Build.MODEL + " " + "(" + Build.VERSION.RELEASE + ")");
//                    startActivity(i);
                startActivity(new Intent(MainActivity.this, Forum.class));
            }
        });
        checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    back.cancel(true);
                    parsebsmmu.cancel(true);
                    parsebsmmu2.cancel(true);
                    bpscParser.cancel(true);
                    dghsParser.cancel(true);
                    dghsParser2.cancel(true);
                    dghsParser3.cancel(true);
                    serviceParser.cancel(true);
                    ccdParser.cancel(true);
                    ccdParser2.cancel(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
        checkinternet.show();
    }

    @Override
    protected void onPause() {
        try {
            back.cancel(true);
            parsebsmmu.cancel(true);
            parsebsmmu2.cancel(true);
            bpscParser.cancel(true);
            dghsParser.cancel(true);
            dghsParser2.cancel(true);
            dghsParser3.cancel(true);
            serviceParser.cancel(true);
            ccdParser.cancel(true);
            ccdParser2.cancel(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
        readNotificationCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readNotificationCount();
        newForumPost();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        readNotificationCount();
    }

    private void bsmmuHome() {
        pdfFilter = "http://www.bsmmu.edu.bd";
        browser(pdfFilter);
    }

    private void residency() {
        parsebsmmu = new BSMMU();
        parsebsmmu.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void admission() {
        parsebsmmu2 = new BSMMU2();
        parsebsmmu2.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void bsmmuNotice() {
        back = new bsmmuParser();
        paramUrl = "http://www.bsmmu.edu.bd";
        paramTagForText = "#tab1 a";
        paramLink = "abs:href";
        back.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void bsmmuNoticeAdministrative() {
        back = new bsmmuParser();
        paramUrl = "http://www.bsmmu.edu.bd";
        paramTagForText = "#tab3 a";
        paramLink = "abs:href";
        back.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void bsmmuCourseDuration() {
        pdfFilter = "http://www.bsmmu.edu.bd/?page=menu&content=139020387254";
        browser(pdfFilter);
    }

    private void courseInstitutions() {
        pdfFilter = "http://www.bsmmu.edu.bd/?page=menu&content=139020390785";
        browser(pdfFilter);
    }

    private void bcpsHome() {
        pdfFilter = "https://bcps.edu.bd/";
        browser(pdfFilter);
    }

    private void bcpsNotice() {
        pdfFilter = "https://bcps.edu.bd/notice.php";
        browser(pdfFilter);
    }

    private void fcpsResults() {
        pdfFilter = "https://bcps.edu.bd/result/";
        browser(pdfFilter);
    }

    private void refundExamFee() {
        buttonTexts.clear();
        urls.clear();
        Dialog.setTitle("Fee refund");
        buttonTexts.add(getString(R.string.refundExamFeesText));
        urls.add("https://bcps.edu.bd/refund.htm");
        progressBar.setVisibility(View.GONE);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void examFees() {
        browser("https://bcps.edu.bd/exam_fees.htm");
    }

    private void dghsHome() {
        pdfFilter = "http://dghs.gov.bd/index.php/bd/";
        browser(pdfFilter);
    }

    private void dghsHomeLinks() {
        dghsParser = new DghsParser();
        paramUrl = "http://dghs.gov.bd/index.php/bd/";
        paramTagForText = "#system h1 a";
        paramLink = "abs:href";
        textMin = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dghsParser.execute();
            }
        },0);

        progressBar.setVisibility(View.VISIBLE);
    }

    private void dghsHomeLinks2() {
        dghsParser2 = new DghsParser2();
        paramUrl = "http://dghs.gov.bd/index.php/bd/";
        paramTagForText = "#system li a";
        paramLink = "abs:href";
        textMin = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dghsParser2.execute();
            }
        },0);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dghsHomeLinks3() {
        dghsParser3 = new DghsParser3();
        paramUrl = "http://dghs.gov.bd/index.php/bd/";
        paramTagForText = "#system a";
        paramTagForLink = "#system a";
        paramLink = "abs:href";
        textMin = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dghsParser3.execute();
            }
        },0);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void postingOrders() {
        back = new bsmmuParser();
        paramUrl = "http://dghs.gov.bd/index.php/bd/?option=com_content&view=article&layout=edit&id=570";
        paramTagForText = "#system a";
        paramTagForLink = "#system a";
        paramLink = "abs:href";
        textMin = 0;
        textMax = 4;
        back.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void HRM() {
        pdfFilter = "http://hrm.dghs.gov.bd/auth/signin";
        browser(pdfFilter);
    }

    private void mohfwHome() {
        pdfFilter = "http://www.mohfw.gov.bd/index.php?option=com_content&view=frontpage&Itemid=1&lang=en";
        browser(pdfFilter);
    }

    private void deputation() {
        pdfFilter = "http://www.mohfw.gov.bd/index.php?option=com_docman&task=doc_download&gid=3189&lang=en";
        browser(pdfFilter);
    }

    private void bpscHpme() {
        pdfFilter = "http://www.bpsc.gov.bd";
        browser(pdfFilter);
    }

    private void regiBCS() {
        pdfFilter = "http://bpsc.teletalk.com.bd";
        browser(pdfFilter);
    }

    private void regiDept() {
        back = new bsmmuParser();
        paramUrl = "http://dept.bpsc.gov.bd";
        paramTagForText = "h5";
        paramTagForLink = "h5 a";
        paramLink = "abs:href";
        textMin = 1;
        textMax = 7;
        back.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void regiSenior() {
        back = new bsmmuParser();
        paramUrl = "http://snsc.bpsc.gov.bd";
        paramTagForText = "a";
        paramTagForLink = "a";
        paramLink = "abs:href";
        textMin = 15;
        textMax = 20;
        back.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void resultBCS() {
        bpscParser = new BpscParser();
        paramUrl = "http://bpsc.gov.bd/site/view/psc_exam/BCS%20Examination/বিসিএস-পরীক্ষা";
        paramTagForText = "tr";
        paramTagForLink = "tr a";
        paramLink = "abs:href";
        textMin = 1;
        bpscParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void resultDept() {
        bpscParser = new BpscParser();
        paramUrl = "http://www.bpsc.gov.bd/site/view/psc_exam/Departmental%20Examination/বিভাগীয়-পরীক্ষা";
        paramTagForText = "tr";
        paramTagForLink = "tr td a";
        paramLink = "abs:href";
        textMin = 1;
        bpscParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void resultSenior() {
        bpscParser = new BpscParser();
        paramUrl = "http://www.bpsc.gov.bd/site/view/psc_exam/Senior%20Scale%20Examination/সিনিয়র-স্কেল-পরীক্ষা";
        paramTagForText = "tr";
        paramTagForLink = "tr td a";
        paramLink = "abs:href";
        textMin = 1;
        bpscParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void bpscForms() {
        back = new bsmmuParser();
        paramUrl = "http://bpsc.gov.bd";
        paramTagForText = "a";
        paramTagForLink = "#box-5 a";
        paramLink = "abs:href";
        textMin = 80;
        textMax = 84;
        back.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void executeService() {
        serviceParser = new ServiceParser();
        paramTagForText = "tr";
        paramLink = "abs:href";
        serviceParser.execute();
    }

    private void bmdcHpme() {
        pdfFilter = "http://bmdc.org.bd/";
        browser(pdfFilter);
    }

    private void bmdcDownloadableForms() {
        pdfFilter = "https://web.bmdc.org.bd/forms-all";
        browser(pdfFilter);
    }

    private void registrationForm() {
        pdfFilter = "https://web.bmdc.org.bd/docs/Registration-for-Medical-Dental-Practitioners17.pdf";
        browser(pdfFilter);
    }

    private void findDoctor() {
        pdfFilter = "https://web.bmdc.org.bd/search-doctor";
        browser(pdfFilter);
    }

    private void bdsResult() {
        pdfFilter = "http://result.dghs.gov.bd/bds/";
        browser(pdfFilter);
    }

    private void mbbsResult() {
        pdfFilter = "http://result.dghs.gov.bd/mbbs/";
        browser(pdfFilter);
    }

    private void dgfpHome() {
        pdfFilter = "http://dgfp.gov.bd/";
        browser(pdfFilter);
    }

    private void ccdNotices1() {
        ccdParser = new CcdParser();
        paramUrl = "http://www.badas-dlp.org/";
        paramTagForText = "tr td a";
        paramTagForLink = "tr td a";
        paramLink = "abs:href";
        textMin = 9;
        textMax = 14;
        ccdParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void ccdNotices2() {
        ccdParser2 = new CcdParser2();
        paramUrl = "http://www.badas-dlp.org/";
        paramTagForText = "tr td p";
        paramTagForLink = "tr td p";
        paramLink = "abs:href";
        textMin = 2;
        textMax = 3;
        ccdParser2.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dgfpOrder() {
        bpscParser = new BpscParser();
        paramUrl = "http://dgfp.gov.bd/site/view/office_order/অফিস-আদেশ";
        paramTagForText = "tr";
        paramTagForLink = "tr td a";
        paramLink = "abs:href";
        textMin = 1;
        bpscParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dgfpNotice() {
        bpscParser = new BpscParser();
        paramUrl = "http://dgfp.gov.bd/site/view/notices/নোটিশ";
        paramTagForText = "tr";
        paramTagForLink = "tr td a";
        paramLink = "abs:href";
        textMin = 1;
        bpscParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dgfpNOC() {
        bpscParser = new BpscParser();
        paramUrl = "http://dgfp.gov.bd/site/view/publications/এনওসি /এনওসি";
        paramTagForText = "tr";
        paramTagForLink = "tr td a";
        paramLink = "abs:href";
        textMin = 1;
        bpscParser.execute();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void seniorGazette() {
        startActivity(new Intent(MainActivity.this, GazetteActivity.class).putExtra("examname", getString(R.string.filterSeniorScale)));
    }

    private void departmentalGazette() {
        startActivity(new Intent(MainActivity.this, GazetteActivity.class).putExtra("examname", getString(R.string.filterDepartmental)));
    }

    private void weeklyGazette() {
        startActivity(new Intent(MainActivity.this, GazetteActivity.class).putExtra("examname", getString(R.string.weeklyGazetteHeading)));
    }

    private void serviceConfirmGazette() {
        executeService();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void browser(String inurl) {
        startActivity(new Intent(MainActivity.this, Browser.class).putExtra("value", inurl));
    }


    public void selectDeselect(String putBooleanName) {
        preferences.edit().putBoolean(putBooleanName, true).apply();
    }

    public void selectAll() {
        selectDeselect("residencyChecked");
        selectDeselect("noticeChecked");
        selectDeselect("dghsChecked");
        selectDeselect("reultBcsChecked");
        selectDeselect("resultDeptChecked");
        selectDeselect("resultSeniorChecked");
        selectDeselect("regiDeptChecked");
        selectDeselect("regiSeniorChecked");
        selectDeselect("assistantSurgeonChecked");
        selectDeselect("juniorConsultantChecked");
        selectDeselect("seniorConsultantChecked");
        selectDeselect("assistantProfessorChecked");
        selectDeselect("associateProfessorChecked");
        selectDeselect("professorChecked");
        selectDeselect("civilSurgeonChecked");
        selectDeselect("adhocChecked");
        selectDeselect("mohfwChecked");
        selectDeselect("deputationChecked");
        selectDeselect("dgfpChecked");
        selectDeselect("ccdChecked");
        selectDeselect("leaveChecked");
        selectDeselect("appLaunchedchecked");
    }

    private void loadButtonHeading() {
        buttonHeadidng = new ArrayList<>();
        buttonDescription = new ArrayList<>();
        buttonHint = new ArrayList<>();
        String[] headingName = getResources().getStringArray(R.array.heading);
        String[] headingDescription = getResources().getStringArray(R.array.description);
        String[] hints = getResources().getStringArray(R.array.hints);
        Collections.addAll(buttonHeadidng, headingName);
        Collections.addAll(buttonDescription, headingDescription);
        Collections.addAll(buttonHint, hints);
    }

    private void loadButtonOptions() {
        String[] bsmmuOption = getResources().getStringArray(R.array.bsmmuOptions);
        String[] bcpsOption = getResources().getStringArray(R.array.bcpsOptions);
        String[] dghsOption = getResources().getStringArray(R.array.dghsOptions);
        String[] mohfwOption = getResources().getStringArray(R.array.mohfwOptions);
        String[] bpscOption = getResources().getStringArray(R.array.bpscOptions);
        String[] gazetteOption = getResources().getStringArray(R.array.gazetteOptions);
        String[] bmdcOption = getResources().getStringArray(R.array.bmdcOptions);
        String[] resultsOption = getResources().getStringArray(R.array.resultsOption);
        String[] dgfpOption = getResources().getStringArray(R.array.dgfpOptions);
//        String[] ccdOption = getResources().getStringArray(R.array.ccdOptions);
//        driveViewer = "https://docs.google.com/viewer?url=";

        buttonTexts = new ArrayList<>();
        urls = new ArrayList<>();

        bsmmuOptions = new ArrayList<>();
        bcpsOptions = new ArrayList<>();
        dghsOptions = new ArrayList<>();
        mohfwOptions = new ArrayList<>();
        bpscOptions = new ArrayList<>();
        gazetteOptions = new ArrayList<>();
        bmdcOptions = new ArrayList<>();
        resultOptions = new ArrayList<>();
        dgfpOptions = new ArrayList<>();
        Collections.addAll(bsmmuOptions, bsmmuOption);
        Collections.addAll(bcpsOptions, bcpsOption);
        Collections.addAll(dghsOptions, dghsOption);
        Collections.addAll(mohfwOptions, mohfwOption);
        Collections.addAll(bpscOptions, bpscOption);
        Collections.addAll(gazetteOptions, gazetteOption);
        Collections.addAll(bmdcOptions, bmdcOption);
        Collections.addAll(resultOptions, resultsOption);
        Collections.addAll(dgfpOptions, dgfpOption);
//        Collections.addAll(ccdOptions, ccdOption);
    }

    private void manageSettings() {
        builder = new AlertDialog.Builder(MainActivity.this);
        checkinternet = builder.create();
        checkinternet.setCancelable(false);
        checkinternet.setMessage(getString(R.string.settingDialog));
        checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Go to setting", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("wentToSetting", true).apply();
                Intent settingIntent = new Intent(MainActivity.this, Settings.class);
                startActivity(settingIntent);
            }
        });
        boolean checkpop = preferences.getBoolean("wentToSetting", false);
        if (!checkpop) {
            checkinternet.show();
        }
    }

    private void checkApplaunched() {
        versionCode = BuildConfig.VERSION_CODE;
        preferences = getSharedPreferences("notification", Context.MODE_PRIVATE);
        boolean applaunched = preferences.getBoolean("appLaunchedchecked", false);
        if (!applaunched) {
            selectAll();
        }
    }

    private void forumSubscription() {
        boolean unsubscribed;
        SharedPreferences notificationPreference = getSharedPreferences("forum_notification", Context.MODE_PRIVATE);
        unsubscribed = notificationPreference.getBoolean("unsubscribed_post_noti", false);
        if (!unsubscribed) {
            subscribeTopic();
        }
    }

    public void setFont(Context context, Activity activity) {
        Calligrapher font = new Calligrapher(context);
        font.setFont(activity, "kalpurush.ttf", true);
    }

    private void buildAlertDialogue() {
        Dialog = builder.create();
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {
            }
        });
        Dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                bsmmuClicked = bcpsClicked = dghsClicked = mohfwClicked =
                        bpscClicked = gazetteClicked = bmdcClicked = resultsClicked = dgfpClicked = false;
                buttonTexts.clear();
                urls.clear();
                progressBar.setVisibility(View.GONE);
                try {
                    back.cancel(true);
                    serviceParser.cancel(true);
                    parsebsmmu.cancel(true);
                    parsebsmmu2.cancel(true);
                    ccdParser.cancel(true);
                    ccdParser2.cancel(true);
                    dghsParser.cancel(true);
                    dghsParser2.cancel(true);
                    dghsParser3.cancel(true);
                    bpscParser.cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Dialog.setView(null);
        Dialog.setView(m);
    }

    @SuppressLint("InflateParams")
    private void setListView() {
        bsmmuClicked = bcpsClicked = dghsClicked = mohfwClicked =
                bpscClicked = gazetteClicked = bmdcClicked = resultsClicked = dgfpClicked = false;
        adapter = new MyAdapter(MainActivity.this, buttonTexts, urls);
        m = getLayoutInflater().inflate(R.layout.listview, null);
        list = m.findViewById(R.id.ListView);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressBar = m.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void createGridView() {
        gridView = findViewById(R.id.grid);
        loadButtonHeading();
        GridAdapter gridAdapter = new GridAdapter(MainActivity.this, buttonHeadidng, buttonDescription, buttonHint);
        gridView.setAdapter(gridAdapter);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                readNotificationCount();
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    fab.show();
                    forum.show();
                    newForumPost();
                    if (newNotifications) {
                        forumhelpNotify.setVisibility(View.VISIBLE);
                    }

                } else {
                    fab.hide();
                    forum.hide();
                    forumhelpNotify.setVisibility(View.GONE);
                }

            }
        });
    }

    private void checkAppUpdates() {
        DatabaseReference updateReference = database.getReference();
        updateReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int versionCodeInt = 0;
                int versionCodeDocNotify = 0;
                String notify = dataSnapshot.child("docNotifyMessage").getValue(String.class);
                String notifyMessage = dataSnapshot.child("docUpdateMessage").getValue(String.class);

                Integer appVersionCode = dataSnapshot.child("docUpdateVersion").getValue(Integer.class);
                Integer appDocNotify = dataSnapshot.child("docNotifyVersion").getValue(Integer.class);
                if (appVersionCode != null) {
                    versionCodeInt = appVersionCode;
                }
                if (appDocNotify != null) {
                    versionCodeDocNotify = appDocNotify;
                }
                if (versionCodeInt > versionCode) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage(notifyMessage);
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=zubayer.docsites")));
                        }
                    });
                    checkinternet.show();
                }
                if (versionCodeDocNotify > versionCode) {
                    updateNotifier.setVisibility(View.VISIBLE);
                    updateNotifier.setText(notify);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void listViewOptionLoader(int position) {

        if (urls.isEmpty()) {
            if (bsmmuClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        bsmmuHome();
                        break;
                    case 1:
                        buttonTexts.clear();
                        residency();
                        Dialog.show();
                        break;
                    case 2:
                        buttonTexts.clear();
                        admission();
                        Dialog.show();
                        break;
                    case 3:
                        buttonTexts.clear();
                        bsmmuNotice();
                        Dialog.show();
                        break;
                    case 4:
                        buttonTexts.clear();
                        bsmmuNoticeAdministrative();
                        Dialog.show();
                        break;
                    case 5:
                        bsmmuCourseDuration();
                        break;
                    case 6:
                        courseInstitutions();
                        break;
                }
            }
            if (bcpsClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        bcpsHome();
                        break;
                    case 1:
                        bcpsNotice();
                        break;
                    case 2:
                        examFees();
                        break;
                    case 3:
                        fcpsResults();
                        break;
                    case 4:
                        refundExamFee();
                        break;
                }
            }
            if (dghsClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        dghsHome();
                        break;
                    case 1:
                        buttonTexts.clear();
                        dghsHomeLinks();
                        Dialog.show();
                        break;
                    case 2:
                        buttonTexts.clear();
                        postingOrders();
                        Dialog.show();
                        break;
                    case 3:
                        HRM();
                        break;
                }
            }
            if (mohfwClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        mohfwHome();
                        break;
                    case 1:
                        deputation();
                        break;
                    case 2:
                        browser("http://www.mohfw.gov.bd/index.php?option=com_content&view=article&id=61%3Amedical-education&catid=46%3Amedical-education&Itemid=&lang=en");
                        break;
                    case 3:
                        buttonTexts.clear();
                        filterContent = getString(R.string.assistantSurgeon);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 4:
                        buttonTexts.clear();
                        filterContent = getString(R.string.juniorConsultant);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 5:
                        buttonTexts.clear();
                        filterContent = getString(R.string.seniorConsultant);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 6:
                        buttonTexts.clear();
                        filterContent = getString(R.string.assistantProfessor);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 7:
                        buttonTexts.clear();
                        filterContent = getString(R.string.associateProfessor);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 8:
                        buttonTexts.clear();
                        filterContent = getString(R.string.professor);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 9:
                        buttonTexts.clear();
                        filterContent = getString(R.string.civilSurgeon);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 10:
                        buttonTexts.clear();
                        filterContent = getString(R.string.adhoc);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 11:
                        browser("http://mohfw.gov.bd/index.php?option=com_content&view=article&id=121%3Aearn-leave&catid=101%3Aearn-leave-ex-bangladesh-leave&Itemid=&lang=en");
                        break;
                }
            }
            if (bpscClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        bpscHpme();
                        break;
                    case 1:
                        regiBCS();
                        break;
                    case 2:
                        buttonTexts.clear();
                        resultBCS();
                        Dialog.show();
                        break;
                    case 3:
                        buttonTexts.clear();
                        bpscForms();
                        Dialog.show();
                        break;
                    case 4:
                        buttonTexts.clear();
                        filterContent = getString(R.string.assistantSurgeon);
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 5:
                        buttonTexts.clear();
                        regiDept();
                        Dialog.show();
                        break;
                    case 6:
                        buttonTexts.clear();
                        resultDept();
                        Dialog.show();
                        break;
                    case 7:
                        buttonTexts.clear();
                        regiSenior();
                        Dialog.show();
                        break;
                    case 8:
                        buttonTexts.clear();
                        resultSenior();
                        Dialog.show();
                        break;
                }
            }
            if (gazetteClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        seniorGazette();
                        break;
                    case 1:
                        departmentalGazette();
                        break;
                    case 2:
                        buttonTexts.clear();
                        filterContent = getString(R.string.service);
                        serviceConcirmClicked=true;
                        serviceConfirmGazette();
                        Dialog.show();
                        break;
                    case 3:
                        weeklyGazette();
                        break;
                }
            }
            if (bmdcClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        bmdcHpme();
                        break;
                    case 1:
                        findDoctor();
                        break;
                    case 2:
                        bmdcDownloadableForms();
                        break;
                    case 3:
                        registrationForm();
                        break;
                }
            }
            if (resultsClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        mbbsResult();
                        break;
                    case 1:
                        bdsResult();
                        break;
                }
            }
            if (dgfpClicked) {
                Dialog.setTitle(buttonTexts.get(position));
                switch (position) {
                    case 0:
                        dgfpHome();
                        break;
                    case 1:
                        buttonTexts.clear();
                        dgfpOrder();
                        break;
                    case 2:
                        buttonTexts.clear();
                        dgfpNotice();
                        break;
                    case 3:
                        buttonTexts.clear();
                        dgfpNOC();
                        break;
                }
            }

        } else {
            if (mohfwClicked||serviceConcirmClicked) {
                mohfwClicked=false;
                serviceConcirmClicked=false;
                String html = buttonTexts.get(position);
                buttonTexts.clear();
                urls.clear();
                adapter.notifyDataSetChanged();
                try {
                    Document doc = Jsoup.parse(html);
                    Elements links = doc.select("a");
                    for (int i = 0; i < links.size(); i++) {
                        Element link = links.get(i);
                        btxt = link.text();
                        url = "http://www.mohfw.gov.bd" + link.attr("href");
                        buttonTexts.add(btxt);
                        urls.add(url);
                        list.setAdapter(adapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Dialog.setTitle(buttonTexts.get(position));
                try {
                    for (int i = 0; i < urls.size(); i++) {
                        if (position == i) {
                            pdfFilter = urls.get(position);
                            browser(pdfFilter);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void gridViewOptionLoader(int position) {
        Dialog.setTitle(buttonHeadidng.get(position));
        switch (position) {
            case 0:
                loadBsmmuOptions();
                bsmmuClicked = true;
                break;
            case 1:
                loadBcpsOptions();
                bcpsClicked = true;
                break;
            case 2:
                loadDghsOptions();
                dghsClicked = true;
                break;
            case 3:
                loadMohfwOptions();
                mohfwClicked = true;
                break;
            case 4:
                loadBpscOptions();
                bpscClicked = true;
                break;
            case 5:
                loadGazetteOptions();
                gazetteClicked = true;
                break;
            case 6:
                loaddgfpOptions();
                dgfpClicked = true;
                break;
            case 7:
                ccdNotices1();

                break;
            case 8:
                loadBMDCOptions();
                bmdcClicked = true;
                break;
            case 9:
                loadResultOptions();
                resultsClicked = true;
                break;

        }
    }

    public void readNotificationCount() {
        try {
            SharedPreferences oldsize = getSharedPreferences("finalNotificationCount", Context.MODE_PRIVATE);
            int oldNotificatinSize = oldsize.getInt("finalsize", 0);

            String notificationNumberText = Integer.toString(oldNotificatinSize);
            if (oldNotificatinSize == 0) {
                showNotificationNumber.setVisibility(View.GONE);
                newNotifications = false;
            } else {
                newNotifications = true;
                showNotificationNumber.setVisibility(View.VISIBLE);
                showNotificationNumber.setText(notificationNumberText);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeWidgetVariable() {
        mWorkManager = WorkManager.getInstance(this);
        notificationSummery = findViewById(R.id.notificationSumery);
        forumNotification = findViewById(R.id.facebookupdateNotifier);
        forumNotificationCount = findViewById(R.id.facebooknotificationCount);
        forum = findViewById(R.id.forum);
        showNotificationNumber = findViewById(R.id.notificationCount);
        forumhelpNotify = findViewById(R.id.forumhelpNotify);
        updateNotifier = findViewById(R.id.updateNotifier);
        TextView appVersion = findViewById(R.id.version);
        String app_version = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;
        appVersion.setText(app_version);
        updateNotifier.setVisibility(View.GONE);
        forumhelpNotify.setVisibility(View.GONE);
        showNotificationNumber.setVisibility(View.GONE);
        database = FirebaseDatabase.getInstance();
    }

    private void subscribeTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("forum").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    private void newForumPost() {
        queryID = new ArrayList<>();
        queryname = new ArrayList<>();
        DatabaseReference rootReference = database.getReference().child("user");
        rootReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    queryID.add(0, snapshot.getKey());
                    queryname.add(0, snapshot.child("first_name").getValue(String.class));
                }

                queryNotification = preferences.getString("query", null);
                if (queryID.size() != 0 && !queryID.get(0).equals(queryNotification)) {
                    forumhelpNotify.setVisibility(View.VISIBLE);
                    String newpost = queryname.get(0) + " asked...";
                    forumhelpNotify.setText(newpost);
                } else {
                    forumhelpNotify.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadNotificationCount() {

        SharedPreferences facebook_id_preference = getSharedPreferences("myID", Context.MODE_PRIVATE);
        String facebook_id = facebook_id_preference.getString("myID", null);

        if (facebook_id != null) {
            final ArrayList<String> unseencount = new ArrayList<>();
            final DatabaseReference notificationReference = database.getReference().child("notifications");
            notificationReference.child(facebook_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    unseencount.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String seenUnseen = snapshot.child("seenUnseen").getValue(String.class);
                        if (seenUnseen != null && seenUnseen.equals("unseen")) {
                            unseencount.add(".");
                        }
                    }
                    if (!unseencount.isEmpty()) {
                        String notificationText = Integer.toString(unseencount.size());
                        forumNotificationCount.setText(notificationText);
                        forumNotificationCount.setVisibility(View.VISIBLE);

                    } else {
                        forumNotificationCount.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void setAlarm() {
        Intent newIntent = new Intent(MainActivity.this, NotificationReceiver.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            assert manager != null;
            manager.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 1);

        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingIntent);

    }
}