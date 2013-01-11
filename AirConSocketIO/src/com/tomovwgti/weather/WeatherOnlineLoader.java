
package com.tomovwgti.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.arnx.jsonic.JSON;
import android.net.Uri;
import android.os.AsyncTask;

import com.tomovwgti.weather.WeatherOnlineLoader.Weather;
import com.worldweatheronline.json.JsonObj;

public class WeatherOnlineLoader extends AsyncTask<String, Void, Weather> {
    static final String TAG = WeatherOnlineLoader.class.getSimpleName();

    public interface WeatherOnlineListener {
        public void viewResult(String temp, String weather, String imageUrl);
    }

    private static final String TARGET_URL = "free.worldweatheronline.com";
    private static final String KEY = "a75e78070e034532122203";

    private WeatherOnlineListener mListener;
    private HttpURLConnection mHttp = null;

    public WeatherOnlineLoader(WeatherOnlineListener listener) {
        mListener = listener;
    }

    @Override
    protected Weather doInBackground(String... location) {
        URL url = null;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority(TARGET_URL);
        builder.path("/feed/weather.ashx");
        builder.appendQueryParameter("key", KEY);
        builder.appendQueryParameter("format", "json");
        try {
            url = new URL(builder.toString() + "&q=" + location[0] + "," + location[1]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        String jsonString = null;
        try {
            mHttp = (HttpURLConnection) url.openConnection();
            mHttp.setRequestMethod("GET");
            mHttp.connect();
            // データを取得
            InputStream in = mHttp.getInputStream();
            jsonString = convertString(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // JSONをパースする
        JsonObj jsonObj = JSON.decode(jsonString, JsonObj.class);
        Weather weather = new Weather(jsonObj.getData().getCurrentCondition().get(0).getTempC(),
                jsonObj.getData().getCurrentCondition().get(0).getWeatherDesc().get(0).getValue(),
                jsonObj.getData().getCurrentCondition().get(0).getWeatherIconUrl().get(0)
                        .getValue());

        return weather;
    }

    @Override
    protected void onPostExecute(Weather result) {
        mListener.viewResult(result.temperature, result.weather, result.imageUrl);
    }

    /**
     * InputStream to String
     * 
     * @param is
     * @return
     * @throws IOException
     */
    private String convertString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while (null != (line = reader.readLine())) {
            sb.append(line);
        }
        return sb.toString();
    }

    public class Weather {
        public String temperature;
        public String weather;
        public String imageUrl;

        public Weather(String temperature, String weather, String imageUrl) {
            this.temperature = temperature;
            this.weather = weather;
            this.imageUrl = imageUrl;
        }
    }
}
