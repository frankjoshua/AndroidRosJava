#include <Arduino.h>
#include <ArduinoJson.h> //https://github.com/bblanchon/ArduinoJson
#include <Usb.h> //https://github.com/felis/USB_Host_Shield
#include <AndroidAccessory.h>
#include <PocketBot.h>
#include <Wire.h>
#include <Adafruit_MotorShield.h>
//#include <RunningMedian.h>

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

//RunningMedian avg(5);

PocketBot pocketBot;

long mLastUpdate = 0;
long mLastSensorRead = 0;
boolean mRoamingMode = true;
int mDistance = 0;

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
  
  Serial.println(F("Waiting for Android device"));
  while(acc.isConnected() == false){
    Serial.print(".");
    delay(250); 
  }
  
  pinMode(6, OUTPUT);
}

void loop(void){

  if(acc.isConnected()){ 
    byte in[1];
    while(acc.read(in, sizeof(in), 1) > 0){
      char input = (char) in[0];
      Serial.print(input);
      //pocketBot.printRawTo(Serial);
      //Serial.println("");
      if(pocketBot.read(input)){
        JsonObject& root = pocketBot.getJson();
        if(!root.success()){
         Serial.println("");
         Serial.println(F("*************** JSON ERROR **************")); 
         return;
        }
        
        //Serial.println("JSON");
        for (JsonObject::iterator it=root.begin(); it!=root.end(); ++it){
          Serial.print(it->key);
          //Serial.print(" = ");
          Serial.println(it->value.asString());
        }
        Serial.println("");
        
        int faceId = root[F("face_id")];
        if(faceId > 0){
          mRoamingMode = false;
          //Get face location
          float x = root[F("face_x")];
          float z = root[F("face_z")];
          //Navigate towards face
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
            //Serial.println("Forward");
            rightMotor->run(BACKWARD);
            rightMotor->setSpeed(100);
            leftMotor->run(BACKWARD);
            leftMotor->setSpeed(100);
          } else if ( z > .5) {
            //Backward 
            //Serial.println("Backward");
            rightMotor->run(FORWARD);
            rightMotor->setSpeed(100);
            leftMotor->run(FORWARD);
            leftMotor->setSpeed(100);
          }else {
            rightMotor->setSpeed(0);
            leftMotor->setSpeed(0);
          }
        } else {
          mRoamingMode = true;
        }
        mLastUpdate = millis();
      }
    }
  }

  if(mRoamingMode){
    if(millis() - mLastSensorRead > 100){
      mLastSensorRead = millis();
      //Read from sonar sensor
      //Add to running median
      int pulse = pulseIn(6, HIGH) / 147;
      if(pulse < 253){
        //avg.add(pulse);
        //mDistance = avg.getMedian();
        mDistance = pulse;
        Serial.println(mDistance);
      }
      //If no obsticals move forward
      if(mDistance > 12){
         //Forward 
        //Serial.println("Forward");
        rightMotor->run(BACKWARD);
        rightMotor->setSpeed(100);
        leftMotor->run(BACKWARD);
        leftMotor->setSpeed(100);
      } else {
        rightMotor->run(FORWARD);
        rightMotor->setSpeed(150);
        leftMotor->run(BACKWARD);
        leftMotor->setSpeed(150);
      }
    }
  } 
  
  if(millis() - mLastUpdate > 400){
     mLastUpdate = millis();
     mRoamingMode = true;
  }
  
  
}


