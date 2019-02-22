package com.example.uberjim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TripReviewActivity extends AppCompatActivity {

    private TextView mGiveRating;
    private TextView mTripCostView;
    private TextView mTripDistanceView;
    private Button mSubmitReview;
    private RatingBar mRatingBar;
    private Float mTripDistance;
    private String mTripCost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_review);

        Bundle extras_from_DriverWaiting= getIntent().getExtras();

        if(extras_from_DriverWaiting != null) {
            mTripCost = extras_from_DriverWaiting.getString("fare");
            mTripDistance = extras_from_DriverWaiting.getFloat("distance");
        }

        mGiveRating = findViewById(R.id.giveReviewTitle);
        mGiveRating.setText("Give your driver a rating:");

        mTripCostView = findViewById(R.id.tripcost);
        mTripCostView.setText("Total trip cost: "+ mTripCost);

        mTripDistanceView = findViewById(R.id.tripdistance);
        mTripDistanceView.setText("Total trip distance: "+(mTripDistance/1000000)+" km");

        mRatingBar = findViewById(R.id.rating);

        mSubmitReview = findViewById(R.id.submitReview);
        mSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(TripReviewActivity.this);
                progress.setTitle("Submitting Review");
                progress.setMessage("Returning to home screen");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Intent intent = new Intent(TripReviewActivity.this, CustomerMapActivity.class);
                        startActivity(intent);
                    }
                }, 3000);
            }
        });
    }
}
