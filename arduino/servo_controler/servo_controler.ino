#include <Servo.h>
#include <EasyTransfer.h>
#include <SoftwareSerial.h>

//SErvo Control Pins
#define  SERVO1         11
#define  SERVO2         12

//Command IDs of Servos
const int SERVO_HEAD_VERT=10;
const int SERVO_HEAD_HORZ=11;

EasyTransfer etData; 

struct COM_DATA_STRUCTURE{
  //put your variable definitions here for the data you want to receive
  //THIS MUST BE EXACTLY THE SAME ON THE OTHER ARDUINO
  int tar;
  int cmd;
  int val;
  int dur;
};

//give a name to the group of data
COM_DATA_STRUCTURE dataStruct;

SoftwareSerial serial(2, 3);

Servo servos[2];
 
void setup() 
{ 
  initCom();
  initServos();
  
  //Register as Listener
  dataStruct.tar = 1;
  dataStruct.cmd = 10;
  dataStruct.val = 1;
  dataStruct.dur = 0;
  etData.sendData();
  delay(100);
  dataStruct.tar = 1;
  dataStruct.cmd = 11;
  dataStruct.val = 1;
  dataStruct.dur = 0;
  etData.sendData();
} 
 
 int pos = 0;
void loop() 
{ 
  delay(10);
  
  if(etData.receiveData()){
    Servo servo = getServo(dataStruct.tar);
    servo.write(dataStruct.val); 
  }

} 

Servo getServo(int target){
    //Return servo based on ID
     switch(target){
      case SERVO_HEAD_VERT:
        return servos[0];
      case SERVO_HEAD_HORZ:
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

void initCom(){
  Serial.begin(9600);
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  etData.begin(details(dataStruct), &Serial); 
}
