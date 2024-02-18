package com.example.winterbreak2023app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class EmailClients extends AppCompatActivity {

    private ArrayList<String[]> eventList = new ArrayList<>();
    final private EventDataBase db = new EventDataBase(EmailClients.this);
    private String[] emailEvent = new String[]{};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_clients);

        initialize();
    }

    public void initialize() {
        if(getIntent() != null) {
            Intent contentReceived = getIntent();
            Bundle bundle = contentReceived.getExtras();

            if(bundle != null) {
                emailEvent = bundle.getStringArray("KEY_email_event");
            }
        }
        eventList = db.getAllEvents();
    }

    public void sendEmail(View v) {
        EditText emailBox = findViewById(R.id.manyMailBox);

        if(!String.valueOf(emailBox.getText()).equals("")) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");

            String eventName = emailEvent[1];
            String eventDescription = emailEvent[0];
            String[] emails = String.valueOf(emailBox.getText()).split(",", 0);

            emailIntent.putExtra(Intent.EXTRA_EMAIL, emails);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Event Email Notification");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello there!\nYou are being notified about the following Event: " + eventName + "\nThis event has the following description:\n" + eventDescription);

            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter at least one email address", Toast.LENGTH_SHORT).show();
        }
    }

    public void noEmails(View v) {
        Intent mainActivity = new Intent(EmailClients.this, MainActivity.class);
        Bundle bundle = new Bundle();

        for(int i = 0; i < eventList.size(); i++) {
            bundle.putStringArray("KEY_eventList_" + i, eventList.get(i));
        }

        bundle.putInt("KEY_eventList_size", eventList.size());
        mainActivity.putExtras(bundle);
        startActivity(mainActivity);
    }
}