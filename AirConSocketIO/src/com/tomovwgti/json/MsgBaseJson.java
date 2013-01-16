
package com.tomovwgti.json;

public class MsgBaseJson {
    @SuppressWarnings("unused")
    private static final String TAG = MsgBaseJson.class.getSimpleName();

    String sender;
    String command;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
