
package com.tomovwgti.aircon;

import net.arnx.jsonic.JSON;
import android.app.Activity;

import com.tomovwgti.aircon.json.Msg;
import com.tomovwgti.android.accessory.io.InputDataListener;
import com.tomovwgti.android.accessory.io.OutputData;

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

    public void sendWebSocket() {
        Msg msg = new Msg();
        msg.setCommand("AirCon");
        msg.setSender("android");
        msg.setSetting(mSetting);
        msg.setTemperature(mTemp);
        String message = JSON.encode(msg);
        WebSocketManager.send(message);
    }
}
