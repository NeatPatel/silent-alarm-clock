package com.example.winterbreak2023app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //For eventList, the first parameter is eventDescription and second is eventName
    private ArrayList<String[]> eventList = new ArrayList<>();
    final private EventDataBase db = new EventDataBase(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        handleViews();
    }

    public void initialize() {
        eventList = db.getAllEvents();
    }

    public void handleViews() {
        for (int i = 0; i < eventList.size(); i++) {
            //Take event list and add CardViews for every item in eventList
            LinearLayout cardLayout = findViewById(R.id.card_layout);
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.topMargin = 36;
            } else {
                params.topMargin = 0;
            }
            cardView.setLayoutParams(params);

            LinearLayout innerCardView = new LinearLayout(this);
            innerCardView.setOrientation(LinearLayout.VERTICAL);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            innerCardView.setLayoutParams(params);

            LinearLayout innerTitleLayout = new LinearLayout(this);
            innerTitleLayout.setOrientation(LinearLayout.HORIZONTAL);
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            innerTitleLayout.setLayoutParams(params);

            createView(innerCardView, innerTitleLayout, eventList.get(i));
            cardView.addView(innerCardView);
            cardLayout.addView(cardView);
        }

        checkEmptyList();
    }

    public void checkEmptyList() {
        if(eventList.size() == 0) {
            LinearLayout cardLayout = findViewById(R.id.card_layout);
            TextView noEventsYet = new TextView(this);
            String noEventsText = "There are no events yet";
            noEventsYet.setText(noEventsText);
            noEventsYet.setTextSize(20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 100;
            noEventsYet.setLayoutParams(params);
            cardLayout.addView(noEventsYet);
        }
    }

    public void createEventClicked(View v) {
        //Run the create event activity

        Intent createEvent = new Intent(MainActivity.this, CreateEvent.class);
        Bundle bundle = new Bundle();
        for (int i = 0; i < eventList.size(); i++) {
            bundle.putStringArray("KEY_eventList_" + i, eventList.get(i));
        }
        bundle.putInt("KEY_eventList_size", eventList.size());
        createEvent.putExtras(bundle);
        startActivity(createEvent);
    }

    public void createView(LinearLayout innerCardLayout, LinearLayout innerTitleLayout, String[] event) {
        //Create a Card displaying event name and description/items

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView eventDescription = new TextView(this);

        TextView eventName = new TextView(this);
        String message = "Event: " + event[1];
        eventName.setText(message);
        eventName.setTextSize(24);
        params.weight = 0.9f;
        eventName.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        message = "Event Description:\n" + event[0];
        eventDescription.setText(message);
        eventDescription.setTextSize(14);
        params.topMargin = 10;
        eventDescription.setLayoutParams(params);

        message = "Delete";
        Button delButton = new Button(this);
        delButton.setText(message);
        delButton.setTextSize(14);
        delButton.setOnClickListener(this::deleteView);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 0.1f;
        delButton.setLayoutParams(params);

        innerTitleLayout.addView(eventName);
        innerTitleLayout.addView(delButton);
        innerCardLayout.addView(innerTitleLayout);
        innerCardLayout.addView(eventDescription);
    }

    public void deleteView(View v) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                LinearLayout cardLayout = findViewById(R.id.card_layout);
                for (int i = 0; i < cardLayout.getChildCount(); i++) {
                    if (cardLayout.getChildAt(i) == v.getParent().getParent().getParent()) {
                        if (db.deleteOne(eventList.get(i)[1], eventList.get(i)[0])) {
                            eventList.remove(i);
                            cardLayout.removeView(cardLayout.getChildAt(i));
                        }
                        break;
                    }
                }

                checkEmptyList();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage("Are you sure you want to delete this event?").setPositiveButton("Delete", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }
}