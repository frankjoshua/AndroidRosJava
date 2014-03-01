#include <Servo.h>
#include <EasyTransfer.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define REGISTER 1
#define UNREGISTER 2
#define REGISTRATION 1

class Listener {
  public:
    int targets[32];
    int pointer;
    void registerTarget(int target);
    void unregisterTarget(int target);
    boolean isTarget(int target);
};

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

const int DATA_CHANNELS = 4;
EasyTransfer etData[DATA_CHANNELS];

//Listeners 
//0-3 = Serial Ports
//4 USB
const int DATA_LISTENERS = 5;
Listener listeners[DATA_LISTENERS];

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
  acc.powerOn();
  pinMode(13, OUTPUT);
}

void loop()
{
  //Loop through each input
  for(int dataLine = 0; dataLine < DATA_CHANNELS; dataLine++){
    if(etData[dataLine].receiveData()){
      int tar = dataStruct.tar;
      int val = dataStruct.val;
      int cmd = dataStruct.cmd;
      //Check for registration request
      if(tar == REGISTRATION){
        if(val == REGISTER){
          //Register Listener
          listeners[dataLine].registerTarget(cmd);
          //Respond that registration was successful
          etData[dataLine].sendData();
        } else if(val == UNREGISTER){
          //Register Listener
          listeners[dataLine].unregisterTarget(cmd);
        }
      } else {
        //Route to correct output
        routeData();
      }
    }
  }

  //Read from connected Android device
  if (acc.isConnected()) {
    byte msg[3];
    int len = acc.read(msg, sizeof(msg), 1);

    if (len > 0) {
      dataStruct.tar = msg[0];
      dataStruct.cmd = msg[1];
      dataStruct.val = msg[2];
      dataStruct.dur = 0;
      routeData();
    }
  } 

  delay(10);
}

void routeData(){
  for(int l = 0; l < DATA_LISTENERS; l++){
     if(listeners[l].isTarget(dataStruct.tar)){
        etData[l].sendData();
     }
  }
}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  Serial.begin(9600);
  etData[0].begin(details(dataStruct), &Serial); 
  Serial1.begin(9600);
  etData[1].begin(details(dataStruct), &Serial1);
  Serial2.begin(9600);
  etData[2].begin(details(dataStruct), &Serial2); 
  Serial3.begin(9600);
  etData[3].begin(details(dataStruct), &Serial3);  
}

void Listener::registerTarget(int target){
  //First unregister target if registered
  unregisterTarget(target);
  //Add to list of registered targets
  targets[pointer] = target;
  pointer++;
  //Reset pointer if too large
  if(pointer > 31){
     pointer = 0; 
  }
}

void Listener::unregisterTarget(int target){
  for(int i = 0; i < 32; i++){
     if(targets[i] == target){
        targets[i] = 0;
     } 
  }
}

boolean Listener::isTarget(int target){
  for(int i = 0; i < 32; i++){
     if(targets[i] == target){
        return true;
     } 
  }
  return false;
}

