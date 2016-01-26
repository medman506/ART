package at.fhj.mad.art.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import at.fhj.mad.art.R;
import at.fhj.mad.art.helper.HttpStatusHelper;
import at.fhj.mad.art.helper.HttpSubscriptionHelper;
import at.fhj.mad.art.helper.HttpTeamHelper;
import at.fhj.mad.art.helper.QuickstartPreferences;
import at.fhj.mad.art.interfaces.ICallbackHttpStatusHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpTeamHelper;

/**
 * Show InfoActivity where the User can change his Username
 * Also, User is redirected if on startup no username is set
 */
public class InfoActivity extends AppCompatActivity implements ICallbackHttpStatusHelper, ICallbackHttpTeamHelper {

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
    private TextView currentTeam;
    private CountDownTimer cdt;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        prefs= getApplicationContext().getSharedPreferences(SHARED_PREFS_SETTINGS, 0);
        editor= prefs.edit();

        activityContext = this;

        server_status = (TextView) findViewById(R.id.info_tf_server_status);
        currentTeam = (TextView) findViewById(R.id.info_tf_teamresult);




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

        String url = QuickstartPreferences.WEB_ADRESS;
        httpStatusHelper.execute(url);
    }

    /**
     * Calls the server to check current team of logged in user
     */
    private void updateTeam() {
        HttpTeamHelper httpTeamHelper = new HttpTeamHelper();
        httpTeamHelper.setCallbackHttpHelper(this);

        String url = QuickstartPreferences.WEB_ADRESS+"users/"+prefs.getInt("userID", -1)+"/team";
        httpTeamHelper.execute(url);
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

    @Override
    public void returnTeam(String team) {
       currentTeam.setText(team);
    }
}