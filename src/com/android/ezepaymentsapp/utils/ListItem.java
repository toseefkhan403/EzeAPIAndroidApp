package com.android.ezepaymentsapp.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListItem {
    private String name,ward,mohalla,house_no,qrcode,contact_no,torrent,unique_id,monthly_charge,month_year;

    public ListItem(String name, String ward, String mohalla, String house_no, String qrcode,
                    String contact_no, String torrent, String unique_id, String monthly_charge, String month_year) {
        this.name = name;
        this.ward = ward;
        this.mohalla = mohalla;
        this.house_no = house_no;
        this.qrcode = qrcode;
        this.contact_no = contact_no;
        this.torrent = torrent;
        this.unique_id = unique_id;
        this.monthly_charge = monthly_charge;
        this.month_year = month_year;
    }

    public ListItem(JSONObject object){
        try {
            this.name = object.getString("name");
            this.ward = object.getString("ward");
            this.mohalla =object.getString("mohalla");
            this.house_no = object.getString("house_no");
            this.qrcode = object.getString("qrcode");
            this.contact_no = object.getString("contact_no");
            this.torrent = object.getString("torrent");
            this.unique_id = object.getString("unique_id");
            this.monthly_charge = object.getString("monthly_charge");
            this.month_year = object.getString("month_year");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<ListItem> fromJson(JSONArray jsonObjects) {
        ArrayList<ListItem> users = new ArrayList<ListItem>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                users.add(new ListItem(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getMohalla() {
        return mohalla;
    }

    public void setMohalla(String mohalla) {
        this.mohalla = mohalla;
    }

    public String getHouse_no() {
        return house_no;
    }

    public void setHouse_no(String house_no) {
        this.house_no = house_no;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getContact_no() {
        return contact_no;
    }

    public void setContact_no(String contact_no) {
        this.contact_no = contact_no;
    }

    public String getTorrent() {
        return torrent;
    }

    public void setTorrent(String torrent) {
        this.torrent = torrent;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public void setUnique_id(String unique_id) {
        this.unique_id = unique_id;
    }

    public String getMonthly_charge() {
        return monthly_charge;
    }

    public void setMonthly_charge(String monthly_charge) {
        this.monthly_charge = monthly_charge;
    }

    public String getMonth_year() {
        return month_year;
    }

    public void setMonth_year(String month_year) {
        this.month_year = month_year;
    }

}
