
package com.tomovwgti.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
    final static String TAG = ImageLoader.class.getSimpleName();

    public interface ImageListener {
        public void viewResult(Bitmap image);
    }

    private ImageListener mListener;
    private HttpURLConnection mHttp = null;

    public ImageLoader(ImageListener listener) {
        mListener = listener;
    }

    @Override
    protected Bitmap doInBackground(String... imageUrl) {
        URL url;
        Bitmap bitmap = null;
        try {
            url = new URL(imageUrl[0]);
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
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        mListener.viewResult(result);
    }
}
