
package com.tomovwgti.aircon;

import io.socket.SocketIO;
import io.socket.util.SocketIOManager;
import net.arnx.jsonic.JSON;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tomovwgti.json.AirconJson;
import com.tomovwgti.json.ConnectionJson;
import com.tomovwgti.json.PowerJson;
import com.tomovwgti.json.SoundJson;
import com.tomovwgti.json.TemperatureJson;

public class MainActivity extends Activity {
    static final String TAG = MainActivity.class.getSimpleName();

    private SocketIOManager mSocketManager;
    private SocketIO mSocket;
    private AlertDialog mAlertDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private SeekBar mControlBar;
    private TextView mControl;
    private TextView mTempText;
    private Switch mPowerSwitch;

    private static final String UU_STR = "uu";
    private static final String NYAA_STR = "nyaa";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SocketIOManager.SOCKETIO_DISCONNECT:
                    Log.i(TAG, "SOCKETIO_DISCONNECT");
                    Toast.makeText(MainActivity.this, "Disconnect", Toast.LENGTH_SHORT).show();
                    break;
                case SocketIOManager.SOCKETIO_CONNECT:
                    Log.i(TAG, "SOCKETIO_CONNECT");
                    ConnectionJson value = new ConnectionJson();
                    ConnectionJson.Connection connectionJson = value.new Connection();
                    connectionJson.setCommand("Connection");
                    connectionJson.setSender("mobile");
                    connectionJson.setType("connect");
                    value.setValue(connectionJson);
                    String message = JSON.encode(value);
                    try {
                        mSocket.emit("message", new JSONObject(message));
                    } catch (org.json.JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "Connect", Toast.LENGTH_SHORT).show();
                    break;
                case SocketIOManager.SOCKETIO_HERTBEAT:
                    Log.i(TAG, "SOCKETIO_HERTBEAT");
                    break;
                case SocketIOManager.SOCKETIO_MESSAGE:
                    Log.i(TAG, "SOCKETIO_MESSAGE");
                    break;
                case SocketIOManager.SOCKETIO_JSON_MESSAGE:
                    Log.i(TAG, "SOCKETIO_JSON_MESSAGE");
                    AirconJson airconJson = JSON.decode((String) (msg.obj), AirconJson.class);
                    // 設定値が変更された
                    if (airconJson.getValue().getCommand().equals("Aircon")) {
                        // 自分で設定した場合は無視
                        if (airconJson.getValue().getSender().equals("mobile")) {
                            break;
                        }
                        // 設定表示の更新
                        String str = String.valueOf(airconJson.getValue().getSetting());
                        if (str == null) {
                            break;
                        }
                        if (!str.equals("100")) {
                            setSetting(airconJson.getValue().getSetting(), Color.GREEN);
                        }
                        break;
                    } else
                    // 室内温度の取得
                    if (airconJson.getValue().getCommand().equals("Temperature")) {
                        // 取得しなおし
                        TemperatureJson temperatureJson = JSON.decode((String) (msg.obj),
                                TemperatureJson.class);
                        // 温度表示の更新
                        setTemperature(String.valueOf(temperatureJson.getValue().getTemperature()));
                        break;
                    }
                    // USB電源の取得
                    PowerJson powerJson = JSON.decode((String) (msg.obj), PowerJson.class);
                    if (powerJson.getValue().getCommand().equals("Power")) {
                        if (powerJson.getValue().getOnoff() == 1) {
                            mPowerSwitch.setChecked(true);
                        } else {
                            mPowerSwitch.setChecked(false);
                        }
                    }
                    break;
                case SocketIOManager.SOCKETIO_EVENT:
                    Log.i(TAG, "SOCKETIO_EVENT");
                    break;
                case SocketIOManager.SOCKETIO_ERROR:
                    Log.i(TAG, "SOCKETIO_ERROR");
                    Toast.makeText(MainActivity.this, "Connect Error!!", Toast.LENGTH_SHORT).show();
                    break;
                case SocketIOManager.SOCKETIO_ACK:
                    Log.i(TAG, "SOCKETIO_ACK");
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSocketManager = new SocketIOManager(mHandler);

        findViewById(R.id.outside_temperature_text).setVisibility(View.INVISIBLE);
        findViewById(R.id.outside_temprature).setVisibility(View.INVISIBLE);
        findViewById(R.id.address).setVisibility(View.INVISIBLE);

