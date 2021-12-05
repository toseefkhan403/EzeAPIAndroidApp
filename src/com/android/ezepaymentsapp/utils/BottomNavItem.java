package com.android.ezepaymentsapp.utils;

public class BottomNavItem {
    private String text;
    private int icon;
    private String url;

    public BottomNavItem(String text, int icon, String url) {
        this.text = text;
        this.icon = icon;
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
