package com.example.joblist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private final static String EMAIL = "email";
    private final static String PUBLIC_PROFILE = "public_profile";
    private CallbackManager fbCallbackManager;
    private ProfileTracker fbProfileTracker;

    private GoogleSignInClient gsc;

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

        findViewById(R.id.login_button_google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = gsc.getSignInIntent();
                startActivityForResult(signInIntent, 1);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null){
                Globals.profileName = account.getDisplayName();
                Globals.loginMethod = Globals.LOGIN_BY_GOOGLE;
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        } catch (ApiException e) {

        }
    }

    private void fbCheckLogin(){
        fbCallbackManager = CallbackManager.Factory.create();

        // check fb login
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isFbLoggedIn = accessToken != null && !accessToken.isExpired();

        fbProfileTracker = new ProfileTracker() {
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
            }
        };

        if(isFbLoggedIn){
            LoginManager.getInstance().logOut();
        }
    }

    private void googleCheckLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(getApplicationContext(), gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null){
            Globals.profileName = account.getDisplayName();
            Globals.loginMethod = Globals.LOGIN_BY_GOOGLE;
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
    }
}