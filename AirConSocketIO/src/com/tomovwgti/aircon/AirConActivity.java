
package com.tomovwgti.aircon;

import io.socket.SocketIO;
import io.socket.util.SocketIOManager;
import net.arnx.jsonic.JSON;
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
import com.tomovwgti.json.Msg;
import com.tomovwgti.json.Value;
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

    private int currentProgress;

    private LocationManager mLocationManager;
    private WeatherOnlineLoader mWeatherLoader;
    private PlaceLoader mPlaceLoader;
    private ProgressDialog mProgress;

    private byte mOutsideTemp;
    private String mAddress;

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
                    Value value = JSON.decode((String) (msg.obj), Value.class);
                    if (value.getValue().getCommand().equals("AirCon")) {
                        executeCommand(value.getValue());
                    }
                    break;
                case SocketIOManager.SOCKETIO_EVENT:
                    Log.i(TAG, "SOCKETIO_EVENT");
                    break;
                case SocketIOManager.SOCKETIO_ERROR:
                    Log.i(TAG, "SOCKETIO_ERROR");
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

        // 設定用
        final Temperature tempSetting = new Temperature();

        findViewById(R.id.btn_uu_btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_nyaa_btn).setVisibility(View.INVISIBLE);
        mControl = (TextView) findViewById(R.id.control);
        mTemp = (TextView) findViewById(R.id.temprature);
        mControlBar = (SeekBar) findViewById(R.id.control_bar);
        mControlBar.setProgress(0);
        mControlBar.setMax(11);
        mControlBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress;
                if (fromUser) {
                    mControl.setText(String.valueOf(progress + 19));
                    tempSetting.mSetting = (byte) (progress + 19);
                    tempSetting.mTemp = mTemperature;
                    // ADK出力
                    tempSetting.sendData();
                    // WebSocket送信
                    tempSetting.sendWebSocket(mSocket);
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

    private int sendFlag = 0;

    public void viewTemprature(String temprature) {
        // 設定用
        final Temperature tempSetting = new Temperature();
        mTemperature = Integer.parseInt(temprature);
        mTemp.setText(temprature);
        if (sendFlag == 10) {
            // WebSocket送信
            tempSetting.mSetting = (byte) (currentProgress + 19);
            tempSetting.mTemp = mTemperature;
            tempSetting.sendWebSocket(mSocket);
            sendFlag = 0;
        }
        sendFlag++;
    }

    /**
     * AirConコマンドを受けた時の処理
     */
    private void executeCommand(final Msg msg) {
        // ADKへ出力
        final Temperature tempSetting = new Temperature();
        // tempSetting.mSetting = (byte) msg.getSetting();
        tempSetting.mSetting = (byte) mOutsideTemp;
        tempSetting.mTemp = (byte) Integer.parseInt(msg.getTemperature());
        tempSetting.sendData();
        // 変化を反映する
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mControl.setText(String.valueOf(msg.getSetting()));
                // tempSetting.mSetting = (byte) (msg.getSetting() - 19);
                tempSetting.mSetting = (byte) mOutsideTemp;
                mControlBar.setProgress(msg.getSetting() - 19);
            }
        });
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
        return new AlertDialog.Builder(this).setTitle("Server IP Address").setView(entryView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                }).create();
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
    }

    @Override
    public void viewResult(String temp, String weather, String imageUrl) {
        mProgress.dismiss();
        int outside = Integer.valueOf(temp);
        mOutsideTemp = (byte) (outside);
        Log.v("気温 : ", temp + " ℃");
        Log.v("天気 : ", weather);
        TextView outsideTemp = (TextView) findViewById(R.id.outside_temprature);
        outsideTemp.setText(temp + " ℃");
    }
}
