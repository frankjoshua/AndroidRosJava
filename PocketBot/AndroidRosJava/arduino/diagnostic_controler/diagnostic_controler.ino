#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <Wire.h>
#include <LiquidTWI.h>

#define NEO_PIN 13

ClientTarget clientTarget;
SoftwareSerial SWSerial(4, 5);

// Connect via i2c, default address #0 (A0-A2 not jumpered)
LiquidTWI lcd(0);

Adafruit_NeoPixel strip2 = Adafruit_NeoPixel(8, 2, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel strip = Adafruit_NeoPixel(8, 6, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel strip3 = Adafruit_NeoPixel(8, 7, NEO_GRB + NEO_KHZ800);
Adafruit_NeoPixel strip4 = Adafruit_NeoPixel(8, 3, NEO_GRB + NEO_KHZ800);

int mLeftSpeed = 0;
int mRightSpeed = 0;
int mTar = 0;
int mVal = 0;
int mCmd = 0;
int mDur = 0;
int mDelay = 0;
int mGpsHeading = -1;
int mCompassHeading = -1;
int mWaypoint = -1;

void setup() 
{ 
  //Setup LCD
  lcd.begin(16,2);
  lcd.print("LCD: OK");
  lcd.setBacklight(HIGH);
  
  Serial.begin(115200);
  
  //Start NEO Pixels
  initNeoStrip(strip2); //bottom
  initNeoStrip(strip4); //low mid
  initNeoStrip(strip); //upper mid
  initNeoStrip(strip3); //top
  testStrip(strip2);
  testStrip(strip4);
  testStrip(strip);
  testStrip(strip3);
  
  //Start ClientTarget
  SWSerial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial);
  
  //Register as Listener
  lcd.setCursor(0,1);
  lcd.print("Registering");
  delay(350);
  lcd.setCursor(0,1);
  lcd.print("Left Motor");
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  //delay(250);
  lcd.setCursor(0,1);
  lcd.print("Right Motor");
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
  //delay(250);
  lcd.setCursor(0,1);
  lcd.print("Pan Servo  ");
  clientTarget.registerListener(TARGET_SERVO_PAN);
  //delay(250);
  lcd.setCursor(0,1);
  lcd.print("Tilt Servo ");
  clientTarget.registerListener(TARGET_SERVO_TILT);
  //delay(250);
  lcd.setCursor(0,1);
  lcd.print("GPS        ");
  clientTarget.registerListener(TARGET_GPS);
  lcd.setCursor(0,1);
  lcd.print("Ready                  ");
  
} 

bool mActive = false; 
void loop() 
{ 
  delay(5);
  if(clientTarget.receiveData()){
    processData(clientTarget.getTarget(), clientTarget.getCommand(), clientTarget.getValue(), clientTarget.getDuration());
    updateDisplay();
    //Flash cursor
    mActive = !mActive;
    if(mActive){
      clientTarget.setPixelColor(COLOR_ACTIVE);
    } else{
      clientTarget.setPixelColor(0,0,0);
    }
  } else {
    clientTarget.setPixelColor(COLOR_OK);
  }
  
  
} 

void initNeoStrip(Adafruit_NeoPixel &neo){
  neo.begin();
  neo.show(); // Initialize all pixels to 'off'

}

void testStrip(Adafruit_NeoPixel &neo){
  uint16_t i, j;

  for(j=0; j<256; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      neo.setPixelColor(i, Wheel((i+j) & 255));
    }
    neo.show();
    delay(20);
  }
  
  setColor(neo, 255,0,255);
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  if(WheelPos < 85) {
   return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } else if(WheelPos < 170) {
   WheelPos -= 85;
   return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else {
   WheelPos -= 170;
   return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
}

void setColor(Adafruit_NeoPixel &neo, int red, int green, int blue){
  Serial.println("Setting Color");
   for(int i = 0; i < 8; i++){
      neo.setPixelColor(i, red, green, blue);
   } 
   neo.show();
}

void processData(int tar, int cmd, int val, int dur){
  //Save last values
  mTar = tar;
  mCmd = cmd;
  mVal = val;
  mDur = dur;
  switch(tar){
      case TARGET_MOTOR_LEFT:
      //Look for out of value ranges
      if(abs(mLeftSpeed - val) > 127){
        mDelay += 1000; 
      }
      mLeftSpeed = val;
      setColor(strip, 255, 0, constrain(map(val, -127, 127, 0, 255), 0, 255));
      //setColor(strip3, constrain(map(val, -127, 0, 255, 25), 0, 255), 255, constrain(map(val, 0, 127, 25, 255), 0, 255));
      setColor(strip3, 255, constrain(map(val, -127, 25, 110, 0),0 , 255), constrain(map(val, -25, 127, 0, 180), 0, 255));
      break;
      case TARGET_MOTOR_RIGHT:
      if(abs(mRightSpeed - val) > 127){
        mDelay += 1000; 
      }
      mRightSpeed = val;
      setColor(strip2, 255, 0, constrain(map(val, -127, 127, 0, 255), 0, 255));
      setColor(strip4, 255, constrain(map(val, -127, 25, 110, 0),0 , 255), constrain(map(val, -25, 127, 0, 180), 0, 255));
      break;
      case TARGET_GPS:
        if(cmd == COMMAND_GPS_HEADING){
          if(abs(mGpsHeading - val) > 20 ){
            mDelay += 1000; 
          }
          mGpsHeading = val; 
        } else if(cmd == COMMAND_GPS_COMPASS){
          if(abs(mCompassHeading - val) > 20 ){
            mDelay += 1000; 
          }
          mCompassHeading = val;
        } else if(cmd == COMMAND_GPS_LOCATION){
           //This should be the current way point
           mWaypoint = val; 
        }
      break;
  }
}

void updateDisplay(){
  lcd.clear();
  lcd.setCursor(0,0);
  if(mDelay > 0){
     lcd.print("!"); 
  }
  
    lcd.print("L");
    lcd.print(mLeftSpeed);
    lcd.print(" R");
    lcd.print(mRightSpeed);
    lcd.print(" ");
    if(mLeftSpeed > mRightSpeed){
       lcd.print("<-"); 
    } else if (mLeftSpeed < mRightSpeed){
       lcd.print("->"); 
    } else if (mLeftSpeed == 0){
       lcd.print("<>"); 
    } else if (mLeftSpeed > 0){
       lcd.print("^^"); 
    } else {
       lcd.print("--"); 
    }
  
  
  lcd.print(" ");
  lcd.print(millis() / 1000);
  
  //Move to second line
  lcd.setCursor(0,1);
  
  if(mCompassHeading != -1 || mGpsHeading != -1){
    //Print navigation info
    lcd.print("G:");
    lcd.print(mGpsHeading);
    lcd.print(" C:");
    lcd.print(mCompassHeading);
    lcd.print(" W:");
    lcd.print(mWaypoint);
  } else { 
    //Print last transmition
    lcd.print("T");
    lcd.print(mTar);
    lcd.print(" C");
    lcd.print(mCmd);
    lcd.print(" V");
    lcd.print(mVal);
    lcd.print(" D");
    lcd.print(mDur);
  }
  
  //Pause if needed
  delay(mDelay);
  //reset delay
  mDelay = 0;
}







