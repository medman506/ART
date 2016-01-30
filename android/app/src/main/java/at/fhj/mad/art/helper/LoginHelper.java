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

import at.fhj.mad.art.interfaces.ICallbackLogin;


/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class LoginHelper extends AsyncTask<String, String, String>  {



    private ICallbackLogin loginCallback;

    public  LoginHelper(ICallbackLogin loginCallback) {
        this.loginCallback = loginCallback;

    }

    @Override
    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection urlConnection = null;
        String result = null;

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
            //Write the json object
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
            Log.i("Server-Response to " , tmp);

            result = tmp;

        } catch (IOException e) {
            e.printStackTrace();
            return "-1";

        } finally {
            // Close the UrlConnection after the Data has been saved
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        // Return how the Server has reacted or what the Server has responded
        // Will be processed in the onPostExecute-Method
        return result;

    }

    @Override
    protected void onPostExecute(String success) {

        loginCallback.callback(success);
    }

    @Override
    protected void onCancelled() {
        loginCallback.callback(null);
    }






}
