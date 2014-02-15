#include <Servo.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define  SERVO1         11
#define  SERVO2         12
#define  SERVO3         13


AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");
Servo servos[3];

void setup();
void loop();

void setup()
{
	Serial.begin(115200);
	Serial.print("\r\nStart");


	servos[0].attach(SERVO1);
	servos[0].write(90);
	servos[1].attach(SERVO2);
	servos[1].write(90);
	servos[2].attach(SERVO3);
	servos[2].write(90);


	acc.powerOn();
}

void loop()
{
	byte err;
	byte idle;
	static byte count = 0;
	byte msg[3];
	long touchcount;

	if (acc.isConnected()) {
		int len = acc.read(msg, sizeof(msg), 1);
		int i;
		byte b;
		uint16_t val;
		int x, y;
		char c0;

		if (len > 0) {
			// assumes only one command per packet
			if (msg[0] == 0x2) {
                                if (msg[1] == 0x10)
					servos[0].write(map(msg[2], 0, 255, 0, 180));
				else if (msg[1] == 0x11)
					servos[1].write(map(msg[2], 0, 255, 0, 180));
				else if (msg[1] == 0x12)
					servos[2].write(map(msg[2], 0, 255, 0, 180));
			} 
		}


	} else {
		// reset outputs to default values on disconnect
		servos[0].write(90);
		servos[1].write(90);
		servos[2].write(90);
	}

	delay(10);
}

