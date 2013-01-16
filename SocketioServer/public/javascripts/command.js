/**
 * Created with JetBrains WebStorm.
 * User: tomo
 * Date: 12/04/29
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
/* JSONの形式は以下のようになっている
 {
    "sender":"browser",
    "command":"AirCon",
    "temperature": 19,
    "setting": 19
    "message" : {
        "type": "connect",
        "chat": "text"
        },
    "sound": "uu",
    "devices" :2
 }
 [Aircon]
 {
 "sender" : "browser",
 "command" : "Aircon",
 "setting" : 25,
 }

 [Temperature]
 {
 "sender" : "android",
 "command" : "Temperature",
 "temperature" : 10,
 "address" : "渋谷区""
 }

 [Sound]
 {
 "sender" : "mobile",
 "command" : "Sound",
 "message" : "uu"
 }

 [Connection]
 {
 "sender" : "mobile",
 "command" : "Connection",
 "type" : "connect"
 "devices" : 2,
 }
 */
$(function () {
    // Socket.IO
    var socket = io.connect();
    var line_temp = 19;
    var line_setting = 19;
    var options = {
        'minValue': 19,
        'maxValue': 30
    }

    // メッセージを受けたとき
    socket.on('message', function(event) {
        var receive_message = event.value;
        console.log('receive message <-- ' + receive_message.sender);

        switch (receive_message.command) {
            case 'Aircon':
                console.log("Command : Aircon");
                airconCommand(receive_message);
                break;
            case 'Connection':
                console.log("Command : Connection");
                connectionCommand(receive_message);
                break;
            case 'Sound':
                console.log("Command : Sound");
                soundCommand(receive_message);
                break;
            case 'Address':
                console.log("Command : Address");
                addressCommand(receive_message);
                break;
            case 'Temperature':
                console.log("Command : Temperature");
                temperatureCommand(receive_message)
                break;
            case 'Outside':
                console.log("Command : Outside");
                outsideCommand(receive_message)
                break;
        }
    });

    $(function() {
        $('#knob').knob({
            'change' : function(value) {
                var msg = new Object();
                msg.sender = 'browser';
                msg.command = 'Aircon';
                msg.setting = value;
                console.log('send message <-- ' + JSON.stringify(msg));
                // メッセージを送信する
                socket.emit('message', { value: msg });
            }
        });
    });

    // ブラウザ接続イベント
    window.onload = function() {
        var msg = new Object();
        var message = new Object();
        msg.sender = 'browser';
        msg.command = 'Connection';
        msg.type = 'connect';
        // メッセージを送信する
        socket.emit('message', { value: msg });
    }

    // ブラウザ終了イベント
    window.onbeforeunload = function () {
        var msg = new Object();
        var message = new Object();
        msg.sender = 'browser';
        msg.command = 'Connection';
        msg.type = 'disconnect';
        // メッセージを送信する
        socket.emit('message', { value: msg });
    }

    // Airconコマンド
    function airconCommand(receive_message) {
        line_setting = receive_message.setting;
        // ノブの更新
        $('#knob').val(receive_message.setting).trigger('change');
    }

    // Connectionコマンド
    function connectionCommand(receive_message) {
        if (receive_message.type === 'connect') {
            console.log("connect");
            // メッセージを画面に表示する
            $('#receiveMsg').prepend("接続されました: " + receive_message.sender + '<br>');
            $('#receiveMsg').prepend("接続数: " + receive_message.devices + '<br>');
        } else if (receive_message.type === 'disconnect') {
            console.log("disconnect");
            $('#receiveMsg').prepend("切断されました: " + receive_message.sender + '<br>');
            $('#receiveMsg').prepend("接続数: " + receive_message.devices + '<br>');
        }
    }

    // Soundコマンド
    function soundCommand(receive_message) {
        if (receive_message.message === 'uu') {
            // （」・ω・）」うー！
            $('#uu').vtoggle();
            if ($('#uu').css('visibility') === 'visible') {
                uu_sound.play();
            }
        } else if(receive_message.message === 'nyaa') {
            $('#nyaa').vtoggle();
            if ($('#nyaa').css('visibility') === 'visible') {
                nyaa_sound.play();
            }
        }
    }

    // Addressコマンド
    function addressCommand(receive_message) {
        console.log(receive_message.address);
        $('#address').append("場所: " + receive_message.address + '<br>');
    }

    // Temperatureコマンド <--- 温度センサの値
    function temperatureCommand(receive_message) {
        console.log(receive_message.temperature);
        if (receive_message.temperature < 40) {
            $('#temperature').text(receive_message.temperature);
        }
    }

    // Outsideコマンド <-- 外気温
    function outsideCommand(receive_message) {
        console.log(receive_message.outside);
        $('#outside_temp').replaceWith("外気温: " + receive_message.outside + '<br>');
    }
});