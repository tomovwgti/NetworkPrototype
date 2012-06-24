
package com.tomovwgti.aircon;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.tomovwgti.android.accessory.AccessoryBaseActivity;
import com.tomovwgti.android.accessory.io.ADKCommandAbstractReceiver;
import com.tomovwgti.android.accessory.io.ADKCommandReceiver;

public class AirConActivity extends AccessoryBaseActivity {
    static final String TAG = AirConActivity.class.getSimpleName();

    private TextView mTemp;
    private SeekBar mControlBar;

    @Override
    protected ADKCommandAbstractReceiver createReceiver() {
        return new ADKCommandReceiver(this);
    }

    @Override
    protected void showControls() {
        setContentView(R.layout.main);

        // 設定用
        final Temperature tempSetting = new Temperature();

        final TextView control = (TextView) findViewById(R.id.control);
        mTemp = (TextView) findViewById(R.id.temprature);
        mControlBar = (SeekBar) findViewById(R.id.control_bar);
        mControlBar.setProgress(0);
        mControlBar.setMax(11);
        mControlBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                control.setText(String.valueOf(progress + 19));
                tempSetting.mSetting = (byte) (progress + 19);
                tempSetting.sendData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void viewTemprature(String temprature) {
        mTemp.setText(temprature);
    }
}
