#ifndef TargetRegistration_h
#define TargetRegistration_h
#include "Stream.h"
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

//Led Colors
#define COLOR_REGISTRATION 255,0,100
#define COLOR_STARTING 0,255,0
#define COLOR_ERROR 255,0,0
#define COLOR_OK 75,0,255
#define COLOR_ACTIVE 0,255,255

//Communication values
#define COM_SPEED 115200
#define COM_REGISTRATION 1
#define REGISTER 1
#define UNREGISTER 2

//States of distance
#define DISTANCE_TOUCHING 1
#define DISTANCE_NEAR 2
#define DISTANCE_FAR 3

//TARGETS
#define TARGET_PING_CENTER 50
#define TARGET_PING_LEFT 51
#define TARGET_PING_RIGHT 52
#define TARGET_PING_BACK 53
#define TARGET_SERVO_PAN 10
#define TARGET_SERVO_TILT 11
#define TARGET_MOTOR_LEFT 20
#define TARGET_MOTOR_RIGHT 21
#define TARGET_GPS 90

//COMMANDS
#define COMMAND_FORWARD 10
#define COMMAND_BACKWARD 11
#define COMMAND_LEFT 12
#define COMMAND_RIGHT 13
#define COMMAND_SET_WAYPOINT 21
#define COMMAND_REMOVE_WAYPOINT 22
#define COMMAND_NEXT_WAYPOINT 23
#define COMMAND_LAST_WAYPOINT 24
#define COMMAND_GPS_LOCATION 91
#define COMMAND_GPS_HEADING 92
#define COMMAND_GPS_COMPASS 92

#define NEO_PIN            13

struct COM_DATA_STRUCTURE{
  //put your variable definitions here for the data you want to receive
  //THIS MUST BE EXACTLY THE SAME ON THE OTHER ARDUINO
  int tar;
  int cmd;
  int val;
  int dur;
};

struct Color{
   int red;
   int green;
   int blue; 
};

class ClientTarget {
public:
void begin(int pin, Stream *theStream);
void setPixelColor(int red, int green, int blue);
void registerListener(int target);
bool receiveData();
void sendData(int tar, int cmd, int val, int dur);
void sendData();
int getTarget();
int getCommand();
int getValue();
int getDuration();
private:
Adafruit_NeoPixel* pixel = NULL;
void initPixel();
};

class ServerTarget {
	public:
	void begin();
};

class Listener {
  public:
    int targets[32];
    int pointer;
    void registerTarget(int target);
    void unregisterTarget(int target);
    boolean isTarget(int target);
};

#endif

