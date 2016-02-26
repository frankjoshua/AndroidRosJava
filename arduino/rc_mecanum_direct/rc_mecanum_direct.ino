#include <SoftwareSerial.h>
#include <RunningMedian.h>
#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include "utility/Adafruit_PWMServoDriver.h"

#define MIN_SPEED -127
#define MAX_SPEED 127
#define MIN_INPUT 1010
#define MAX_INPUT 1955
#define LEFT 1
#define RIGHT 2

//RC Values
#define CH1_PIN 3
#define CH2_PIN 4
#define CH3_PIN 5
#define CH4_PIN 6
#define CH5_PIN 7
#define CH6_PIN 8
#define CH1 0
#define CH2 1
#define CH3 2
#define CH4 3
#define CH5 4
#define CH6 5

#define CHANNELS 6
#define SAMPLES 5

int channels[CHANNELS];

Adafruit_MotorShield AFMS = Adafruit_MotorShield();
Adafruit_DCMotor *leftMotor = AFMS.getMotor(1);
Adafruit_DCMotor *rightMotor = AFMS.getMotor(2);
Adafruit_DCMotor *frontLeftMotor = AFMS.getMotor(4);
Adafruit_DCMotor *frontRightMotor = AFMS.getMotor(3);

RunningMedian filter[CHANNELS]{
  RunningMedian(SAMPLES),
  RunningMedian(SAMPLES),
  RunningMedian(SAMPLES),
  RunningMedian(SAMPLES),
  RunningMedian(SAMPLES),
  RunningMedian(SAMPLES)
};

void setup() {
  AFMS.begin();  // create with the default frequency 1.6KHz
  initCom();
  initRc();

}


void loop() {
  delay(1);

  readRc();
  
  //Send data
  if(channels[CH6] > 1200){
    //Motors
    int minSpeed =  constrain(map(channels[CH6], MIN_INPUT, MAX_INPUT, 0, MIN_SPEED), MIN_SPEED, MAX_SPEED);
    int maxSpeed =  constrain(map(channels[CH6], MIN_INPUT, MAX_INPUT, 0, MAX_SPEED), MIN_SPEED, MAX_SPEED);
    int mSpeed = map(channels[CH2], MIN_INPUT, MAX_INPUT, minSpeed, maxSpeed);
    int strafe = map(channels[CH4], MIN_INPUT, MAX_INPUT, minSpeed, maxSpeed);
    int dir = map(channels[CH1], MAX_INPUT, MIN_INPUT, minSpeed, maxSpeed);
    int powerR = constrain(mSpeed + dir - strafe, minSpeed, maxSpeed);
    int powerL = constrain(mSpeed - dir + strafe, minSpeed, maxSpeed);
    int powerRf = constrain(mSpeed + dir + strafe, minSpeed, maxSpeed);
    int powerLf = constrain(mSpeed - dir - strafe, minSpeed, maxSpeed);
    
    Serial.print(strafe);
    Serial.print(" Right Front: ");
    Serial.print(powerRf);
    Serial.print(" Rear: ");
    Serial.println(powerR);
    Serial.print("Left Front: ");
    Serial.print(powerLf);
    Serial.print(" Read: ");
    Serial.println(powerL);
    
        //Set motors to FORWARD or BACKWARD
    if(powerL < 0){
      leftMotor->run(BACKWARD);
    } else {
      leftMotor->run(FORWARD);
    }
    if(powerR < 0){
      rightMotor->run(BACKWARD);
    } else {
      rightMotor->run(FORWARD);
    }
    if (powerLf < 0) {
  	frontLeftMotor->run(BACKWARD);
    } else {
      frontLeftMotor->run(FORWARD);
    }
    if (powerRf < 0) {
	frontRightMotor->run(BACKWARD);
    } else {
	frontRightMotor->run(FORWARD);
    }
    //Adjust power from -127:127 to 1:255
    int frontRight = abs(powerRf) * 2 + 1;
    int frontLeft = abs(powerLf) * 2 + 1;
    int rearRight = abs(powerR) * 2 + 1;
    int rearLeft = abs(powerL) * 2 + 1;
    
    //Set motor speeds
    rightMotor->setSpeed(rearRight);
    leftMotor->setSpeed(rearLeft);		
    frontRightMotor->setSpeed(frontRight);
    frontLeftMotor->setSpeed(frontLeft);
  }
}

void readRc(){
  filter[CH1].add(pulseIn(CH1_PIN, HIGH)); // Read the pulse width of 
  filter[CH2].add(pulseIn(CH2_PIN, HIGH)); // each channel
  filter[CH3].add(pulseIn(CH3_PIN, HIGH));
  filter[CH4].add(pulseIn(CH4_PIN, HIGH));
  filter[CH5].add(pulseIn(CH5_PIN, HIGH));
  filter[CH6].add(pulseIn(CH6_PIN, HIGH));
  //Average out readings to remove spikes
  channels[CH1] = filter[CH1].getMedian();
  channels[CH2] = filter[CH2].getMedian();
  channels[CH3] = filter[CH3].getMedian();
  channels[CH4] = filter[CH4].getMedian();
  channels[CH5] = filter[CH5].getMedian();
  channels[CH6] = filter[CH6].getMedian();
  
  Serial.print(channels[CH1]);
  Serial.print("   ");
  Serial.print(channels[CH2]);
  Serial.print("   ");
  Serial.print(channels[CH3]);
  Serial.print("   ");
  Serial.print(channels[CH4]);
  Serial.print("   ");
  Serial.print(channels[CH5]);
  Serial.print("   ");
  Serial.println(channels[CH6]);
}

void initCom(){
  //start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
  Serial.begin(115200);
}

void initRc(){
  pinMode(CH1_PIN, INPUT); // Set our input pins as such
  pinMode(CH2_PIN, INPUT);
  pinMode(CH3_PIN, INPUT);
  pinMode(CH4_PIN, INPUT);
  pinMode(CH5_PIN, INPUT);
  pinMode(CH6_PIN, INPUT);
}
