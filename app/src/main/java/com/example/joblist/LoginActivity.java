package com.example.joblist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private final static String EMAIL = "email";
    private final static String PUBLIC_PROFILE = "public_profile";
    private CallbackManager fbCallbackManager;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fbCheckLogin();
        googleCheckLogin();

        setContentView(R.layout.activity_login);

        // setup login button
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.login_button_fb);
        fbLoginButton.setPermissions(Arrays.asList(PUBLIC_PROFILE, EMAIL));

        fbLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Login Result", "logged in");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Login Result", error.toString());
            }
        });

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.joblist",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {
        }
        catch (NoSuchAlgorithmException e) {
        }
    }

    private void fbCheckLogin(){
        fbCallbackManager = CallbackManager.Factory.create();

        // check fb login
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isFbLoggedIn = accessToken != null && !accessToken.isExpired();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                if(currentProfile != null){
                    Globals.profileName = currentProfile.getName();
                    Globals.loginMethod = Globals.LOGIN_BY_FB;
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                } else {
                    Globals.profileName = "";
                    Globals.loginMethod = "";
                }
                // App code
            }
        };

        if(isFbLoggedIn){
            LoginManager.getInstance().logOut();
        }
    }

    private void googleCheckLogin() {

    }
}