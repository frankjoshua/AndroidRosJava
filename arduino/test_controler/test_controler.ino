#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

#define NEO_PIN 13

ClientTarget clientTarget;

void setup() 
{ 
  Serial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &Serial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
} 
 
void loop() 
{ 

  if(clientTarget.receiveData()){
    clientTarget.setPixelColor(COLOR_ACTIVE);
  } else {
    clientTarget.setPixelColor(COLOR_OK);
  }

} 








