package com.example.havel.bluetooth;

import android.os.Message;

/**
 * Created by 王海峰 on 2018/1/24.
 */

public class EventText {
    private String message;
    public EventText(String message)
    {
        this.message=message;
    }
    public String getEventText ()
    {
        return message;
    }
}
