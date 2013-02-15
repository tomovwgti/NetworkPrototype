
package com.tomovwgti.json;

public class PowerJson {
    @SuppressWarnings("unused")
    private static final String TAG = PowerJson.class.getSimpleName();

    Power value;

    public Power getValue() {
        return value;
    }

    public void setValue(Power value) {
        this.value = value;
    }

    public class Power extends MsgBaseJson {
        int onoff;

        public int getOnoff() {
            return onoff;
        }

        public void setOnoff(int onoff) {
            this.onoff = onoff;
        }
    }
}
