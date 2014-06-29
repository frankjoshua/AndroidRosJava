// ---------------------------------------------------------
// This example code was used to successfully communicate
// with 15 ultrasonic sensors. You can adjust the number of
// sensors in your project by changing SONAR_NUM and the
// number of NewPing objects in the "sonar" array. You also
// need to change the pins for each sensor for the NewPing
// objects. Each sensor is pinged at 33ms intervals. So, one
// cycle of all sensors takes 495ms (33 * 15 = 495ms). The
// results are sent to the "oneSensorCycle" function which
// currently just displays the distance data. Your project
// would normally process the sensor results in this
// function (for example, decide if a robot needs to turn
// and call the turn function). Keep in mind this example is
// event-driven. Your complete sketch needs to be written so
// there's no "delay" commands and the loop() cycles at
// faster than a 33ms rate. If other processes take longer
// than 33ms, you'll need to increase PING_INTERVAL so it
// doesn't get behind.
// ---------------------------------------------------------
#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <NewPing.h>
#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>
#include <Servo.h> 

#define NEO_PIN 13

#define CENTER 0
#define LEFT 1
#define RIGHT 2
#define BACK 3

#define PIN_SERVO_CENTER 2
#define PIN_SERVO_RIGHT 1
#define PIN_SERVO_LEFT 3

#define LEFT_MOTOR 2
#define RIGHT_MOTOR 1

#define FAR 10
#define NEAR 5

#define SPEED 127
 
#define SONAR_NUM     4 // Number or sensors.
#define MAX_DISTANCE 400 // Max distance in cm.
#define PING_INTERVAL 33 // Milliseconds between pings.

#define FACE_FORWARD 65
#define FACE_DOWN 110

ClientTarget clientTarget;

SoftwareSerial SWSerial(NOT_A_PIN, 4); // RX on no pin (unused), TX on pin 11 (to S1).
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

int stateList[SONAR_NUM];

unsigned long pingTimer[SONAR_NUM]; // When each pings.
unsigned int cm[SONAR_NUM]; // Store ping distances.
uint8_t currentSensor = 0; // Which sensor is active.
 
NewPing sonar[SONAR_NUM] = { // Sensor object array.
  NewPing(11, 12, MAX_DISTANCE), //Center
  NewPing(9, 10, MAX_DISTANCE), //Left
  NewPing(7, 8, MAX_DISTANCE), //Right
  NewPing(5, 6, MAX_DISTANCE) //Back
};

Servo servos[3];

void setup() {
  Serial.begin(9600);
  initCom();
  
  //Register as Listener
//  clientTarget.registerListener(TARGET_PING_CENTER);
//  clientTarget.registerListener(TARGET_PING_LEFT);
//  clientTarget.registerListener(TARGET_PING_RIGHT);
//  clientTarget.registerListener(TARGET_PING_BACK);
  
  //Init ping sensors
  initSensors();
  initServos();
  
  SWSerial.begin(9600);
  ST.motor(RIGHT_MOTOR, SPEED);
  ST.motor(LEFT_MOTOR, SPEED);
}

boolean flip = true;
void loop() {
  readSensors();
 
  flip = !flip;
  if(flip){
    servos[CENTER].write(FACE_FORWARD);
    servos[LEFT].write(FACE_FORWARD);
  } else {
    servos[CENTER].write(FACE_DOWN);
    servos[LEFT].write(FACE_DOWN);
  }
}


void stateChange(){
  //Choose direction to move
  if(stateList[CENTER] != DISTANCE_FAR || stateList[RIGHT] != DISTANCE_FAR){
    ST.motor(RIGHT_MOTOR, SPEED);
    ST.motor(LEFT_MOTOR, -SPEED);
  } else if (stateList[LEFT] != DISTANCE_FAR){
    ST.motor(RIGHT_MOTOR, -SPEED);
    ST.motor(LEFT_MOTOR, SPEED);
  } else {
    ST.motor(RIGHT_MOTOR, SPEED);
    ST.motor(LEFT_MOTOR, SPEED);
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
      cm[currentSensor] = 0;
      sonar[currentSensor].ping_timer(echoCheck);
    }
  }
}

void echoCheck() { // If ping echo, set distance to array.
  if (sonar[currentSensor].check_timer())
    cm[currentSensor] = sonar[currentSensor].ping_result / US_ROUNDTRIP_CM;
}
 
void oneSensorCycle() { // Do something with the results.
  for (uint8_t i = 0; i < SONAR_NUM; i++) {
    int curState = 0;
    if(cm[i] > FAR){
      curState = DISTANCE_FAR;
    } else if(cm[i] > NEAR) {
      curState = DISTANCE_NEAR;
    } else {
      curState = DISTANCE_TOUCHING;
    } 
    Serial.print(cm[i]);
   
    
    if(setState(i, curState)){
      stateChange();
      //Report state change
      Serial.print(" ");
      Serial.print(curState);
    } 
     Serial.print(" ** ");
  }
  Serial.print("\n");
}

boolean setState(int sensor, int newState){
  boolean change = newState != stateList[sensor];
  stateList[sensor] = newState;
  return change;
}


void initCom(){
  clientTarget.begin(NEO_PIN, &Serial);
  clientTarget.setPixelColor(COLOR_OK);
}

void initSensors(){
  pingTimer[0] = millis() + 75; // First ping start in ms.
  for (uint8_t i = 1; i < SONAR_NUM; i++){
    pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
  }
}

void initServos(){
  servos[CENTER].attach(PIN_SERVO_CENTER);
  servos[LEFT].attach(PIN_SERVO_LEFT);
  
  servos[CENTER].write(FACE_FORWARD);
  servos[LEFT].write(FACE_FORWARD);
  }
