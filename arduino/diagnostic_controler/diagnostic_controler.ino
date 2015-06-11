#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <Wire.h>
#include <LiquidTWI.h>

#define NEO_PIN 13

ClientTarget clientTarget;
SoftwareSerial SWSerial(4, 5);

// Connect via i2c, default address #0 (A0-A2 not jumpered)
LiquidTWI lcd(0);

void setup() 
{ 
  lcd.begin(16,2);
  lcd.print("LCD: OK");
  lcd.setBacklight(HIGH);
  SWSerial.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial);
  
  //Register as Listener
  lcd.setCursor(0,1);
  lcd.print("Registering");
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
  lcd.setCursor(0,1);
  lcd.print("Ready      ");
  
} 

bool mActive = false; 
void loop() 
{ 
  delay(50);
  if(clientTarget.receiveData()){
    lcd.setCursor(0,0);
    mActive = !mActive;
    if(mActive){
      clientTarget.setPixelColor(COLOR_ACTIVE);
      lcd.print("*              ");
    } else{
      clientTarget.setPixelColor(0,0,0);
      lcd.print("-              ");
    }
  } else {
    clientTarget.setPixelColor(COLOR_OK);
  }
  
  
} 








