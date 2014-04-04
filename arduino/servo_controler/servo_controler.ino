#include <Servo.h>
#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

#define NEO_PIN 13

#define LASER_PIN 7

//Servo Control Pins
#define  SERVO1         11
#define  SERVO2         12

ClientTarget clientTarget;

Servo servos[2];
int servoPos[2];
 
void setup() 
{ 
  pinMode(LASER_PIN, OUTPUT);
  
  initServos();
  
  Serial.begin(9600);
  clientTarget.begin(NEO_PIN, &Serial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_SERVO_HEAD_VERT);
  clientTarget.registerListener(TARGET_SERVO_HEAD_HORZ);  
  clientTarget.registerListener(TARGET_LASER_HEAD);
} 
 
void loop() 
{ 

  if(clientTarget.receiveData()){
    int target = clientTarget.getTarget();
    int cmd = clientTarget.getCommand();
    switch(target){
       case TARGET_SERVO_HEAD_VERT:
       case TARGET_SERVO_HEAD_HORZ:
       {
         if(cmd == CMD_SERVO_SET){
           setServo(target, clientTarget.getValue());
         } else {
           moveServo(target, clientTarget.getValue());
         }
         break;
       }
       case TARGET_LASER_HEAD:
          
          if(cmd == CMD_LASER_ON){
            digitalWrite(LASER_PIN, HIGH);
          } else {
            digitalWrite(LASER_PIN, LOW);
          }
       break; 
    }
  }

} 

Servo getServo(int target){
    //Return servo based on ID
     switch(target){
      case TARGET_SERVO_HEAD_VERT:
        return servos[0];
      case TARGET_SERVO_HEAD_HORZ:
        return servos[1];
    };
    //Return default servo
    return servos[0]; 
}

void initServos(){
  servos[0].attach(SERVO1);
  setServo(TARGET_SERVO_HEAD_VERT, 90);
  servos[1].attach(SERVO2);
  setServo(TARGET_SERVO_HEAD_HORZ, 90);
}

void setServo(int servo, int pos){
   getServo(servo).write(pos);
   servoPos[servo] = pos; 
}

void moveServo(int servo, int dest){
   int pos = servoPos[servo] + dest;
   setServo(servo, pos);
}

