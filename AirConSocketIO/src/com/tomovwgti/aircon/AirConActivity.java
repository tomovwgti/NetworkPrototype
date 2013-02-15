
package com.tomovwgti.aircon;

import io.socket.SocketIO;
import io.socket.util.SocketIOManager;
import net.arnx.jsonic.JSON;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tomovwgti.android.accessory.AccessoryBaseActivity;
import com.tomovwgti.android.accessory.io.ADKCommandAbstractReceiver;
import com.tomovwgti.android.accessory.io.ADKCommandReceiver;
import com.tomovwgti.json.AddressJson;
import com.tomovwgti.json.AirconJson;
import com.tomovwgti.json.OutsideJson;
import com.tomovwgti.weather.PlaceLoader;
import com.tomovwgti.weather.PlaceLoader.PlaceListener;
import com.tomovwgti.weather.WeatherOnlineLoader;
import com.tomovwgti.weather.WeatherOnlineLoader.WeatherOnlineListener;

/**
 * ADK接続時のActivity
 * 
 * @author tomo
 */
public class AirConActivity extends AccessoryBaseActivity implements WeatherOnlineListener,
        PlaceListener, LocationListener {
    static final String TAG = AirConActivity.class.getSimpleName();

    private SocketIOManager mSocketManager;
    private SocketIO mSocket;
    private AlertDialog mAlertDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private TextView mTemp;
    private TextView mControl;
    private SeekBar mControlBar;
    private int mTemperature;
    private String mPlace = null;
    private int mOutSide = -100;

    private LocationManager mLocationManager;
    private WeatherOnlineLoader mWeatherLoader;
    private PlaceLoader mPlaceLoader;
    private ProgressDialog mProgress;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SocketIOManager.SOCKETIO_DISCONNECT:
                    Log.i(TAG, "SOCKETIO_DISCONNECT");
                    Toast.makeText(AirConActivity.this, "Disconnect", Toast.LENGTH_SHORT).show();
                    break;
                case SocketIOManager.SOCKETIO_CONNECT:
                    Log.i(TAG, "SOCKETIO_CONNECT");
                    Toast.makeText(AirConActivity.this, "Connect", Toast.LENGTH_SHORT).show();
                    break;
                case SocketIOManager.SOCKETIO_HERTBEAT:
                    Log.i(TAG, "SOCKETIO_HERTBEAT");
                    break;
                case SocketIOManager.SOCKETIO_MESSAGE:
                    Log.i(TAG, "SOCKETIO_MESSAGE");
                    break;
                case SocketIOManager.SOCKETIO_JSON_MESSAGE:
                    Log.i(TAG, "SOCKETIO_JSON_MESSAGE");
                    // スライダーの変化だけ
                    AirconJson value = JSON.decode(((String) msg.obj), AirconJson.class);
                    if (value.getValue().getCommand().equals("Aircon")) {
                        mControl.setText(String.valueOf(value.getValue().getSetting()));
                        mControlBar.setProgress(value.getValue().getSetting());
                    }
                    break;
                case SocketIOManager.SOCKETIO_EVENT:
                    Log.i(TAG, "SOCKETIO_EVENT");
                    break;
                case SocketIOManager.SOCKETIO_ERROR:
                    Log.i(TAG, "SOCKETIO_ERROR");
                    Toast.makeText(AirConActivity.this, "Connect Error!!", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SocketIOManager.SOCKETIO_ACK:
                    Log.i(TAG, "SOCKETIO_ACK");
                    break;
            }
        }
    };

    @Override
    protected ADKCommandAbstractReceiver createReceiver() {
        return new ADKCommandReceiver(this);
    }

    @Override
    protected void showControls() {
        setContentView(R.layout.main);

        mSocketManager = new SocketIOManager(mHandler);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        // IPアドレス確認ダイアログ
        mAlertDialog = showAlertDialog();
        mAlertDialog.show();

        findViewById(R.id.btn_uu_btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_nyaa_btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.power).setVisibility(View.INVISIBLE);

        mControl = (TextView) findViewById(R.id.control);
        mTemp = (TextView) findViewById(R.id.temperature);
        mControlBar = (SeekBar) findViewById(R.id.control_bar);
        mControlBar.setProgress(0);
        mControlBar.setMax(20);
        mControlBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mControl.setText(String.valueOf(progress));
                    // 設定値をWebSocketで送信
                    AirconJson value = new AirconJson();
                    AirconJson.Aircon airconJson = value.new Aircon();
                    airconJson.setCommand("Aircon");
                    airconJson.setSender("mobile");
                    airconJson.setSetting(progress);
                    value.setValue(airconJson);
                    String message = JSON.encode(value);
                    try {
                        mSocket.emit("message", new JSONObject(message));
                    } catch (org.json.JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void startLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mWeatherLoader = new WeatherOnlineLoader(this);
        mPlaceLoader = new PlaceLoader(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("情報取得中...");
        mProgress.show();

        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    protected void onPauseActivity() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSocketManager.disconnect();
    }

    public void viewTemprature(String temprature) {
        final Temperature tempSetting = new Temperature();
        mTemperature = Integer.parseInt(temprature);
        mTemp.setText(temprature);
        tempSetting.mTemp = mTemperature;
        tempSetting.sendWebSocket(mSocket);
    }

    private AlertDialog showAlertDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View entryView = factory.inflate(R.layout.dialog_entry, null);
        final EditText edit = (EditText) entryView.findViewById(R.id.username_edit);

        if (pref.getString("IPADDRESS", "").equals("")) {
            edit.setHint("***.***.***.***");
        } else {
            edit.setText(pref.getString("IPADDRESS", ""));
        }
        // キーハンドリング
        edit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Enterキーハンドリング
                if (KeyEvent.KEYCODE_ENTER == keyCode) {
                    // 押したときに改行を挿入防止処理
                    if (KeyEvent.ACTION_DOWN == event.getAction()) {
                        return true;
                    }
                    // 離したときにダイアログ上の[OK]処理を実行
                    else if (KeyEvent.ACTION_UP == event.getAction()) {
                        if (edit != null && edit.length() != 0) {
                            // ここで[OK]が押されたときと同じ処理をさせます
                            String editStr = edit.getText().toString();
                            // OKボタン押下時のハンドリング
                            Log.v(TAG, editStr);
                            mSocket = mSocketManager.connect("http://" + editStr + ":3000/");
                            // AlertDialogを閉じます
                            mAlertDialog.dismiss();
                            // 位置情報取得と気温取得開始
                            startLocation();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // AlertDialog作成
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle("Server IP Address")
                .setView(entryView);
        dialog.setPositiveButton("PORT:3000", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String editStr = edit.getText().toString();
                // OKボタン押下時のハンドリング
                Log.v(TAG, editStr);
                editor.putString("IPADDRESS", editStr);
                editor.commit();
                mSocket = mSocketManager.connect("http://" + editStr + ":3000/");
                // 位置情報取得と気温取得開始
                startLocation();
            }
        });
        dialog.setNegativeButton("PORT:80", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String editStr = edit.getText().toString();
                // OKボタン押下時のハンドリング
                Log.v(TAG, editStr);
                editor.putString("IPADDRESS", editStr);
                editor.commit();
                mSocket = mSocketManager.connect("http://" + editStr + "/");
                // 位置情報取得と気温取得開始
                startLocation();
            }
        });

        return dialog.create();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationManager.removeUpdates(this);
        if (mLocationManager == null) {
            // 位置取得キャンセル
            return;
        }
        mLocationManager = null;

        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());

        // 天気情報
        mWeatherLoader.execute(lat, lon);
        // 位置情報取得
        mPlaceLoader.execute(lat, lon);

        Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed()));
        Log.v("Bearing", String.valueOf(location.getBearing()));
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void viewResult(String place) {
        TextView address = (TextView) findViewById(R.id.address);
        address.setText(place);
        mPlace = place;
        // 住所の送信
        sendAddress();
    }

    @Override
    public void viewResult(String temp, String weather, String imageUrl) {
        mProgress.dismiss();
        mOutSide = Integer.valueOf(temp);
        Log.v("気温 : ", temp + " ℃");
        Log.v("天気 : ", weather);
        TextView outsideTempText = (TextView) findViewById(R.id.outside_temprature);
        outsideTempText.setText(temp + " ℃");
        // 外気温をADKへ出力
        final Temperature tempSetting = new Temperature();
        tempSetting.mSetting = (byte) (mOutSide);
        tempSetting.sendData();

        // 外気温の送信
        sendOutside();
    }

    // 住所の送信
    public void sendAddress() {
        if (mPlace == null) {
            // 住所未確定
            return;
        }
        AddressJson value = new AddressJson();
        AddressJson.Address addressJson = value.new Address();
        addressJson.setSender("mobile");
        addressJson.setCommand("Address");
        addressJson.setAddress(mPlace);
        value.setValue(addressJson);
        String message = JSON.encode(value);
        try {
            mSocket.emit("message", new JSONObject(message));
        } catch (org.json.JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // 外気温の送信
    public void sendOutside() {
        if (mOutSide == -100) {
            // 外気温未確定
            return;
        }
        OutsideJson value = new OutsideJson();
        OutsideJson.Outside outsideJson = value.new Outside();
        outsideJson.setCommand("Outside");
        outsideJson.setOutside(mOutSide);
        outsideJson.setSender("mobile");
        value.setValue(outsideJson);
        String message = JSON.encode(value);
        try {
            mSocket.emit("message", new JSONObject(message));
        } catch (org.json.JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
