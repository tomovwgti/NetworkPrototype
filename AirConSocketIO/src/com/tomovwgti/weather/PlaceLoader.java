
package com.tomovwgti.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.os.AsyncTask;

public class PlaceLoader extends AsyncTask<String, Void, String> {
    static final String TAG = PlaceLoader.class.getSimpleName();

    private static final String KEY = "m1rDqkWxg64db3KhqLdIXxJaALj0tKKzuaPitulv01AXaX_0ov_qzh5jzVdkBlA-";
    private static final String TARGET_URL = "reverse.search.olp.yahooapis.jp";

    private XmlPullParser xpp;
    private int eventType;

    public interface PlaceListener {
        public void viewResult(String place);
    }

    private PlaceListener mListener;
    private HttpURLConnection mHttp = null;

    public PlaceLoader(PlaceListener listener) {
        mListener = listener;
    }

    @Override
    protected String doInBackground(String... location) {
        URL url = null;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority(TARGET_URL);
        builder.path("/OpenLocalPlatform/V1/reverseGeoCoder");
        builder.appendQueryParameter("appid", KEY);
        builder.appendQueryParameter("lat", location[0]);
        builder.appendQueryParameter("lon", location[1]);
        try {
            url = new URL(builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        InputStream in = null;
        try {
            mHttp = (HttpURLConnection) url.openConnection();
            mHttp.setRequestMethod("GET");
            mHttp.connect();
            // データを取得
            in = mHttp.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // XMLをパース
        String tag = null;
        String end_tag = null;
        String address = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            this.xpp = factory.newPullParser();
            xpp.setInput(in, "UTF-8");
            this.eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tag = xpp.getName();

                    // 住所
                    if (tag.compareTo("Address") == 0) {
                        address = getText();
                        break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    end_tag = xpp.getName();
                    if (end_tag.compareTo("area") == 0) {
                        break;
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return address;
    }

    @Override
    protected void onPostExecute(String result) {
        mListener.viewResult(result);
    }

    private String getText() throws XmlPullParserException, IOException {
        if (eventType != XmlPullParser.START_TAG) {
            eventType = xpp.next();
            return "UnKnown";
        }
        while (eventType != XmlPullParser.TEXT) {
            eventType = xpp.next();
        }
        return xpp.getText();
    }
}
