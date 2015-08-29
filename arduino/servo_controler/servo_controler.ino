#include <Servo.h>
#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>

#define NEO_PIN 13

//Servo Control Pins
#define  SERVO1         10
#define  SERVO2         11

ClientTarget clientTarget;

Servo servos[2];
int servoPos[2];

SoftwareSerial SWSerial(4, 5);

void setup() 
{ 
  
  initServos();
  
  SWSerial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_SERVO_PAN);
  clientTarget.registerListener(TARGET_SERVO_TILT); 
} 
 
void loop() 
{ 

  if(clientTarget.receiveData()){
    int target = clientTarget.getTarget();
    int cmd = clientTarget.getCommand();
    switch(target){
       case TARGET_SERVO_PAN:
       case TARGET_SERVO_TILT:
       {
         setServo(target, constrain(clientTarget.getValue(), 0, 90));
         break;
       }
    }
  }

} 

Servo getServo(int target){
    //Return servo based on ID
     switch(target){
      case TARGET_SERVO_PAN:
        return servos[0];
      case TARGET_SERVO_TILT:
        return servos[1];
    };
    //Return default servo
    return servos[0]; 
}

void initServos(){
  servos[0].attach(SERVO1);
  setServo(TARGET_SERVO_PAN, 90);
  servos[1].attach(SERVO2);
  setServo(TARGET_SERVO_TILT, 90);
}

void setServo(int servo, int pos){
   getServo(servo).write(pos);
   servoPos[servo] = pos; 
}

void moveServo(int servo, int dest){
   int pos = servoPos[servo] + dest;
   setServo(servo, pos);
}

