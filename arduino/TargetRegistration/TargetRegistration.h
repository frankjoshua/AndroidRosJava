#ifndef TargetRegistration_h
#define TargetRegistration_h
#include "Stream.h"
#include <EasyTransfer.h>
#include <Adafruit_NeoPixel.h>

//Led Colors
#define COLOR_REGISTRATION 255,0,100
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
#define TARGET_MOTOR_LEFT 20
#define TARGET_MOTOR_RIGHT 21

//COMMANDS
#define COMMAND_FORWARD 10
#define COMMAND_BACKWARD 11
#define COMMAND_LEFT 12
#define COMMAND_RIGHT 13

#define NEO_PIN            13

class ClientTarget {
public:
void begin(int pin, Stream *theStream);
void setPixelColor(int red, int green, int blue);
void registerListener(int target);
bool receiveData();
void sendData(int tar, int cmd, int val, int dur);
int getTarget();
int getCommand();
int getValue();
int getDuration();
private:
void initPixel();
};

#endif

