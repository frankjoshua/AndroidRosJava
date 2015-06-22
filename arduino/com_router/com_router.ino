#include <TargetRegistration.h>
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

Adafruit_NeoPixel mPixel = Adafruit_NeoPixel(NUM_OF_PIXELS, PIXEL_PIN, NEO_GRB + NEO_KHZ800);

const int DATA_CHANNELS = 3;
EasyTransfer mTransmiters[DATA_CHANNELS];
//EasyTransfer etData[DATA_CHANNELS];
#define I2C_SLAVE_ADDRESS 9
EasyTransferI2C etI2Cdata;
COM_DATA_STRUCTURE mDataStruct[DATA_CHANNELS];
//Listeners 
//0-2 = Serial Ports
//3 I2C port
#define I2C_PORT 3
const int DATA_LISTENERS = DATA_CHANNELS + 1;
Listener listeners[DATA_LISTENERS];

//COM_DATA_STRUCTURE mDataStruct;

//Used for rotating the ring pixels
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
  updatePixels();
  initCom();
  //Set start time
  lastRotate = millis();
}

void loop()
{
  delay(10);
  Serial.println(lastRotate);
  if(millis() > lastRotate + 200){
    lastRotate = millis();
    updatePixels();
  }

  //Loop through each Serial line
  for(int dataLine = 0; dataLine < DATA_CHANNELS; dataLine++){
//    Serial.print(" dataLine: ");
//    Serial.print(dataLine);
//    Serial.print(" target: ");
//    Serial.print(mDataStruct[dataLine].tar);
//    Serial.print(" value: ");
//    Serial.print(mDataStruct[dataLine].val);
//    Serial.print(" command: ");
//    Serial.println(mDataStruct[dataLine].cmd);
    if(mTransmiters[dataLine].receiveData()){
      int tar = mDataStruct[dataLine].tar;
      int cmd = mDataStruct[dataLine].cmd;
      int val = mDataStruct[dataLine].val;
      int dur = mDataStruct[dataLine].dur;
      readData(dataLine, tar, cmd, val, dur);
     }
  }
  //Check I2C
  //if(etI2Cdata.receiveData()){
    //readData(I2C_PORT);
  //}
  
}

void updatePixels() {
  //Rotate
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
      mPixel.setPixelColor(pixelNum - 1, red, green, blue);
  }
  mPixel.show();
  
}

/**
*
*/
void readData(int dataLine, int tar, int cmd, int val, int dur){
    Serial.print(" dataLine: ");
    Serial.print(dataLine);
    Serial.print(" target: ");
    Serial.print(tar);
    Serial.print(" value: ");
    Serial.print(val);
    Serial.print(" command: ");
    Serial.println(cmd);
    //Check for registration request
    if(tar == REGISTRATION){
      Serial.println("Registration Request");
      if(val == REGISTER){
        Serial.print("Registration for target: ");
        Serial.println(cmd);
        //Register Listener
        listeners[dataLine].registerTarget(cmd);
        //Respond that registration was successful
        //delay(20);
        if(dataLine > DATA_CHANNELS - 1){
           Serial.println("I2C");
           etI2Cdata.sendData(dataLine);
        } else {
           Serial.println("Serial");
           mTransmiters[dataLine].sendData();
        }
        //Mark line as connected
        setPixelColor(dataLine, 75,0,255);
      } else if(val == UNREGISTER){
        //Register Listener
        listeners[dataLine].unregisterTarget(cmd);
      } 
    } else {
        //Route to correct output
        routeData(tar); 
    }
}

/**
* Send data to all active targets
*/
void routeData(int tar){
  for(int listener = 0; listener < DATA_LISTENERS; listener++){
     if(listeners[listener].isTarget(tar)){
       Serial.print("Routing: ");
       Serial.println(listener);
       if(listener > DATA_CHANNELS - 1){
         //Send through I2C
         etI2Cdata.sendData(listener);
       } else {
         //Send though Serial
         mTransmiters[listener].sendData();
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
  Serial.begin(COM_SPEED); //Serial0 not working currently
  Serial.println("Starting Com");
//  etData[0].begin(details(mDataStruct), &Serial); 
  Serial1.begin(COM_SPEED);
  mTransmiters[0].begin(details(mDataStruct[0]), &Serial1);
  Serial2.begin(COM_SPEED);
  mTransmiters[1].begin(details(mDataStruct[1]), &Serial2); 
  Serial3.begin(COM_SPEED);
  mTransmiters[2].begin(details(mDataStruct[2]), &Serial3);
  //Set I2C
  Wire.begin(I2C_SLAVE_ADDRESS);
  //etI2Cdata.begin(details(mDataStruct), &Wire);
  //define handler function on receiving data
  Wire.onReceive(receive);
  Serial.println("Ending Com");
}

void initPixel(){
  mPixel.begin();
  mPixel.setBrightness(50);
  //Set initial pixel colors for data lines
  setPixelColor(0,255,0,100);
  setPixelColor(1,255,0,100);
  setPixelColor(2,255,0,100);
  setPixelColor(3,255,0,100);
  mPixel.show();
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

