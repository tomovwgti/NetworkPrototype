
package com.tomovwgti.onx;

import io.socket.SocketIO;
import io.socket.util.SocketIOManager;
import net.arnx.jsonic.JSON;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tomovwgti.json.Msg;
import com.tomovwgti.json.Value;

public class SampleOnXActivity extends Activity {
    static final String TAG = SampleOnXActivity.class.getSimpleName();

    private SocketIOManager mSocketManager;
    private SocketIO mSocket;

    private AlertDialog mAlertDialog;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SocketIOManager.SOCKETIO_DISCONNECT:
                    Log.i(TAG, "SOCKETIO_DISCONNECT");
                    Toast.makeText(SampleOnXActivity.this, "Disconnect", Toast.LENGTH_SHORT).show();
                    break;
                case SocketIOManager.SOCKETIO_CONNECT:
                    Log.i(TAG, "SOCKETIO_CONNECT");
                    Toast.makeText(SampleOnXActivity.this, "Connect", Toast.LENGTH_SHORT).show();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSocketManager = new SocketIOManager(mHandler);
        int status = (int) getIntent().getDoubleExtra("status", 0.0);
        Log.i(TAG, "status: " + status);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        if (pref.getString("IPADDRESS", "").equals("")) {
            // IPアドレス確認ダイアログ
            mAlertDialog = showAlertDialog();
            mAlertDialog.show();
            return;
        }

        mSocket = mSocketManager.connect("http://" + pref.getString("IPADDRESS", "") + ":3000/");

        Value value = new Value();
        Msg msg = new Msg();
        msg.setCommand("AirCon");
        msg.setSender("onX");
        msg.setSetting(26);
        value.setValue(msg);
        String message = JSON.encode(value);
        try {
            mSocket.emit("message", new JSONObject(message));
        } catch (org.json.JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SampleOnXActivity.this, "AirCon ON!", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSocketManager.disconnect();
    }

    private AlertDialog showAlertDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View entryView = factory.inflate(R.layout.dialog_entry, null);
        final EditText edit = (EditText) entryView.findViewById(R.id.username_edit);

        edit.setHint("***.***.***.***");
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
                            editor.putString("IPADDRESS", editStr);
                            editor.commit();
                            // AlertDialogを閉じます
                            mAlertDialog.dismiss();
                            finish();
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
                        editor.putString("IPADDRESS", editStr);
                        editor.commit();
                        finish();
                    }
                }).create();
    }
}
