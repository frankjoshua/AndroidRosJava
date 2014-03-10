#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>

#define NEO_PIN 13

#define SPEED 50

ClientTarget clientTarget;

SoftwareSerial SWSerial(NOT_A_PIN, 11); // RX on no pin (unused), TX on pin 11 (to S1).
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

void setup() 
{ 
  SWSerial.begin(9600);
  
  Serial.begin(9600);
  clientTarget.begin(NEO_PIN, &Serial);

  
  //Register as Listener
  clientTarget.registerListener(TARGET_PING_CENTER);
  
  ST.motor(1, SPEED);
  ST.motor(2, SPEED);
} 
 
void loop() 
{ 
  delay(10);
  
    if(clientTarget.receiveData()){
      int powerR = 0;
      int powerL = 0;
    switch(clientTarget.getValue()){
       case DISTANCE_TOUCHING:
         clientTarget.setPixelColor(255,0,0);
         powerR = -SPEED;
         powerL = -SPEED;
       break;
       case DISTANCE_NEAR:
         clientTarget.setPixelColor(255,255,0);
         powerR = SPEED;
         powerL = -SPEED;
       break;
       case DISTANCE_FAR:
         clientTarget.setPixelColor(0,255,0);
         powerR = SPEED;
         powerL = SPEED;
       break;
    }
     ST.motor(1, powerR);
     ST.motor(2, powerL);

  }
  
 

} 








