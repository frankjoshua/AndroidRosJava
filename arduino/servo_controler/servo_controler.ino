#include <Servo.h>
#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

#define NEO_PIN 13

//Servo Control Pins
#define  SERVO1         11
#define  SERVO2         12

ClientTarget clientTarget;

Servo servos[2];
 
void setup() 
{ 
  initServos();
  
  Serial.begin(9600);
  clientTarget.begin(NEO_PIN, &Serial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_SERVO_HEAD_VERT);
  clientTarget.registerListener(TARGET_SERVO_HEAD_HORZ);  
} 
 
void loop() 
{ 

  if(clientTarget.receiveData()){
    Servo servo = getServo(clientTarget.getTarget());
    servo.write(clientTarget.getValue()); 
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
  servos[0].write(90);
  servos[1].attach(SERVO2);
  servos[1].write(90);
}

