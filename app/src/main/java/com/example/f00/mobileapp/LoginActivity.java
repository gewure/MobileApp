package com.example.f00.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    GoogleApiClient mGoogleApiClient;
    private  static final String TAG = "Google Sign In";
    private static final int RC_SIGN_IN = 9001;
    private TextView mStatus;

    private String userEmail;
    private String userName;
    private String userIdToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = initGoogleAPI();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);

        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.revoke_button).setOnClickListener(this);

        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        findViewById(R.id.revoke_button).setVisibility(View.GONE);

        mStatus = (TextView) findViewById(R.id.tv);
    }

    @NonNull
    private GoogleSignInOptions initGoogleAPI() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("317096533096-0qcg2rq503thto8qh8o70g2d8kjk5qik.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        return gso;
    }

    @Override
    public void onStart() {
        super.onStart();

        final Context ctx = this.getApplicationContext();

        ctx.getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            mStatus.setText("Attempting to Sign in...");
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;

            case R.id.revoke_button:
                revokeAccess();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut(){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "signOut Done");
                        mStatus.setText("Signed Out.");
                        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                        findViewById(R.id.revoke_button).setVisibility(View.GONE);
                    }
                });
    }

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "Revoke Done");
                        mStatus.setText("Access Revoked.");
                        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                        findViewById(R.id.revoke_button).setVisibility(View.GONE);
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            userName = acct.getDisplayName();
            userEmail = acct.getEmail();
            userIdToken = acct.getIdToken();

            Log.i(TAG, "OK: "+userIdToken);

            checkToken(userIdToken);

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.revoke_button).setVisibility(View.VISIBLE);
        } else {
            // Signed out, show unauthenticated UI.
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            mStatus.setText("Signed Out.");
        }
    }

    private void checkToken(final String userIdToken) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("token", userIdToken);
        client.post("https://omega.aizio.net:1234/api/checkToken/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject resp) {
                // Pull out the first event on the public timeline

                try {
                    JSONObject response = resp.getJSONObject("response");    // this will return correct
                    String status = response.getString("status");

                    if (status.equals("success")) {

                        Log.d("STATUS", status);

                        LoginManager loginManager = new LoginManager();
                        loginManager.setState(LoginActivity.this, true);
                        loginManager.setName(LoginActivity.this, userName);
                        loginManager.setEmail(LoginActivity.this, userEmail);
                        loginManager.setIdToken(LoginActivity.this, userIdToken);
                        // TO-DO: Google Cloud Messaging.
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                    }

                } catch (JSONException e) {

                }
            }
        });

        /*DownloadTask asyncTask = new DownloadTask(new AsyncResponse() {

            @Override
            public void processFinish(Object output) {

                try {
                    JSONObject json = new JSONObject((String)output);
                    JSONObject json_response = json.getJSONObject("response");
                    String json_status = json_response.getString("status");
                    Log.d(TAG, json_status);

                    if(json_status.equals("success")) {

                        LoginManager loginManager = new LoginManager();
                        loginManager.setState(LoginActivity.this,true);
                        loginManager.setName(LoginActivity.this,userName);
                        loginManager.setEmail(LoginActivity.this,userEmail);
                        loginManager.setIdToken(LoginActivity.this,userIdToken);
                        // TO-DO: Google Cloud Messaging.
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);


                    }
                }
                catch (JSONException e) {

                }
            }
        });

        asyncTask.execute("https://omega.aizio.net:1234/api/checkToken/" + userIdToken);*/
    }
}