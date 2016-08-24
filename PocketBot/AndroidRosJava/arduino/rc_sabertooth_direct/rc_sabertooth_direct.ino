#include <SoftwareSerial.h>
#include <RunningMedian.h>
#include <SabertoothSimplified.h>

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

SoftwareSerial SWSerial(NOT_A_PIN, 12);
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

RunningMedian filter[CHANNELS]{
	RunningMedian(SAMPLES),
	RunningMedian(SAMPLES),
	RunningMedian(SAMPLES),
	RunningMedian(SAMPLES),
	RunningMedian(SAMPLES),
	RunningMedian(SAMPLES)
};

void setup() {
	initCom();
	initRc();

}


void loop() {
	delay(1);

	readRc();

	//Send data
	if (channels[CH5] > 1200) {
		//Motors
		int minSpeed = constrain(map(channels[CH5], MIN_INPUT, MAX_INPUT, 0, MIN_SPEED), MIN_SPEED, MAX_SPEED);
		int maxSpeed = constrain(map(channels[CH5], MIN_INPUT, MAX_INPUT, 0, MAX_SPEED), MIN_SPEED, MAX_SPEED);
		int mSpeed = map(channels[CH2], MIN_INPUT, MAX_INPUT, minSpeed, maxSpeed);
		int dir = map(channels[CH1], MAX_INPUT, MIN_INPUT, minSpeed, maxSpeed);
		int powerR = constrain(mSpeed + dir, minSpeed, maxSpeed);
		int powerL = constrain(mSpeed - dir, minSpeed, maxSpeed);


		Serial.print("L: ");
		Serial.print(powerL);
		Serial.print(" R: ");
		Serial.println(powerR);
		ST.motor(RIGHT, powerR);
		ST.motor(LEFT, powerL);
	}
}

void readRc() {
        int ch1 = pulseIn(CH1_PIN, HIGH);
	filter[CH1].add(ch1); // Read the pulse width of 
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

	Serial.print(ch1);
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

void initCom() {
	//start the easy transfer library, pass in the data details and the name of the serial port. Can be Serial, Serial1, Serial2, etc.
	Serial.begin(115200);
	SWSerial.begin(9600);
}

void initRc() {
	pinMode(CH1_PIN, INPUT); // Set our input pins as such
	pinMode(CH2_PIN, INPUT);
	pinMode(CH3_PIN, INPUT);
	pinMode(CH4_PIN, INPUT);
	pinMode(CH5_PIN, INPUT);
	pinMode(CH6_PIN, INPUT);
}
