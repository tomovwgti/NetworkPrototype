
/**
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , path = require('path');

var app = express()
    , http = require('http')
    , server = http.createServer(app)
    , io = require('socket.io').listen(server);


app.configure(function(){
  app.set('port', process.env.PORT || 3000);
  app.set('views', __dirname + '/views');
  app.set('view engine', 'ejs');
  app.use(express.favicon());
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.cookieParser('your secret here'));
  app.use(express.session());
  app.use(app.router);
  app.use(express.static(path.join(__dirname, 'public')));
});

app.configure('development', function(){
  app.use(express.errorHandler());
});

app.get('/', routes.index);

// クライアントの接続を待つ(IPアドレスとポート番号を結びつけます)
server.listen(app.get('port'));

// 接続端末情報
var sidMap = new Object();

// クライアントが接続してきたときの処理
io.sockets.on('connection', function(socket) {
    sidMap[socket.id] = 'browser';
    console.log("connection");

    for (var i in sidMap) {
        console.log("connect: " + sidMap[i]);
    }
    // メッセージを受けたときの処理
    socket.on('message', function(data) {
        data.value.devices = getLength();
        console.log(data.value.devices);
        if (data.value.command === 'Connection') {
            if (data.value.type === 'disconnect') {
                data.value.devices--;
            }
        }
        // つながっているクライアント全員に送信
        io.sockets.emit('message', { value: data.value });
    });

    // クライアントが切断したときの処理
    socket.on('disconnect', function(){
        console.log("disconnect: " + socket.id);
        delete sidMap[socket.id];
        console.log("disconnect");
        // 長さを取るだけ
        console.log("connect: " + getLength());
    });
});

// サイズの取得
function getLength() {
    var length = 0;
    for(var i in sidMap ){ length++; }
    return length;
}