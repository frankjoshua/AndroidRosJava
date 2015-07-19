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
  lcd.begin(16,2);
  lcd.print("LCD: OK");
  lcd.setBacklight(HIGH);
  SWSerial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial);
  
  //Register as Listener
  lcd.setCursor(0,1);
  lcd.print("Registering");
  delay(2000);
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
  clientTarget.registerListener(TARGET_SERVO_PAN);
  clientTarget.registerListener(TARGET_SERVO_TILT);
  clientTarget.registerListener(TARGET_GPS);
  lcd.setCursor(0,1);
  lcd.print("Ready      ");
  
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
      break;
      case TARGET_MOTOR_RIGHT:
      if(abs(mRightSpeed - val) > 127){
        mDelay += 1000; 
      }
      mRightSpeed = val;
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







