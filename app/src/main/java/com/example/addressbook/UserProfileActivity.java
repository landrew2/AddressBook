package com.example.addressbook;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;

public class UserProfileActivity extends AppCompatActivity {

    DatabaseReference userref;
    Button updateButton;
    EditText Name_;
    EditText PhoneNum_;
    EditText Address_;
    EditText Email_;
    String locationLat;
    String locationLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userref = FirebaseDatabase.getInstance().getReference("Users");

        Name_ = (EditText)findViewById(R.id.editTextName);
        PhoneNum_ = (EditText)findViewById(R.id.editTextPhoneNum);
        Address_ = (EditText)findViewById(R.id.editTextAddress);

        //locationLat = (EditText)findViewById(R.id.)
        // MAKE LAT AND LONG TEXT VIEW here

        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
        }
        });

    }

    private void updateData() {

        String name  = Name_.getText().toString();
        String address = Address_.getText().toString();
        String phonenum = PhoneNum_.getText().toString();

        // if the strings are not empty, update Data
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(phonenum)) {

            String id = userref.push().getKey();
            LatLng location = getLocationFromAddress(getApplicationContext(), address);

            User user = new User(name, address, String.valueOf(location.latitude), String.valueOf(location.longitude), phonenum);
            userref.child(id).setValue(user);

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
