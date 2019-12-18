package zubayer.docsites.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import zubayer.docsites.R;

import static zubayer.docsites.utility.UiFeatures.dataconnected;

public class FacebookLogIn extends Activity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String facebook_id, facebook_user_name, first_name, last_name;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_log_in);

        initWidgets();
        genetatekeyHash();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLigin();
            }
        });
    }

    private void initWidgets() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.loginButton);
    }

    private void facebookLigin() {
        if (dataconnected(this)) {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    graphRequest();
                }

                @Override
                public void onCancel() {
                    alertMessage("You need to log in first");
                }

                @Override
                public void onError(FacebookException error) {
                    alertMessage("Log in failed");
                    LoginManager.getInstance().logOut();
                }
            });
        } else {
            alertMessage("Turn on data");
        }
    }

    private void graphRequest() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(final JSONObject object, GraphResponse response) {
                try {
                    if (object != null) {
                        facebook_id = object.getString("id");
                        first_name = object.getString("first_name");
                        last_name = object.getString("last_name");

                        facebook_user_name = first_name + " " + last_name;

                        subscribeTopic(facebook_id);

                        SharedPreferences myIDpreference = getSharedPreferences("myID", Context.MODE_PRIVATE);
                        myIDpreference.edit().putString("myID", facebook_id).apply();
                        myIDpreference.edit().putString("myName", facebook_user_name).apply();
                        myIDpreference.edit().putString("first_name", first_name).apply();

                        finish();
                    } else {
                        alertMessage("Server error");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void subscribeTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    private void alertMessage(String text) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        AlertDialog alert = alertBuilder.create();
        alert.setCancelable(false);
        alert.setMessage(text);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(FacebookLogIn.this, Forum.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }

    private void genetatekeyHash() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "zubayer.docsites",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
