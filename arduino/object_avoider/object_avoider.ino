#include <SoftwareSerial.h>
#include <SPI.h>
#include <SabertoothSimplified.h>
#include <NewPing.h>
#include <Adafruit_NeoPixel.h>
//#include <Usb.h>
//#include <AndroidAccessory.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
#include <ArduinoJson.h> //https://github.com/bblanchon/ArduinoJson
#include <PocketBot.h> //https://github.com/frankjoshua/PocketBot
#include <RunningMedian.h> //https://github.com/RobTillaart/Arduino
#include <MemoryFree.h>


//Define pins
#define MOTOR_PIN 45
#define NEO_PIN 44

Adafruit_NeoPixel strip = Adafruit_NeoPixel(15, NEO_PIN, NEO_GRB + NEO_KHZ800);
#define COLOR_OK 0,255,90
#define COLOR_BLOCKED 255,0,150
#define COLOR_CLIFF 255,90,0
#define COLOR_LISTENING 255,255,255

//Define motors
#define LEFT 2
#define RIGHT 1

//Define Sensors
#define SENSOR_CENTER 0
#define SENSOR_LEFT 1
#define SENSOR_RIGHT 2

//Define ping sensor settings
#define DISTANCE_CLIFF 1
#define DISTANCE_OK 2
#define DISTANCE_BLOCKED 3
#define SONAR_NUM     3 // Number or sensors.
#define MAX_DISTANCE 400 // Max distance in cm.
#define PING_INTERVAL 50 // Milliseconds between pings.

//AndroidAccessory acc("Google, Inc.",
//		     "DemoKit",
//		     "DemoKit Arduino Board",
//		     "1.0",
//		     "http://www.android.com",
//		     "0000000012345678");

int stateList[SONAR_NUM];
unsigned long pingTimer[SONAR_NUM]; // When each pings.
int cm[SONAR_NUM]; // Store ping distances.
uint8_t currentSensor = 0; // Which sensor is active.
int mBaseDistance[SONAR_NUM]; //Inital distance of sensors
RunningMedian avg[SONAR_NUM] = {
   RunningMedian(3),
   RunningMedian(3),
   RunningMedian(3)
};

NewPing sonar[SONAR_NUM] = { // Sensor object array.
 NewPing(34, 35, MAX_DISTANCE),
 NewPing(32, 33, MAX_DISTANCE),
 NewPing(30, 31, MAX_DISTANCE)
};

SoftwareSerial SWSerial(NOT_A_PIN, MOTOR_PIN);
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

#define BLUEFRUIT_SPI_CS               8
#define BLUEFRUIT_SPI_IRQ              7
#define BLUEFRUIT_SPI_RST              6
#define BLUEFRUIT_SPI_SCK              13
#define BLUEFRUIT_SPI_MISO             12
#define BLUEFRUIT_SPI_MOSI             11
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST); //Hardware
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST); //Software


boolean mChange = false;
long mLastChange = 0;
boolean mFavorRight = true;
boolean mTurning = false; 
int mOffPix = 6;
boolean mPixelDir = false;
long mLastPixelUpdate = 0;
int mSpeed = 60;
long mRandomDelay = 2000;
long mLastHumanSpotted = 0;
long mLastManualControl = 0;
int mLeftPower = mSpeed;
int mRightPower = mSpeed;

int mDestHeading = 90;
int mHeading = 0;

#define HUMAN_DELAY_MILLIS 3000

PocketBot pocketBot;

void setup() {
  Serial.begin(115200);
  Serial.println("Starting..");
  
  initBluetooth();

  //Turn on LED
  pinMode(10, OUTPUT);
  digitalWrite(10, LOW);
  
  //Start up Neo Pixel
  strip.begin();
  strip.setBrightness(100);
  strip.show(); // Initialize all pixels to 'off'
  
  //Used for Motor Controler
  SWSerial.begin(9600);
  
  Serial.println("2 sec delay");
  delay(2000);
  
  //Init ping sensors
  initSensors();
  
  for(int i = 0; i < SONAR_NUM; i++){
    stateList[i] = DISTANCE_OK;
  }
  
  pocketBot.begin(&ble);
}

void initBluetooth(){
     /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(true) )
  {
    Serial.println(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
    while(1);
  }
  Serial.println( F("OK!") );
  /* Disable command echo from Bluefruit */
  ble.echo(false);
  ble.verbose(false);  // debug info is a little annoying after this point!
  /* Wait for connection */
  int count = 0;
  while (! ble.isConnected() && count < 5) {
      Serial.print(".");
      delay(500);
      //count ++;
  }
  // Set module to DATA mode
  ble.setMode(BLUEFRUIT_MODE_DATA);
}

