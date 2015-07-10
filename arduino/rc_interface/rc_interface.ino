#include <EasyTransfer.h>
#include <SoftwareSerial.h>

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
#define CH1 0
#define CH2 1
#define CH3 2
#define CH4 3
#define CH5 4
#define CH6 5
int channels[6];

SoftwareSerial SWSerial(11, 12);

#define FILTER_READINGS 5
struct BasicFilter {
  // Define the number of samples to keep track of.  The higher the number,
  // the more the readings will be smoothed, but the slower the output will
  // respond to the input.  Using a constant rather than a normal variable lets
  // use this value to determine the size of the readings array.  
  int readings[FILTER_READINGS];      // the readings from the analog input
  int index;                  // the index of the current reading
  int total;                  // the running total
  int average;                // the average
};
int getFiterValue(BasicFilter filter, int value);
//Creat a filter for each channel
BasicFilter mFilters[6];

void setup() {
  initCom();
  initRc();
  
  for(int i = 0; i < 6; i++){
   for(int k = 0; k < FILTER_READINGS; k++){
    mFilters[i].readings[k] = 0;
   } 
  }
}


void loop() {
  delay(1);

  readRc();
  
  
  
  //Send data
  if(channels[CH5] > 1200){
    //Motors
    int minSpeed =  constrain(map(channels[CH5], MIN_INPUT, MAX_INPUT, 0, MIN_SPEED), MIN_SPEED, MAX_SPEED);
    int maxSpeed =  constrain(map(channels[CH5], MIN_INPUT, MAX_INPUT, 0, MAX_SPEED), MIN_SPEED, MAX_SPEED);
    int mSpeed = map(channels[CH2], MIN_INPUT, MAX_INPUT, minSpeed, maxSpeed);
    int dir = map(channels[CH1], MAX_INPUT, MIN_INPUT, minSpeed, maxSpeed);
    int powerR = constrain(mSpeed + dir, minSpeed, maxSpeed);
    int powerL = constrain(mSpeed - dir, minSpeed, maxSpeed);
    
    dataStruct.tar = 20;
    dataStruct.val = powerR;
    etData.sendData();
    dataStruct.tar = 21;
    dataStruct.val = powerL;
    etData.sendData();
    Serial.print("L: ");
    Serial.print(powerL);
    Serial.print(" R: ");
    Serial.println(powerR);
  } else if(channels[CH5] < 1100 && channels[CH5] > MIN_INPUT){
    //PAN
    dataStruct.tar = 10;
    dataStruct.val = map(channels[CH3], MAX_INPUT, MIN_INPUT, 0, 180);
    etData.sendData();
    //TILT
    dataStruct.tar = 11;
    dataStruct.val = map(channels[CH6], MAX_INPUT, MIN_INPUT, 0, 180);
    etData.sendData();
  }
  //Serial.println(channels[CH6]);
}

void readRc(){
  channels[CH1] = pulseIn(CH1_PIN, HIGH, 25000); // Read the pulse width of 
  channels[CH2] = pulseIn(CH2_PIN, HIGH, 25000); // each channel
  channels[CH3] = pulseIn(CH3_PIN, HIGH, 25000);
  channels[CH4] = pulseIn(CH4_PIN, HIGH, 25000);
  channels[CH5] = pulseIn(CH5_PIN, HIGH, 25000);
  channels[CH6] = pulseIn(CH6_PIN, HIGH, 25000);
  //Average out readings to remove spikes
  Serial.println(channels[CH5]);
  for(int i = 0; i < 6; i++){
    //If 0 the the channel is diconnected
    if(channels[i] > 0){
      BasicFilter& filter = mFilters[i];
      //Subtract the last reading
      filter.total = filter.total - filter.readings[filter.index];
      //Add the new reading
      filter.readings[filter.index] = channels[i];
      //Add the total
      filter.total = filter.total + filter.readings[filter.index];
      filter.index = filter.index + 1;
      if(filter.index >= FILTER_READINGS){
        filter.index = 0; 
      }
      filter.average = filter.total / FILTER_READINGS;
      channels[i] = filter.average;
    }
  }
}

//int getFilterValue(BasicFilter filter, int value){
//  //Subtract the last reading
//  filter.total -= filter.readings[filter.index];
//  //Add the new reading
//  filter.readings[filter.index] = value;
//  //Add the total
//  filter.total += value;
//  if(filter.index >= filter.numberOfReadings){
//     filter.index = 0; 
//  }
//  filter.average = filter.total / filter.numberOfReadings;
//  return filter.average;
//  return 0;
//}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  Serial.begin(115200);
  SWSerial.begin(115200);
  etData.begin(details(dataStruct), &SWSerial); 
}

void initRc(){
  pinMode(CH1_PIN, INPUT); // Set our input pins as such
  pinMode(CH2_PIN, INPUT);
  pinMode(CH3_PIN, INPUT);
  pinMode(CH4_PIN, INPUT);
  pinMode(CH5_PIN, INPUT);
  pinMode(CH6_PIN, INPUT);
}
