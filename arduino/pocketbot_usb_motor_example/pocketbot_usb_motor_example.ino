#include <Arduino.h>
#include <ArduinoJson.h>
#include <Usb.h> //https://github.com/felis/USB_Host_Shield
#include <AndroidAccessory.h>
#include <PocketBot.h>
#include <Wire.h>
#include <Adafruit_MotorShield.h>

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

// Create the motor shield object with the default I2C address
Adafruit_MotorShield AFMS = Adafruit_MotorShield(); 
// Select which 'port' M1, M2, M3 or M4
Adafruit_DCMotor *rightMotor = AFMS.getMotor(3);
Adafruit_DCMotor *leftMotor = AFMS.getMotor(4);

PocketBot pocketBot;

long mLastUpdate = 0;

float mapfloat(float x, float in_min, float in_max, float out_min, float out_max){
 return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
  
void setup(void){
  Serial.begin(115200);

  AFMS.begin();
  rightMotor->run(RELEASE);
  leftMotor->run(RELEASE);
        
  acc.powerOn();
  pocketBot.begin();
}

void loop(void){

  if(acc.isConnected()){
    byte in[1];
    while(acc.read(in, sizeof(in), 1) > 0){
      char input = (char) in[0];
      if(pocketBot.read(input)){
        JsonObject& root = pocketBot.getJson();
        if(!root.success()){
         Serial.println(F("*************** JSON ERROR **************")); 
         return;
        }
        
        Serial.println("JSON");
        root.printTo(Serial);
        Serial.println("");
        
        float x = root["face_x"];
        float z = root["face_z"];
       

        if(x > 1.1){
          int s = mapfloat(x, 1, 2, 50, 100);
          Serial.println(s);
          rightMotor->run(FORWARD);
          rightMotor->setSpeed(s);
          leftMotor->run(BACKWARD);
          leftMotor->setSpeed(s);
        } else if(x < .9){
          int s = mapfloat(x, 0.0, 1, 100, 50);
          Serial.println(s);
          rightMotor->run(BACKWARD);
          rightMotor->setSpeed(s);
          leftMotor->run(FORWARD);
          leftMotor->setSpeed(s);
        } else if (z > .3 && z < .45){
          //Forward 
          Serial.println("Forward");
          rightMotor->run(BACKWARD);
          rightMotor->setSpeed(100);
          leftMotor->run(BACKWARD);
          leftMotor->setSpeed(100);
        } else if ( z > .5) {
          //Backward 
          Serial.println("Backward");
          rightMotor->run(FORWARD);
          rightMotor->setSpeed(100);
          leftMotor->run(FORWARD);
          leftMotor->setSpeed(100);
        }else {
          rightMotor->setSpeed(0);
          leftMotor->setSpeed(0);
        }
        mLastUpdate = millis();
      }
    }
  }

  if(millis() - mLastUpdate > 400){
     mLastUpdate = millis();
     rightMotor->setSpeed(0);
     leftMotor->setSpeed(0);
  }
  
}
