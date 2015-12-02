package at.fhj.mad.art.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import at.fhj.mad.art.R;
import at.fhj.mad.art.helper.HttpPOSTHelper;
import at.fhj.mad.art.helper.PositionListener;
import at.fhj.mad.art.interfaces.ICallbackHttpPOSTHelper;
import at.fhj.mad.art.interfaces.ICallbackPositionListener;

/**
 * Automatically sends our actual GPS position to our pushserver
 */
public class PositionService extends Service implements ICallbackPositionListener, ICallbackHttpPOSTHelper {

    private Messenger messenger;

    private boolean isRunning;
    private CountDownTimer cdt;
    private double latitude;
    private double longitude;

    private SharedPreferences prefs;
    private HttpPOSTHelper httpPOSTHelper;
    private PositionService positionService;
    private LocationManager locationManager;
    private PositionListener posList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get messenger from the Intent to communicate with the activity
        if (intent != null) {
            messenger = intent.getParcelableExtra("messenger");
        }

        longitude = latitude = 0.0;

        String SHARED_PREFS_SETTINGS = "Settings";
        prefs = getSharedPreferences(SHARED_PREFS_SETTINGS, 0);

        // Needed for httpPOSTHelper.setCallback() in the CountDownTimer
        positionService = this;

        isRunning = true;

        Log.i("Position-Service", "Service started");

        // Get locationManager from Android if not initialized yet
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        // Start the Position Listener
        if (posList == null) {
            posList = new PositionListener();
            posList.setiCallbackPositionListener(this);
        }

        // Check if detailed GPS is activated. If not, send Activity a message to
        // trigger the GPS-Dialog
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Message message = Message.obtain(null, 1);
            try {
                messenger.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
        }

        // Start getting actual GPS-Position
        //noinspection ResourceType
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, posList);

        // every 60 seconds do onFinish()
        // CountDownTimer(Future Task to be done (in MilliSec), countDownInterval (in MilliSec))
        cdt = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                // Log.i("WEATHER SERVICE", "Service CountDownTimer Tick ");
            }

            public void onFinish() {
                httpPOSTHelper = new HttpPOSTHelper();
                httpPOSTHelper.setCallback(positionService);

                String url = "http://kerbtech.diphda.uberspace.de/art/location";
                JSONObject obj = new JSONObject();

                String username = prefs.getString("username", "");

                if (latitude != 0.0 && longitude != 0.0) {
                    try {
                        obj.put("user", URLEncoder.encode(username, "UTF-8"));
                        obj.put("latitude", URLEncoder.encode(Double.toString(latitude), "UTF-8"));
                        obj.put("longitude", URLEncoder.encode(Double.toString(longitude), "UTF-8"));
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //Log.i("Position-Service", "JSON-Obj: " + obj.toString());
                    httpPOSTHelper.execute(url, obj.toString(), "location");
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.posservice_toast_no_location), Toast.LENGTH_LONG).show();
                }

                // Restart Countdown to do this task every 60 seconds
                if (isRunning) {
                    cdt.start();
                }
            }

        };

        cdt.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (cdt != null) {
            cdt.cancel();
        }

        isRunning = false;

        if (locationManager != null && posList != null) {
            //noinspection ResourceType
            locationManager.removeUpdates(posList);
        }

        Log.i("Position-Service", "Service has stopped!");
    }

    @Override
    public void handleLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void finished_subscribe(String response) {

    }

    @Override
    public void finished_unsubscribe(String response) {

    }

    @Override
    public void finished_error() {

    }

}
