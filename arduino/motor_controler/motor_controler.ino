#include <TargetRegistration.h>
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>
#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>
#include <SPI.h>

#define NEO_PIN 13



#define LEFT 2
#define RIGHT 1

#define MOTOR_PIN 6

//47.8523 cm diameter wheels
//63500 Pulses per revolution
#define CM_PER_PULSE 0.0007536

ClientTarget clientTarget;

SoftwareSerial SWSerial(NOT_A_PIN, MOTOR_PIN); // RX on no pin (unused), TX on pin 11 (to S1).
SoftwareSerial SWSerial2(4, 5); // RX on no pin (unused), TX on pin 11 (to S1).
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.

// Slave Select pins for encoders 1 and 2
// Feel free to reallocate these pins to best suit your circuit
const int slaveSelectEnc1 = 7;
const int slaveSelectEnc2 = 8;

// These hold the current encoder count.
signed long encoder1count = 0;
signed long encoder2count = 0;

void setup() 
{ 
  Serial.begin(COM_SPEED);
  //Used for Motor Controler
  SWSerial.begin(9600);
  
  //Begin Target Registration
  SWSerial2.begin(COM_SPEED);
  clientTarget.begin(NEO_PIN, &SWSerial2);

  //Register as Listener
  clientTarget.registerListener(TARGET_MOTOR_RIGHT);
  clientTarget.registerListener(TARGET_MOTOR_LEFT);
  //clientTarget.registerListener(TARGET_PING_CENTER);
  clientTarget.registerListener(TARGET_PING_LEFT);
  clientTarget.registerListener(TARGET_PING_RIGHT);

  //initEncoders(); 
  //clearEncoderCount();
} 

int rightSpeed = 0;
int leftSpeed = 0;
float rightSpeedAdjust = 1;
float leftSpeedAdjust = 1;

void loop() 
{ 
  delay(10);

  
  //Check for recieved data
  if(clientTarget.receiveData()){
    //clearEncoderCount();
    int tar = clientTarget.getTarget();
    int val = clientTarget.getValue();
    int cmd = clientTarget.getCommand();
    if(cmd == COMMAND_FORWARD){
        //Forward
       rightSpeed = val;
       leftSpeed = val;
    } else if(cmd == COMMAND_BACKWARD){
       //Backward
       rightSpeed = -val;
       leftSpeed = -val;
    } else if(tar == TARGET_MOTOR_RIGHT){
      rightSpeed = val;
    } else if (tar == TARGET_MOTOR_LEFT){
      leftSpeed = val;
    } else if (tar == TARGET_PING_CENTER || tar == TARGET_PING_LEFT){
      //Adjust speed right
      switch(val){
         case DISTANCE_TOUCHING:
             rightSpeedAdjust = 0.25;
         break;
         case DISTANCE_NEAR:
             rightSpeedAdjust = 0.5;
         break;
         case DISTANCE_FAR:
            rightSpeedAdjust = 1;
         break;
      }
    } else if (clientTarget.getTarget() == TARGET_PING_RIGHT){
      //Adjust speed left
      switch(clientTarget.getValue()){
         case DISTANCE_TOUCHING:
             leftSpeedAdjust = 0.25;
             break;
         case DISTANCE_NEAR:
             leftSpeedAdjust = 0.5;
         break;
         case DISTANCE_FAR:
            leftSpeedAdjust = 1;
         break;
      }
    }
    
    //Send values to the motor controler
    int rightAdjusted = constrain(rightSpeed * rightSpeedAdjust, -127, 127);
    int leftAdjusted = constrain(leftSpeed * leftSpeedAdjust, -127, 127);
    ST.motor(RIGHT, rightSpeed);
    ST.motor(LEFT, leftAdjusted);
    
    Serial.print("R:");
    Serial.print(rightAdjusted);
    Serial.print("    L:");
    Serial.println(leftAdjusted);
  }

// Retrieve current encoder counters
 //encoder1count = readEncoder(1); ,
 //encoder2count = readEncoder(2);
 
 //Serial.println(encoder1count * CM_PER_PULSE);
} 

