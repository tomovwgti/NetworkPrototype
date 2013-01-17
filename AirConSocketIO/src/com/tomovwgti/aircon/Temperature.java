
package com.tomovwgti.aircon;

import io.socket.SocketIO;
import net.arnx.jsonic.JSON;

import org.json.JSONObject;

import android.app.Activity;

import com.tomovwgti.android.accessory.io.InputDataListener;
import com.tomovwgti.android.accessory.io.OutputData;
import com.tomovwgti.json.TemperatureJson;

public class Temperature extends OutputData implements InputDataListener {
    static final String TAG = Temperature.class.getSimpleName();

    private final AirConActivity mActivity;
    // ADK Receive Data
    public static final byte TYPE_TEMPERATURE = 1;
    // ADK device command
    private static final byte TEMPERATURE_COMMAND = 2;

    public byte mSetting = 0;
    public int mTemp = 0;;

    private static int sendCount = 0;;

    public Temperature() {
        this.mActivity = null;
    }

    public Temperature(Activity mActivity, int temperature) {
        this.mActivity = (AirConActivity) mActivity;
        this.mTemp = temperature;
    }

    @Override
    public void handleMassage() {
        // 10回呼ばれたら1回送信する
        if (sendCount == 10) {
            mActivity.viewTemprature(String.valueOf(mTemp));
            mActivity.sendAddress();
            mActivity.sendOutside();
            sendCount = 0;
        }
        sendCount++;
    }

    @Override
    public void sendData() {
        sendCommand(TEMPERATURE_COMMAND, mSetting, 0);
    }

    /**
     * 気温設定値を送信
     * 
     * @param socket
     */
    public void sendWebSocket(SocketIO socket) {
        TemperatureJson value = new TemperatureJson();
        TemperatureJson.Temperature temperatureJson = value.new Temperature();
        temperatureJson.setCommand("Temperature");
        temperatureJson.setSender("mobile");
        temperatureJson.setTemperature(mTemp);
        value.setValue(temperatureJson);
        String message = JSON.encode(value);
        try {
            socket.emit("message", new JSONObject(message));
        } catch (org.json.JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
