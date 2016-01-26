package at.fhj.mad.art.helper;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import at.fhj.mad.art.interfaces.ICallbackHttpStatusHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpTeamHelper;

/**
 * Helper Class to verify the actual Pushserver status (online or offline)
 */
public class HttpTeamHelper extends AsyncTask<String, String, String> {

    private ICallbackHttpTeamHelper callbackHttpHelper;

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

            // Get Server Status
            int status = urlConnection.getResponseCode();

            // See, what server has sent back
            if (status == 200) {
                result = urlConnection.getResponseMessage();
            } else {
                result = "[]";
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
        return result;
    }

    /**
     * Callback to calling methods
     *
     * @param s return parameter from doInBackground-Method
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

      if(s.equals("[]")){
          callbackHttpHelper.returnTeam("none");
      }else{
          callbackHttpHelper.returnTeam(s);
      }

    }

    /**
     * Sets the activity which implements the HttpStatusHelper Interface
     *
     * @param callbackHttpHelper Activity which needs data from this helper class
     */
    public void setCallbackHttpHelper(ICallbackHttpTeamHelper callbackHttpHelper) {
        this.callbackHttpHelper = callbackHttpHelper;
    }
}
