package com.example.addressbook;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import static java.lang.Double.parseDouble;

public class UserProfileActivity extends Navigation {

    FirebaseAuth mAuth2;
    DatabaseReference userref;
    Button updateButton;
    EditText Name_;
    EditText PhoneNum_;
    EditText Address_;
    EditText Email_;
    TextView location_;
    String currentUserEmail;
    String locationLat;
    String locationLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putInt("ContentView",R.layout.activity_user_profile_with_navigation);
        super.onCreate(savedInstanceState);

        userref = FirebaseDatabase.getInstance().getReference();

        Name_ = (EditText)findViewById(R.id.editTextName);
        PhoneNum_ = (EditText)findViewById(R.id.editTextPhoneNum);
        Address_ = (EditText)findViewById(R.id.editTextAddress);
        location_ = (TextView)findViewById(R.id.textViewLocation);
        Email_ = (EditText)findViewById(R.id.editTextEmail);

        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
        }
        });

        mAuth2 = FirebaseAuth.getInstance();

        // get current user login email and display
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
            Email_.setText(currentUserEmail);
        } else {
            // optional to show warning message of user does not exist
        }

        // get current user and display their current info in firebase
        String uid = mAuth2.getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(uid);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationLat = (String) dataSnapshot.child("LocationLat").getValue();
                locationLong = (String) dataSnapshot.child("LocationLong").getValue();
                String name_fb = (String) dataSnapshot.child("Name").getValue();
                String phone_fb = (String) dataSnapshot.child("PhoneNum").getValue();
                String address_fb = (String) dataSnapshot.child("Address").getValue();


                Name_.setText(name_fb);
                PhoneNum_.setText(phone_fb);
                Address_.setText(address_fb);

                String tvLocation = ("Lat: " + locationLat + "   " + "Long: " + locationLong);
                location_.setText(tvLocation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        myRef.addListenerForSingleValueEvent(eventListener);




    }

    private void updateData() {

        String name  = Name_.getText().toString();
        String address = Address_.getText().toString();
        String phonenum = PhoneNum_.getText().toString();
        currentUserEmail = Email_.getText().toString();

        // if the strings are not empty, update Data
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(phonenum)) {

            String id = mAuth2.getCurrentUser().getUid();
            //LatLng location = getLocationFromAddress(getApplicationContext(), address);
            /*if(location == null)
            {
                location = new LatLng(-34,151);
            }
            */
            //User user = new User(name, address, String.valueOf(location.latitude), String.valueOf(location.longitude), phonenum, currentUserEmail);


            String tvLocation = ("Lat: " + locationLat + "   " + "Long: " + locationLong);
            location_.setText(tvLocation);

            //userref.child(id).setValue(user);
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

            myRef.child(id).child("Name").setValue(name);
            myRef.child(id).child("Address").setValue(address);
            myRef.child(id).child("PhoneNum").setValue(phonenum);
            myRef.child(id).child("EmailAddress").setValue(currentUserEmail);


        }

    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng locationLatLong = null;

        if (strAddress == null) {
            return null;
        }
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
