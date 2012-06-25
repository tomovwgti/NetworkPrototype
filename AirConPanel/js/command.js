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
    // WebSocket
    var ws = new WebSocket('ws://192.168.0.2:8001/');

    // Message from Server
    ws.onmessage = function (event) {
        var receive_message = JSON.parse(event.data);
        console.log('receive message <-- ' + event.data);

        switch (receive_message.command) {
            case 'AirCon':
                $('.jquery-ui-slider-red-value').val(receive_message.setting);
                $('#jquery-ui-slider-red').slider('value', receive_message.setting);
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
    }

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
                    }

                    console.log(JSON.stringify(msg));
                    ws.send(JSON.stringify(msg));
                }
            } );
            $(inputValue).val($(this).slider('value'));
            $(inputValue).html($(this).slider('value'));
        } );
    } );
});