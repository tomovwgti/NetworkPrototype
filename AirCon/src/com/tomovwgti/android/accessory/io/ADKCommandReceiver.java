
package com.tomovwgti.android.accessory.io;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

import com.tomovwgti.aircon.Temperature;

public class ADKCommandReceiver extends ADKCommandAbstractReceiver {
    private static final String TAG = ADKCommandReceiver.class.getSimpleName();

    private final Activity mActivity;

    public ADKCommandReceiver(Activity activity) {
        this.mActivity = activity;
    }

    public void onAccessoryMessage(byte[] buffer) {
        int i = 0;
        int ret = buffer.length;

        while (i < ret) {
            int len = ret - i;

            switch (buffer[i]) {
                case Temperature.TYPE_TEMPERATURE:
                    if (len >= 1) {
                        Message m = Message.obtain(mHandler, Temperature.TYPE_TEMPERATURE);
                        int temp = buffer[i + 1];
                        m.obj = new Temperature(mActivity, temp);
                        mHandler.sendMessage(m);
                    }
                    i += 2;
                    break;

                default:
                    Log.d(TAG, "unknown msg: " + buffer[i]);
                    i = len;
                    break;
            }
        }
    }
}
