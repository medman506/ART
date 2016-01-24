package at.fhj.mad.art.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import at.fhj.mad.art.interfaces.ICallbackUpdateListener;

/**
 * Created by mayerhfl13 on 13.11.2015.
 * Broadcast Receiver to refresh ListView when a new Notification arrives
 */
public class UpdateHelper extends BroadcastReceiver {

    public static final String UPDATE_STRING = "UPDATE_LISTVIEW";
    private ICallbackUpdateListener myUpdateListener = null;

    public UpdateHelper(ICallbackUpdateListener dataUpdateListener) {
        myUpdateListener = dataUpdateListener;
    }

    /**
     * Broadcast to propagate a new Task is available
     * (Used to update ListView when user is in app
     *
     * @param ctx
     * @param intent
     */
    @Override
    public void onReceive(Context ctx, Intent intent) {
        // assuming data is available in the delivered intent

        //Handle only if correct action is set
        if(intent.getAction().equals(UPDATE_STRING)) {
            Log.i("UPDATE", "BroadCast Received");
            myUpdateListener.handleListUpdate();
        }
    }
}
