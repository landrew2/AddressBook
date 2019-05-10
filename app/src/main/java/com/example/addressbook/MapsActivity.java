package com.example.addressbook;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener {
    private final int LOCATION_PERMISSION_REQUEST = 99;


    private GoogleMap mMap;

    private LocationManager mMyLocationManager;
    private LatLng mLastLocation;

    private Handler mMyHandler;

    //True is using locations, false is using long\lat
    private boolean mLocationToggle;
    private FloatingActionButton mLocationToggleButton;

    private FirebaseDatabase mMyDB;
    private DatabaseReference mDBRef;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


  /*
        Button button = (Button) findViewById(R.id.signout);
        mAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                }
            }
        };
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });
    }
*/


        mLocationToggleButton = (FloatingActionButton) findViewById(R.id.locationToggle);
        mLocationToggleButton.setOnClickListener(this);

        mMyLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mMyHandler = new Handler();

        mAuth = FirebaseAuth.getInstance();
        mMyDB = FirebaseDatabase.getInstance();
        mDBRef = mMyDB.getReference();
        mDBRef.child("Users");

    }
    @Override
    protected void onPause() {
        super.onPause();
        mMyLocationManager.removeUpdates(this);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // gets permissions if it doesn't have them
            Toast.makeText(MapsActivity.this,
                    "Insufficient Permissions", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
        mMyLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay we can move on!
                    return;

                } else {
                    // permission denied, boo!
                    Toast.makeText(MapsActivity.this,
                            "You should enable GPS for full functionality", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);

        mLastLocation = sydney;
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
           case R.id.locationToggle:
                mapToggle();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String id = mAuth.getCurrentUser().getUid();

        mLastLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mDBRef.child(id).child("LocationLat").setValue(location.getLatitude());
        mDBRef.child(id).child("LocationLong").setValue(location.getLongitude());

        mDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,LatLng> locationList= new HashMap<>();
                User tmp;
                LatLng coords  = null;

                //using the address
                if (mLocationToggle){
                    for (DataSnapshot user: dataSnapshot.getChildren()) {
                        tmp = user.getValue(User.class);
                        String address = tmp.Address;
                        coords = getLocationFromAddress(getApplicationContext(),address);
                        if (coords != null){
                            locationList.put(tmp.Name,coords);
                        }
                    }
                }

                //using the locations
                else{
                    for (DataSnapshot user: dataSnapshot.getChildren()){
                        tmp = user.getValue(User.class);
                        if (!tmp.LocationLat.isEmpty() && !tmp.LocationLong.isEmpty()){
                            coords = new LatLng(parseDouble(tmp.LocationLat),parseDouble(tmp.LocationLong));
                            locationList.put(tmp.Name,coords);
                        }

                    }
                }

                //TODO once login is done we can use the username to get it right out of the db
                UpdateMap mapUpdate = new UpdateMap(locationList,mLastLocation);
                mMyHandler.post(mapUpdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void mapToggle(){
        mDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,LatLng> locationList= new HashMap<>();
                User tmp;
                LatLng coords  = null;
                mLocationToggle = !mLocationToggle;
                //using the address
                if (mLocationToggle){
                    for (DataSnapshot user: dataSnapshot.getChildren()) {
                        tmp = user.getValue(User.class);
                        String address = tmp.Address;
                        coords = getLocationFromAddress(getApplicationContext(),address);
                        if (coords != null){
                            locationList.put(tmp.Name,coords);
                        }
                    }
                }

                //using the locations
                else{
                    for (DataSnapshot user: dataSnapshot.getChildren()){
                        tmp = user.getValue(User.class);
                        if (!tmp.LocationLat.isEmpty() && !tmp.LocationLong.isEmpty()){
                            coords = new LatLng(parseDouble(tmp.LocationLat),parseDouble(tmp.LocationLong));
                            locationList.put(tmp.Name,coords);
                        }

                    }
                }

                //TODO once login is done we can use the username to get it right out of the db
                UpdateMap mapUpdate = new UpdateMap(locationList,mLastLocation);
                mMyHandler.post(mapUpdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private class UpdateMap implements Runnable {
        Map<String,LatLng>  mLocationList;
        LatLng mMyLocation;

        public UpdateMap(Map<String,LatLng> locations, LatLng myLocation) {
            mLocationList.putAll(locations);
            mMyLocation = myLocation;
        }

        @Override
        public void run() {
            LatLng location;
            mMap.clear();
            for (String name: mLocationList.keySet()){
                location = mLocationList.get(name);

                mMap.addMarker(new MarkerOptions().position(location).title(name));
            }
            //mMap.addMarker(new MarkerOptions().position(mMyLocation).title("My location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mMyLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }

    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng locationLatLong = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            locationLatLong = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return locationLatLong;
    }
}
