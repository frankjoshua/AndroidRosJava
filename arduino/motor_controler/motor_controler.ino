#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>
#include <Encoder.h>

#define NEO_PIN 12

#define SPEED 50

#define LEFT 2
#define RIGHT 1

#define MOTOR_PIN 11

#define encoder0PinA 8

#define encoder0PinB 14

volatile long left;
volatile long right;

ClientTarget clientTarget;

SoftwareSerial SWSerial(NOT_A_PIN, MOTOR_PIN); // RX on no pin (unused), TX on pin 11 (to S1).
SoftwareSerial SWSerial2(6, 7); // RX on no pin (unused), TX on pin 11 (to S1).
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.
Encoder myEnc(2, encoder0PinA);

void setup() 
{ 
  //Used for Motor Controler
  SWSerial.begin(9600);
  
  //Begin Target Registration
  Serial.begin(9600);
  SWSerial2.begin(9600);
  clientTarget.begin(NEO_PIN, &SWSerial2);

  //Register as Listener
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  
//  pinMode(encoder0PinA, INPUT); 
//  pinMode(encoder0PinB, INPUT);
 
 // encoder pin on interrupt 0 (pin 2)
// attachInterrupt(0, rightCounter, RISING);
// encoder pin on interrupt 1 (pin 3)
// attachInterrupt(1, leftCounter, RISING);  
} 
 
void loop() 
{ 
  delay(10);

  
  //Check for recieved data
  if(clientTarget.receiveData()){
    //Direct to motor
    if(clientTarget.getTarget() == TARGET_MOTOR_RIGHT){
      ST.motor(RIGHT, clientTarget.getValue());
    } else if (clientTarget.getTarget() == TARGET_MOTOR_LEFT){
      ST.motor(LEFT, clientTarget.getValue());
    }
  }
  
  Serial.print(myEnc.read(), DEC);
  Serial.print(" ** ");
  Serial.println(myEnc.read(), DEC);
} 

void leftCounter(){
    if(PINB&0x01)
        left++; // cw is d2 == d8
    else
        left--;
}

void rightCounter(){
    if(PINC&0x01)
        right++; // cw is d3 == A0
    else
        right--;
}
