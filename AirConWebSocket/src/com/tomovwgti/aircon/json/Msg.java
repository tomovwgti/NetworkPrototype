
package com.tomovwgti.aircon.json;

public class Msg {
    String sender;
    String command;
    int temperature;
    int setting;

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

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getSetting() {
        return setting;
    }

    public void setSetting(int setting) {
        this.setting = setting;
    }
}
