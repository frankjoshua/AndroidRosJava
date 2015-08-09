#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>
#include <NewPing.h>
#include <Average.h>
#include <Adafruit_NeoPixel.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define PIN 5

Adafruit_NeoPixel strip = Adafruit_NeoPixel(15, PIN, NEO_GRB + NEO_KHZ800);
#define COLOR_OK 0,255,90
#define COLOR_BLOCKED 255,0,150
#define COLOR_CLIFF 255,90,0
#define COLOR_LISTENING 255,255,255

//Define pins
#define MOTOR_PIN 6

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

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

int stateList[SONAR_NUM];
unsigned long pingTimer[SONAR_NUM]; // When each pings.
unsigned int cm[SONAR_NUM]; // Store ping distances.
uint8_t currentSensor = 0; // Which sensor is active.
unsigned int mBaseDistance[SONAR_NUM]; //Inital distance of sensors
Average<float> avg[SONAR_NUM] = {
   Average<float>(50),
   Average<float>(50),
   Average<float>(50)
};

NewPing sonar[SONAR_NUM] = { // Sensor object array.
 NewPing(11, 12, MAX_DISTANCE),
 NewPing(9, 10, MAX_DISTANCE),
 NewPing(7, 8, MAX_DISTANCE)
};

SoftwareSerial SWSerial(NOT_A_PIN, MOTOR_PIN);
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

boolean mChange = false;
long mLastChange = 0;
boolean mFavorRight = true;
boolean mTurning = false; 
int mOffPix = 0;
boolean mPixelDir = false;
long mLastPixelUpdate = 0;
int mSpeed = 60;

void setup() {
  Serial.begin(115200);
  Serial.println("Starting..");
  
  strip.begin();
  strip.setBrightness(125);
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
  
  acc.powerOn();
}

void loop() {
  readSensors();
  
  //Update neoPixels
  updatePixels();
  
  //Check if Android device is connected
  if (acc.isConnected()) {
    byte msg[3];
    
    //Read from connected Android device
    int len = acc.read(msg, sizeof(msg), 1);

    if (len > 0) {
      //Stop on any message
      ST.motor(RIGHT, -10);
      ST.motor(LEFT, -10);
      setPixels(0, 14, COLOR_LISTENING);
      delay(5000);
      ST.motor(RIGHT, mSpeed);
      ST.motor(LEFT, mSpeed);
    }
  }
  
  if(mChange || millis() - mLastChange > 1000){
    //Only change if some time has elapsed or not turning
    if(!mTurning || millis() - mLastChange > 2000){
      mLastChange = millis();
      //UpdateSpeed
      mSpeed = map(analogRead(A0), 0, 1023, 0, 127);
      Serial.print(mSpeed);
      //Display current info
      Serial.print("L ");
      int diff = mBaseDistance[SENSOR_LEFT] - cm[SENSOR_LEFT];
      Serial.print(diff);
      Serial.print(" C ");
      diff = mBaseDistance[SENSOR_CENTER] - cm[SENSOR_CENTER];
      Serial.print(diff);
      Serial.print(" R ");
      diff = mBaseDistance[SENSOR_RIGHT] - cm[SENSOR_RIGHT];
      Serial.println(diff);

      
      mChange = false;
      if(stateList[SENSOR_CENTER] != DISTANCE_OK && stateList[SENSOR_LEFT] != DISTANCE_OK && stateList[SENSOR_RIGHT] != DISTANCE_OK){
        //All three blocked
        mTurning = true;
        if(mFavorRight){
          ST.motor(RIGHT, 20);
          ST.motor(LEFT, -20);
        } else {
          ST.motor(RIGHT, -20);
          ST.motor(LEFT, 20);
        }        
      } else if(stateList[SENSOR_CENTER] == DISTANCE_OK && stateList[SENSOR_LEFT] == DISTANCE_OK && stateList[SENSOR_RIGHT] == DISTANCE_OK){
          //All three OK
         Serial.println("Forward");
         mTurning = false;
         ST.motor(RIGHT, mSpeed);
         ST.motor(LEFT, mSpeed);
      } else if (stateList[SENSOR_CENTER] != DISTANCE_OK){
        //Center Blocked
        if(mFavorRight){
          ST.motor(RIGHT, mSpeed);
          ST.motor(LEFT, -mSpeed);
        } else {
          ST.motor(RIGHT, -mSpeed);
          ST.motor(LEFT, mSpeed);
        }
      } else if(stateList[SENSOR_LEFT] != DISTANCE_OK) {
        //Left Blocked
         Serial.println("Right");
         mFavorRight = true;
         mTurning = true;
         ST.motor(RIGHT, -mSpeed / 2);
         ST.motor(LEFT, -mSpeed);
      } else {
        //Right Blocked
         Serial.println("Left");
         mFavorRight = false;
         mTurning = true;
         ST.motor(RIGHT, -mSpeed);
         ST.motor(LEFT, -mSpeed / 2);
      }
    }
  }
  
  
}

void updatePixels() {
  if(millis() - mLastPixelUpdate > 50){
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
    
    setPixels(mOffPix, mOffPix, 0, 0, 0);
    if(mPixelDir){
      mOffPix++;
    } else {
      mOffPix--;
    } 
    if(mOffPix > 14){
      mOffPix = 14;
      mPixelDir = !mPixelDir;
    } else if (mOffPix < 0){
      mOffPix = 0;
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
    mBaseDistance[i] = avg[i].mean();
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
    if(cm[i] > mBaseDistance[i] + 55){
      curState = DISTANCE_OK;
    } else if(cm[i] < 40) {
      curState = DISTANCE_BLOCKED;
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
      avg[currentSensor].push(distance);
      //cm[currentSensor] = avg[currentSensor].mean();
      cm[currentSensor] = distance;
    }
  }
}
