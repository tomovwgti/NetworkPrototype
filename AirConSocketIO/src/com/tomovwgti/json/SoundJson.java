
package com.tomovwgti.json;

public class SoundJson {
    @SuppressWarnings("unused")
    private static final String TAG = SoundJson.class.getSimpleName();

    Sound value;

    public Sound getValue() {
        return value;
    }

    public void setValue(Sound value) {
        this.value = value;
    }

    public class Sound extends MsgBaseJson {
        String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
