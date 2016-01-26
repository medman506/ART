package at.fhj.mad.art.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import at.fhj.mad.art.R;
import at.fhj.mad.art.gcm.QuickstartPreferences;
import at.fhj.mad.art.gcm.RegistrationIntentService;
import at.fhj.mad.art.helper.HttpSubscriptionHelper;
import at.fhj.mad.art.helper.LoginHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpPOSTHelper;
import at.fhj.mad.art.interfaces.ICallbackLogin;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements ICallbackLogin, ICallbackHttpPOSTHelper {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoginHelper loginHelper = null;

    // UI references.
    private EditText mUsername;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private SharedPreferences prefs;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private int userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = getSharedPreferences(ListActivity.SHARED_PREFS_SETTINGS, 0);
        // Set up the login form.
        mUsername = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Toast.makeText(context, getResources().getString(R.string.main_toast_saved_token), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, getResources().getString(R.string.main_toast_not_saved_token), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        Log.i("TOKEN",prefs.getString(RegistrationIntentService.TOKEN,""));
        //check if token available
        if(prefs.getString(RegistrationIntentService.TOKEN,"").equals("")){
            Log.i("LOGIN", "Need new token");
            //check play service and get token
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }


    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (loginHelper != null) {
            return;
        }

        // Reset errors.
        mUsername.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsername.getText().toString();
        String password = mPasswordView.getText().toString();
        Log.i("PASSWORD",password);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        } else if (!isUsernameValid(email)) {
            mUsername.setError(getString(R.string.error_invalid_email));
            focusView = mUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            loginHelper = new LoginHelper(this);
            callLogin(loginHelper);

        }
    }

    private boolean isUsernameValid(String username) {
        return username.contains(".");
    }


    /**
     * Makes an HttpRequest to the Server to login the user
     */
    private void callLogin(LoginHelper loginHelper)  {
        // Reinitialize the URL Connection every time the switch button has changed

        //TODO: Rewerite url
        String url = "http://kerbtech.diphda.uberspace.de/art2/login";
        JSONObject obj = new JSONObject();


        try {
            obj.put("user", URLEncoder.encode(mUsername.getText().toString(), "UTF-8"));
            obj.put("pass", URLEncoder.encode(mPasswordView.getText().toString(), "UTF-8"));
            loginHelper.execute(url, obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        Log.i("PLAY SERVICE","CHHECKING");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Play Services Check", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void callback(String success) {
        loginHelper = null;
        showProgress(false);




        if(success.equals("-1")) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_LONG).show();
        }else if(success.equals("0")) {

            //callback failed
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }else {
            //Parse id
            userID = Integer.parseInt(success);

            //get token from prefs
            String token=prefs.getString(RegistrationIntentService.TOKEN,"");

            //check if token available
            if(token.equals("")){
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }else {
                //subscribe

                try {
                    call_subscribe(userID,token);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }


            finish();
        }





    }



    /**
     * Makes an HttpRequest to the Server to subscribe to the Service
     * Throws a Toast if some Data is missing (username or GCM-Token)
     *
     * @throws JSONException                Thrown by JSONObject
     * @throws UnsupportedEncodingException Thrown by URLEncoder
     */
    private void call_subscribe(int id, String token) throws JSONException, UnsupportedEncodingException {

        HttpSubscriptionHelper httpSubscriptionHelper= new HttpSubscriptionHelper();
        httpSubscriptionHelper.setCallback(this);

        //TODO: Change URL
        String url = "http://kerbtech.diphda.uberspace.de/art2/subscribe";
        JSONObject obj = new JSONObject();


        obj.put("user", URLEncoder.encode(String.valueOf(id), "UTF-8"));
        obj.put("token", URLEncoder.encode(prefs.getString(RegistrationIntentService.TOKEN, ""), "UTF-8"));
        httpSubscriptionHelper.execute(url, obj.toString(), "subscribe");

    }


    @Override
    public void finished_subscribe(String response) {

        //write to shared prefs
        Editor editor = prefs.edit();
        editor.putInt("userID", userID);
        editor.commit();
        Log.i("USERID",String.valueOf(userID));
        //finish and start list activity
        Intent listIntent = new Intent(getApplicationContext(), ListActivity.class);
        startActivity(listIntent);
        finish();


    }

    @Override
    public void finished_unsubscribe() {
        //Not implemented
    }

    @Override
    public void finished_error() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.undefined_error), Toast.LENGTH_SHORT).show();
    }
}

