
package com.tomovwgti.json;

public class OutsideJson {
    @SuppressWarnings("unused")
    private static final String TAG = OutsideJson.class.getSimpleName();

    Outside value;

    public Outside getValue() {
        return value;
    }

    public void setValue(Outside value) {
        this.value = value;
    }

    public class Outside extends MsgBaseJson {
        int outside;

        public int getOutside() {
            return outside;
        }

        public void setOutside(int outside) {
            this.outside = outside;
        }
    }
}
