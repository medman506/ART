package at.fhj.mad.art.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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
import at.fhj.mad.art.helper.HttpStatusHelper;
import at.fhj.mad.art.helper.HttpSubscriptionHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpGETHelper;

/**
 * Show InfoActivity where the User can change his Username
 * Also, User is redirected if on startup no username is set
 */
public class InfoActivity extends AppCompatActivity implements ICallbackHttpGETHelper {

    public static final String SHARED_PREFS_SETTINGS = "Settings";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static Handler handler;
    private EditText input;
    private SharedPreferences prefs;
    private Context context;
    private HttpSubscriptionHelper httpSubscriptionHelper;
    private Context activityContext;
    private Switch sw_active;
    private SharedPreferences.Editor editor;
    private TextView server_status;
    private CountDownTimer cdt;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        prefs= getApplicationContext().getSharedPreferences(SHARED_PREFS_SETTINGS, 0);
        editor= prefs.edit();

        activityContext = this;

        server_status = (TextView) findViewById(R.id.settings_tf_server_status);



        updateServerStatus();

        // Periodically check if Server is reachable or not
       isRunning = true;

        cdt = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                updateServerStatus();
                // Restart Countdown to do this task every 5 seconds
                if (isRunning) {
                    cdt.start();
                }
            }

        };

        cdt.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cdt != null) {
            cdt.cancel();
        }

        isRunning = false;
    }




    /**
     * Calls the helper method to check the current server status
     */
    private void updateServerStatus() {
        HttpStatusHelper httpStatusHelper = new HttpStatusHelper();
        httpStatusHelper.setCallbackHttpHelper(this);

        String url = "http://kerbtech.diphda.uberspace.de/art/";
        httpStatusHelper.execute(url);
    }



    @Override
    public void isAvailable(boolean status) {
        editor.putBoolean("serverstatus", status);
        editor.apply();
        if (prefs.getBoolean("serverstatus", false)) {
            server_status.setText(getResources().getString(R.string.main_tf_server_active));
            server_status.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
        } else {
            server_status.setText(getResources().getString(R.string.main_tf_server_inactive));
            server_status.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }
    }

}