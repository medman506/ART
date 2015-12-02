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
import android.os.Messenger;
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
import at.fhj.mad.art.helper.HttpGETHelper;
import at.fhj.mad.art.helper.HttpPOSTHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpGETHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpPOSTHelper;
import at.fhj.mad.art.services.PositionService;

/**
 * Show SettingsActivity where the User can change his Username
 * Also, User is redirected if on startup no username is set
 */
public class SettingsActivity extends AppCompatActivity implements ICallbackHttpPOSTHelper, ICallbackHttpGETHelper {

    public static final String SHARED_PREFS_SETTINGS = "Settings";
    private static final String TAG = "SettingsActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static Handler handler;
    private EditText input;
    private SharedPreferences prefs;
    private Context context;
    private HttpPOSTHelper httpPOSTHelper;
    private Context activityContext;
    private Switch sw_active;
    private SharedPreferences.Editor editor;
    private TextView server_status;
    private CountDownTimer cdt;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        activityContext = this;

        // Handler receives 1 if LocationService is started and GPS is not activated
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    buildAlertMessageNoGps();
                }
            }
        };

        sw_active = (Switch) findViewById(R.id.settings_switch_active);
        server_status = (TextView) findViewById(R.id.settings_tf_server_status);

        prefs = getSharedPreferences(SHARED_PREFS_SETTINGS, 0);
        editor = prefs.edit();

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

        sw_active.setChecked(prefs.getBoolean("active", false));

        // Toggle active and inactive
        // Save state to shared prefs
        sw_active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateServerStatus();
                if (prefs.getBoolean("serverstatus", false)) {
                    // isChecked shows new Value, not actual Value
                    if (isChecked) {
                        try {
                            call_subscribe();
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            call_unsubscribe();
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Remain in actual status if Server is down
                    if (isChecked) {
                        sw_active.setChecked(false);
                    } else {
                        sw_active.setChecked(true);
                    }
                }
            }
        });

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

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

        input = (EditText) findViewById(R.id.settings_et_username);
        Button btn_save = (Button) findViewById(R.id.settings_btn_save);
        context = getApplicationContext();

        // Set the already saved username in the EditText, if available
        if (!(prefs.getString("username", "not_set").equals("not_set"))) {
            input.setText(prefs.getString("username", ""));
        }

        // User shouldn't be able to change username if he's active
        if (prefs.getBoolean("active", false)) {
            input.setFocusable(false);
            input.setFocusableInTouchMode(false);
            input.setClickable(false);
        }

        // Show toast if user still wants to change his username if he's active
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefs.getBoolean("active", false)) {
                    Toast.makeText(context, getResources().getString(R.string.settings_toast_username_not_editable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handling save of username
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = input.getText().toString().replace(" ", "");
                if (prefs.getString("username", "null").equals(username)) {
                    Toast.makeText(context, getResources().getString(R.string.settings_toast_username_didnt_change), Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("username", username);
                    editor.apply();
                    if (prefs.getString("username", "null").equals(username)) {
                        Toast.makeText(context, getResources().getString(R.string.settings_toast_username_saved), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getResources().getString(R.string.undefined_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Makes an HttpRequest to the Server to subscribe to the Service
     * Throws a Toast if some Data is missing (username or GCM-Token)
     *
     * @throws JSONException                Thrown by JSONObject
     * @throws UnsupportedEncodingException Thrown by URLEncoder
     */
    private void call_subscribe() throws JSONException, UnsupportedEncodingException {
        // Reinitialize the URL Connection every time the switch button has changed
        httpPOSTHelper = new HttpPOSTHelper();
        httpPOSTHelper.setCallback(this);

        String url = "http://kerbtech.diphda.uberspace.de/art/subscribe";
        JSONObject obj = new JSONObject();

        if (!(prefs.getString("username", "").equals(""))) {
            obj.put("user", URLEncoder.encode(prefs.getString("username", ""), "UTF-8"));
            if (!(prefs.getString("token", "").equals(""))) {
                obj.put("token", URLEncoder.encode(prefs.getString("token", ""), "UTF-8"));
                httpPOSTHelper.execute(url, obj.toString(), "subscribe");
            } else {
                sw_active.setChecked(false);
                Toast.makeText(activityContext, getResources().getString(R.string.main_toast_gcm_failure), Toast.LENGTH_LONG).show();
            }
        } else {
            sw_active.setChecked(false);
            Toast.makeText(activityContext, getResources().getString(R.string.main_toast_no_username), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Makes an HttpRequest to the server to unsubscribe from the service
     * Throws a Toast if the Username is missing
     *
     * @throws JSONException                Thrown by JSONObject
     * @throws UnsupportedEncodingException Thrown by URLEncoder
     */
    private void call_unsubscribe() throws JSONException, UnsupportedEncodingException {
        // Reinitialize the URL Connection every time the switch button has changed
        httpPOSTHelper = new HttpPOSTHelper();
        httpPOSTHelper.setCallback(this);

        String url = "http://kerbtech.diphda.uberspace.de/art/unsubscribe";
        JSONObject obj = new JSONObject();

        if (!(prefs.getString("username", "").equals(""))) {
            obj.put("user", URLEncoder.encode(prefs.getString("username", ""), "UTF-8"));
            httpPOSTHelper.execute(url, obj.toString(), "unsubscribe");
        } else {
            //sw_active.setChecked(true);
            Toast.makeText(activityContext, getResources().getString(R.string.main_toast_no_username), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Calls the helper method to check the current server status
     */
    private void updateServerStatus() {
        HttpGETHelper httpGETHelper = new HttpGETHelper();
        httpGETHelper.setCallbackHttpHelper(this);

        String url = "http://kerbtech.diphda.uberspace.de/art/";
        httpGETHelper.execute(url);
    }

    /**
     * Starts the location service
     * Messenger is passed through the Intent to communicate with the handler
     */
    private void startLocationService() {
        Intent i = new Intent(this, PositionService.class);
        Log.i("StartLocationService", "Triggered");
        Messenger messenger = new Messenger(handler);
        i.putExtra("messenger", messenger);
        startService(i);
    }

    /**
     * Stops the location service
     */
    private void stopLocationService() {
        Intent i = new Intent(this, PositionService.class);
        Log.i("StopLocationService", "Triggered");
        stopService(i);
    }

    /**
     * Checks if given Service-Class is already a running Service or not.
     *
     * @param serviceClass Service-Class which should be checked
     * @return true or false
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shows a Dialog if no gps is activated
     * Gives the User the opinion to activate GPS
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setMessage(getResources().getString(R.string.dialog_no_gps))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void finished_subscribe(String response) {
        if (!response.equals("OK")) {
            editor.putBoolean("active", false);
            editor.apply();
        } else {
            Toast.makeText(activityContext, getResources().getString(R.string.main_toast_activated), Toast.LENGTH_SHORT).show();
            editor.putBoolean("active", true);
            editor.apply();
            startLocationService();
        }
    }

    @Override
    public void finished_unsubscribe(String response) {
        if (!response.equals("OK")) {
            editor.putBoolean("active", true);
            editor.apply();
        } else {
            Toast.makeText(activityContext, getResources().getString(R.string.main_toast_deactivated), Toast.LENGTH_SHORT).show();
            editor.putBoolean("active", false);
            editor.apply();
            stopLocationService();
        }
    }

    @Override
    public void finished_error() {
        if (sw_active.isChecked()) {
            editor.putBoolean("active", false);
            editor.apply();
        } else {
            Toast.makeText(activityContext, getResources().getString(R.string.undefined_error), Toast.LENGTH_SHORT).show();
            editor.putBoolean("active", true);
            editor.apply();
        }
    }

    @Override
    public void isAvailable(boolean status) {
        editor.putBoolean("serverstatus", status);
        editor.apply();
        if (prefs.getBoolean("serverstatus", false)) {
            if (!(isMyServiceRunning(PositionService.class)) && prefs.getBoolean("active", false)) {
                startLocationService();
            }
            server_status.setText(getResources().getString(R.string.main_tf_server_active));
            server_status.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
        } else {
            if (isMyServiceRunning(PositionService.class)) {
                stopLocationService();
            }
            server_status.setText(getResources().getString(R.string.main_tf_server_inactive));
            server_status.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }
    }

}