void initEncoders() {
  
  // Set slave selects as outputs
  pinMode(slaveSelectEnc1, OUTPUT);
  pinMode(slaveSelectEnc2, OUTPUT);
  
  // Raise select pins
  // Communication begins when you drop the individual select signsl
  digitalWrite(slaveSelectEnc1,HIGH);
  digitalWrite(slaveSelectEnc2,HIGH);
  
  SPI.begin();
  
  // Initialize encoder 1
  //    Clock division factor: 0
  //    Negative index input
  //    free-running count mode
  //    x4 quatrature count mode (four counts per quadrature cycle)
  // NOTE: For more information on commands, see datasheet
  digitalWrite(slaveSelectEnc1,LOW);        // Begin SPI conversation
  SPI.transfer(0x88);                       // Write to MDR0
  SPI.transfer(0x03);                       // Configure to 4 byte mode
  digitalWrite(slaveSelectEnc1,HIGH);       // Terminate SPI conversation 

  // Initialize encoder 2
  //    Clock division factor: 0
  //    Negative index input
  //    free-running count mode
  //    x4 quatrature count mode (four counts per quadrature cycle)
  // NOTE: For more information on commands, see datasheet
  digitalWrite(slaveSelectEnc2,LOW);        // Begin SPI conversation
  SPI.transfer(0x88);                       // Write to MDR0
  SPI.transfer(0x03);                       // Configure to 4 byte mode
  digitalWrite(slaveSelectEnc2,HIGH);       // Terminate SPI conversation 
}

long readEncoder(int encoder) {
  
  // Initialize temporary variables for SPI read
  unsigned int count_1, count_2, count_3, count_4;
  long count_value;  
  
  // Read encoder 1
  if (encoder == 1) {
    digitalWrite(slaveSelectEnc1,LOW);      // Begin SPI conversation
    SPI.transfer(0x60);                     // Request count
    count_1 = SPI.transfer(0x00);           // Read highest order byte
    count_2 = SPI.transfer(0x00);           
    count_3 = SPI.transfer(0x00);           
    count_4 = SPI.transfer(0x00);           // Read lowest order byte
    digitalWrite(slaveSelectEnc1,HIGH);     // Terminate SPI conversation 
  }
  
  // Read encoder 2
  else if (encoder == 2) {
    digitalWrite(slaveSelectEnc2,LOW);      // Begin SPI conversation
    SPI.transfer(0x60);                      // Request count
    count_1 = SPI.transfer(0x00);           // Read highest order byte
    count_2 = SPI.transfer(0x00);           
    count_3 = SPI.transfer(0x00);           
    count_4 = SPI.transfer(0x00);           // Read lowest order byte
    digitalWrite(slaveSelectEnc2,HIGH);     // Terminate SPI conversation 
  }
  
  // Calculate encoder count
  count_value = (count_1 << 8) + count_2;
  count_value = (count_value << 8) + count_3;
  count_value = (count_value << 8) + count_4;
  
  return count_value / 4;
}

void clearEncoderCount() {
    
  // Set encoder1's data register to 0
  digitalWrite(slaveSelectEnc1,LOW);      // Begin SPI conversation  
  // Write to DTR
  SPI.transfer(0x98);    
  // Load data
  SPI.transfer(0x00);  // Highest order byte
  SPI.transfer(0x00);           
  SPI.transfer(0x00);           
  SPI.transfer(0x00);  // lowest order byte
  digitalWrite(slaveSelectEnc1,HIGH);     // Terminate SPI conversation 
  
  delayMicroseconds(100);  // provides some breathing room between SPI conversations
  
  // Set encoder1's current data register to center
  digitalWrite(slaveSelectEnc1,LOW);      // Begin SPI conversation  
  SPI.transfer(0xE0);    
  digitalWrite(slaveSelectEnc1,HIGH);     // Terminate SPI conversation   
  
  // Set encoder2's data register to 0
  digitalWrite(slaveSelectEnc2,LOW);      // Begin SPI conversation  
  // Write to DTR
  SPI.transfer(0x98);    
  // Load data
  SPI.transfer(0x00);  // Highest order byte
  SPI.transfer(0x00);           
  SPI.transfer(0x00);           
  SPI.transfer(0x00);  // lowest order byte
  digitalWrite(slaveSelectEnc2,HIGH);     // Terminate SPI conversation 
  
  delayMicroseconds(100);  // provides some breathing room between SPI conversations
  
  // Set encoder2's current data register to center
  digitalWrite(slaveSelectEnc2,LOW);      // Begin SPI conversation  
  SPI.transfer(0xE0);    
  digitalWrite(slaveSelectEnc2,HIGH);     // Terminate SPI conversation 
}
