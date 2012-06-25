
package com.tomovwgti.aircon;

import net.arnx.jsonic.JSON;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.tomovwgti.aircon.json.Msg;
import com.tomovwgti.android.accessory.AccessoryBaseActivity;
import com.tomovwgti.android.accessory.io.ADKCommandAbstractReceiver;
import com.tomovwgti.android.accessory.io.ADKCommandReceiver;

import de.roderick.weberknecht.WebSocketEventHandler;
import de.roderick.weberknecht.WebSocketMessage;

public class AirConActivity extends AccessoryBaseActivity {
    static final String TAG = AirConActivity.class.getSimpleName();

    private static String WS_URI = "ws://192.168.110.110:8001/";
    private Handler handler = new Handler();
    private AlertDialog mAlertDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private TextView mTemp;
    private TextView mControl;
    private SeekBar mControlBar;
    private int mTemperature;

    private int currentProgress;

    @Override
    protected ADKCommandAbstractReceiver createReceiver() {
        return new ADKCommandReceiver(this);
    }

    @Override
    protected void showControls() {
        setContentView(R.layout.main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        // IPアドレス確認ダイアログ
        mAlertDialog = showAlertDialog();
        mAlertDialog.show();

        // 設定用
        final Temperature tempSetting = new Temperature();

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
                    tempSetting.sendWebSocket();
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
            tempSetting.sendWebSocket();
            sendFlag = 0;
        }
        sendFlag++;
    }

    private void connectWebSocket() {
        Log.i(TAG, "connect start");
        // WebSocket通信開始
        WebSocketManager.connect(WS_URI, new WebSocketEventHandler() {

            @Override
            public void onOpen() {
                Log.d(TAG, "websocket connect open");
            }

            @Override
            public void onMessage(WebSocketMessage message) {
                Log.d(TAG, "websocket message");
                String str = message.getText();
                Msg msg = JSON.decode(str, Msg.class);

                if (msg.getCommand().equals("AirCon")) {
                    executeCommand(msg);
                }
            }

            @Override
            public void onClose() {
                Log.d(TAG, "websocket connect close");
            }
        });
    }

    /**
     * AirConコマンドを受けた時の処理
     */
    private void executeCommand(final Msg msg) {
        // ADKへ出力
        final Temperature tempSetting = new Temperature();
        tempSetting.mSetting = (byte) msg.getSetting();
        tempSetting.mTemp = msg.getTemperature();
        tempSetting.sendData();
        // 変化を反映する
        handler.post(new Runnable() {
            @Override
            public void run() {
                mControl.setText(String.valueOf(msg.getSetting()));
                tempSetting.mSetting = (byte) (msg.getSetting() - 19);
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
                            (new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    connectWebSocket();
                                }
                            })).start();
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
        return new AlertDialog.Builder(this).setTitle("Server IP Address").setView(entryView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String editStr = edit.getText().toString();
                        // OKボタン押下時のハンドリング
                        Log.v(TAG, editStr);
                        editor.putString("IPADDRESS", editStr);
                        editor.commit();
                        WS_URI = "ws://" + editStr + ":8001/";
                        (new Thread(new Runnable() {
                            @Override
                            public void run() {
                                connectWebSocket();
                            }
                        })).start();
                    }
                }).create();
    }
}
