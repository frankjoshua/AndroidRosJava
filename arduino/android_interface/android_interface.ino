#include <Servo.h>
#include <EasyTransfer.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

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
  acc.powerOn();
  pinMode(13, OUTPUT);
}

void loop()
{
  //Loop through each input
  //Route to correct output
	if (acc.isConnected()) {
                byte msg[3];
		int len = acc.read(msg, sizeof(msg), 1);

		if (len > 0) {
                  dataStruct.tar = msg[0];
                  dataStruct.cmd = msg[1];
                  dataStruct.val = msg[2];
                  dataStruct.dur = 0;
                  digitalWrite(13, LOW);
    	          etData.sendData();
                  delay(100);
                  digitalWrite(13, HIGH);
		}

	} 

	delay(10);
}

void initCom(){
  Serial1.begin(9600);
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  etData.begin(details(dataStruct), &Serial1); 
}

class Listener {
  public:
    uint8_t location;
    uint8_t messageType;  
};

