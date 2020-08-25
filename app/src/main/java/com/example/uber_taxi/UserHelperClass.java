package com.example.uber_taxi;

public class UserHelperClass {

        //empty constructor
        public UserHelperClass(){
            // Default constructor required for calls to DataSnapshot.getValue(User.class)

        }

    // This is for the "Customers " table of firebase database
        String name,address,date,mobile,country;



    public UserHelperClass(String dphone,String dname,String daddr,String coun,String ddob){
            this.name=dname;
            this.address=daddr;
            this.date=ddob;
            this.mobile = dphone;
            this.country = coun;
        }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }


}
