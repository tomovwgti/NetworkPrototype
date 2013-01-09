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
 }
 */
$(function () {
    // Socket.IO
    var socket = io.connect();
    var temperature = new TimeSeries();
    var setting = new TimeSeries();
    var line_temp = 19;
    var line_setting = 19;
    var options = {
        'minValue': 19,
        'maxValue': 30
    }

    // グラフ
    var chart = new SmoothieChart(options);
    chart.addTimeSeries(temperature, { strokeStyle: 'rgba(0, 255, 0, 1)', fillStyle: 'rgba(0, 255, 0, 0)', lineWidth: 4 });
    chart.addTimeSeries(setting, { strokeStyle: 'rgba(0, 128, 255, 1)', fillStyle: 'rgba(0, 255, 0, 0)', lineWidth: 4 });
    chart.streamTo(document.getElementById("chart"), 500);

    setInterval(function() {
        temperature.append(new Date().getTime(), line_temp);
        setting.append(new Date().getTime(), line_setting);
    }, 500);

    // メッセージを受けたとき
    socket.on('message', function(event) {
        var receive_message = event.value;
        console.log('receive message <-- ' + receive_message.sender);

        switch (receive_message.command) {
            case 'AirCon':
/*
                $('.jquery-ui-slider-red-value').val(receive_message.setting);
                $('#jquery-ui-slider-red').slider('value', receive_message.setting);
*/
                line_setting = receive_message.setting;
                line_temp = receive_message.temperature;
                if (receive_message.sender !== 'onX') {
                    if (receive_message.temperature < 40) {
                        $('#temperature').text(receive_message.temperature);
                    }
                    // ノブの更新
                    $('#knob').val(receive_message.setting).trigger('change');;
                }
                break;
        }

        // Message
        switch (receive_message.message) {
            // （」・ω・）」うー！
            case 'uu':
                $('#uu').vtoggle();
                if ($('#uu').css('visibility') === 'visible') {
                    uu_sound.play();
                }
                break;
            // （／・ω・）／にゃー！
            case 'nyaa':
                $('#nyaa').vtoggle();
                if ($('#nyaa').css('visibility') === 'visible') {
                    nyaa_sound.play();
                }
                break;
        }
    });
/* slider
    $(function() {
        var msg = {
            'sender': 'browser',
            'command': 'AirCon',
            'temperature': 19,
            'setting': 19
        }

        $('#jquery-ui-slider > div > .jquery-ui-slider-multi').each(function() {

            var value = parseInt($(this).text(),10);
            var inputValue = '.' + $(this).attr('id') + '-value';
            $(this).empty().slider( {
                value: value,
                range: 'min',
                min: 19,
                max: 30,
                animate: true,
                slide: function( event, ui ) {
                    $(inputValue).val(ui.value);
                    $(inputValue).html(ui.value);

                    if (inputValue === '.jquery-ui-slider-red-value') {
                        msg.setting = ui.value;
//                        $('#temperature').text(ui.value);
                        msg.temperature = $('#temperature').text();
                    }

                    console.log('send message <-- ' + JSON.stringify(msg));
                    // メッセージを送信する
                    socket.emit('message', { value: msg });

                }
            } );
            $(inputValue).val($(this).slider('value'));
            $(inputValue).html($(this).slider('value'));
        } );
    } );
*/
    $(function() {
        var msg = {
            'sender': 'browser',
            'command': 'AirCon',
            'temperature': 19,
            'setting': 19
        }

        $('#knob').knob({
            'change' : function(value) {
                msg.setting = value;
                msg.temperature = $('#temperature').text();
                console.log('send message <-- ' + JSON.stringify(msg));
                // メッセージを送信する
                socket.emit('message', { value: msg });
            }
        });
    });
});