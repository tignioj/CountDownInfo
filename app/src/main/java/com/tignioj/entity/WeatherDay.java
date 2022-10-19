package com.tignioj.entity;

import androidx.annotation.NonNull;

public class WeatherDay {
    private String text;

    public WeatherDay(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
