package at.fhj.mad.art.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import at.fhj.mad.art.R;
import at.fhj.mad.art.adapters.TwoLineAdapter;
import at.fhj.mad.art.helper.HttpSubscriptionHelper;
import at.fhj.mad.art.helper.QuickstartPreferences;
import at.fhj.mad.art.helper.SQLiteHelper;
import at.fhj.mad.art.helper.SwipeDetector;
import at.fhj.mad.art.helper.UpdateHelper;
import at.fhj.mad.art.interfaces.ICallbackHttpPOSTHelper;
import at.fhj.mad.art.interfaces.ICallbackUpdateListener;
import at.fhj.mad.art.model.Task;

/**
 * Activity to display all saved Tasks
 * Main Screen of app
 */
public class ListActivity extends AppCompatActivity implements ICallbackUpdateListener, ICallbackHttpPOSTHelper {

    //Strings for the Hamburger Menu
    private String INFO_STRING;
    private String CONTACT_STRING;
    private String LOGOUT_STRING;

    //Hamburger menu
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    //Custom list for displaying tasks
    private ArrayList<Task> saved_list;
    private TwoLineAdapter adapter;

    //Updates list when notification is received
    private UpdateHelper myUpdateHelper;
    //Helper class for db access
    private SQLiteHelper sqLiteHelper;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = getSharedPreferences(QuickstartPreferences.SHARED_PREFS_SETTINGS, 0);

        // Redirect to login screen screen if none is "logged in"
        if (prefs.getInt("userID", -1)==-1) {
           callLoginScreen();
        }


        super.onCreate(savedInstanceState);
        // Associate with layout
        setContentView(R.layout.activity_list);
        // set this as callback in helper
        myUpdateHelper = new UpdateHelper(this);

        // Initiate Strings for Hamburger Menu
        INFO_STRING = getResources().getString(R.string.nav_info);
        CONTACT_STRING = getResources().getString(R.string.nav_contact);
        LOGOUT_STRING = getResources().getString(R.string.nav_logout);

        // Init Hamburger menu
        mDrawerList = (ListView) findViewById(R.id.settings_navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.settings_drawer_layout);
        mActivityTitle = getTitle().toString();

        //Setting up Drawer( option menu)
        addDrawerItems();
        setupDrawer();

        // Activates Hamburger-Symbol and animation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Gives visual Feedback that the button has been pressed
        getSupportActionBar().setHomeButtonEnabled(true);

        // get list from layout
        ListView list = (ListView) findViewById(R.id.list);

