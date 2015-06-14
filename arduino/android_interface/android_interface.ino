#include <Wire.h>
#include <EasyTransferI2C.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

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

void setup()
{
  initCom();
  acc.powerOn();

}

void loop()
{
  delay(5);
  //Send data to Android device if connected
//  if (acc.isConnected()) {
//    byte msg[3];
//    msg[0] = tar;
//    msg[1] = cmd;
//    msg[2] = val;
//    acc.write(msg, 3);
//  }

  //Check if Android device is connected
  if (acc.isConnected()) {
    byte msg[3];
    
    //Read from connected Android device
    int len = acc.read(msg, sizeof(msg), 1);

    if (len > 0) {
      //Send the data to the serial line
      dataStruct.tar = msg[0];
      dataStruct.cmd = msg[1];
      dataStruct.val = msg[2];
      dataStruct.dur = 0;
      etData.sendData(I2C_SLAVE_ADDRESS);
    }
  }
  
}

void initCom(){
//  Serial.begin(115200);
//  etData.begin(details(dataStruct), &Serial);
  Wire.begin();
  etData.begin(details(dataStruct), &Wire);
}

