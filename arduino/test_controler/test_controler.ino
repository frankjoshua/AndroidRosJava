#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

#define NEO_PIN 13

ClientTarget clientTarget;

void setup() 
{ 
  Serial.begin(9600);
  clientTarget.begin(NEO_PIN, &Serial);
  
  //Register as Listener
  clientTarget.registerListener(TARGET_SERVO_HEAD_VERT);
  clientTarget.registerListener(TARGET_SERVO_HEAD_HORZ);
} 
 
void loop() 
{ 

  if(clientTarget.receiveData()){
    clientTarget.setPixelColor(COLOR_ACTIVE);
  } else {
    clientTarget.setPixelColor(COLOR_OK);
  }

} 