        mTempText = (TextView) findViewById(R.id.temperature);
        mControl = (TextView) findViewById(R.id.control);
        mControlBar = (SeekBar) findViewById(R.id.control_bar);
        mControlBar.setProgress(0);
        mControlBar.setMax(20);
        mControlBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mControl.setText(String.valueOf(progress));

                if (fromUser) {
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
                    setSetting(progress, Color.BLUE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // うーボタン押下時の挙動
        Button uuBtn = (Button) findViewById(R.id.btn_uu_btn);
        uuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundJson value = new SoundJson();
                SoundJson.Sound soundJson = value.new Sound();
                soundJson.setCommand("Sound");
                soundJson.setSender("mobile");
                soundJson.setMessage(UU_STR);
                value.setValue(soundJson);
                String sendMessage = JSON.encode(value);
                try {
                    mSocket.emit("message", new JSONObject(sendMessage));
                } catch (org.json.JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // にゃーボタン押下時の挙動
        Button nyaaBtn = (Button) findViewById(R.id.btn_nyaa_btn);
        nyaaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundJson value = new SoundJson();
                SoundJson.Sound soundJson = value.new Sound();
                soundJson.setCommand("Sound");
                soundJson.setSender("mobile");
                soundJson.setMessage(NYAA_STR);
                value.setValue(soundJson);
                String sendMessage = JSON.encode(value);
                try {
                    mSocket.emit("message", new JSONObject(sendMessage));
                } catch (org.json.JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // USB電源スイッチ
        mPowerSwitch = (Switch) findViewById(R.id.power);
        mPowerSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "" + isChecked);
                PowerJson value = new PowerJson();
                PowerJson.Power powerJson = value.new Power();
                powerJson.setCommand("Power");
                powerJson.setSender("mobile");
                powerJson.setOnoff(isChecked == true ? 1 : 0);
                value.setValue(powerJson);
                String sendMessage = JSON.encode(value);
                try {
                    mSocket.emit("message", new JSONObject(sendMessage));
                } catch (org.json.JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_MAIN)) {
            // IPアドレス確認ダイアログ
            mAlertDialog = showAlertDialog();
            mAlertDialog.show();
        } else if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            // NFCで起動された
            getNdefMessage(intent);
        }
    }

    private void getNdefMessage(Intent intent) {
        Parcelable[] raws = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] msgs = new NdefMessage[raws.length];
        String str = "";
        byte[] payload = null;
        for (int i = 0; i < raws.length; i++) {
            msgs[i] = (NdefMessage) raws[i];
            for (NdefRecord record : msgs[i].getRecords()) {
                str += "Type : " + new String(record.getType()) + "\n";
                str += "TNF : " + record.getTnf() + "\n";
                payload = record.getPayload();
                if (payload == null)
                    break;
            }
        }
        // ペイロードからURLを取り出す
        String url = "";
        for (int i = 13; i < payload.length; i++) {
            url += String.format("%c", payload[i]);
        }
        Log.d("URL", url);
        // IPV6での接続
        mSocket = mSocketManager.connect("http://" + url + ":3000/");
        // プレファレンスへの書き込み
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        editor.putString("IPADDRESS", url);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 切断メッセージ送信
        ConnectionJson value = new ConnectionJson();
        ConnectionJson.Connection connectionJson = value.new Connection();
        connectionJson.setCommand("Connection");
        connectionJson.setSender("mobile");
        connectionJson.setType("disconnect");
        value.setValue(connectionJson);
        String sendDisconnect = JSON.encode(value);
        try {
            mSocket.emit("message", new JSONObject(sendDisconnect));
        } catch (org.json.JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mSocketManager.disconnect();
    }

    private void setSetting(final int setting, final int color) {
        // WebSocketHandlerのonMessageは別スレッドなのでhandlerを用いてviewの書き換えを行う
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mControl.setText(String.valueOf(setting));
                mControl.setTextColor(color);
                mControlBar.setProgress(setting);
            }
        });
    }

    private void setTemperature(final String temperature) {
        // WebSocketHandlerのonMessageは別スレッドなのでhandlerを用いてviewの書き換えを行う
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTempText.setText(temperature);
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
            }
        });

        return dialog.create();
    }
}
