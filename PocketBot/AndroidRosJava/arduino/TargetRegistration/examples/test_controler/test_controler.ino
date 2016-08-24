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
  clientTarget.registerListener(10);
  clientTarget.registerListener(11);
} 
 
void loop() 
{ 
  delay(10);
  
  if(clientTarget.receiveData()){
    clientTarget.setPixelColor(COLOR_ACTIVE);
  } else {
    clientTarget.setPixelColor(COLOR_OK);
  }

} 








