
package com.worldweatheronline.json;

import java.util.ArrayList;

import net.arnx.jsonic.JSONHint;

public class CurrentCondition {
    private String cloudcover;
    private String humidity;
    @JSONHint(name = "observation_time")
    private String observationTime;
    private String precipMM;
    private String pressure;
    @JSONHint(name = "temp_C")
    private String tempC;
    @JSONHint(name = "temp_F")
    private String tempF;
    private String visibility;
    private String weatherCode;
    private String winddir16Point;
    private String winddirDegree;
    private String windspeedKmph;
    private String windspeedMiles;
    private ArrayList<WeatherIcon> weatherIconUrl;
    private ArrayList<weatherDescription> weatherDesc;

    public String getTempC() {
        return tempC;
    }

    public void setTempC(String tempC) {
        this.tempC = tempC;
    }

    public String getCloudcover() {
        return cloudcover;
    }

    public void setCloudcover(String cloudcover) {
        this.cloudcover = cloudcover;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getObservationTime() {
        return observationTime;
    }

    public void setObservationTime(String observationTime) {
        this.observationTime = observationTime;
    }

    public String getPrecipMM() {
        return precipMM;
    }

    public void setPrecipMM(String precipMM) {
        this.precipMM = precipMM;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTempF() {
        return tempF;
    }

    public void setTempF(String tempF) {
        this.tempF = tempF;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getWinddir16Point() {
        return winddir16Point;
    }

    public void setWinddir16Point(String winddir16Point) {
        this.winddir16Point = winddir16Point;
    }

    public String getWinddirDegree() {
        return winddirDegree;
    }

    public void setWinddirDegree(String winddirDegree) {
        this.winddirDegree = winddirDegree;
    }

    public String getWindspeedKmph() {
        return windspeedKmph;
    }

    public void setWindspeedKmph(String windspeedKmph) {
        this.windspeedKmph = windspeedKmph;
    }

    public String getWindspeedMiles() {
        return windspeedMiles;
    }

    public void setWindspeedMiles(String windspeedMiles) {
        this.windspeedMiles = windspeedMiles;
    }

    public ArrayList<WeatherIcon> getWeatherIconUrl() {
        return weatherIconUrl;
    }

    public void setWeatherIconUrl(ArrayList<WeatherIcon> weatherIconUrl) {
        this.weatherIconUrl = weatherIconUrl;
    }

    public ArrayList<weatherDescription> getWeatherDesc() {
        return weatherDesc;
    }

    public void setWeatherDesc(ArrayList<weatherDescription> weatherDesc) {
        this.weatherDesc = weatherDesc;
    }
}
