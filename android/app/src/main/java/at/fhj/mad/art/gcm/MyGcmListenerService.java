/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.fhj.mad.art.gcm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import at.fhj.mad.art.R;
import at.fhj.mad.art.activities.ListTaskActivity;
import at.fhj.mad.art.helper.SQLiteHelper;
import at.fhj.mad.art.helper.UpdateHelper;
import at.fhj.mad.art.model.Task;

/**
 * Receives Push Notifications
 */
public class MyGcmListenerService extends GcmListenerService {

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //noinspection StatementWithEmptyBody
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        Log.i("NOTIFICATION","NEW NOTIFICATION");
        // Only notify if actual data was received
        if (data.getString("message", null) != null && data.getString("link", null) != null) {
            sendNotification(data, from);
        }

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param data JSON-Object from Server
     * @param from Subscribed topics
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(Bundle data, String from) {
        // To show multiple Notifications, assign unique notification-id

        // Adding the new Task
        Task t = new Task();
        t.setMessage(data.getString("message", "-"));
        t.setTopic(from);
        t.setLink(data.getString("link", "-"));
        t.setAddress(data.getString("adress", "-"));
        SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());
        long id = sqLiteHelper.addTask(t);

        // Prepare Intent
        Intent intent = new Intent(this, ListTaskActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("taskID", id);

        // Sending Broadcast that a new Item arrived( for users in listview)
        sendBroadcast(new Intent(UpdateHelper.UPDATE_STRING));

        // PendingIntent with parent stack
        // Lets the user get back to listview if clicked on notification
        // Backstack
        PendingIntent pendingIntent = TaskStackBuilder.create(getApplicationContext())
                .addNextIntentWithParentStack(intent).getPendingIntent((int) id, PendingIntent.FLAG_UPDATE_CURRENT);

        // Building the Notification which  will be shown
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.launcher_icon))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("ART")
                .setContentText(data.getString("message", "-"))
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm_short))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 1000, 1000)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Send Notification to screen
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());

    }
}
