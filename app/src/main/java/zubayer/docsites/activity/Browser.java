package zubayer.docsites.activity;

import android.annotation.SuppressLint;
import android.app.*;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.*;
import android.content.*;
import android.util.Log;
import android.webkit.*;
import android.net.*;
import android.view.*;
import android.widget.*;
import android.app.DownloadManager;

import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.material.internal.NavigationMenu;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import zubayer.docsites.adapters.MyAdapter;
import zubayer.docsites.R;

import static android.widget.Toast.makeText;
import static zubayer.docsites.utility.UiFeatures.dataconnected;

public class Browser extends Activity {
    private String urls;
    private WebView website;
    private ProgressBar progressbar;
    private AlertDialog checkinternet;
    private String driveViewer, paramTagForText, paramLink, btxt;
    private int textMin;
    private AlertDialog Dialog;
    private AlertDialog.Builder builder;
    private View m;
    private ArrayList<String> buttonTexts, urlss;
    private Button downloadButton;
    private boolean errorConnection, downloading;
    private GazetteParser gazetteParser;
    private ArrayList<String> monthUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        buttonTexts = new ArrayList<>();
        buttonTexts.add(getString(R.string.browseralert));
        urlss = new ArrayList<>();
        downloadButton = findViewById(R.id.downloadButton);

        setProgressBar();
        initializeWebViewAndUrls();
        setFloatingActionButton();
        setListView();
        buildAlertDialogue();
        reloadAdviceDialogue();
        webViewSettings();
        filterUrlPDF();

