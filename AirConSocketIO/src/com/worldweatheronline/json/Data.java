
package com.worldweatheronline.json;

import java.util.ArrayList;
import java.util.List;

import net.arnx.jsonic.JSONHint;

public class Data {
    @JSONHint(name = "current_condition")
    private ArrayList<CurrentCondition> currentCondition;

    public List<CurrentCondition> getCurrentCondition() {
        return currentCondition;
    }

    public void setCurrentCondition(ArrayList<CurrentCondition> currentCondition) {
        this.currentCondition = currentCondition;
    }
}