void loop() {
  readSensors();

  
  //Reset to forward after a while
  if(millis() - mLastHumanSpotted > HUMAN_DELAY_MILLIS && millis() - mLastManualControl > HUMAN_DELAY_MILLIS){
    mLeftPower = mSpeed;
    mRightPower = mSpeed; 
    if(mHeading > 10 && mHeading <= 180){
      //Turn left
      mLeftPower -= 10;
      mRightPower += 10;
    } else if (mHeading < 350 && mHeading >= 180){
      //Turn right
      mLeftPower += 10;
      mRightPower -= 10;
    }
    strip.setBrightness(100); 
  } else {
    strip.setBrightness(255);
  }
  
  updatePixels();
  
  updateMotors();
  
  readBluetooth();
  
    //Check if Android device is connected
//  if (acc.isConnected()) {
//    byte msg[3];
//    
//    //Read from connected Android device
//    int len = acc.read(msg, sizeof(msg), 1);
//
//    if (len > 0) {
//      //Stop on any message
//      ST.motor(RIGHT, -10);
//      ST.motor(LEFT, -10);
//      setPixels(0, 14, COLOR_LISTENING);
//      delay(5000);
//      ST.motor(RIGHT, mSpeed);
//      ST.motor(LEFT, mSpeed);
//    }
//  }

}


void readBluetooth(){
  // Echo received data
  if(pocketBot.read()){
    
    JsonObject& root = pocketBot.getJson();
    if(!root.success()){
     Serial.println("*************** JSON ERROR **************"); 
     return;
    }
    //Debuging output
    Serial.println("-----------------   JSON    ----------------");
    root.printTo(Serial);
    Serial.println(millis());
    
    if(!root.containsKey("action")){
      if(millis() > mLastManualControl + HUMAN_DELAY_MILLIS){
        mLastHumanSpotted = millis();
        float x = root["FACE_X"];
        float y = root["FACE_Y"];
        float z = root["FACE_Z"];
        mHeading = root["heading"];
        Serial.print(" HEADING = ");
        Serial.println(mHeading);
//        Serial.print(x);
//        Serial.print(",");
//        Serial.print(y);
//        int speed = 0;
//        if(z > 0.3){
//          //Face is close so stop
//          speed = 0;
//        } else if (z > .4) {
//          //Face is too close - back up
//          speed = -mSpeed;
//        } else {
//          speed = mapFloat(y, 0.3, 0.6, -mSpeed, mSpeed);
//        }
//        int dir = mapFloat(x, 0.8, 1.12, -mSpeed/2, mSpeed/2);
//        mLeftPower =  constrain(speed - dir, -mSpeed, mSpeed);
//        mRightPower = constrain(speed + dir, -mSpeed, mSpeed);
//        Serial.print(",");
//        Serial.print(mLeftPower);
//        Serial.print(",");
//        Serial.println(mRightPower);
      }
    } else {
      Serial.println("Manual Control");
      //Manual Control
      mLastManualControl = millis();
      mLeftPower = -mSpeed;
      mRightPower = -mSpeed;
    }
  } else {
    Serial.print("freeMemory()=");
    Serial.println(freeMemory());
    pocketBot.printRawTo(Serial);
    Serial.println("");
  }
}

void updateMotors(){
  if(mChange || millis() - mLastChange > mRandomDelay){
    //Only change if some time has elapsed or not turning
    if(!mTurning || millis() - mLastChange > mRandomDelay){
      mLastChange = millis();
      mRandomDelay = random(50, 200);
      //mRandomDelay = 0;
      //UpdateSpeed
      mSpeed = map(analogRead(A0), 0, 1023, 0, 127);
      Serial.print(mSpeed);
      //Display current info
      Serial.print(" L");
      int diff = cm[SENSOR_LEFT];
      Serial.print(diff);
      Serial.print(" C");
      diff = cm[SENSOR_CENTER];
      Serial.print(diff);
      Serial.print(" R");
      diff = cm[SENSOR_RIGHT];
      Serial.println(diff);

      
      mChange = false;
      if(stateList[SENSOR_CENTER] == DISTANCE_CLIFF || stateList[SENSOR_LEFT] == DISTANCE_CLIFF || stateList[SENSOR_RIGHT] == DISTANCE_CLIFF){
        //Any cliff detection is an error stop to be safe
        mTurning = true;
        ST.motor(RIGHT, 0);
        ST.motor(LEFT, 0);
      } else if(stateList[SENSOR_CENTER] != DISTANCE_OK && stateList[SENSOR_LEFT] != DISTANCE_OK && stateList[SENSOR_RIGHT] != DISTANCE_OK){
        //All three blocked
        turnFavorite(); 
      } else if(stateList[SENSOR_LEFT] != DISTANCE_OK && stateList[SENSOR_RIGHT] != DISTANCE_OK){
        //Left and right blocked, keep turing last direction
        turnFavorite();
      } else if(stateList[SENSOR_CENTER] == DISTANCE_OK && stateList[SENSOR_LEFT] == DISTANCE_OK && stateList[SENSOR_RIGHT] == DISTANCE_OK){
          //All three OK
         forward();
      } else if (stateList[SENSOR_RIGHT] != DISTANCE_OK){
         //Right Blocked
         turnLeft();
      } else if(stateList[SENSOR_LEFT] != DISTANCE_OK) {
        //Left Blocked
         turnRight();
      } else {
        //Center Blocked
        turnFavorite();
      }
    }
  }
}

