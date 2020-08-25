package com.example.uber_taxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    ProgressDialog dialog;

    FirebaseUser user;
    FirebaseDatabase mdriver;

    Button logout,EndRide;
    private String custId = "";

    FloatingActionButton profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MainActivity.textToSpeech.speak("Welcome to the Uber Driver application.", TextToSpeech.QUEUE_FLUSH,null);


        logout = (Button) findViewById(R.id.logoutBtn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
                return;
            }
        });

//        profile = (FloatingActionButton)findViewById(R.id.menu);
//        profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(DriverMapActivity.this,Menu.class);
//                startActivity(intent);
//            }
//        });

        dialog = new ProgressDialog(DriverMapActivity.this);

        EndRide =(Button)findViewById(R.id.endRide);
        EndRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endRide();
            }
        });

        getAssignedCustomer();
    }

    public void endRide(){
        dialog.setMessage("Ending ride please wait....");
        dialog.show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("BusyDriver").child(user.getUid());//removing the value from the database
        myRef.removeValue();

        dialog.dismiss();
//        Intent i1 = new Intent(DriverMapActivity.this,MainActivity.class);
//        startActivity(i1);


    }
    private void getAssignedCustomer(){

        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCust = FirebaseDatabase.getInstance().getReference("UserType").child("Drivers").child(driverId).child("RequestedCustomerId");
        assignedCust.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                   custId = dataSnapshot.getValue().toString();
                   getAssignedCustomerPickUpLocation();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getAssignedCustomerPickUpLocation(){

        DatabaseReference custPickUpLocationRef = FirebaseDatabase.getInstance().getReference("RequestForUber").child(custId).child("l");
        custPickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    List<Object> custLocation = (List<Object>) dataSnapshot.getValue();

                    double latitudeValue = 0;
                    double longitutdeValue = 0;

                    if(custLocation.get(0)!=null){
                        latitudeValue = Double.parseDouble(custLocation.get(0).toString());
                    }

                    if(custLocation.get(1)!=null){
                        longitutdeValue = Double.parseDouble(custLocation.get(1).toString());
                    }

                    LatLng pickupLocation = new LatLng(latitudeValue,longitutdeValue);

                    mMap.addMarker(new MarkerOptions().position(pickupLocation).title("PickUp Here"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient(){


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

//        moving the map/ camera in the same pace in which the user moves so that user always stays in the center of the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //ITS VALUE RANGES FROM (1-21) you can play around with these indices i.e 11

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverAvailableRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers");
            DatabaseReference driverBusyRef = FirebaseDatabase.getInstance().getReference("BusyDriver");

            //geogire automatically , in its own way puts value in the database
            GeoFire geoFireAvailable = new GeoFire(driverAvailableRef);
            GeoFire geoFireBusy = new GeoFire(driverBusyRef);


            switch (custId) {

                case "":
                    //if no customer id in driver then it means driver is not busy he is available
                    //So removing driver location from BusyDriver table
                        geoFireBusy.removeLocation(userId, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    Toast.makeText(DriverMapActivity.this, "Can't go Active", Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(DriverMapActivity.this, "You are Active", Toast.LENGTH_SHORT).show();
                            }
                        });
                    //Since driver is available so setting the driver location
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Toast.makeText(DriverMapActivity.this, "Can't go Active", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(DriverMapActivity.this, "You are Active", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                default:
                    //if customer id is present in driver table then it means driver is busy he is not available
                    //So removing driver location from AvailableDriver table
                    geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Toast.makeText(DriverMapActivity.this, "Can't go Active", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(DriverMapActivity.this, "You are Active", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //Since driver is available so setting the driver location
                    geoFireBusy.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Toast.makeText(DriverMapActivity.this, "Can't go Active", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(DriverMapActivity.this, "You are Active", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

            }
        }
        else{
            //do nothing
        }

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //when the map is connected and is ready to get started

        //getting location second to second

        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(1000); //1000 = 1second
//        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //This helps in getting the best location accuracy that mobile can get

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //This onStop() method is when the user closes the application then SUCH drives are regarded as inactive driver and removing its location values

    @Override
    protected void onStop() {
        super.onStop();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AvailableDrivers");

            //geogire automatically , in its own way puts value in the database
            GeoFire geoFire = new GeoFire(ref);

            //removing the location of this particular driver from the database when he goes out of the activity
            geoFire.removeLocation(userId);
        }
    }
}
