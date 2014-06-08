#include "TargetRegistration.h"

Adafruit_NeoPixel pixel = Adafruit_NeoPixel(60, PIN, NEO_GRB + NEO_KHZ800);

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

void ClientTarget::begin(int pin, Stream *theStream){
  initPixel();
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  etData.begin(details(dataStruct), theStream);
};

void ClientTarget::initPixel(){
  pixel.begin();
  pixel.setBrightness(50);
  pixel.show();
  setPixelColor(COLOR_REGISTRATION);
}

void ClientTarget::setPixelColor(int red, int green, int blue){
  pixel.setPixelColor(0, red, green, blue);
  pixel.show();
}

void ClientTarget::registerListener(int target){
  //Display registration color
  setPixelColor(COLOR_REGISTRATION);
  //Loop until registered
  bool registered = false;
  while(registered == false){
	delay(250);
	//Send command
	sendData(COM_REGISTRATION, target, REGISTER, 0);
    etData.sendData();
	//Clear cmd to verify registration
	dataStruct.cmd = 0;
	delay(10);
    //Check if received acknowledgement
    if(etData.receiveData()){
      if(dataStruct.cmd == target){
        registered = true; 
		setPixelColor(COLOR_OK);
      } else {
		//Set error color
		setPixelColor(COLOR_ERROR);
	  }
    }
  } 
}

void ClientTarget::sendData(int tar, int cmd, int val, int dur){
	dataStruct.tar = tar;
    dataStruct.cmd = cmd;
    dataStruct.val = val;
    dataStruct.dur = dur;
    etData.sendData();
}

bool ClientTarget::receiveData(){
	return etData.receiveData();
}

int ClientTarget::getTarget(){
	return dataStruct.tar;
}

int ClientTarget::getValue(){
	return dataStruct.val;
}

int ClientTarget::getCommand(){
	return dataStruct.cmd;
}

int ClientTarget::getDuration(){
	return dataStruct.dur;
}