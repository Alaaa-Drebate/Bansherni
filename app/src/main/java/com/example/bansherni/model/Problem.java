package com.example.bansherni.model;

public class Problem {

    private int id;
    private int user_id;
    private String user_name;
    private String phone_number;
    private String details;
    private double price;
    private int type;
    private int status;
    private double lat;
    private double lon;


    //when sending a report we , status always equals STATUS_NEW, price is equal to 30.00 by default
    public Problem(int user_id, String problem_details, int type, double lat, double lon, double price) {
        this.price = price;
        this.user_id = user_id;
        this.details = problem_details;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
    }

    public Problem(int id, int user_id, String user_name, String phone_number, String details, int type, int status, double lat, double lon, double price) {
        this.price = price;
        this.id = id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.phone_number = phone_number;
        this.details = details;
        this.type = type;
        this.status = status;
        this.lat = lat;
        this.lon = lon;
    }

    //get my reports for user
    public Problem(int id, String problem_details, int type, int status, double price) {
        this.price = price;
        this.id = id;
        this.details = problem_details;
        this.type = type;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
