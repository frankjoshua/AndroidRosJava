#include <EasyTransfer.h>

#define MIN_SPEED -127
#define MAX_SPEED 127
#define MIN_INPUT 1010
#define MAX_INPUT 1955

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

//RC Values
#define CH1_PIN 8
#define CH2_PIN 7
#define CH3_PIN 6
#define CH4_PIN 5
#define CH5_PIN 4
#define CH6_PIN 3
#define CH7_PIN 2
#define CH1 0
#define CH2 1
#define CH3 2
#define CH4 3
#define CH5 4
#define CH6 5
int channels[5];

void setup() {
  initCom();
  initRc();
}


void loop() {
  delay(10);

  readRc();
  
  //Motors
  int mSpeed = map(channels[CH2], MIN_INPUT, MAX_INPUT, MIN_SPEED, MAX_SPEED);
  int dir = map(channels[CH1], MAX_INPUT, MIN_INPUT, MIN_SPEED, MAX_SPEED);
  int powerR = mSpeed + dir;
  int powerL = mSpeed - dir;
  
  //Send data
  dataStruct.tar = 20;
  dataStruct.val = powerR;
  etData.sendData();
  dataStruct.tar = 21;
  dataStruct.val = powerL;
  etData.sendData();
  
}

void readRc(){
  channels[CH1] = pulseIn(CH1_PIN, HIGH, 25000); // Read the pulse width of 
  channels[CH2] = pulseIn(CH2_PIN, HIGH, 25000); // each channel
  channels[CH3] = pulseIn(CH3_PIN, HIGH, 25000);
  channels[CH4] = pulseIn(CH4_PIN, HIGH, 25000);
  channels[CH5] = pulseIn(CH5_PIN, HIGH, 25000);
  channels[CH6] = pulseIn(CH6_PIN, HIGH, 25000);
}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  Serial.begin(9600);
  etData.begin(details(dataStruct), &Serial); 
}

void initRc(){
  pinMode(CH1_PIN, INPUT); // Set our input pins as such
  pinMode(CH2_PIN, INPUT);
  pinMode(CH3_PIN, INPUT);
  pinMode(CH4_PIN, INPUT);
  pinMode(CH5_PIN, INPUT);
  pinMode(CH6_PIN, INPUT);
}
