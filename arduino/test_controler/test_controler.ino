#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>

#define NEO_PIN 13

ClientTarget clientTarget;
SoftwareSerial SWSerial(4, 5);

void setup() 
{ 
  SWSerial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
  
  
} 

bool mActive = false; 
void loop() 
{ 
  delay(50);
  if(clientTarget.receiveData()){
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








