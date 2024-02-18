package com.example.winterbreak2023app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateEvent extends AppCompatActivity {

    //For eventList, the first parameter is eventDescription and second is eventName
    private ArrayList<String[]> eventList = new ArrayList<>();
    final private EventDataBase db = new EventDataBase(CreateEvent.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        LinearLayout customEventLayout = findViewById(R.id.custom_event);
        customEventLayout.setVisibility(View.GONE);

        initialize();
    }

    public void initialize() {
        eventList = db.getAllEvents();
    }

    public void customEvent(View v) {
        RadioButton customEventRadio = findViewById(R.id.custom_event_radio);
        LinearLayout customEventLayout = findViewById(R.id.custom_event);
        LinearLayout regularEventLayout = findViewById(R.id.regular_event);

        if(customEventRadio.isChecked()) {
            customEventLayout.setVisibility(View.VISIBLE);
            regularEventLayout.setVisibility(View.GONE);
        } else {
            customEventLayout.setVisibility(View.GONE);
            regularEventLayout.setVisibility(View.VISIBLE);
        }
    }

    public void submitEvent(View v) {
        RadioButton customEventRadio = findViewById(R.id.custom_event_radio);
        if(customEventRadio.isChecked()) {

            EditText eventDescription = findViewById(R.id.event_description_multi);
            EditText eventName = findViewById(R.id.event_name);
            String eventNameText = String.valueOf(eventName.getText());
            String eventDescriptionText = String.valueOf(eventDescription.getText());

            if(!eventDescriptionText.equals("") && !eventNameText.equals("") && isUnique(eventNameText, eventDescriptionText)) {
                Intent emailClients = new Intent(CreateEvent.this, EmailClients.class);
                Bundle bundle = new Bundle();
                String[] newEvent = {eventDescriptionText, eventNameText};
                if(db.addOne(eventNameText, eventDescriptionText)) {
                    eventList.add(newEvent);
                }

                for(int i = 0; i < eventList.size(); i++) {
                    bundle.putStringArray("KEY_eventList_" + i, eventList.get(i));
                }

                bundle.putInt("KEY_eventList_size", eventList.size());
                bundle.putStringArray("KEY_email_event", eventList.get(eventList.size() - 1));
                emailClients.putExtras(bundle);
                startActivity(emailClients);
            } else {
                Toast.makeText(this, "Please type UNIQUE event Name and Description", Toast.LENGTH_SHORT).show();
              }
        } else {
            EditText eventDescription = findViewById(R.id.event_description_multi_regular);
            EditText eventName = findViewById(R.id.event_name);
            EditText eventDate = findViewById(R.id.event_day);
            String eventNameText = String.valueOf(eventName.getText());
            String eventDescriptionText = String.valueOf(eventDescription.getText());
            String eventDateText = String.valueOf(eventDate.getText());

            String message = eventDescriptionText + "\nDate: " + eventDateText;

            if(!eventDescriptionText.equals("") && !eventNameText.equals("") && !eventDateText.equals("") && isUnique(eventNameText, message)) {
                //Intent mainActivity = new Intent(CreateEvent.this, MainActivity.class);

                Intent emailClients = new Intent(CreateEvent.this, EmailClients.class);
                Bundle bundle = new Bundle();
                String[] newEvent = {message, eventNameText};
                if (db.addOne(eventNameText, eventDescriptionText)) {
                    eventList.add(newEvent);
                }

                for(int i = 0; i < eventList.size(); i++) {
                    bundle.putStringArray("KEY_eventList_" + i, eventList.get(i));
                }

                bundle.putInt("KEY_eventList_size", eventList.size());
                bundle.putStringArray("KEY_email_event", eventList.get(eventList.size() - 1));
                emailClients.putExtras(bundle);
                startActivity(emailClients);
        } else {
                Toast.makeText(this, "Please type UNIQUE event Name, Date, and Description", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isUnique(String eventName, String eventDescription) {
        String[] checkEvent = {eventDescription, eventName};
        for(int i = 0; i < eventList.size(); i++) {
            if(Arrays.equals(checkEvent, eventList.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void previewEvent(View v) {
        RadioButton customEventRadio = findViewById(R.id.custom_event_radio);

        if(customEventRadio.isChecked()) {
            EditText eventDescription = findViewById(R.id.event_description_multi);
            String eventDescriptionText = String.valueOf(eventDescription.getText());
            TextView tempText = findViewById(R.id.event_preview);

            if(!eventDescriptionText.equals("")) {
                tempText.setText(eventDescriptionText);
            } else {
                Toast.makeText(this, "Please type event Description", Toast.LENGTH_SHORT).show();
            }
        } else {
            EditText eventDescription = findViewById(R.id.event_description_multi_regular);
            EditText eventDate = findViewById(R.id.event_day);
            String eventDescriptionText = String.valueOf(eventDescription.getText());
            String eventDateText = String.valueOf(eventDate.getText());
            TextView tempText = findViewById(R.id.event_preview);

            if(!eventDescriptionText.equals("") && !eventDateText.equals("")) {
                String message = eventDescriptionText + "\nDate: " + eventDateText;
                tempText.setText(message);
            } else {
                Toast.makeText(this, "Please type event Description and Date", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cancelCreateEvent(View v) {
        Intent mainActivity = new Intent(CreateEvent.this, MainActivity.class);
        Bundle bundle = new Bundle();

        for(int i = 0; i < eventList.size(); i++) {
            bundle.putStringArray("KEY_eventList_" + i, eventList.get(i));
        }

        bundle.putInt("KEY_eventList_size", eventList.size());
        mainActivity.putExtras(bundle);
        startActivity(mainActivity);
    }
}