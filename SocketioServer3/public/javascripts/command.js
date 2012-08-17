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

    // メッセージを受けたとき
    socket.on('message', function(event) {

        var receive_message = event.value;
        console.log('receive message <-- ' + receive_message.sender);

        switch (receive_message.command) {
            case 'AirCon':
                $('.jquery-ui-slider-red-value').val(receive_message.setting);
                $('#jquery-ui-slider-red').slider('value', receive_message.setting);
                if (receive_message.sender !== 'onX') {
                    if (receive_message.temperature < 40) {
                        $('#temperature').text(receive_message.temperature);
                    }
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
});