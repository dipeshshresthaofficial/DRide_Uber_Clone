package com.example.uber_taxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    //Button logout,requestUber;
    FloatingActionButton requestUber,profile;

    private LatLng pickUpLocation;

    public static float currentUserLatitude,currentUserLongitude,destinationLatitude,destinationLongitude;

    private int radius =1;
    private boolean driverFound= false;
    private String driverFoundId;
    public float price;

    public static boolean isclicked;

    public static SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MainActivity.textToSpeech.speak("Welcome to the Uber ride.", TextToSpeech.QUEUE_FLUSH,null);

        profile = (FloatingActionButton)findViewById(R.id.profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMapActivity.this,Menu.class);
                startActivity(intent);
            }
        });

        requestUber = (FloatingActionButton) findViewById(R.id.requestUberBtn);
        requestUber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirebaseAuth.getInstance().getCurrentUser()!=null && isclicked){


                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RequestForUber");

                    //geogire automatically , in its own way puts value in the database
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error!=null)
                            {
                                Toast.makeText(UserMapActivity.this,"Geo Location error in MapActivity of either user or driver",Toast.LENGTH_SHORT).show();
                            }
                            //No Error
                        }
                    });

                    pickUpLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("PickUp Here"));

                    //displaying the cost the user have to pay for the ride
                    dothis();

                    //requestUber.setText("Finding a Ride For You.....");


                    //findNearestDriverForTheUser();
                }
                else{
                    //do nothing
                    Toast.makeText(UserMapActivity.this,"please choose the location you want to go",Toast.LENGTH_SHORT).show();
                    MainActivity.textToSpeech.speak("Please choose the location you want to go.", TextToSpeech.QUEUE_FLUSH,null);
                }
            }
        });

        searchView = (SearchView)findViewById(R.id.togo);
        //listening the text query in the search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = searchView.getQuery().toString();//getting what user enters
                List<Address> addressList = null;
                if(location!=null || location!=""){

                    Geocoder geocoder = new Geocoder(UserMapActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("this is where you want to go"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    float zoomto =  16.0f;
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(zoomto));

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }

    private void findNearestDriverForTheUser(){

        DatabaseReference allTheAvailableDrivers = FirebaseDatabase.getInstance().getReference("AvailableDrivers");

        GeoFire geoFire = new GeoFire(allTheAvailableDrivers);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude,pickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //check if the driver is available or not true means available
                if(!driverFound){
                    driverFound = true;
                    driverFoundId = key;

                    //storing the customer id as the child of found drivers in db

                    DatabaseReference driver = FirebaseDatabase.getInstance().getReference("UserType").child("Drivers").child(driverFoundId);
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap hmap = new HashMap();
                    hmap.put("RequestedCustomerId",userId);

                    driver.updateChildren(hmap);


                    System.out.println("hello 0");
                    getDriverLocation();
                    //requestUber.setText("Finding Driver Location.....");





//                    DatabaseReference driverFoundLocation= FirebaseDatabase.getInstance().getReference("AvailableDrivers").child(driverFoundId).child("l");
//                    LatLng driverLocation = new LatLng(driverFoundLocation.getKey(0),mLastLocation.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("PickUp Here"));
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                //if driver not found in current radius then increasing the radius

                if(!driverFound){
                    radius++;
                    if(radius<=5){
                        findNearestDriverForTheUser();
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private Marker driverMarker;

    private void getDriverLocation(){
        System.out.println("helow 1");

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference("BusyDriver").child(driverFoundId).child("l");
        driverLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //This function will be called for every change in location
                System.out.println("helow 2");
                if(dataSnapshot.exists()){

                    //requestUber.setText("Driver Found");

                    List<Object> location = (List<Object>) dataSnapshot.getValue();

                    System.out.println("helow 3");
                    double latitudeValue = 0;
                    double longitudeValue = 0;
                    if(location.get(0)!=null){
                        latitudeValue = Double.parseDouble(location.get(0).toString());


                    }
                    if(location.get(1)!=null){
                        longitudeValue = Double.parseDouble(location.get(1).toString());
                    }

                    System.out.println("************************************** "+location.get(0).toString()+" "+location.get(1).toString());
                    Toast.makeText(UserMapActivity.this, " "+latitudeValue+" "+longitudeValue,Toast.LENGTH_SHORT).show();

                    LatLng driverLocation = new LatLng(latitudeValue,longitudeValue);

                    if (driverMarker!=null){
                        driverMarker.remove();
                    }

                    Location cLocation = new Location("");
                    cLocation.setLatitude(pickUpLocation.latitude);
                    cLocation.setLongitude(pickUpLocation.longitude);

                    Location dLocation = new Location("");
                    dLocation.setLatitude(driverLocation.latitude);
                    dLocation.setLongitude(driverLocation.longitude);

                    //calculating the distance between driver and user

                    float distance = cLocation.distanceTo(dLocation);

                    //requestUber.setText("Driver at: "+String.valueOf(distance));
                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_directions_car_black_24dp)).title("Your Driver is Here"));

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


        //to zoom to our current location
        float zoomLevel = 16.0f; //This goes up to 21
        //to set on click listener in map
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                mMap.clear();
                isclicked= true;
                float zoomLevel = 16.0f; //This goes up to 21
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                LatLng object1  = new LatLng(latLng.latitude, latLng.longitude);

                destinationLatitude = (float) latLng.latitude;
                destinationLongitude = (float) latLng.longitude;



                //adding our own icon in the map
                googleMap.addMarker(new MarkerOptions().position(object1)
                        .title("This is where you want to go."));

                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                googleMap.addMarker(markerOptions);


                //to get distance between two points
                float result[] =new float[10];
                Location.distanceBetween(currentUserLatitude,currentUserLongitude,destinationLatitude,destinationLongitude,result);//show distance in meters
                markerOptions.snippet("Distance is = "+result[0]);//result[0] is in meter

                price = (result[0]/1000)*7;//1km distance travelled is equal to Rs 7

                mMap.addMarker(markerOptions);
                System.out.println("the distance is "+result[0]);
                googleMap.addMarker(new MarkerOptions().position(object1)).setTitle("distance is "+result[0]);
                Toast.makeText(UserMapActivity.this,"the distance between two points is "+result[0],Toast.LENGTH_SHORT).show();
                MainActivity.textToSpeech.speak("The distance you are going to travel is "+result[0], TextToSpeech.QUEUE_FLUSH,null);


            }
        });

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

        currentUserLatitude = (float) location.getLatitude();
        currentUserLongitude = (float) location.getLongitude();

//        moving the map/ camera in the same pace in which the user moves so that user always stays in the center of the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //ITS VALUE RANGES FROM (1-21) you can play around with these indices i.e 11

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

    //to add out own marker in the google map application

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){

        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    //function to show alert dialog
    public void dothis(){

        final AlertDialog.Builder alert = new AlertDialog.Builder((UserMapActivity.this));
        View mview = getLayoutInflater().inflate(R.layout.layout_dialog,null); //getting the layout dialog that we have created for dialog

        final TextView text = (TextView)mview.findViewById(R.id.texto);
        text.setText("YOU HAVE TO PAY "+price);
        MainActivity.textToSpeech.speak("YOU HAVE TO PAY "+price, TextToSpeech.QUEUE_FLUSH,null);
        Button cancel = (Button)mview.findViewById(R.id.cancel);
        Button ok = (Button)mview.findViewById(R.id.fine);

        alert.setView(mview);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.textToSpeech.speak("YOUR ride is cancelled", TextToSpeech.QUEUE_FLUSH,null);
                alertDialog.dismiss();//removing the dialog

            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.textToSpeech.speak("Finding nearest ride for you.", TextToSpeech.QUEUE_FLUSH,null);
                findNearestDriverForTheUser();
                //displayNotification();
                Toast.makeText(UserMapActivity.this,"please click in the notification for further operation",Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();//removing the dialog
                //
            }
        });

        alertDialog.show();
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


    }
}

