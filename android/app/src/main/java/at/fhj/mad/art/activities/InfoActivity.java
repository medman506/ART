package at.fhj.mad.art.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import at.fhj.mad.art.R;
import at.fhj.mad.art.helper.HttpTeamHelper;
import at.fhj.mad.art.helper.QuickstartPreferences;
import at.fhj.mad.art.helper.TeamResult;
import at.fhj.mad.art.interfaces.ICallbackHttpTeamHelper;

/**
 * Show InfoActivity where the User can change his Username
 * Also, User is redirected if on startup no username is set
 */
public class InfoActivity extends AppCompatActivity implements ICallbackHttpTeamHelper {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private TextView server_status;
    private TextView currentTeam;
    private CountDownTimer cdt;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        prefs= getApplicationContext().getSharedPreferences(QuickstartPreferences.SHARED_PREFS_SETTINGS, 0);
        editor= prefs.edit();

        server_status = (TextView) findViewById(R.id.info_tf_server_status);
        currentTeam = (TextView) findViewById(R.id.info_tf_teamresult);

        //Call Server  to update team and serverstatus
        updateTeam();

        // Periodically check if Server is reachable or not
       isRunning = true;

        /*
        Countdowntimer to update server status and current team
         */
        cdt = new CountDownTimer(10000, 10000) {

            //Do nothing in tick
            public void onTick(long millisUntilFinished) {
                //No implementation
            }

            public void onFinish() {
                updateTeam();
                // Restart Countdown
                if (isRunning) {
                    cdt.start();
                }
            }
        };
        cdt.start();
    }

    /**
     * Cancel countdowntimer
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cdt != null) {
            cdt.cancel();
        }

        isRunning = false;
    }

    /**
     * Calls the HttpTeamhelper to retrieve team of logged in user and status of Webserver
     */
    private void updateTeam() {
        HttpTeamHelper httpTeamHelper = new HttpTeamHelper();
        httpTeamHelper.setCallbackHttpHelper(this);

        String url = QuickstartPreferences.WEB_ADRESS+"users/"+prefs.getInt("userID", -1)+"/team";
        httpTeamHelper.execute(url);
    }

    /**
     * Implementation of Callback Method
     * Displays current team and server status
     * @param tr: TeamResult Object
     */
    @Override
    public void returnTeamResult(TeamResult tr) {
        currentTeam.setText(tr.getTeam());
        editor.putBoolean("serverstatus", tr.isActive());
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