        // Getting Tasks  from DB
        sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());
        saved_list = sqLiteHelper.readAllTasks();
        Collections.reverse(saved_list);

        // Add Tasks to Listview
        adapter = new TwoLineAdapter(this, saved_list);
        list.setAdapter(adapter);

        // Adding swipedetector
        final SwipeDetector swipeDetector = new SwipeDetector();
        list.setOnTouchListener(swipeDetector);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL || swipeDetector.getAction() == SwipeDetector.Action.LR) {
                        SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());
                        long tasktid = saved_list.get(position).getId();
                        Task t = sqLiteHelper.readId(tasktid);
                        if (sqLiteHelper.deleteTask(t)) {
                            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                                removeListItem(view, "left");
                            } else if (swipeDetector.getAction() == SwipeDetector.Action.LR) {
                                removeListItem(view, "right");
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.list_toast_task_not_deleted), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // If no swipe, just click: open task
                    Intent i = new Intent(getApplicationContext(), ListTaskActivity.class);

                    // Can't send whole task-object through Intent because implementing the Parcable-Interface in Task is too difficult (for now)
                    i.putExtra("taskID", saved_list.get(position).getId());

                    startActivity(i);
                }
            }
        });
    }

    /**
     * Handle Swipe Deletion of Element
     * Render Animation
     *
     * @param rowView        Row-Element which should be animated
     * @param swipedirection String "left" or "right" depending on which Swipedirection
     */
    private void removeListItem(View rowView, String swipedirection) {
        if (swipedirection.equals("right")) {
            Animation animation_LR = AnimationUtils.loadAnimation(
                    ListActivity.this, R.anim.slide_out_right);
            rowView.startAnimation(animation_LR);
        } else {
            Animation animation_RL = AnimationUtils.loadAnimation(
                    ListActivity.this, R.anim.slide_out_left);
            rowView.startAnimation(animation_RL);
        }
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetList();
            }
        }, 250);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.list_toast_task_deleted), Toast.LENGTH_SHORT).show();

    }

    /**
     * Resets the list to null
     * Reloads and redisplays all tasks
     */
    private void resetList() {
        if (adapter != null) {
            adapter.clear();
            saved_list = sqLiteHelper.readAllTasks();
            Collections.reverse(saved_list);
            adapter.addAll(saved_list);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Adds Drawer-Menu-Items to the DrawerList Object and sets an onClickListener to handle
     * actions of the Menu-Items.
     */
    private void addDrawerItems() {
        String[] menuArray = {INFO_STRING, CONTACT_STRING, LOGOUT_STRING};
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleDrawerItem(parent.getItemAtPosition(position).toString());
            }
        });
    }

    /**
     * Setup Drawermenu and corresponding methods
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.nav_drawer_open, R.string.nav_drawer_close) {

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //noinspection ConstantConditions
                getSupportActionBar().setTitle(getResources().getString(R.string.nav_navigation));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //noinspection ConstantConditions
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    // Lifecycle Activities following
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register update receiver when user is on screen
        if (myUpdateHelper != null) {
            registerReceiver(myUpdateHelper, new IntentFilter(UpdateHelper.UPDATE_STRING));
            resetList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //UpdateHelper is not needed when user is not on screen
        unregisterReceiver(myUpdateHelper);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate the navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Use back button to close Drawer
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handles Menu button press
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Open/Close Drawer with Menu-Key (if the Device has one)
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Handle which Item has been pressed in the DrawerLayout and start that corresponding Activity
     *
     * @param item String name of the Activity
     */
    private void handleDrawerItem(String item) {
        if (item.equals(INFO_STRING)) {
            Intent i = new Intent(this, InfoActivity.class);
            startActivity(i);
        } else if (item.equals(CONTACT_STRING)) {
            Intent i = new Intent(this, ContactActivity.class);
            startActivity(i);
        } else if (item.equals(LOGOUT_STRING)){
            try {
                call_unsubscribe();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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
        HttpSubscriptionHelper httpSubscriptionHelper = new HttpSubscriptionHelper();
        httpSubscriptionHelper.setCallback(this);

        String url = "http://kerbtech.diphda.uberspace.de/art2/unsubscribe";
        JSONObject obj = new JSONObject();

        if (prefs.getInt("userID",-1)>0) {
            obj.put("user", URLEncoder.encode(String.valueOf(prefs.getInt("userID",-1)), "UTF-8"));
            httpSubscriptionHelper.execute(url, obj.toString(), "unsubscribe");
        } else {
           callLoginScreen();
        }
    }


    /**
     * Refreshing the Listview when a new Notification arrives
     * Clears the old Elements and re-retrieves all items including new one from DB
     * Implemented method from Updatehelper
     */
    @Override
    public void handleListUpdate() {
        resetList();
    }

    @Override
    public void finished_subscribe(String response) {
        //not implemented here
        //used in login activity
    }

    @Override
    public void finished_unsubscribe() {
        SharedPreferences.Editor editor = prefs.edit();
        //remove userID from settings file
        editor.remove("userID");
        editor.apply();
        //move to login screen
        callLoginScreen();
    }

    @Override
    public void finished_error() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.undefined_error), Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens Login Screen and finishes activity
     */
    private void callLoginScreen(){
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
