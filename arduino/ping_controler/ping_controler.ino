#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

#define NEO_PIN 13

int duration;                                                          //Stores duration of pulse in
int distance;                                                        // Stores distance
int sensorpin = 7;      

ClientTarget clientTarget;

int state = DISTANCE_FAR;

void setup() 
{ 
  Serial.begin(9600);
  clientTarget.begin(NEO_PIN, &Serial);
  clientTarget.setPixelColor(COLOR_OK);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_PING_CENTER);
  //clientTarget.registerListener(11);
} 
 
void loop() 
{ 
  delay(10);
  readSensor();
  
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

void readSensor(){
  pinMode(sensorpin, OUTPUT);
  digitalWrite(sensorpin, LOW);                          // Make sure pin is low before sending a short high to trigger ranging
  delayMicroseconds(2);
  digitalWrite(sensorpin, HIGH);                         // Send a short 10 microsecond high burst on pin to start ranging
  delayMicroseconds(10);
  digitalWrite(sensorpin, LOW);                                  // Send pin low again before waiting for pulse back in
  pinMode(sensorpin, INPUT);
  duration = pulseIn(sensorpin, HIGH);                        // Reads echo pulse in from SRF05 in micro seconds
  distance = duration/58; 

  int curState = 0;
  if(distance > 10){
    curState = DISTANCE_FAR;
  } else if(distance > 4) {
    curState = DISTANCE_NEAR;
  } else {
    curState = DISTANCE_TOUCHING;
  } 
 
  if(setState(curState)){
    clientTarget.sendData(TARGET_PING_CENTER, 0, state, 0); 
  } 
}

boolean setState(int newState){
  boolean change = newState != state;
  state = newState;
  return change;
}