void forward(){
 Serial.print("Forward ");
 Serial.print(mLeftPower);
 Serial.print(" ");
 Serial.println(mRightPower);
 mTurning = false;
 ST.motor(RIGHT, mRightPower);
 ST.motor(LEFT, mLeftPower);
}

void turnFavorite(){
  if(mFavorRight){
    turnRight();
  } else {
    turnLeft();
  }
}

void turnRight(){
  Serial.println("Right");
  mTurning = true;
  mFavorRight = true;
  ST.motor(RIGHT, mSpeed);
  ST.motor(LEFT, -mSpeed);
}

void turnLeft(){
  Serial.println("Left");       
  mTurning = true;
  mFavorRight = false;
  ST.motor(RIGHT, -mSpeed);
  ST.motor(LEFT, mSpeed);
}


void updatePixels() {
  if(millis() - mLastPixelUpdate > map(mSpeed, 0, 127, 10, 100)){
    mLastPixelUpdate = millis();
    for(int i = 0; i < 3; i++){
      int startPix = 0;
      int endPix = 4;
      if( i == SENSOR_CENTER ) {
        startPix = 5;
        endPix = 9;
      } else if ( i == SENSOR_LEFT) {
        startPix = 10;
        endPix = 14;
      }
      
      if(stateList[i] == DISTANCE_OK){
        setPixels(startPix, endPix, COLOR_OK);
      } else if (stateList[i] == DISTANCE_BLOCKED){
        setPixels(startPix, endPix, COLOR_BLOCKED);
      } else {
        setPixels(startPix, endPix, COLOR_CLIFF);
      }
    }
    
    
    setPixels(mOffPix, mOffPix, 125, 125, 125);
    
    if(mPixelDir){
      setPixels(mOffPix - 1, mOffPix - 1, 255, 255, 255);
      setPixels(mOffPix + 1, mOffPix + 1, 0, 0, 0);
      mOffPix++;
    } else {
      setPixels(mOffPix - 1, mOffPix - 1, 0, 0, 0);
      setPixels(mOffPix + 1, mOffPix + 1, 255, 255, 255);
      mOffPix--;
    } 
    if(mOffPix > 13){
      mOffPix = 13;
      mPixelDir = !mPixelDir;
    } else if (mOffPix < 1){
      mOffPix = 1;
      mPixelDir = !mPixelDir;
    }
  }
}

void setPixels(int startPix, int endPix, int red, int green, int blue){
   
   for (int i = startPix; i <= endPix; i++){
      strip.setPixelColor(i, red, green, blue); 
   }
   strip.show();
}

void initSensors(){
  pingTimer[0] = millis() + 75; // First ping start in ms.
  for (uint8_t i = 1; i < SONAR_NUM; i++){
    pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
  }
   
  calibrate();
}

void calibrate(){
  //Get initial sensor distance
  for(uint8_t i = 0; i < 100; i++){
    delay(10);
    readSensors();
  }
  for (uint8_t i = 0; i < SONAR_NUM; i++) {
    mBaseDistance[i] = avg[i].getMedian();
  }
}

void readSensors(){
  for (uint8_t i = 0; i < SONAR_NUM; i++) {
    if (millis() >= pingTimer[i]) {
      pingTimer[i] += PING_INTERVAL * SONAR_NUM;
      if (i == 0 && currentSensor == SONAR_NUM - 1)
        oneSensorCycle(); // Do something with results.
      sonar[currentSensor].timer_stop();
      currentSensor = i;
      //cm[currentSensor] = 0;
      sonar[currentSensor].ping_timer(echoCheck);
    }
  }
}

void oneSensorCycle() { // Do something with the results.
  for (uint8_t i = 0; i < SONAR_NUM; i++) {
    int curState = 0;
    if(cm[i] < 35){
      curState = DISTANCE_BLOCKED;
    } else if(cm[i] > 380) {
      curState = DISTANCE_CLIFF;
    } else {
      curState = DISTANCE_OK;
    } 
   
    if(setState(i, curState)){
      //Report state change
      mChange = true;
    } 
  }
}

boolean setState(int sensor, int newState){
  boolean change = newState != stateList[sensor];
  stateList[sensor] = newState;
  return change;
}

void echoCheck() { // If ping echo, set distance to array.
  if (sonar[currentSensor].check_timer()){
    int distance = sonar[currentSensor].ping_result / US_ROUNDTRIP_CM;
    //Filter 0 distance because it's probally an error
    if(distance != 0){
      avg[currentSensor].add(distance);
      cm[currentSensor] = avg[currentSensor].getMedian();
      //cm[currentSensor] = distance;
    }
  }
}

float mapFloat(float x, float in_min, float in_max, float out_min, float out_max){
 return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
