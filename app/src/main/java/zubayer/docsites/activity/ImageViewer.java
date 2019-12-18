package zubayer.docsites.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Random;

import zubayer.docsites.R;

public class ImageViewer extends Activity {

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        ImageView showImage = findViewById(R.id.showImage);
        TextView download = findViewById(R.id.downloadButton);
        if (getIntent().getExtras() != null)
            url = getIntent().getExtras().getString("showImage");
        Glide.with(this).load(url).into(showImage);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(url);
            }
        });


    }

    private void download(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        try {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageName());
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
            }
        } catch (Exception e) {
            try {
                Intent intentNew = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentNew);
            } catch (ActivityNotFoundException eee) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewer.this);
                AlertDialog checkinternet = builder.create();
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

    private String imageName() {
        Random random = new Random();
        random.nextInt();
        return "Image" + (random.nextInt());
    }
}
