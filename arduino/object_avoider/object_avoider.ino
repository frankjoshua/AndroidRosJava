#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>
#include <NewPing.h>
#include <Average.h>

//Define pins
#define MOTOR_PIN 6

//Define motors
#define LEFT 2
#define RIGHT 1

//Define ping sensor settings
#define DISTANCE_CLIFF 1
#define DISTANCE_OK 2
#define DISTANCE_BLOCKED 3
#define SONAR_NUM     1 // Number or sensors.
#define MAX_DISTANCE 400 // Max distance in cm.
#define PING_INTERVAL 33 // Milliseconds between pings.

int stateList[SONAR_NUM];
unsigned long pingTimer[SONAR_NUM]; // When each pings.
unsigned int cm[SONAR_NUM]; // Store ping distances.
uint8_t currentSensor = 0; // Which sensor is active.
unsigned int mBaseDistance[SONAR_NUM]; //Inital distance of sensors
Average<float> avg[SONAR_NUM] = {
   Average<float>(10)
};

NewPing sonar[SONAR_NUM] = { // Sensor object array.
 NewPing(11, 12, MAX_DISTANCE)
};

SoftwareSerial SWSerial(NOT_A_PIN, MOTOR_PIN);
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

boolean mChange = false;

void setup() {
  Serial.begin(115200);
  Serial.println("Starting..");
  
  //Used for Motor Controler
  SWSerial.begin(9600);
  
  Serial.println("2 sec delay");
  delay(2000);
  
  //Init ping sensors
  initSensors();
  
  for(int i = 0; i < SONAR_NUM; i++){
    stateList[i] = DISTANCE_OK;
  }
}

void loop() {
  readSensors();
  
  if(mChange){
    Serial.print(mBaseDistance[0]);
    Serial.print("-");
    Serial.print(cm[0]);
    Serial.print("= ");
    int diff = mBaseDistance[0] - cm[0];
    Serial.print(diff);
    Serial.print(" ");
    mChange = false;
    if(stateList[0] == DISTANCE_OK){
       Serial.println("Forward");
       ST.motor(RIGHT, 60);
       ST.motor(LEFT, 60);
    } else {
       Serial.println("Turn");
       ST.motor(RIGHT, 60);
       ST.motor(LEFT, -20);
    }
  }
  
//  Serial.print(mBaseDistance[0]);
//  Serial.print(",");
//  Serial.println(cm[0]);
}

void initSensors(){
  pingTimer[0] = millis() + 75; // First ping start in ms.
  for (uint8_t i = 1; i < SONAR_NUM; i++){
    pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
  }
  
  //Get initial sensor distance
  for(uint8_t i = 0; i < 50; i++){
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
    if(avg[i].mean() > mBaseDistance[i] + 7){
      curState = DISTANCE_CLIFF;
    } else if(avg[i].mean() < mBaseDistance[i] - 7) {
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
      cm[currentSensor] = avg[currentSensor].mean();
    }
  }
}
