#include <EasyTransfer.h>

#define DOWN true;
#define UP false;

// Store the Arduino pin associated with each input
const byte PIN_BUTTON_SELECT = 2; // Select button is triggered when joystick is pressed

const byte PIN_BUTTON_RIGHT = 3;
const byte PIN_BUTTON_UP = 4;
const byte PIN_BUTTON_DOWN = 5;
const byte PIN_BUTTON_LEFT = 6;

const byte PIN_ANALOG_X = 0;
const byte PIN_ANALOG_Y = 1;

//START Easy Transfer
EasyTransfer etData;
struct COM_DATA_STRUCTURE{
  //put your variable definitions here for the data you want to receive
  //THIS MUST BE EXACTLY THE SAME ON THE OTHER ARDUINO
  int tar;
  int cmd;
  int val;
  int dur;
};

//give a name to the group of data
COM_DATA_STRUCTURE dataStruct;
//END Easy Transfer

void setup() {
  
  initCom();
  initBoard();
  
}

//Holds button values
boolean left;
boolean right;
boolean up;
boolean down;
boolean select;
int x;
int y;

void loop() {
  delay(10);
  
  readValues();

  if(left == true){
    
  }

}

void readValues(){
    // Print the current values of the inputs (joystick and
  // buttons) to the console.
  left = digitalRead(PIN_BUTTON_LEFT);
  right = digitalRead(PIN_BUTTON_RIGHT); 
  up = digitalRead(PIN_BUTTON_UP); 
  down = digitalRead(PIN_BUTTON_DOWN);
  x = analogRead(PIN_ANALOG_X); 
  y = analogRead(PIN_ANALOG_Y); 
  select = digitalRead(PIN_BUTTON_SELECT);
}

void initBoard(){
    //    * HIGH = the button is not pressed
  //    * LOW = the button is pressed  
  pinMode(PIN_BUTTON_RIGHT, INPUT);  
  digitalWrite(PIN_BUTTON_RIGHT, HIGH);
  
  pinMode(PIN_BUTTON_LEFT, INPUT);  
  digitalWrite(PIN_BUTTON_LEFT, HIGH);
  
  pinMode(PIN_BUTTON_UP, INPUT);  
  digitalWrite(PIN_BUTTON_UP, HIGH);
  
  pinMode(PIN_BUTTON_DOWN, INPUT);  
  digitalWrite(PIN_BUTTON_DOWN, HIGH);
  
  pinMode(PIN_BUTTON_SELECT, INPUT);  
  digitalWrite(PIN_BUTTON_SELECT, HIGH); 
}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  Serial.begin(9600);
  etData.begin(details(dataStruct), &Serial); 
}
