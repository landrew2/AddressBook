package com.example.addressbook;

public class User {
    public String Name;
    public String Address;
    public String LocationLat;
    public String LocationLong;
    public String PhoneNum;
    public String EmailAddress;

    public User() {
    }


    //could just make the location ones "" when not connected to the gps
    public User(String name, String address, String locationLat, String locationLong, String phoneNum, String emailAddress) {
        Name = name;
        Address = address;
        LocationLat = locationLat;
        LocationLong = locationLong;
        PhoneNum = phoneNum;
        EmailAddress = emailAddress;

    }
}
