#include <Wire.h>
#include <EasyTransferI2C.h>
#include <adk.h> //https://github.com/felis/USB_Host_Shield_2.0
#include <usbhub.h> //https://github.com/felis/USB_Host_Shield_2.0
#include <SPI.h>
#include <PocketBot.h> //https://github.com/frankjoshua/PocketBot


//Init USB connection for android device
USB Usb;
//These two lines give support for using a powered USB hub
USBHub hub0(&Usb);
USBHub hub1(&Usb);

/*
* These parameters must match what the app expects
*/
ADK adk(&Usb, "Tesseract Mobile LLC",
		     "PocketBot",
		     "",
		     "1.0",
		     "http://pocketbot.io",
		     "1");

//START Easy Transfer
#define I2C_SLAVE_ADDRESS 9
EasyTransferI2C etData;
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

/** This will be used to decode messages from the Android device */
PocketBot pocketBot;
/* Allocate space for the decoded message. */
PocketBotMessage message = PocketBotMessage_init_zero;

void setup()
{
  initCom();
  
  //Connect to Android device
  if(Usb.Init() == -1) {
    Serial.println(F("OSCOKIRQ failed to assert"));
    while(1); //halt
  }
  Serial.println(F("Waiting for Android device..."));
  while(adk.isReady() == false){
     Usb.Task();
  }
  Serial.println("");
  Serial.println(F("Connected"));

}

void loop(){
  uint8_t rcode;
  byte msg[1] = { 0x00 };
  uint16_t len = sizeof(msg);
  Usb.Task();
  if(adk.isReady()){ 
    len = sizeof(msg);
    rcode = adk.RcvData(&len, msg);
    while(!rcode && len > 0){
      Usb.Task();
      if(pocketBot.read(msg[0], message)){
        Serial.println(millis());
        Serial.print(F("FaceID = ")); Serial.println(message.face.faceId);
        Serial.print(F("FaceX = ")); Serial.println(message.face.faceX);
        Serial.print(F("FaceY = ")); Serial.println(message.face.faceY);
        Serial.print(F("FaceZ = ")); Serial.println(message.face.faceZ);
        Serial.print(F("JoyX = ")); Serial.println(message.control.joyX);
        Serial.print(F("JoyY = ")); Serial.println(message.control.joyY);
        Serial.print(F("JoyZ = ")); Serial.println(message.control.joyZ);
        Serial.print(F("Proximity = ")); Serial.println(message.sensor.proximity);
        Serial.print(F("Heading = ")); Serial.println(message.sensor.heading);
      }
      rcode = adk.RcvData(&len, msg);
    }
  }
  
}

void sendData(int tar, int cmd, int val){
  dataStruct.tar = tar;
  dataStruct.cmd = cmd;
  dataStruct.val = val;
  dataStruct.dur = 0;
  etData.sendData(I2C_SLAVE_ADDRESS);
}

void initCom(){
  Serial.begin(115200);
//  etData.begin(details(dataStruct), &Serial);
  Wire.begin();
  etData.begin(details(dataStruct), &Wire);
}

