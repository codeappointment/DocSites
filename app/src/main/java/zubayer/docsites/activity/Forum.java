package zubayer.docsites.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenu;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import me.anwarshahriar.calligrapher.Calligrapher;
import zubayer.docsites.BuildConfig;
import zubayer.docsites.adapters.BlockListAdapter;
import zubayer.docsites.adapters.ForumAdapter;
import zubayer.docsites.adapters.ForumNotificationAdapter;
import zubayer.docsites.adapters.LollipopAdapter;
import zubayer.docsites.R;

import static android.widget.Toast.makeText;

public class Forum extends Activity {
    private AlertDialog alert;
    private Button notificationCount;
    private CallbackManager callbackManager;
    private CardView chooser_cardview;
    private CircularImageView post_image;
    private ImageView send, pic_preview, icon;
    private ForumAdapter adapter;
    private ForumNotificationAdapter forumNotificationAdapter;
    private LollipopAdapter lollipopAdapter;
    private FabSpeedDial forum_subscription;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private ArrayList<String> namess, textss, times, user_id, post_id, comment_count,
            total_comments_each_post, preview_replierID, preview_replierText, preview_replierName, postImage_url, reply_imageUrl;
    private EditText edit_text;
    private SharedPreferences notificationPreference;
    private FirebaseDatabase database;
    private DatabaseReference rootReference, reply_preview_reference, blockReference, imageReference;
    private HashMap<String, Object> post;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private String facebook_id, facebook_user_name, postID, blocked, reported, openNotification,
            replierID, replierName, facebook_post_time, replyImage, post_image_name, first_name,
            post_text;

