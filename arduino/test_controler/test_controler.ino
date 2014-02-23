#include <EasyTransfer.h>
#include <SoftwareSerial.h>

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
 
void setup() 
{ 
  initCom();
  pinMode(13, OUTPUT);
  
  //Register as Listener
  dataStruct.tar = 1;
  dataStruct.cmd = 10;
  dataStruct.val = 1;
  dataStruct.dur = 0;
  etData.sendData();
} 
 
void loop() 
{ 
  delay(10);
  
  if(etData.receiveData()){
     digitalWrite(13, HIGH);
     delay(100);
     digitalWrite(13, LOW);
  }

} 

void initCom(){
  Serial.begin(9600);
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  etData.begin(details(dataStruct), &Serial); 
}
