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

#define NEO_PIN 13

#define CENTER 0
#define LEFT 1
#define RIGHT 2
#define BACK 3
 
#define SONAR_NUM     4 // Number or sensors.
#define MAX_DISTANCE 400 // Max distance in cm.
#define PING_INTERVAL 33 // Milliseconds between pings.

ClientTarget clientTarget;

int stateList[SONAR_NUM];

unsigned long pingTimer[SONAR_NUM]; // When each pings.
unsigned int cm[SONAR_NUM]; // Store ping distances.
uint8_t currentSensor = 0; // Which sensor is active.

//Echo BLUE
//Trigger YELLOW
//NewPing(Trigger, Echo, MAX_DISTANCE)
NewPing sonar[SONAR_NUM] = { // Sensor object array.
  NewPing(11, 12, MAX_DISTANCE),
  NewPing(9, 10, MAX_DISTANCE),
  NewPing(7, 8, MAX_DISTANCE),
  NewPing(5, 6, MAX_DISTANCE)
};
 
void setup() {
  initCom();
  
  //Register as Listener
  clientTarget.registerListener(TARGET_PING_CENTER);
  clientTarget.registerListener(TARGET_PING_LEFT);
  clientTarget.registerListener(TARGET_PING_RIGHT);
  //clientTarget.registerListener(TARGET_PING_BACK);
  
  //Init ping sensors
  initSensors();
}
 
void loop() {
  readSensors();
  readSerial();  
}

void readSerial(){
  if(clientTarget.receiveData()){
      switch(clientTarget.getValue()){
         case DISTANCE_TOUCHING:
           clientTarget.setPixelColor(255,0,0);
         break;
         case DISTANCE_NEAR:
           clientTarget.setPixelColor(255,255,0);
         break;
         case DISTANCE_FAR:
           clientTarget.setPixelColor(0,255,0);
         break;
      }
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
    if(cm[i] > 50){
      curState = DISTANCE_FAR;
    } else if(cm[i] > 30) {
      curState = DISTANCE_NEAR;
    } else {
      curState = DISTANCE_TOUCHING;
    } 
   
    if(setState(i, curState)){
      //Report state change
      clientTarget.sendData(sensorToTarget(i), 0, stateList[i], 0); 
    } 
  }
}

boolean setState(int sensor, int newState){
  boolean change = newState != stateList[sensor];
  stateList[sensor] = newState;
  return change;
}

int sensorToTarget(int sensor){
   switch(sensor){
     case CENTER:
      return TARGET_PING_CENTER;
     case LEFT:
      return TARGET_PING_LEFT;
     case RIGHT:
      return TARGET_PING_RIGHT;
     case BACK:
      return TARGET_PING_BACK;
     default:
       return TARGET_PING_CENTER;
   } 
}

void initCom(){
  Serial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &Serial);
  clientTarget.setPixelColor(COLOR_OK);
}

void initSensors(){
  pingTimer[0] = millis() + 75; // First ping start in ms.
  for (uint8_t i = 1; i < SONAR_NUM; i++){
    pingTimer[i] = pingTimer[i - 1] + PING_INTERVAL;
  }
}
