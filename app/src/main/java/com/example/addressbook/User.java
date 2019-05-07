package com.example.addressbook;

public class User {
    public String Name;
    public String Address;
    public String LocationLat;
    public String LocationLong;
    public String PhoneNum;

    public User() {
    }

    public User(String name, String address, String locationLat, String locationLong, String phoneNum) {
        Name = name;
        Address = address;
        LocationLat = locationLat;
        LocationLong = locationLong;
        PhoneNum = phoneNum;
    }
}
