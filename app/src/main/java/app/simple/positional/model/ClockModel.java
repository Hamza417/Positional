package app.simple.positional.model;

import android.graphics.Bitmap;

public class ClockModel {
    Bitmap hour;
    Bitmap minute;
    Bitmap second;
    Bitmap face;
    Bitmap trail;
    Bitmap dayNight;
    String date;

    public ClockModel() {

    }

    public ClockModel(Bitmap hour, Bitmap minute, Bitmap second, Bitmap face, Bitmap trail, Bitmap dayNight, String date) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.face = face;
        this.trail = trail;
        this.dayNight = dayNight;
        this.date = date;
    }

    public Bitmap getHour() {
        return hour;
    }

    public void setHour(Bitmap hour) {
        this.hour = hour;
    }

    public Bitmap getMinute() {
        return minute;
    }

    public void setMinute(Bitmap minute) {
        this.minute = minute;
    }

    public Bitmap getSecond() {
        return second;
    }

    public void setSecond(Bitmap second) {
        this.second = second;
    }

    public Bitmap getFace() {
        return face;
    }

    public void setFace(Bitmap face) {
        this.face = face;
    }

    public Bitmap getTrail() {
        return trail;
    }

    public void setTrail(Bitmap trail) {
        this.trail = trail;
    }

    public Bitmap getDayNight() {
        return dayNight;
    }

    public void setDayNight(Bitmap dayNight) {
        this.dayNight = dayNight;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
