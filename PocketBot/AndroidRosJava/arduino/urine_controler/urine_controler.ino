const int TxPin = 6;

#include <SoftwareSerial.h>
SoftwareSerial mySerial = SoftwareSerial(255, TxPin);

const int warmupTime = 180 * 4;
// Define the number of samples to keep track of.  The higher the number,
// the more the readings will be smoothed, but the slower the output will
// respond to the input.  Using a constant rather than a normal variable lets
// use this value to determine the size of the readings array.
const int numReadings = 20;

int readings[numReadings];      // the readings from the analog input
int index = 0;                  // the index of the current reading
int total = 0;                  // the running total
int average = 0;                // the average


void setup() {
    
  pinMode(TxPin, OUTPUT);
  digitalWrite(TxPin, HIGH);
  
  mySerial.begin(9600);
  delay(100);
  mySerial.write(12);                 // Clear             
  mySerial.write(17);                 // Turn backlight on
  delay(5);                           // Required delay

  // initialize all the readings to 0: 
  for (int thisReading = 0; thisReading < numReadings; thisReading++)
    readings[thisReading] = 0;
}

const int gasPin = 0;
int gasVal;
const String message = "Urine: ";
const String msgWarming = "Warming up..";

boolean isWarm = false;
int timeElapsed;

void loop() {
  if(isWarm){
    delay(50);
  } else {
    delay(250);
  }
  
  mySerial.write(12); //Clear
  
  if(isWarm == false){
     timeElapsed++;
      
     mySerial.print(msgWarming + String(warmupTime - timeElapsed));

     if(timeElapsed >= warmupTime){
       isWarm = true;
     }
  }
  
  // subtract the last reading:
  total= total - readings[index];         
  // read from the sensor:
  gasVal = analogRead(gasPin);  
  readings[index] = gasVal; 
  // add the reading to the total:
  total= total + readings[index];       
  // advance to the next position in the array:  
  index = index + 1;                    

  // if we're at the end of the array...
  if (index >= numReadings)              
    // ...wrap around to the beginning: 
    index = 0;                           

  // calculate the average:
  average = total / numReadings;
  
  String strGas = String(gasVal) + "/" + String(average); 
  
  mySerial.write(13); //Line return
  mySerial.print(message + strGas);
  
  if(gasVal > average * 1.01){
    mySerial.write(212);
    mySerial.write(220); 
  }
}
