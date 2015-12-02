package at.fhj.mad.art.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import at.fhj.mad.art.interfaces.ICallbackHttpPOSTHelper;

/**
 * Helper Class to perform an Async-HTTP-Request (GET or POST) to the Pushserver to un-/subscribe for
 * future tasks
 * <p/>
 * Created by kevin on 16.10.2015.
 */
public class HttpPOSTHelper extends AsyncTask<String, String, String> {

    private ICallbackHttpPOSTHelper callbackHttpHelper;

    @Override
    protected String doInBackground(String... params) {

        URL url;
        HttpURLConnection urlConnection = null;
        String result = "";

        try {
            // params[0] is the given prepared url
            url = new URL(params[0]);

            // Build up an UrlConnection
            urlConnection = (HttpURLConnection) url.openConnection();

            // Define the Request Data as type POST and content-type JSON
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type",
                    "application/json");
            urlConnection.setRequestProperty("Content-Length", "" +
                    Integer.toString(params[1].getBytes().length));
            // Allowed to use cache?
            urlConnection.setUseCaches(false);
            // Is there an expected Input into that Connection?
            urlConnection.setDoInput(true);
            // Is there an expected Output from that Connection?
            urlConnection.setDoOutput(true);

            // Send POST-Data
            DataOutputStream wr = new DataOutputStream(
                    urlConnection.getOutputStream());
            wr.writeBytes(params[1]);
            wr.flush();
            wr.close();

            // Get Server Response
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            // Convert the finished result into an String
            String tmp = out.toString();

            // See, what server has sent back
            Log.i("Server-Response to " + params[2], tmp);

            if (tmp.equals("OK")) {
                result = tmp;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the UrlConnection after the Data has been saved
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        // Return how the Server has reacted or what the Server has responded
        // Will be processed in the onPostExecute-Method
        return result + "," + params[2];
    }

    /**
     * Callback to calling methods
     *
     * @param s return parameter from doInBackground-Method
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        String status, type;
        String[] splitted = s.split(",");
        status = splitted[0];
        type = splitted[1];

        // Handle, how the Server has reacted
        // s comes from the doInBackground-Method
        if (status.equals("OK")) {
            switch (type) {
                case "subscribe":
                    callbackHttpHelper.finished_subscribe(status);
                    break;
                case "unsubscribe":
                    callbackHttpHelper.finished_unsubscribe(status);
                    break;
            }
        } else {
            callbackHttpHelper.finished_error();
        }

    }

    /**
     * Set the Activity, where the ICallbackHttpPOSTHelper Interface is implemented
     *
     * @param callback Activity which needs data from this helper class
     */
    public void setCallback(ICallbackHttpPOSTHelper callback) {
        callbackHttpHelper = callback;
    }

}