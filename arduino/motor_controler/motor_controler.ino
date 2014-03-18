#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>

#define NEO_PIN 13

#define SPEED 50

#define LEFT 2
#define RIGHT 1

ClientTarget clientTarget;

SoftwareSerial SWSerial(NOT_A_PIN, 11); // RX on no pin (unused), TX on pin 11 (to S1).
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

void setup() 
{ 
  SWSerial.begin(9600);
  
  Serial.begin(9600);
  clientTarget.begin(NEO_PIN, &Serial);

  
  //Register as Listener
//  clientTarget.registerListener(TARGET_PING_CENTER);
//  clientTarget.registerListener(TARGET_PING_LEFT);
//  clientTarget.registerListener(TARGET_PING_RIGHT);
clientTarget.registerListener(TARGET_MOTOR_RIGHT);
clientTarget.registerListener(TARGET_MOTOR_LEFT);
  
//  ST.motor(1, SPEED);
//  ST.motor(2, SPEED);
} 
 
void loop() 
{ 
  delay(10);
  
    if(clientTarget.receiveData()){
      if(clientTarget.getTarget() == TARGET_MOTOR_RIGHT){
        ST.motor(RIGHT, clientTarget.getValue());
      } else if (clientTarget.getTarget() == TARGET_MOTOR_LEFT){
        ST.motor(LEFT, clientTarget.getValue());
      }
    }
  
} 








