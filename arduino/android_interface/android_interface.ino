#include <Servo.h>
#include <EasyTransfer.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <Adafruit_NeoPixel.h>

#define REGISTER 1
#define UNREGISTER 2
#define REGISTRATION 1
#define PIXEL_PIN            13
#define NUM_OF_PIXELS 12

Adafruit_NeoPixel pixel = Adafruit_NeoPixel(NUM_OF_PIXELS, PIXEL_PIN, NEO_GRB + NEO_KHZ800);

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

//Used for rotating the ring pixels
struct Color{
   int red;
   int green;
   int blue; 
};
int offset = 0;
unsigned long lastRotate = 0;
Color colors[NUM_OF_PIXELS];

void setup()
{
  //Set all colors off by default
  for(int i = 0; i < NUM_OF_PIXELS; i++){
    setPixelColor(i,50,0,0);
  }
  
  initPixel();
  initCom();
  //acc.powerOn();
  pinMode(PIXEL_PIN, OUTPUT);
  //Set start time
  lastRotate = millis();
}

void loop()
{
  delay(5);
  //Rotate
  if(millis() > lastRotate + 200){
    lastRotate = millis();
    offset++;
    if(offset > NUM_OF_PIXELS){
      offset = 0; 
    }
    //Update the pixels
    for(int pixelPosition = 0; pixelPosition < NUM_OF_PIXELS; pixelPosition++){
        //Set the pixel color
        int red = colors[pixelPosition].red;
        int green = colors[pixelPosition].green;
        int blue = colors[pixelPosition].blue;
        
        //Rotate the pixel
        int pixelNum = pixelPosition + offset;
        //Loop back to start if needed
        if(pixelNum > NUM_OF_PIXELS){
           pixelNum -= NUM_OF_PIXELS; 
        }
        //Display the color
        pixel.setPixelColor(pixelNum - 1, red, green, blue);
    }
    pixel.show();
  }
  
  
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
          delay(20);
          etData[dataLine].sendData();
          //Mark line as connected
          setPixelColor(dataLine, 75,0,255);
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

  //Check if Android device is connected
//  if (acc.isConnected()) {
//    byte msg[3];
//    
//    //Read from connected Android device
//    int len = acc.read(msg, sizeof(msg), 1);
//
//    if (len > 0) {
//      dataStruct.tar = msg[0];
//      dataStruct.cmd = msg[1];
//      dataStruct.val = msg[2];
//      dataStruct.dur = 0;
//      routeData();
//    }
//  } 

  
}


void routeData(){
  int target = dataStruct.tar;
  for(int listener = 0; listener < DATA_LISTENERS; listener++){
     if(listeners[listener].isTarget(target)){
       etData[listener].sendData();
       lastRotate -= 150;
       setPixelColor(listener + 4, 0,255,50);
     } else {
       setPixelColor(listener + 4, 0,50,0);
     }
  } 
          //Send data to Android device if connected
//        if (acc.isConnected()) {
//          byte msg[3];
//          msg[0] = tar;
//          msg[1] = cmd;
//          msg[2] = val;
//          acc.write(msg, 3);
//        }
}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  Serial.begin(115200);
  etData[0].begin(details(dataStruct), &Serial); 
  Serial1.begin(115200);
  etData[1].begin(details(dataStruct), &Serial1);
  Serial2.begin(115200);
  etData[2].begin(details(dataStruct), &Serial2); 
  Serial3.begin(115200);
  etData[3].begin(details(dataStruct), &Serial3);
  //Set initial pixel colors for data lines
  setPixelColor(0,255,0,100);
  setPixelColor(1,255,0,100);
  setPixelColor(2,255,0,100);
  setPixelColor(3,255,0,100);
}

void initPixel(){
  pixel.begin();
  pixel.setBrightness(50);
  pixel.show();
}

/**
* Records the pix color to set into a single int
*/
void setPixelColor(int pixelNum, int red, int green, int blue){
  //Save the color
  colors[pixelNum].red = red;
  colors[pixelNum].green = green;
  colors[pixelNum].blue = blue;
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

