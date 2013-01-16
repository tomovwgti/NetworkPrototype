NetworkPrototype
================
## Overview
### フォルダ構成
* SocketioServer : Node.js + ejs + Socket.IOのサーバ
* AirConSocketIO : アプリとADK対応のAndroidアプリ
* forMegaAdkLib : ADK用のライブラリ
* SampleOnX : On{X}を使ったサンプルアプリ

### 動作確認済みバージョン
* Node.js v0.6.6/v0.8.7
* express 3.x
* Socket.IO v0.9

#### メモ
* express3.x系でのひな形作成とSocket.IOのインストール

		$ express -tejs SampleSocketIO
		$ cd SampleSocketIO && npm install
		$ npm install socket.io

* nvmの使い方

		$ source ~/.nvm/nvm.sh
		$ nvm use v0.8.7
		// nodeのバージョンリスト
		$ nvm ls

* JSON

		[Aircon] : エアコン設定
		{
			"sneder" : "browser",
			"command" : "Aircon",
			"setting" : 25,
		}

		[Temperature] : 室内気温
		{
			"sender" : "mobile",
			"command" : "Temperature",
			"temperature" : 28,
		}

		[Outside] : 外気温
		{
			"sender" : "mobile",
			"command" : "Outside",
			"outside" : 10,
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

		[Addrsss]
		{
			"sender" : "android",
			"command" : "Address",
			"address" : "渋谷区""
		}

