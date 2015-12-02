package at.fhj.mad.art.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import at.fhj.mad.art.R;
import at.fhj.mad.art.helper.SQLiteHelper;
import at.fhj.mad.art.model.Task;

/**
 * Basic Template for an already saved Task.
 */
public class ListTaskActivity extends AppCompatActivity {

    private SQLiteHelper sqLiteHelper;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_task);

        sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());

        TextView tv_todo_content = (TextView) findViewById(R.id.task_tv_todo_content);
        TextView tv_gmaps_link = (TextView) findViewById(R.id.task_tv_gmaps_links);
        TextView tv_address = (TextView) findViewById(R.id.task_tv_address);

        //Get id of current task by extracting info from intent
        long id = getIntent().getExtras().getLong("taskID");

        //Read from DB
        currentTask = sqLiteHelper.readId(id);
        Log.i("Tasklist", currentTask.toString());

        //Setting information
        String message = currentTask.getMessage();
        String topics = currentTask.getTopic();
        String address = currentTask.getAddress();
        final String link = currentTask.getLink();
        int year = currentTask.getYear();
        int month = currentTask.getMonth();
        int day = currentTask.getDay();

        //Set in GUI, only if all data is set
        if ((!(message != null && message.equals("")) && !(topics != null && topics.equals("")) && !(link != null && link.equals("")) && year != 0 && month != 0 && day != 0)) {
            tv_todo_content.setText(message);
            tv_address.setText(address);
            tv_gmaps_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(i);
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.task_toast_data_from_list_error), Toast.LENGTH_SHORT).show();
        }

        //DELETE BUTTON
        //Finish on success
        Button delButton = (Button) findViewById(R.id.task_bt_dismiss);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sqLiteHelper.deleteTask(currentTask)) {
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.list_toast_task_not_deleted), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
