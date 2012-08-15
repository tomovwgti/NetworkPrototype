
package com.tomovwgti.aircon;

import io.socket.SocketIO;

import org.json.JSONObject;

import net.arnx.jsonic.JSON;
import android.app.Activity;

import com.tomovwgti.android.accessory.io.InputDataListener;
import com.tomovwgti.android.accessory.io.OutputData;
import com.tomovwgti.json.Msg;
import com.tomovwgti.json.Value;

public class Temperature extends OutputData implements InputDataListener {
    static final String TAG = Temperature.class.getSimpleName();

    private final AirConActivity mActivity;
    // ADK Receive Data
    public static final byte TYPE_TEMPERATURE = 1;
    // ADK device command
    private static final byte TEMPERATURE_COMMAND = 2;

    public byte mSetting = 0;
    public int mTemp = 0;;

    public Temperature() {
        this.mActivity = null;
    }

    public Temperature(Activity mActivity, int temperature) {
        this.mActivity = (AirConActivity) mActivity;
        this.mTemp = temperature;
    }

    @Override
    public void handleMassage() {
        mActivity.viewTemprature(String.valueOf(mTemp));
    }

    @Override
    public void sendData() {
        sendCommand(TEMPERATURE_COMMAND, mSetting, 0);
    }

    public void sendWebSocket(SocketIO socket) {
        Value value = new Value();
        Msg msg = new Msg();
        msg.setCommand("AirCon");
        msg.setSender("android");
        msg.setSetting(mSetting);
        msg.setTemperature(String.valueOf(mTemp));
        value.setValue(msg);
        String message = JSON.encode(value);
        try {
            socket.emit("message", new JSONObject(message));
        } catch (org.json.JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
