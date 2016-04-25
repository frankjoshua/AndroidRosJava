// include the library code:
#include <LiquidCrystal.h>

// initialize the library with the numbers of the interface pins
LiquidCrystal lcd(8, 9, 4, 5, 6, 7);
String lastString = "";
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  
  Serial.begin(115200);
  
  lcd.print("Ready...");
}

void loop() {
  delay(50);
  
  // print the string when a newline arrives:
  if (stringComplete) {
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print(lastString);
    lcd.setCursor(0,1);
    lcd.print(inputString);
    //Save the input sting
    lastString = inputString;
    // clear the string:
    inputString = "";
    stringComplete = false;
    delay(250);
  }
 
}

/*
  SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 */
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    Serial.print(inChar);
    
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
      return;
    }
    
    // add it to the inputString:
    inputString += inChar;
  }
}
