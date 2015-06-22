#include <Servo.h>
#include <EasyTransfer.h>
#include <Usb.h>
#include <AndroidAccessory.h>
#include <Adafruit_NeoPixel.h>
#include <Wire.h>
#include <EasyTransferI2C.h>

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

const int DATA_CHANNELS = 3;
EasyTransfer etData[DATA_CHANNELS];
#define I2C_SLAVE_ADDRESS 9
EasyTransferI2C etI2Cdata;

//Listeners 
//0-2 = Serial Ports
//3 I2C port
#define I2C_PORT 3
const int DATA_LISTENERS = DATA_CHANNELS + 1;
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
  
  
  //Loop through each Serial line
  for(int dataLine = 0; dataLine < DATA_CHANNELS; dataLine++){
    if(etData[dataLine].receiveData()){
      readData(dataLine);
    }
  }
  //Check I2C
  if(etI2Cdata.receiveData()){
    readData(I2C_PORT);
  }

}

/**
*
*/
void readData(int dataLine){
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
        if(dataLine > DATA_CHANNELS - 1){
           etI2Cdata.sendData(dataLine);
        } else {
           etData[dataLine].sendData();
        }
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

/**
* Send data to all active targets
*/
void routeData(){
  int target = dataStruct.tar;
  for(int listener = 0; listener < DATA_LISTENERS; listener++){
     if(listeners[listener].isTarget(target)){
       if(listener > DATA_CHANNELS - 1){
         //Send through I2C
         etI2Cdata.sendData(listener);
       } else {
         //Send though Serial
         etData[listener].sendData();
       }
       lastRotate -= 150;
       setPixelColor(listener + 4, 0,255,50);
     } else {
       setPixelColor(listener + 4, 0,50,0);
     }
  } 
}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
//  Serial.begin(115200); //Serial0 not working currently
//  etData[0].begin(details(dataStruct), &Serial); 
  Serial1.begin(115200);
  etData[0].begin(details(dataStruct), &Serial1);
  Serial2.begin(115200);
  etData[1].begin(details(dataStruct), &Serial2); 
  Serial3.begin(115200);
  etData[2].begin(details(dataStruct), &Serial3);
  //Set I2C
  Wire.begin(I2C_SLAVE_ADDRESS);
  etI2Cdata.begin(details(dataStruct), &Wire);
  //define handler function on receiving data
  Wire.onReceive(receive);
}

void initPixel(){
  pixel.begin();
  pixel.setBrightness(50);
  //Set initial pixel colors for data lines
  setPixelColor(0,255,0,100);
  setPixelColor(1,255,0,100);
  setPixelColor(2,255,0,100);
  setPixelColor(3,255,0,100);
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

/**
* Called when data passed to I2C
*/
void receive(int numBytes) {}

