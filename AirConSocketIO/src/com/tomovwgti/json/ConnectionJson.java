
package com.tomovwgti.json;

public class ConnectionJson {
    @SuppressWarnings("unused")
    private static final String TAG = ConnectionJson.class.getSimpleName();

    Connection value;

    public Connection getValue() {
        return value;
    }

    public void setValue(Connection value) {
        this.value = value;
    }

    public class Connection extends MsgBaseJson {
        String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