    private StorageReference storageRef;
    private TextView imageChooser, del_chooser, notifications, my_profile, writeNotify, writeforeceUpdate;
    private Uri imageUri;
    private long postSize, reportSize;
    private boolean clicked = false, isScrolling;
    private int currentVisibleItems, scrolledOutItems, totalItemCount, postLimit = 10;
    private Parcelable recyclerViewState;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            chooser_cardview.setVisibility(View.VISIBLE);
            pic_preview.setImageURI(imageUri);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_forum);

        initialize();
        getIntentExtras();
        getreportSize();
        setFont(this, this);
        getFCMdataPlayLoad();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentVisibleItems = manager.getChildCount();
                totalItemCount = manager.getItemCount();
                scrolledOutItems = manager.findFirstVisibleItemPosition();
                if (currentVisibleItems + scrolledOutItems == totalItemCount) {
                    if (isScrolling) {
                        isScrolling = false;
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (openNotification == null)
                                    loadForumPost(postLimit);
                            }
                        }, 1000);
                        postLimit = totalItemCount + 10;
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        writeNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_text.getText().toString().length() != 0) {
                    alert.setMessage("Notify user about udate?");
                    alert.setButton(DialogInterface.BUTTON_POSITIVE, "Notify user", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference wrieNotify = database.getReference();
                            wrieNotify.child("docNotifyMessage").setValue(edit_text.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    edit_text.setText(null);
                                }
                            });
                            wrieNotify.child("docNotifyVersion").setValue(BuildConfig.VERSION_CODE);
                        }
                    });
                    alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alert.show();
                }
            }
        });
        writeforeceUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_text.getText().toString().length() != 0) {
                    alert.setMessage("Force user to update?");
                    alert.setButton(DialogInterface.BUTTON_POSITIVE, "Force update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference wrieNotify = database.getReference();
                            wrieNotify.child("docUpdateMessage").setValue(edit_text.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    edit_text.setText(null);
                                }
                            });
                            wrieNotify.child("docUpdateVersion").setValue(BuildConfig.VERSION_CODE);
                        }
                    });
                    alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alert.show();
                }
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebook_id != null && facebook_id.equals("1335608633238560")) {
                    showBlocklist();
                    clicked = true;
                }
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification();
                clicked = true;
            }
        });
        notificationCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification();
                clicked = true;
            }
        });
        my_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMyPosts();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post_image_name = currentPostTime();
                if (imageUri != null) {
                    upload(post_image_name);
                    HashMap<String, Object> imageref = new HashMap<>();
                    imageref.put(post_image_name, "");
                    imageReference.updateChildren(imageref);
                } else {
                    post.put("imageUrl", "blank");
                    if (edit_text.getText().toString().length() != 0) {
                        post_text = edit_text.getText().toString();
                        post.put("text", post_text);
                        postQuery(post_image_name, post_text);
                    } else {
                        alert.setMessage("Empty text");
                        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alert.show();
                    }

                }

                chooser_cardview.setVisibility(View.GONE);

            }
        });
        imageChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        del_chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUri = null;
                chooser_cardview.setVisibility(View.GONE);
            }
        });
        forum_subscription.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.forum_on:
                        FirebaseMessaging.getInstance().subscribeToTopic("forum").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                myToaster("Notification turned on");
                                notificationPreference.edit().putBoolean("unsubscribed_post_noti", false).apply();
                            }
                        });
                        break;
                    case R.id.forum_off:
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("forum").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                myToaster("Notification turned off");
                                notificationPreference.edit().putBoolean("unsubscribed_post_noti", true).apply();
                            }
                        });
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed() {

            }
        });
    }

    private void getFacebookUser() {
        SharedPreferences myIDpreference = getSharedPreferences("myID", Context.MODE_PRIVATE);
        facebook_id = myIDpreference.getString("myID", null);
        facebook_user_name = myIDpreference.getString("myName", null);
        first_name = myIDpreference.getString("first_name", null);
        String imageUrl = "https://graph.facebook.com/" + facebook_id + "/picture?width=800";
        Glide.with(this.getApplicationContext()).load(imageUrl).into(post_image);

        if (first_name == null) startActivity(new Intent(this, FacebookLogIn.class));
        else
            loadForumPost(postLimit);
        if (facebook_id != null) loadAdminButton();
    }

    private void getIntentExtras() {

        if (getIntent().getExtras() != null) {
            openNotification = getIntent().getExtras().getString("forumNotification");
        }
    }

    private void loadMyPosts() {
        clicked = true;
        final ArrayList<String> name = new ArrayList<>();
        final ArrayList<String> text = new ArrayList<>();
        final ArrayList<String> time = new ArrayList<>();
        final ArrayList<String> userid = new ArrayList<>();
        final ArrayList<String> postid = new ArrayList<>();
        final ArrayList<String> previewreplierText = new ArrayList<>();
        final ArrayList<String> previewreplierName = new ArrayList<>();
        final ArrayList<String> previewreplierID = new ArrayList<>();
        final ArrayList<String> commentcount = new ArrayList<>();
        final ArrayList<String> postImageurl = new ArrayList<>();
        final ArrayList<String> replyimageUrl = new ArrayList<>();

        ForumAdapter profileAdapter = new ForumAdapter(Forum.this, name, text, time, userid,
                postid, previewreplierText, previewreplierName, previewreplierID
                , commentcount, postImageurl, replyimageUrl);
        LollipopAdapter lollipopProfileAdapter = new LollipopAdapter(Forum.this, name, userid, text, time
                , postImageurl, postid, commentcount);
        name.clear();
        text.clear();
        time.clear();
        userid.clear();
        postid.clear();
        previewreplierText.clear();
        previewreplierName.clear();
        previewreplierID.clear();
        commentcount.clear();
        postImageurl.clear();
        replyimageUrl.clear();
        for (int i = 0; i < user_id.size(); i++) {
            if (user_id.get(i).equals(facebook_id)) {
                name.add(namess.get(i));
                text.add(textss.get(i));
                time.add(times.get(i));
                userid.add(user_id.get(i));
                postid.add(post_id.get(i));
                commentcount.add(comment_count.get(i));
                postImageurl.add(postImage_url.get(i));
                if (Build.VERSION.SDK_INT >= 23) {
                    previewreplierText.add(preview_replierText.get(i));
                    previewreplierName.add(preview_replierName.get(i));
                    previewreplierID.add(preview_replierID.get(i));
                    replyimageUrl.add(reply_imageUrl.get(i));
                    recyclerView.setAdapter(profileAdapter);
                    profileAdapter.notifyDataSetChanged();
                } else {
                    recyclerView.setAdapter(lollipopProfileAdapter);
                    lollipopProfileAdapter.notifyDataSetChanged();
                }
            }
        }
        if (name.size() == 0) {
            myToaster("You did not post anything yet");
        }
    }


    @Override
    public void onBackPressed() {

        if (clicked) {
            clicked = false;
            if (Build.VERSION.SDK_INT >= 23) {
                recyclerView.setAdapter(adapter);
            } else {
                recyclerView.setAdapter(lollipopAdapter);
                lollipopAdapter.notifyDataSetChanged();
            }
            post_image.setVisibility(View.VISIBLE);
            edit_text.setVisibility(View.VISIBLE);
            send.setVisibility(View.VISIBLE);
            imageChooser.setVisibility(View.VISIBLE);
            forum_subscription.setVisibility(View.VISIBLE);
        } else {
            if (edit_text.getText().toString().length() != 0 || imageUri != null) {
                alert.setMessage("Discard post?");
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Keep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.show();
            } else {

                super.onBackPressed();

            }

        }

    }

    private void showNotification() {
        progressBar.setVisibility(View.INVISIBLE);
        final ArrayList<String> notifytime = new ArrayList<>();
        final ArrayList<String> notificationID = new ArrayList<>();
        final ArrayList<String> userSource = new ArrayList<>();
        final ArrayList<String> notifyText = new ArrayList<>();
        final ArrayList<String> mainPostID = new ArrayList<>();
        final ArrayList<String> seenUnseen = new ArrayList<>();
        forumNotificationAdapter = new ForumNotificationAdapter(Forum.this, notificationID, userSource, notifyText, notifytime, mainPostID, seenUnseen);
        final DatabaseReference notificationReference = database.getReference().child("notifications");
        if (facebook_id != null) {
            notificationReference.child(facebook_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    notifytime.clear();
                    notifyText.clear();
                    userSource.clear();
                    mainPostID.clear();
                    seenUnseen.clear();
                    notificationID.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        notificationID.add(0, snapshot.getKey());
                        notifytime.add(0, elapsedTime(snapshot.getKey(), snapshot.child("time").getValue(String.class)));
                        notifyText.add(0, snapshot.child("notificationText").getValue(String.class));
                        userSource.add(0, snapshot.child("myid").getValue(String.class));
                        mainPostID.add(0, snapshot.child("mainPostID").getValue(String.class));
                        seenUnseen.add(0, snapshot.child("seenUnseen").getValue(String.class));
                    }

                    post_image.setVisibility(View.GONE);
                    edit_text.setVisibility(View.GONE);
                    send.setVisibility(View.GONE);
                    imageChooser.setVisibility(View.GONE);
                    forum_subscription.setVisibility(View.GONE);
                    for (int i = 0; i < notificationID.size(); i++) {
                        if (notificationExpiry(notificationID.get(i)) > 1000 * 60 * 60 * 24 * 7) {
                            notificationReference.child(facebook_id)
                                    .child(notificationID.get(i))
                                    .setValue(null);
                        }

                    }
                    recyclerView.setAdapter(forumNotificationAdapter);
                    forumNotificationAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void showBlocklist() {
        final ArrayList<String> id = new ArrayList<>();
        final ArrayList<String> name = new ArrayList<>();
        final BlockListAdapter blockListAdapter = new BlockListAdapter(Forum.this, id, name);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference blockReference = database.getReference().child("block");
        blockReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                id.clear();
                name.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    id.add(snapshot.getKey());
                    name.add(snapshot.getValue(String.class));
                }
                recyclerView.setAdapter(blockListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getreportSize() {
        DatabaseReference reportReference = database.getReference().child("reportSize");
        reportReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Long reportSizeLong = dataSnapshot.getValue(Long.class);
                if (reportSizeLong != null)
                    reportSize = reportSizeLong;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void upload(final String post_image_name) {
        progressDialog = ProgressDialog.show(Forum.this, "", "uploading...", true, true);

        storageRef.child(post_image_name).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageRef.child(post_image_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        post.put("imageUrl", downloadUrl.toString());
                        if (edit_text.getText().toString().length() != 0) {
                            post_text = edit_text.getText().toString();
                            post.put("text", post_text);
                        } else {
                            post_text = " ";
                            post.put("text", post_text);
                        }
                        postQuery(post_image_name, post_text);
                        progressDialog.dismiss();
                        imageUri = null;
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                alert.setMessage("Upload failed");
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                long progress = taskSnapshot.getBytesTransferred();
                progressDialog.dismiss();
                progressDialog = ProgressDialog.show(Forum.this, "", "uploading..." + (int) progress * 100 / taskSnapshot.getTotalByteCount() + "%", true, true);

            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 11);
    }

    private void loadForumPost(int count) {
        rootReference.limitToLast(count)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (recyclerView.getLayoutManager() != null)
                            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
                        namess.clear();
                        textss.clear();
                        times.clear();
                        user_id.clear();
                        post_id.clear();
                        preview_replierID.clear();
                        preview_replierText.clear();
                        preview_replierName.clear();
                        postImage_url.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            post_id.add(0, snapshot.getKey());
                            user_id.add(0, snapshot.child("id").getValue(String.class));
                            namess.add(0, snapshot.child("name").getValue(String.class));
                            textss.add(0, snapshot.child("text").getValue(String.class));
                            postImage_url.add(0, snapshot.child("imageUrl").getValue(String.class));
                            times.add(0, elapsedTime(snapshot.getKey(), snapshot.child("time").getValue(String.class)));
                            if (Build.VERSION.SDK_INT >= 23) {
                                replyPreview(snapshot.getKey());
                                perPost_reportCount(snapshot.getKey());
                            } else {
                                replycount(snapshot.getKey());
                                perPost_reportCount(snapshot.getKey());
                            }
                        }
                        getSize();
                        loadReportedIDlist();
                        loadBlocklist();
                        loadNotificationCount();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadNotificationCount() {
        final ArrayList<String> unseencount = new ArrayList<>();
        final DatabaseReference notificationReference = database.getReference().child("notifications");
        if (facebook_id != null)
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
                        notificationCount.setText(notificationText);
                        notificationCount.setVisibility(View.VISIBLE);

                    } else {
                        notificationCount.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void perPost_reportCount(final String post_ID) {
        reply_preview_reference = database.getReference().child("user").child(post_ID).child("reportPost");
        reply_preview_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> total_report_each_post = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    total_report_each_post.add(snapshot.getKey());

                }
                if (total_report_each_post.size() != 0) {
                    if (total_report_each_post.size() > reportSize) {
                        rootReference.child(post_ID).child("reply").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.getKey() != null) {
                                        storageRef.child(snapshot.getKey()).delete();
                                        imageReference.child(snapshot.getKey()).setValue(null);
                                    }

                                }

                                rootReference.child(post_ID).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        rootReference.child(post_ID).setValue(null);
                        storageRef.child(post_ID).delete();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void replyPreview(final String post_ID) {
        reply_preview_reference = database.getReference().child("user").child(post_ID).child("reply");
        reply_preview_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    total_comments_each_post.add(snapshot.getKey());
                    replierID = snapshot.child("myID").getValue(String.class);
                    postID = snapshot.child("text").getValue(String.class);
                    replierName = snapshot.child("name").getValue(String.class);
                    replyImage = snapshot.child("imageUrl").getValue(String.class);
                }
                if (total_comments_each_post.size() != 0) {
                    if (total_comments_each_post.size() > 1) {
                        preview_replierText.add(0, postID);
                        preview_replierName.add(0, replierName);
                        preview_replierID.add(0, replierID);
                        reply_imageUrl.add(0, replyImage);
                        comment_count.add(0, (total_comments_each_post.size()) + " comments");
                        total_comments_each_post.clear();
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        if (recyclerView.getLayoutManager() != null)
                            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                    } else {
                        preview_replierText.add(0, postID);
                        preview_replierName.add(0, replierName);
                        preview_replierID.add(0, replierID);
                        reply_imageUrl.add(0, replyImage);
                        comment_count.add(0, (total_comments_each_post.size()) + " comment");
                        total_comments_each_post.clear();
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        if (recyclerView.getLayoutManager() != null)
                            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                    }

                } else {
                    comment_count.add(0, "Reply");
                    preview_replierText.add(0, "blank");
                    preview_replierName.add(0, "blank");
                    preview_replierID.add(0, "blank");
                    reply_imageUrl.add(0, "blank");
                    total_comments_each_post.clear();
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if (recyclerView.getLayoutManager() != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void replycount(final String post_ID) {
        reply_preview_reference = database.getReference().child("user").child(post_ID).child("reply");
        reply_preview_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    total_comments_each_post.add(snapshot.getKey());
                }
                if (total_comments_each_post.size() != 0) {
                    if (total_comments_each_post.size() > 1) {
                        comment_count.add(0, (total_comments_each_post.size()) + " comments");
                        total_comments_each_post.clear();
                        recyclerView.setAdapter(lollipopAdapter);
                        lollipopAdapter.notifyDataSetChanged();
                    } else {
                        comment_count.add(0, (total_comments_each_post.size()) + " comment");
                        total_comments_each_post.clear();
                        recyclerView.setAdapter(lollipopAdapter);
                        lollipopAdapter.notifyDataSetChanged();
                    }

                } else {
                    comment_count.add(0, "Reply");
                    recyclerView.setAdapter(lollipopAdapter);
                    lollipopAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAdminButton() {
        if (facebook_id != null && facebook_id.equals("1335608633238560")) {
            writeNotify.setVisibility(View.VISIBLE);
            writeforeceUpdate.setVisibility(View.VISIBLE);
        }
    }

    private void postQuery(String current_post_time, String post_text) {
        post.put("name", facebook_user_name);
        post.put("first_name", first_name);
        post.put("time", postDate());
        post.put("id", facebook_id);
        post.put("notifyMe", "yes");
        rootReference.child(current_post_time).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                edit_text.setText(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                myToaster("failed");
            }
        });
        workSequence(post_text, current_post_time);
    }

    private void initialize() {

        callbackManager = CallbackManager.Factory.create();
        progressBar = findViewById(R.id.forumProgressbar);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Forum.this);
        alert = alertBuilder.create();
        chooser_cardview = findViewById(R.id.chooser_cardview);
        chooser_cardview.setVisibility(View.GONE);
        forum_subscription = findViewById(R.id.forum_subscription);
        imageChooser = findViewById(R.id.imageChooser);
        del_chooser = findViewById(R.id.del_chooser);
        notifications = findViewById(R.id.updateNotifier);
        writeforeceUpdate = findViewById(R.id.updateforce);
        writeforeceUpdate.setVisibility(View.GONE);
        writeNotify = findViewById(R.id.notifyServer);
        writeNotify.setVisibility(View.GONE);
        notificationCount = findViewById(R.id.notificationCount);
        notificationCount.setVisibility(View.GONE);
        my_profile = findViewById(R.id.my_profile);
        namess = new ArrayList<>();
        textss = new ArrayList<>();
        times = new ArrayList<>();
        post_id = new ArrayList<>();
        user_id = new ArrayList<>();
        preview_replierID = new ArrayList<>();
        preview_replierText = new ArrayList<>();
        preview_replierName = new ArrayList<>();
        comment_count = new ArrayList<>();
        postImage_url = new ArrayList<>();
        reply_imageUrl = new ArrayList<>();
        total_comments_each_post = new ArrayList<>();
        FirebaseApp.initializeApp(this);
        notificationPreference = getSharedPreferences("forum_notification", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        rootReference = database.getReference().child("user");
        imageReference = database.getReference().child("imageReference");
        storageRef = FirebaseStorage.getInstance().getReference("image/");
        post = new HashMap<>();
        edit_text = findViewById(R.id.edit_text);
        recyclerView = findViewById(R.id.forum_recyclerView);
        icon = findViewById(R.id.icon);
        send = findViewById(R.id.send);
        pic_preview = findViewById(R.id.pic_preview);
        adapter = new ForumAdapter(Forum.this, namess, textss, times, user_id,
                post_id, preview_replierText, preview_replierName, preview_replierID,
                comment_count, postImage_url, reply_imageUrl);
        lollipopAdapter = new LollipopAdapter(Forum.this, namess, user_id, textss, times,
                postImage_url, post_id, comment_count);
        manager = new LinearLayoutManager(Forum.this);
        post_image = findViewById(R.id.post_image);
        recyclerView.setLayoutManager(manager);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void myToaster(String text) {
        Toast toast = makeText(Forum.this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    private String postDate() {
        long mydate = System.currentTimeMillis();
        Locale locale = new Locale("US");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy h:mm a", locale);
        facebook_post_time = sdf.format(mydate);
        return facebook_post_time;
    }

    private String currentPostTime() {
        long mydate = System.currentTimeMillis();
        facebook_post_time = Long.toString(mydate);
        return facebook_post_time;
    }

    private String elapsedTime(String postTimeMillis, String genuine_pot_time) {
        Locale locale = new Locale("US");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy h:mm a", locale);
        String ago;
        long one_second = 1000;
        long one_minute = one_second * 60;
        long one_hour = one_minute * 60;
        long day = one_hour * 24;
        long post_time = Long.parseLong(postTimeMillis);
        String post_time_DateFormat = sdf.format(post_time).substring(0, 11);
        long current_time = System.currentTimeMillis();
        String current_time_DateFormat = sdf.format(current_time).substring(0, 11);
        long elapse_time = current_time - post_time;

        if (elapse_time < one_second) {
            ago = "jut now";
        } else if (elapse_time < one_minute) {
            if (elapse_time / one_second == 1) {
                ago = "1 second";
            } else {
                ago = (elapse_time / one_second) + " seconds ago";
            }

        } else if (elapse_time >= one_minute && elapse_time < one_hour) {
            if (elapse_time / one_minute == 1) {
                ago = "1 min";
            } else {
                ago = (elapse_time / one_minute) + " minutes ago";
            }

        } else if (elapse_time >= one_hour && elapse_time < day) {
            if (elapse_time / one_hour == 1) {
                ago = "1 hour";
            } else if (elapse_time / one_hour > 1 && post_time_DateFormat.equals(current_time_DateFormat)) {
                ago = (elapse_time / one_hour) + " hours ago";
            } else {
                ago = "Yesterday at " + sdf.format(post_time).substring(12);
            }
        } else if (elapse_time >= day && elapse_time < day * 1.5) {
            ago = "Yesterday at " + sdf.format(post_time).substring(12);
        } else {
            ago = genuine_pot_time;
        }
        return ago;

    }

    private Long notificationExpiry(String postTimeMillis) {
        long post_time = Long.parseLong(postTimeMillis);
        long current_time = System.currentTimeMillis();
        return current_time - post_time;
    }

    public void setFont(Context context, Activity activity) {
        Calligrapher font = new Calligrapher(context);
        font.setFont(activity, "kalpurush.ttf", true);
    }

    private void sendFCMPush(String title, String message, String data) {

        JSONObject obj = null;
        JSONObject fcmdataPlayload;

        try {
            obj = new JSONObject();
            fcmdataPlayload = new JSONObject();

            fcmdataPlayload.put("body", message);
            fcmdataPlayload.put("title", title);
            fcmdataPlayload.put("postID", data);
            fcmdataPlayload.put("icon", "imageUrl");

            //            "/topics/"+salary.getText().toString()
            obj.put("to", "/topics/forum");
            obj.put("priority", "high");
            obj.put("data", fcmdataPlayload);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + getString(R.string.serverKey));
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    private void workSequence(final String post_text, final String postID) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("forum").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (imageUri != null) {
                    sendFCMPush(facebook_user_name, "Uploaded an image", postID);
                } else {
                    sendFCMPush(facebook_user_name, post_text, postID);
                }

            }
        });
    }

    private void loadBlocklist() {
        blockReference = database.getReference().child("block");
        blockReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey() != null && snapshot.getKey().equals(facebook_id)) {
                        blocked = snapshot.getKey();
                    }
                    if (blocked != null) {
                        edit_text.setEnabled(false);
                        alert.dismiss();
                        alert.setMessage("You are permanently blocked from forum. You still can read forum posts but cannot post anything");
                        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogs, int which) {
                            }
                        });

                        try {
                            alert.show();
                        } catch (WindowManager.BadTokenException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadReportedIDlist() {
        final DatabaseReference reportReference = database.getReference().child("reportedID");
        reportReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey() != null && snapshot.getKey().equals(facebook_id)) {
                        reported = snapshot.getKey();
                    }
                }
                if (reported != null) {
                    autoBlock(reported);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void autoBlock(final String reported) {

        final DatabaseReference autoBlockReference = database.getReference().child("reportedID").child(reported);
        autoBlockReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> reporterIDlist = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    reporterIDlist.add(snapshot.getKey());
                }
                if (reporterIDlist.size() > reportSize) {
                    blockReference = database.getReference().child("block");
                    HashMap<String, Object> autoBlock = new HashMap<>();
                    autoBlock.put(reported, facebook_user_name);
                    blockReference.updateChildren(autoBlock);
                    autoBlockReference.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSize() {
        DatabaseReference postReference = database.getReference().child("postSize");

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long postSizeLong = dataSnapshot.getValue(Long.class);
                if (postSizeLong != null)
                    postSize = postSizeLong;
                if (post_id.size() > postSize) {
                    rootReference.child(post_id.get(post_id.size() - 1)).child("reply").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot != null) {
                                    myToaster(snapshot.getKey());
                                    if (snapshot.getKey() != null) {
                                        storageRef.child(snapshot.getKey()).delete();
                                        imageReference.child(snapshot.getKey()).setValue(null);
                                    }

                                }

                            }

                            rootReference.child(post_id.get(post_id.size() - 1)).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                            storageRef.child(post_id.get(post_id.size() - 1)).delete();
                            imageReference.child(post_id.get(post_id.size() - 1)).setValue(null);
                            rootReference.child(post_id.get(post_id.size() - 1)).setValue(null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFCMdataPlayLoad() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            String postID = intent.getExtras().getString("postID");
            if (postID != null && facebook_id != null) {
                startActivity(new Intent(Forum.this, Reply.class).putExtra("postID", postID));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFacebookUser();
    }
}