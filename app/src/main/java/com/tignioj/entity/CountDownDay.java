package com.tignioj.entity;

import java.util.Date;

//倒计时
public class CountDownDay {
    private Date date; //
    private String event; // 事件
    private String note; // 备注

    public CountDownDay() {
    }

    public CountDownDay(Date date, String event, String note) {
        this.date = date;
        this.event = event;
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
