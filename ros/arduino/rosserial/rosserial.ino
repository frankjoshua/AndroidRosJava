/*
 * rosserial Subscriber Example
 * Blinks an LED on callback
 */
#include <ros.h>
#include <geometry_msgs/Twist.h>
#include <SoftwareSerial.h>
#include <SabertoothSimplified.h>

SoftwareSerial SWSerial(NOT_A_PIN, 12);
SabertoothSimplified ST(SWSerial); // Use SWSerial as the serial port.
#define MOTOR_LEFT 2
#define MOTOR_RIGHT 1

#define MAX_SPEED 65

/*
* Helper function to map Floats, based on Arduino map()
*/
float mapfloat(float x, float in_min, float in_max, float out_min, float out_max){
 return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

ros::NodeHandle nh;

int mSpeed = 0;
int mDir = 0;

int mGoalLeftPower = 0;
int mGoalRightPower = 0;
int mCurrentLeftPower = 0;
int mCurrentRightPower = 0;

unsigned long mTimeElapsed = 0;
unsigned long mLastUpdate = 0;

void messageCb( const geometry_msgs::Twist& toggle_msg){
  if(toggle_msg.linear.x > 0.05){
    digitalWrite(13, HIGH);
  } else {
    digitalWrite(13, LOW);
  }

  mSpeed = mapfloat(toggle_msg.linear.x, -1, 1, -MAX_SPEED, MAX_SPEED);
  mDir = mapfloat(toggle_msg.angular.z, -1, 1, -MAX_SPEED, MAX_SPEED);

  mGoalLeftPower = constrain(mSpeed + mDir, -MAX_SPEED, MAX_SPEED);
  mGoalRightPower = constrain(mSpeed - mDir, -MAX_SPEED, MAX_SPEED);

}

ros::Subscriber<geometry_msgs::Twist> sub("/cmd_vel", &messageCb );

//Speed 127 / 2000
const float SPEED = 0.0535;
// = 1 / SPEED rounded up
const float updateSpeed = 20; 

int updateGoal(int goal, int current, int timeElapsed){
  float change = timeElapsed * SPEED;
  if(current + change < goal){
    return current + change;
  }
  if(current - change > goal){
    return current - change;
  }
  return current;
}

void setup()
{
  pinMode(13, OUTPUT);
  SWSerial.begin(9600);
  nh.initNode();
  nh.subscribe(sub);
}

void loop()
{
  nh.spinOnce();
  //Udpate motors if needed
  mTimeElapsed = millis() - mLastUpdate;
  if(mTimeElapsed > updateSpeed){
    if(mCurrentRightPower != mGoalRightPower || mCurrentLeftPower != mGoalLeftPower){
      mCurrentLeftPower = updateGoal(mGoalLeftPower, mCurrentLeftPower, mTimeElapsed);
      mCurrentRightPower = updateGoal(mGoalRightPower, mCurrentRightPower, mTimeElapsed);     
      ST.motor(MOTOR_LEFT, constrain(mCurrentLeftPower, -127, 127));
      ST.motor(MOTOR_RIGHT, constrain(mCurrentRightPower, -127, 127));
    }
    mLastUpdate = millis();
  }
  delay(1);
}
