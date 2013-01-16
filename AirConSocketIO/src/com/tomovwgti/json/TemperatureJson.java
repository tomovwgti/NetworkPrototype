
package com.tomovwgti.json;

public class TemperatureJson {
    @SuppressWarnings("unused")
    private static final String TAG = TemperatureJson.class.getSimpleName();

    Temperature value;

    public Temperature getValue() {
        return value;
    }

    public void setValue(Temperature value) {
        this.value = value;
    }

    public class Temperature extends MsgBaseJson {
        int temperature;

        public int getTemperature() {
            return temperature;
        }

        public void setTemperature(int temperature) {
            this.temperature = temperature;
        }
    }
}
