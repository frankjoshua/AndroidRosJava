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
int servoDest[2];

SoftwareSerial SWSerial(4, 5);


void setup() 
{ 
  Serial.begin(115200);
  Serial.println("Starting...");
  
  
  SWSerial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_SERVO_PAN);
  clientTarget.registerListener(TARGET_SERVO_TILT); 
  
  initServos();
  Serial.println("Ready...");
} 
 
void loop() 
{ 
  delay(10);
  
  if(clientTarget.receiveData()){
    Serial.println("...");
    int target = clientTarget.getTarget();
    int cmd = clientTarget.getCommand();
    int val = clientTarget.getValue();
    Serial.println(val);
    switch(target){
       case TARGET_SERVO_PAN:
       if(cmd == COMMAND_RIGHT){
         servoDest[0] -= val;
       } else if (cmd == COMMAND_LEFT){
         servoDest[0] += val;
       } else {
         servoDest[0] = val;
       }
       break;
       case TARGET_SERVO_TILT:
       servoDest[1] = val;
       break;
    }
  }

  if(servoDest[0] > servoPos[0]){
    servoPos[0]++;
  } else if(servoDest[0] < servoPos[0]){
    servoPos[0]--;
  }
  if(servoDest[1] > servoPos[1]){
    servoPos[1]++;
  } else if(servoDest[1] < servoPos[1]){
    servoPos[1]--;
  }
  setServo(TARGET_SERVO_PAN, servoPos[0]);
  setServo(TARGET_SERVO_TILT, servoPos[1]);
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
  
  setServo(TARGET_SERVO_PAN, 90);
  servoDest[0] = 90;
  setServo(TARGET_SERVO_TILT, 90);
  servoDest[1] = 90;
  servos[0].attach(SERVO1);
  servos[1].attach(SERVO2);
}

void setServo(int servo, int pos){
   getServo(servo).write(pos);
   servoPos[servo] = pos; 
}

void moveServo(int servo, int dest){
   int pos = servoPos[servo] + dest;
   setServo(servo, pos);
}

