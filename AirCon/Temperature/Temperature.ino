#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include "SevenSegment.h"

// 温度センサ
#define TEMPERATURE A0

SevenSegment seg7(2,5,6,13, // 桁1,桁2,桁3,桁4,
3,7,11,9,8,4,12,10); // A,B,C,D,E,F,G,DP

int value; // アナログ入力値(0～203)
float tempC   = 0; // 摂氏値( ℃ )
unsigned long lastUpdateTime;
int count = 0;

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

void setup()
{
  Serial.begin(9600);
  Serial.print("Start");
  acc.powerOn();
  pinMode(TEMPERATURE, INPUT);
  seg7.begin(); // 7セグLED動作開始
  // 7seg初期化
  seg7.print_lo(0);
  seg7.print_hi(0);
}

void loop()
{
  byte msg[3];
  
  if (acc.isConnected()) {

    // 温度設定
    int len = acc.read(msg, sizeof(msg), 1);
    if ( len > 0 ) {
      if (msg[0] == 0x02) {
          seg7.print_hi(msg[1]); // 7セグLED表示更新
      }
    }
    
    value = analogRead(TEMPERATURE);
    tempC = ((5 * value) / 1024.0) * 100;
    byte tempDisplay = tempC;

    if ( count == 1000 ) {
      msg[0] = 0x1;
      msg[1] = tempC;
      acc.write(msg, 2);
      count = 0;
    }
    count++;
  
    if(seg7.update()) { // ダイナミック点灯制御を行うのでできるだけ短い間隔で呼ぶ
      unsigned long cur = millis();
      if(cur - lastUpdateTime > 60) { // 60ms間隔でLEDを更新

        seg7.print_lo(tempDisplay); // 7セグLED表示更新

        lastUpdateTime = cur;
      }
    }
  }
}
