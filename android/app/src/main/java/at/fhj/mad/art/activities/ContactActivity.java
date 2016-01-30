package at.fhj.mad.art.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import at.fhj.mad.art.R;

/**
 * Show ContactActivity where the User can see which persons where part of this project,
 * how to contact these persons and what the Homepage of ITM is
 */
public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        Button btn_email = (Button) findViewById(R.id.contact_btn_email);
        Button btn_homepage = (Button) findViewById(R.id.contact_btn_homepage);

        // Implicit Intents for mailing and displaying a webpage
        btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:florian.mayerhofer@edu.fh-joanneum.at"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ARTv2 Feedback");
                try {
                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.intent_start_through)));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.undefined_error), Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_homepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.fh-joanneum.at/itm";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
