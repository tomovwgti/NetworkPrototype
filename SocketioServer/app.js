
/**
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , api = require('./api')
  , mongoose = require('mongoose')
  , path = require('path');

var app = express()
  , http = require('http')
  , server = http.createServer(app)
  , db = mongoose.connect('mongodb://localhost/demo')
  , io = require('socket.io').listen(server);

var DemoSchema = new mongoose.Schema({
    device: String,
    uuid: String
});

var Model = db.model('demo', DemoSchema);

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

// DBの初期化
Model.remove({}, function(err){});

// クライアントが接続してきたときの処理
io.sockets.on('connection', function(socket) {

    // メッセージを受けたときの処理
    socket.on('message', function(data) {
        // つながっているクライアント全員に送信
        socket.broadcast.emit('message', { value: data.value });
    });

    // 接続してきた時の処理
    socket.on('connected', function(data) {
        // 登録してから送信
        registerDB(data);
    });

    // 切断された時の処理
    socket.on('disconnected', function(data) {
        // 削除してから送信
        unregisterDB(data);
    });

    // DBへの登録
    function registerDB(data) {
        var devices = new Model();
        devices.device = data.value.sender;
        devices.uuid = socket.id;

        // 保存
        devices.save(function(err) {
            if(err) {
                console.log(err);
                return;
            }

            Model.count({}, function(err, count) {
                data.value.devices = count;
                // つながっているクライアント全員に送信
                io.sockets.emit('connected', { value: data.value });
            });
        });
    }

    // DBから削除
    function unregisterDB(data) {
        // 切断されたIDを削除
        Model.remove({uuid: socket.id}, function(err) {
            Model.count({}, function(err, count) {
                data.value.devices = count;
                // つながっているクライアント全員に送信
                io.sockets.emit('disconnected', { value: data.value });
            });
        });
    }
});

// Web APIエントリポイント
app.get('/api/sensor', function(req, res){

    res.send({'status': 1, 'value' : req.query.value, 'date': req.query.date, 'time': req.query.time });

    var msg = new Object();
    msg.sender = 'arduino';
    msg.command = '3gsensor';
    msg.date = req.query.date;
    msg.time = req.query.time;
    msg.temperature = req.query.value;
    msg.setting = req.query.value;
    // つながっているクライアント全員に送信
    io.sockets.emit('message', { value: msg });
});