package zubayer.docsites.activity;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;

import me.anwarshahriar.calligrapher.Calligrapher;
import zubayer.docsites.adapters.GazetteAdapter;
import zubayer.docsites.adapters.MyAdapter;
import zubayer.docsites.R;

import static zubayer.docsites.utility.UiFeatures.dataconnected;

public class GazetteActivity extends Activity {
    private ArrayList<String> yearArray, monthArray, resultArray, yearUrls, monthUrls, listUrls;
    private ListView yearList, resultList;
    private MyAdapter resultAdapter;
    private GazetteAdapter yearAdapter;
    private TextView text, yearHeading, resultHeading;
    private AlertDialog checkinternet;
    private AlertDialog.Builder builder;
    private YearParser pareseYear;
    private YearNextParser yearNextParser;
    private MonthParser monthParser;
    private ResultParser resultParser;
    private VolumrParser volumeParser;
    private NavigationParser navigationParser;
    private GazetteParser gazetteParser;
    private String btxt, url, parentUrl, yearUrl, yearUrlNext, monthUrl, yearName, monthName,
            paramTagForText, paramLink, filterContent, pdfFilter;
    private int textMin, resultMin;
    private ProgressDialog progressDialog;
    private Iterator<String> it, name;
    private GazetteActivity gazetteActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gazette);

        setFont();
        initializeAll();
        filterExamContent();
        if (filterContent.equals(getString(R.string.weeklyGazetteHeading))) {
            parentUrl = "http://www.dpp.gov.bd/bgpress/index.php/document/weekly_gazettes/151";
            yearHeading.setText(getString(R.string.volumeNavigation));
            resultHeading.setText(getString(R.string.volume));
            executeVolume();
        } else {
            progressDialog = ProgressDialog.show(this, "", "Loading list of gazette years...", true, true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    checkinternet = builder.create();
                    checkinternet.setCancelable(false);
                    checkinternet.setMessage("List of gazette years loading is in progress...");
                    checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Wait", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            if (yearArray.isEmpty()) {
                                progressDialog = ProgressDialog.show(GazetteActivity.this, "", "Loading list of gazette years...", true, true);
                            }
                        }
                    });
                    checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            pareseYear.cancel(true);
                            yearNextParser.cancel(true);
                            monthParser.cancel(true);
                            resultParser.cancel(true);
                            dismissProgressDialog();
                            finish();
                        }
                    });

                    try {
                        checkinternet.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            executeYear();
        }

        yearList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (filterContent.equals(getString(R.string.weeklyGazetteHeading))) {
                    resultArray.clear();
                    listUrls.clear();
                    yearList.setAdapter(yearAdapter);
                    yearAdapter.notifyDataSetChanged();
                    parentUrl = yearUrls.get(position);
                    btxt = null;
                    yearUrls.clear();
                    yearArray.clear();
                    resultList.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                    executeVolume();

                } else {
                    monthParser.cancel(true);
                    resultParser.cancel(true);
                    progressDialog.dismiss();
                    monthUrls.clear();
                    monthArray.clear();
                    resultArray.clear();
                    listUrls.clear();
                    resultList.setAdapter(resultAdapter);
                    resultAdapter.notifyDataSetChanged();
                    monthUrl = yearUrls.get(position);
                    yearName = yearArray.get(position);
                    executeMonth();
                }

            }
        });

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (filterContent.equals(getString(R.string.weeklyGazetteHeading))) {
                    parentUrl = listUrls.get(position);
                    browser("async", parentUrl);
                } else {
                    try {
                        pdfFilter = listUrls.get(position);
                        browser("value", pdfFilter);
                    } catch (Exception e) {
                        checkinternet = builder.create();
                        checkinternet.setMessage("Something went wrong, try again");
                        try {
                            checkinternet.show();
                        } catch (Exception ex) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
    }

    public void yearExecutableTag(String Url, String TagForText, String Attr, int begin) {

        try {
            Document doc = Jsoup.connect(Url).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < 12; i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select("a").attr(Attr);
                yearArray.add(btxt);
                yearUrls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void monthExecutableTag(String TagForText, String Attr, int begin) {

        try {
            Document doc = Jsoup.connect(monthUrl).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select("a").attr(Attr);
                monthArray.add(btxt);
                monthUrls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resultExecutableTag( String TagForText, String Attr, int begin) {

        try {
            Document doc = Jsoup.connect(it.next()).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select("a").attr(Attr);
                if (btxt.contains(filterContent)) {
                    resultArray.add(0,btxt);
                    listUrls.add(0,url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void volumeExecutableTag(String TagForText, String Attr, int begin) {
        try {
            Document doc = Jsoup.connect(parentUrl).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.select("a").attr(Attr);
                if (btxt.contains("Vol")) {
                    resultArray.add(btxt);
                    listUrls.add(url);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void navigationExecutableTag(String TagForText, String Attr, int begin) {

        try {
            Document doc = Jsoup.connect(parentUrl).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.attr(Attr);
                yearArray.add(btxt);
                yearUrls.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gazetteExecutableTag(String TagForText, String Attr, int begin) {
        try {
            Document doc = Jsoup.connect(parentUrl).get();
            Elements links = doc.select(TagForText);
            for (int i = begin; i < links.size(); i++) {
                Element link = links.get(i);
                btxt = link.text();
                url = link.attr(Attr);
                if (btxt.contains(getString(R.string.filterGazette))) {
                    monthUrls.add(url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class YearParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            yearExecutableTag(yearUrl, paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            if (yearArray != null && yearUrls != null) {
                yearArray.clear();
                yearUrls.clear();
            }
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            dismissProgressDialog();
            super.onPostExecute(b);
            if (!dataconnected(gazetteActivity)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        progressDialog.dismiss();
                        loadYearAgain();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        finish();
                    }
                });

                try {
                    checkinternet.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (btxt != null) {
                yearList.setAdapter(yearAdapter);
                yearAdapter.notifyDataSetChanged();
                executeYearNext();
                btxt = null;
            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Website is not responding");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        progressDialog.dismiss();
                        loadYearAgain();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        finish();
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

    class YearNextParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            yearExecutableTag(yearUrlNext, paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            if (yearArray != null && yearUrls != null) {
                yearArray.clear();
                yearUrls.clear();
            }
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            dismissProgressDialog();
            if (!dataconnected(gazetteActivity)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        loadYearAgain();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        finish();
                    }
                });

                try {
                    checkinternet.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (btxt != null) {
                resultArray.add(getString(R.string.text));
                yearList.setAdapter(yearAdapter);
                resultList.setAdapter(resultAdapter);
                yearAdapter.notifyDataSetChanged();

            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Website is not responding");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        loadYearAgain();
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

    class MonthParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            monthExecutableTag(paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            dismissProgressDialog();
            if (!dataconnected(gazetteActivity)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeMonth();
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
                it = monthUrls.iterator();
                name = monthArray.iterator();
                btxt = null;
                executeResult();
            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Website is not responding");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeMonth();
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

    class ResultParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            resultExecutableTag( paramTagForText, paramLink, resultMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            resultArray.clear();
            listUrls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            dismissProgressDialog();
            if (!dataconnected(gazetteActivity)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeResult();
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
                resultList.setAdapter(resultAdapter);
                resultAdapter.notifyDataSetChanged();
                btxt = null;
                if (it.hasNext()) {
                    checkConnectivity();
                } else {
                    if (resultArray.size() == 0) {
                        checkinternet = builder.create();
                        checkinternet.setMessage("No Gazette published on " + yearName);
                        checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
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
            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Website is not responding");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeResult();
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

    class VolumrParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            volumeExecutableTag(paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            volumeParser.cancel(true);
            monthArray.clear();
            monthUrls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            dismissProgressDialog();
            if (!dataconnected(gazetteActivity)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        monthUrls.clear();
                        monthArray.clear();
                        yearList.setAdapter(yearAdapter);
                        yearAdapter.notifyDataSetChanged();
                        btxt = null;
                        executeVolume();
                        yearUrls.clear();
                        yearArray.clear();
                        resultList.setAdapter(resultAdapter);
                        resultAdapter.notifyDataSetChanged();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        finish();
                    }
                });

                try {
                    checkinternet.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (btxt != null) {
                resultList.setAdapter(resultAdapter);
                resultAdapter.notifyDataSetChanged();
                btxt = null;
                executeNavigation();
            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Website is not responding");
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        finish();
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        monthUrls.clear();
                        monthArray.clear();
                        yearList.setAdapter(yearAdapter);
                        resultAdapter.notifyDataSetChanged();
                        btxt = null;
                        executeVolume();
                        yearUrls.clear();
                        yearArray.clear();
                        resultList.setAdapter(resultAdapter);
                        resultAdapter.notifyDataSetChanged();
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

    class NavigationParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            navigationExecutableTag(paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            navigationParser.cancel(true);
            monthArray.clear();
            monthUrls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            dismissProgressDialog();
//            saveResults();
            if (!dataconnected(gazetteActivity)) {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Check your network connection");
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeNavigation();
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
                yearList.setAdapter(yearAdapter);
                yearAdapter.notifyDataSetChanged();
                btxt = null;
            } else {
                checkinternet = builder.create();
                checkinternet.setCancelable(false);
                checkinternet.setMessage("Next pages are not loaded");
                checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                    }
                });
                checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int id) {
                        executeNavigation();
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

    class GazetteParser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            gazetteExecutableTag(paramTagForText, paramLink, textMin);
            return null;
        }

        @Override
        protected void onCancelled() {
            gazetteParser.cancel(true);
            resultArray.clear();
            listUrls.clear();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void b) {
            super.onPostExecute(b);
            dismissProgressDialog();
            if (!dataconnected(gazetteActivity)) {
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
                browser("value", monthUrls.get(0));
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

    public void executeYear() {
        yearUrl = "http://www.dpp.gov.bd/bgpress/bangla/index.php/document/extraordinary_gazettes/285";
        pareseYear = new YearParser();
        paramTagForText = "#MyResult tr";
        paramLink = "abs:href";
        textMin = 0;
        pareseYear.execute();
    }

    public void executeYearNext() {
        dismissProgressDialog();
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(GazetteActivity.this, "", "Wait a few more moment..", true, true);
        }
        yearUrlNext = yearUrl + "/publication_date/12";
        yearNextParser = new YearNextParser();
        paramTagForText = "#MyResult tr";
        paramLink = "abs:href";
        textMin = 0;
        yearNextParser.execute();
    }

    private void executeMonth() {
        monthParser = new MonthParser();
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(GazetteActivity.this, "", "This may take some time, please wait..", true, true);
        }
        paramTagForText = "#MyResult tr";
        paramLink = "abs:href";
        textMin = 0;
        monthParser.execute();
    }

    private void executeResult() {
        dismissProgressDialog();
        try {
            monthName = name.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(GazetteActivity.this, "", "Searching " + monthName, true, true);
        }
        resultParser = new ResultParser();
        paramTagForText = "#MyResult tr";
        paramLink = "href";
        resultMin = 1;
        resultParser.execute();
    }

    private void loadYearAgain() {
        progressDialog.dismiss();
        yearUrls.clear();
        yearArray.clear();
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(GazetteActivity.this, "", "Loading gazette years...", true, true);
        }
        executeYear();
    }

    public void executeVolume() {
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(this, "", "Loading volumes...", true, true);
        }
        volumeParser = new VolumrParser();
        paramTagForText = "#MyResult tr";
        paramLink = "abs:href";
        textMin = 0;
        volumeParser.execute();
    }

    public void executeNavigation() {
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(this, "", "Loading next pages...", true, true);
        }
        navigationParser = new NavigationParser();
        paramTagForText = "#pagination-bar a";
        paramLink = "abs:href";
        textMin = 0;
        navigationParser.execute();
    }

    public void executeGazette() {
        gazetteParser = new GazetteParser();
        paramTagForText = "tr a";
        paramLink = "abs:href";
        textMin = 0;
        gazetteParser.execute();
        if (!GazetteActivity.this.isFinishing()) {
            progressDialog = ProgressDialog.show(this, "", "Loading Gazettes...", true, true);
        }
    }

    private void browser(String key, String inurl) {
        startActivity(new Intent(GazetteActivity.this, Browser.class).putExtra(key, inurl));
    }

    @Override
    public void onBackPressed() {

        try {
            volumeParser.cancel(true);
            navigationParser.cancel(true);
            gazetteParser.cancel(true);
            pareseYear.cancel(true);
            yearNextParser.cancel(true);
            monthParser.cancel(true);
            resultParser.cancel(true);
            dismissProgressDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        try {
            volumeParser.cancel(true);
            navigationParser.cancel(true);
            gazetteParser.cancel(true);
            pareseYear.cancel(true);
            yearNextParser.cancel(true);
            monthParser.cancel(true);
            resultParser.cancel(true);
            dismissProgressDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resultAdapter.notifyDataSetChanged();
        yearAdapter.notifyDataSetChanged();
    }

    private void checkConnectivity() {
        if (!dataconnected(this)) {
            checkinternet = builder.create();
            checkinternet.setCancelable(false);
            checkinternet.setMessage("Data connection interrupted!");
            checkinternet.setButton(DialogInterface.BUTTON_POSITIVE, "Reload", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int id) {
                    progressDialog.dismiss();
                    resultArray.clear();
                    listUrls.clear();
                    monthArray.clear();
                    monthUrls.clear();
                    resultList.setAdapter(resultAdapter);
                    executeMonth();
                }
            });

            checkinternet.setButton(DialogInterface.BUTTON_NEGATIVE, "close", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int id) {
                    dismissProgressDialog();
                }
            });
            checkinternet.show();
            dismissProgressDialog();
        } else {
            executeResult();
        }
    }

    private void setFont() {
        Calligrapher font = new Calligrapher(this);
        font.setFont(this, "kalpurush.ttf", true);
    }

    private void initializeAll() {
        gazetteActivity=this;
        yearList = findViewById(R.id.year);
        resultList = findViewById(R.id.result);
        text = findViewById(R.id.heading);
        yearHeading = findViewById(R.id.yearHeading);
        resultHeading = findViewById(R.id.resultHeading);
        yearArray = new ArrayList<>();
        resultArray = new ArrayList<>();
        yearUrls = new ArrayList<>();
        monthArray = new ArrayList<>();
        monthUrls = new ArrayList<>();
        listUrls = new ArrayList<>();
        builder = new AlertDialog.Builder(GazetteActivity.this);
        yearAdapter = new GazetteAdapter(GazetteActivity.this, yearArray);
        resultAdapter = new MyAdapter(GazetteActivity.this, resultArray, listUrls);
        monthParser = new MonthParser();
        pareseYear = new YearParser();
        yearNextParser = new YearNextParser();
        resultParser = new ResultParser();
    }

    private void filterExamContent() {
        if (getIntent().getExtras() != null) {
            filterContent = getIntent().getExtras().getString("examname");
            String examName;
            if (filterContent.equals(getString(R.string.filterSeniorScale))) {
                examName = getString(R.string.seniorScaleOption);
                text.setText(examName);
            } else if (filterContent.equals(getString(R.string.filterDepartmental))) {
                examName = getString(R.string.departmentalOption);
                text.setText(examName);
            } else {
                examName = getString(R.string.weeklyGazetteHeading);
                text.setText(examName);
            }
        }

    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}