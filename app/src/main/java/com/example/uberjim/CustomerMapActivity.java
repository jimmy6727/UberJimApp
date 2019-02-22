package com.example.uberjim;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mLogout;
    private Button mRequestTrip;
    private String customerId = "";
    private Button mTripConfirm;
    private float rideDistance;
    private double mLatitudeLabel;
    private double mLongitudeLabel;
    private Marker destinationMarker;
    ArrayList mMarkerPoints;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    String[] driver_names =  {"Driver Kevin", "Driver Juan", "Driver Jack", "Driver Evan", "Driver Jimmy", "Driver Emma"};
    String[] driver_cars = {"Red Nissan Altima", "White Volkswagen Golf", "Black Audi TT300", "Red BMW i8", "Silver Ferrari 458", "Silver Mercedes C800"};
    LatLng[] driver_locations = {new LatLng (56.338,-2.796), new LatLng (56.342,-2.793), new LatLng (56.340,-2.792), new LatLng (56.339,-2.790), new LatLng (56.339,-2.796), new LatLng (56.337,-2.797), };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Define Location Client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Logout Button logic
        mLogout = (Button) findViewById(R.id.logout);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        // Request Trip Button and Confirm Trip Button
        mRequestTrip = (Button) findViewById(R.id.request);
        mTripConfirm = (Button) findViewById(R.id.ConfirmTrip);

        mRequestTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CustomerMapActivity.this, "" +
                        "Hold and drag the marker to destination",Toast.LENGTH_LONG).show();
                destinationMarker = mMap.addMarker(new MarkerOptions().title("Hold and drag to destination").position(new LatLng (56.340,-2.8)));
                destinationMarker.setDraggable(true);

                mTripConfirm.setVisibility(View.VISIBLE);
            }
        });
        // Initializing
        mMarkerPoints = new ArrayList();

        mTripConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getLastLocation();
                FragmentManager fm = getSupportFragmentManager();
                    mMarkerPoints.clear();
                    LatLng startPoint = new LatLng(mLatitudeLabel, mLongitudeLabel);
                    drawMarker(startPoint);
                    final LatLng dest = new LatLng(destinationMarker.getPosition().latitude, destinationMarker.getPosition().longitude);


                drawMarker(dest);
                mMarkerPoints.add(0, startPoint);
                mMarkerPoints.add(1, dest);

                final String[] mDriver = get_closest_driver(driver_names, driver_cars, driver_locations);
                String mDriver_name_ = new String();
                String mDriver_car_ = new String();

                final ProgressDialog progress = new ProgressDialog(CustomerMapActivity.this);
                progress.setTitle("Routing and Contacting Driver");
                progress.setMessage(mDriver[0] + " is the closest driver to you.");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                // Wait 1 second to simulate routing
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Intent intent = new Intent(CustomerMapActivity.this, DriverWaitingActivity.class);
                        Bundle DriverData = new Bundle();
                        DriverData.putString("mDriver_name_", mDriver[0]);
                        DriverData.putString("mDriver_car_", mDriver[1]);
                        DriverData.putFloat("CurrentLatitude", convertToFloat(mLatitudeLabel));
                        DriverData.putFloat("CurrentLongitude", convertToFloat(mLongitudeLabel));
                        DriverData.putFloat("DestLatitude", convertToFloat(dest.latitude));
                        DriverData.putFloat("DestLongitude", convertToFloat(dest.longitude));
                        intent.putExtras(DriverData);
                        startActivity(intent);
                    }
                }, 3000);
            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.DriverTestMenu);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Handles the event when the user clicks one of the driver names.
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        // specify an adapter for recyclerView that is the driver menu
        mAdapter = new MyAdapter(driver_names);
        recyclerView.setAdapter(mAdapter);
    }

    public static Float convertToFloat(double doubleValue) {
        return (float) doubleValue;
    }

    private void drawMarker(LatLng point){
        mMarkerPoints.add(point);

// Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

// Setting the position of the marker
        options.position(point);

/**
 * For the start location, the color of marker is GREEN and
 * for the end location, the color of marker is RED.
 */
        if(mMarkerPoints.size()==1){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }else if(mMarkerPoints.size()==2){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

// Add new marker to the Google Map Android API V2
        mMap.addMarker(options);
    }


    //Click Listener Implementation for driver menu
    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
            void onLongItemClick(View view, int position);
        }

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && mListener != null) {
                        mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

        @Override
        public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.myExtendedViewHolder> {
        private String[] mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public LinearLayout myLinearLayout;
            public MyViewHolder(LinearLayout v) {
                super(v);
                myLinearLayout = v;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(String[] myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.myExtendedViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_view_item_1, parent, false);
            myExtendedViewHolder vh = new myExtendedViewHolder(v);
            return vh;
        }

        public class myExtendedViewHolder extends RecyclerView.ViewHolder
        {
            TextView driver_name;
            TextView driver_description;
            ImageView driver_image;

            public myExtendedViewHolder(View itemView)
            {
                super(itemView);
                driver_image = itemView.findViewById(R.id.driver_profile_pic);
                driver_name = itemView.findViewById(R.id.DriverNametextView1);
                driver_description = itemView.findViewById(R.id.DriverDescriptiontextView2);
            }
        }
        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(myExtendedViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
                holder.driver_name.setText(driver_names[position]);
                holder.driver_description.setText(driver_cars[position]);
                holder.driver_image.setImageResource(R.drawable.ic_local_taxi_black_24dp);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }

    }


    // Function to get last location
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            mLatitudeLabel = mLastLocation.getLatitude();
                            mLongitudeLabel = mLastLocation.getLongitude();
                        }
                    }
                });
    }

    private float get_distance(double lat_a, double lng_a, double lat_b, double lng_b )
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

        return new Float(distance * meterConversion).floatValue();
    }

    // Function to calculate closest driver
    private String[] get_closest_driver(String[] pdriver_names, String[] pdriver_cars, LatLng[] pdriver_locations) {

        float shortest = 1000000; //A huge number to start with
        int indx = 0;
        for(int i = 0; i<pdriver_names.length; i++){
            float dis = get_distance(pdriver_locations[i].latitude, pdriver_locations[i].longitude, mLatitudeLabel, mLongitudeLabel);
            if(dis < shortest){
                shortest = dis;
                indx=i;
            }
        }
        String[] driver_info;
        driver_info = new String[]{pdriver_names[indx], pdriver_cars[indx]};
        return driver_info;
    }


    public void centerMapOnCurrentLocation(GoogleMap googleMap){
        mMap = googleMap;
        getLastLocation();
        LatLng myLocation = new LatLng(mLatitudeLabel,mLongitudeLabel);
        mMap.addMarker(new MarkerOptions().position(myLocation).visible(false));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(myLocation).zoom(14).build()));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_local_taxi_black_24dp);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void showDriversAvailable(GoogleMap googleMap){
        mMap = googleMap;
        for(int position = 0; position < driver_names.length; position = position+1){
            mMap.addMarker(new MarkerOptions().position(driver_locations[position]).title(driver_names[position]).icon(bitmapDescriptorFromVector(this, R.drawable.ic_local_taxi_black_24dp)));
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        onResume(mMap);
    }

    protected void onResume(GoogleMap googleMap){
        mMap = googleMap;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();

            } else {
                checkLocationPermission();
            }
        }

    }
// ..


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng pos = marker.getPosition();
                marker.setPosition(pos);
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }
        });

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                getLastLocation();
                centerMapOnCurrentLocation(mMap);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng (56.337,-2.797)));
                showDriversAvailable(mMap);
            } else {
                checkLocationPermission();
            }
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    if (!customerId.equals("") && mLastLocation != null && location != null) {
                        rideDistance += mLastLocation.distanceTo(location) / 1000;
                    }
                    mLastLocation = location;


                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

                }
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        assert(mLocationRequest != null);
                        assert(mLocationCallback != null);
                        assert(Looper.myLooper() != null);
                        //mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                        getLastLocation();
                        centerMapOnCurrentLocation(mMap);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

}