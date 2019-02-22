package com.example.uberjim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Driver;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DriverWaitingActivity extends AppCompatActivity {

    public TextView mDriver_Name;
    public TextView mDriver_Car;
    public ImageView mDriver_ProfilePic;
    public String mDriver_name;
    public String mDriver_car;

    public float currentLat;
    public float currentLong;
    public float destLat;
    public float destLong;


    public Button mContactDriverButton;
    public Button mCheckFareButton;
    public Button mETAButton;
    public Button mCancelTripButton;
    public Button mSimulateTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_waiting2);

        Bundle extras_from_CustomerMap= getIntent().getExtras();

        mDriver_Name = (TextView) findViewById(R.id.mDriverName);
        if(extras_from_CustomerMap != null) {
            mDriver_name = extras_from_CustomerMap.getString("mDriver_name_");
            mDriver_Name.setText(mDriver_name);
        }
        mDriver_Car = (TextView) findViewById(R.id.mDriverCar);
        if(extras_from_CustomerMap != null) {
            mDriver_car = extras_from_CustomerMap.getString("mDriver_car_");
            mDriver_Car.setText(mDriver_car);
        }

        if(extras_from_CustomerMap != null) {
            currentLat = extras_from_CustomerMap.getFloat("CurrentLatitude");
            currentLong = extras_from_CustomerMap.getFloat("CurrentLongitude");
            destLat = extras_from_CustomerMap.getFloat("DestLatitude");
            destLong = extras_from_CustomerMap.getFloat("DestLatitude");
        }
        mDriver_ProfilePic = findViewById(R.id.DriverProfPic);
        mDriver_ProfilePic.setImageResource(R.drawable.ic_person_black_24dp);
        mContactDriverButton = (Button) findViewById(R.id.contactDriver);
        mContactDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(DriverWaitingActivity.this);
                progress.setTitle("Contacting Driver");
                progress.setMessage("This is a simulated button :)");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                }, 2000);
            }
        });

        mCheckFareButton = (Button) findViewById(R.id.checkFare);
        mCheckFareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(DriverWaitingActivity.this);
                progress.setTitle("Calculating Fare");
                progress.setMessage("Has anyone told you how good you look today?");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCheckFareButton.setText("Fare = " + (get_distance(currentLat,currentLong,destLat,destLong)));
                        progress.dismiss();
                    }
                }, 2000);
            }
        });

        mETAButton = (Button) findViewById(R.id.ETA);
        mETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(DriverWaitingActivity.this);
                progress.setTitle("Calculating ETA");
                progress.setMessage("Has anyone told you how good you look today?");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mETAButton.setText("ETA: " + (get_ETA()));
                        progress.dismiss();
                    }
                }, 2000);
            }
        });

        mCancelTripButton = (Button) findViewById(R.id.cancelTrip);
        mCancelTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(DriverWaitingActivity.this);
                progress.setTitle("Canceling trip");
                progress.setMessage("Please come back again soon :(");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Intent intent = new Intent(DriverWaitingActivity.this, CustomerMapActivity.class);
                        startActivity(intent);
                    }
                }, 2000);
            }
        });

        mSimulateTrip = findViewById(R.id.simulateTrip);
        mSimulateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(DriverWaitingActivity.this);
                progress.setTitle("Simulating trip");
                progress.setMessage("Since this is a pseudo-app... Have you ever been in a car that drives this fast!?");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Intent intent = new Intent(DriverWaitingActivity.this, TripReviewActivity.class);
                        Bundle TripData = new Bundle();
                        TripData.putString("fare", get_distance(currentLat,currentLong,destLat,destLong));
                        TripData.putFloat("distance", get_only_distance(currentLat,currentLong,destLat,destLong));
                        intent.putExtras(TripData);
                        startActivity(intent);
                    }
                }, 2000);
            }
        });
    }
    private String get_distance(float lat_a, float lng_a, float lat_b, float lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;
        Float ans = new Float(distance * meterConversion).floatValue();
        Float flat_fare = new Float(3);
        Float per_km = new Float(0.6);
        ans = (ans*(per_km/1000000)+flat_fare);
        String ans1 = ans.toString();
        ans1 = "£"+ans1;
        ans1 = ans1.substring(0,5);
        return ans1;
    }

    private Float get_only_distance(float lat_a, float lng_a, float lat_b, float lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;
        Float ans = new Float(distance * meterConversion).floatValue();
        return ans;
    }

    private String get_ETA(){
        String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        return timeStamp;
    }
}
