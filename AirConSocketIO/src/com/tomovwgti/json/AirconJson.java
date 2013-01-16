
package com.tomovwgti.json;

public class AirconJson {
    @SuppressWarnings("unused")
    private static final String TAG = AirconJson.class.getSimpleName();

    Aircon value;

    public Aircon getValue() {
        return value;
    }

    public void setValue(Aircon value) {
        this.value = value;
    }

    public class Aircon extends MsgBaseJson {
        int setting;

        public int getSetting() {
            return setting;
        }

        public void setSetting(int setting) {
            this.setting = setting;
        }
    }
}
