#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

#define PIN            13
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
 
void setup() 
{ 
  initCom();
  initPixel();
  
  //Register as Listener
  registerListener(10);
  registerListener(11);
  setPixelColor(0,255,100);
} 
 
void loop() 
{ 
  delay(10);
  
  if(etData.receiveData()){
    setPixelColor(0,255,75);
  } else {
    setPixelColor(75,0,255);
  }

} 

void initCom(){
  Serial.begin(9600);
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  etData.begin(details(dataStruct), &Serial); 
}

void initPixel(){
  pixel.begin();
  pixel.setBrightness(50);
  pixel.show();
  setPixelColor(255,0,100);
}

void setPixelColor(int red, int green, int blue){
  pixel.setPixelColor(0, red, green, blue);
  pixel.show();
}

void registerListener(int cmd){
  boolean registered = false;
  while(registered == false){
    dataStruct.tar = 1;
    dataStruct.cmd = cmd;
    dataStruct.val = 1;
    dataStruct.dur = 0;
    etData.sendData();
    //Check if recieved acknologment
    if(etData.receiveData()){
      if(dataStruct.cmd == cmd){
         registered = true; 
      }
    }
    delay(10);
  } 
}