        website.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                switch (errorCode) {
                    case -1:
                        downloadButton.setVisibility(View.VISIBLE);
                        if (downloading) {
                            website.loadUrl("file:///android_asset/error.html");
                        }

                        break;
                    case -2:
                        website.loadUrl("file:///android_asset/error2.html");
                        downloadButton.setText(getString(R.string.reload));
                        break;
                }
                errorConnection = true;
                Log.d("btxt", "" + errorCode);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("pdf")) {
                    Intent intent = new Intent(Browser.this, Browser.class);
                    intent.putExtra("value", url);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Browser.this, Browser.class);
                    intent.putExtra("value", url);
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler web_handler, final SslError error) {
                super.onReceivedSslError(view, web_handler, error);
                AlertDialog.Builder builder = new AlertDialog.Builder(Browser.this);
                AlertDialog alertDialog = builder.create();
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }

                message += " Do you want to continue anyway?";
                alertDialog.setTitle("SSL Certificate Error");
                alertDialog.setMessage(message);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ignore SSL certificate errors
                        web_handler.proceed();

                        website.loadUrl(error.getUrl());
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        web_handler.cancel();
                    }
                });
                alertDialog.show();
            }

        });

        website.setDownloadListener(new DownloadListener() {

            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                downloading = true;
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                try {
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (dm != null) {
                        dm.enqueue(request);
                    }
                } catch (Exception e) {
                    try {
                        Intent intentNew = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intentNew);
                    } catch (ActivityNotFoundException eee) {
                        builder = new AlertDialog.Builder(Browser.this);
                        checkinternet = builder.create();
                        checkinternet.setCancelable(true);
                        checkinternet.setMessage("You need to download Google Chrome");
                        checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Download", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.android.chrome"));
                                startActivity(i);
                            }
                        });
                        checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {

                            }
                        });
                        checkinternet.show();
                    }
                }
            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > 23) {
                    try {
                        Intent intentNew = new Intent(Intent.ACTION_VIEW, Uri.parse(urls));
                        startActivity(intentNew);
                    } catch (ActivityNotFoundException e) {
                        builder = new AlertDialog.Builder(Browser.this);
                        checkinternet = builder.create();
                        checkinternet.setCancelable(true);
                        checkinternet.setMessage("You need to download Google Chrome");
                        checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Download", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.android.chrome"));
                                startActivity(i);
                            }
                        });
                        checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {

                            }
                        });
                        checkinternet.show();
                    }
                } else {
                    website.loadUrl(urls);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (website.canGoBack()) {
            if (errorConnection) {
                finish();
            } else {
                website.goBack();
                progressbar.setProgress(0);
                loadProgressBar();
            }


        } else {
            finish();
            super.onBackPressed();
        }
    }

    private void setFloatingActionButton() {
        FabSpeedDial fab = findViewById(R.id.fabweb);
        fab.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.reload:
                        website.loadUrl(urls);
                        loadProgressBar();
                        break;
                    case R.id.copy:
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("url", urls);
                        if (clipboardManager != null) {
                            clipboardManager.setPrimaryClip(clipData);
                        }
                        myToaster();
                        break;
                    case R.id.externalBrowser:
                        try {
                            Intent intentNew = new Intent(Intent.ACTION_VIEW, Uri.parse(urls));
                            startActivity(intentNew);
                        } catch (ActivityNotFoundException e) {
                            builder = new AlertDialog.Builder(Browser.this);
                            checkinternet = builder.create();
                            checkinternet.setCancelable(true);
                            checkinternet.setMessage("You need to download Google Chrome");
                            checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Download", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int id) {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.android.chrome"));
                                    startActivity(i);
                                }
                            });
                            checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int id) {

                                }
                            });
                            checkinternet.show();
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed() {

            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void webViewSettings() {
        website.getSettings().setUseWideViewPort(true);
        website.getSettings().setDatabaseEnabled(true);
        website.getSettings().setDomStorageEnabled(true);
        website.getSettings().setLightTouchEnabled(true);
        website.getSettings().setLoadsImagesAutomatically(true);
        website.getSettings().setPluginState(WebSettings.PluginState.ON);
        website.getSettings().setSaveFormData(true);
        website.getSettings().setSavePassword(true);
        website.getSettings().setJavaScriptEnabled(true);
        website.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        website.getSettings().setLoadWithOverviewMode(true);
        website.getSettings().setAllowContentAccess(true);
        website.getSettings().setAllowFileAccess(true);
        website.getSettings().setAllowFileAccessFromFileURLs(true);
        website.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        website.getSettings().setAllowUniversalAccessFromFileURLs(true);
        website.getSettings().setAppCacheEnabled(true);
        website.getSettings().setBuiltInZoomControls(true);
        website.getSettings().setSupportMultipleWindows(false);
        website.setVerticalScrollBarEnabled(false);
    }

    public void loadProgressBar() {
        website.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressbar.setMax(100);
                progressbar.setVisibility(View.VISIBLE);
                progressbar.setProgress(progress);
                if (progress == 100) {
                    progressbar.setVisibility(View.GONE);

                }
                super.onProgressChanged(view, progress);
            }
        });
    }

    private void reloadAdviceDialogue() {
        SharedPreferences preferences = getSharedPreferences("reload", 0);
        if (!preferences.getBoolean("got it", false)) {
            Dialog.show();
            preferences = getSharedPreferences("reload", 0);
            preferences.edit().putBoolean("got it", true).apply();
        }
    }

    private void myToaster() {
        Toast toast = makeText(Browser.this, "Link copied", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void filterUrlPDF() {

        if (urls != null && urls.contains("pdf")) {
            downloadButton.setVisibility(View.VISIBLE);
            website.loadUrl(driveViewer + urls);
            loadProgressBar();
        } else if (urls != null && urls.contains("download")) {
            if (Build.VERSION.SDK_INT > 23) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(Color.parseColor("#305168"));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(this, Uri.parse(urls));
            } else {
                website.loadUrl(urls);
                loadProgressBar();
            }
        } else {
            downloadButton.setVisibility(View.GONE);
            website.loadUrl(urls);
            loadProgressBar();
        }
    }

    private void setProgressBar() {
        progressbar = findViewById(R.id.progress);
        progressbar.setProgress(0);
    }

    private void initializeWebViewAndUrls() {
        monthUrls = new ArrayList<>();
        website = findViewById(R.id.WebView);
        driveViewer = "https://docs.google.com/viewer?url=";
        if (getIntent().getExtras() != null)
            if (getIntent().getExtras().getString("value") != null) {
                urls = getIntent().getExtras().getString("value");
            } else {
                urls = getIntent().getExtras().getString("async");
                executeGazette();
            }

    }

    private void buildAlertDialogue() {
        builder = new AlertDialog.Builder(this);
        Dialog = builder.create();
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Got it", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {

            }
        });
        Dialog.setView(null);
        Dialog.setView(m);
    }

    @SuppressLint("InflateParams")
    private void setListView() {
        MyAdapter adapter = new MyAdapter(Browser.this, buttonTexts, urlss);
        m = getLayoutInflater().inflate(R.layout.listview, null);
        ListView list = m.findViewById(R.id.ListView);
        list.setAdapter(adapter);
        ProgressBar progressBar = m.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    class GazetteParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            gazetteExecutableTag(paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            gazetteParser.cancel(true);
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            if (!dataconnected(Browser.this)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeGazette();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {

                    }
                });
                try {
                    checkinternet.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (btxt != null) {
                website.loadUrl(monthUrls.get(0));
            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Website is not responding");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeGazette();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {

                    }
                });

                try {
                    checkinternet.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void gazetteExecutableTag(String TagForText, String Attr, int begin) {
        try {
            Document doc = Jsoup.connect(urls).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                String url = link.attr(Attr);
                if (btxt.contains(getString(R.string.filterGazette))) {
                    monthUrls.add(url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeGazette() {
        gazetteParser = new GazetteParser();
        paramTagForText = "tr a";
        paramLink = "abs:href";
        textMin = 0;
        gazetteParser.execute();
    }
}
