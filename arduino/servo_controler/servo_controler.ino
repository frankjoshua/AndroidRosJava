// Arduino pin connected to Pololu Servo Controller
const int servoController = 1;  

// location of Servo plugged into Pololu Servo Controller (defaults 0-7)
const int servoTop = 0;
const int servoBottom = 1;

const int topMax = 5000;
const int topMin = 2000;
const int bottomMax = 3800;
const int bottomMin = 2200;

void setup() {
  Serial.begin(9600);
  servoSetSpeed(servoTop,20);
  servoSetSpeed(servoBottom,20);
  delay(1000);
  rangeTest();
  delay(1000);
  faceFront();
  delay(1000);
}

void loop() {
  
  delay(1000);
  faceFront();
  delay(1000);
  nod(10, 1000);
  nod(10, 1000);
  nod(10, 1000);
  nod(10, 1000);
  delay(1000);
  shake(10,500);
  shake(10,500);
  shake(10,500);
  shake(10,500);  
  delay(1000);
  faceFront();
  delay(1000);
  rangeTest();
  
}

void faceFront(){
  servoMove(servoTop,pos(topMax, topMin, 25));
  servoMove(servoBottom,pos(bottomMax, bottomMin, 50));
}

void nod(int nodSpeed, int nodDelay){
  servoSetSpeed(servoTop,nodSpeed);
  servoMove(servoTop,pos(topMax, topMin, 50));
  delay(nodDelay);
  servoMove(servoTop,pos(topMax, topMin, 25));
  delay(nodDelay);
}

void shake(int shakeSpeed, int nodDelay){
  servoSetSpeed(servoTop,shakeSpeed);
  servoMove(servoBottom,pos(bottomMax, bottomMin, 40));
  delay(nodDelay);
  servoMove(servoBottom,pos(bottomMax, bottomMin, 60));
  delay(nodDelay);
}

int pos(int maxPos, int minPos, int ratio){
  int tick = (maxPos - minPos) / 100;
  return minPos + tick * ratio; 
}

void rangeTest(){
  delay(1000);
  servoMove(servoTop,topMin);
  servoMove(servoBottom,bottomMin);
  delay(1000);
  servoMove(servoTop,topMax);
  servoMove(servoBottom,bottomMax);
  delay(1000);
  servoMove(servoTop,topMin);
  servoMove(servoBottom,bottomMax);
  delay(1000);
  servoMove(servoTop,topMax);
  servoMove(servoBottom,bottomMin);
}

/*
    Move Servo using Pololu Servo Controller
    servo = servo number (default 0-7)
    hornPos = position (500-5500)  {Test your servos maximum positions}
*/
void servoMove(int servo, int pos)
{
  if( pos > 5500 || pos < 500 ) return;   

  // Build a Pololu Protocol Command Sequence
  char cmd[6];
  cmd[0] = 0x80;  // start byte
  cmd[1] = 0x01;  // device id
  cmd[2] = 0x04;  // command number
  cmd[3] = servo; //servo number
  cmd[4] = lowByte(pos >> 7);   // lower byte
  cmd[5] = lowByte(pos & 0x7f); // upper byte

  // Send the command to the Pololu Servo Controller
  for ( int i = 0; i < 6; i++ ){
    Serial.write(cmd[i]);
  }

}

/*
*    Set Servo Speed using Pololu Servo Controller
*    servo = servo number (default 0-7)
*    speed = (1-127) (slowest - fastest) 0 to disable speed control
*/
void servoSetSpeed(int servo, int speed){

   // Build a Pololu Protocol Command Sequence
   char cmd[5];
   cmd[0] = 0x80;     // start byte
   
   cmd[1] = 0x01;     // device id
   cmd[2] = 0x01;     // command number
   cmd[3] = lowByte(servo);    // servo number
   cmd[4] = lowByte(speed);    // speed

  // Send the command to the Pololu Servo Controller
   for ( int i = 0; i < 5; i++ ){
      Serial.write(cmd[i]);
   }
}
