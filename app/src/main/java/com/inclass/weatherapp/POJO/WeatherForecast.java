package com.inclass.weatherapp.POJO;

/**
 * Created by Nikhil on 06/04/2017.
 */

public class WeatherForecast {
    String dayIcon, nightIcon, dayForecast, nightForecast;
    String minTemp, maxTemp, date;
    String mobileLink, link;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayIcon() {
        return dayIcon;
    }

    public void setDayIcon(String dayIcon) {
        this.dayIcon = dayIcon;
    }

    public String getNightIcon() {
        return nightIcon;
    }

    public void setNightIcon(String nightIcon) {
        this.nightIcon = nightIcon;
    }

    public String getDayForecast() {
        return dayForecast;
    }

    public void setDayForecast(String dayForecast) {
        this.dayForecast = dayForecast;
    }

    public String getNightForecast() {
        return nightForecast;
    }

    public void setNightForecast(String nightForecast) {
        this.nightForecast = nightForecast;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getMobileLink() {
        return mobileLink;
    }

    public void setMobileLink(String mobileLink) {
        this.mobileLink = mobileLink;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
