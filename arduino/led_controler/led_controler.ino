#include <Adafruit_NeoPixel.h>
#ifdef __AVR_ATtiny85__ // Trinket, Gemma, etc.
  #include <avr/power.h>
#endif

#define PIN            0

Adafruit_NeoPixel ring = Adafruit_NeoPixel(40, PIN, NEO_GRB + NEO_KHZ800);

int power = 0;

void setup(){
  ring.begin();
  ring.setBrightness(25);
  ring.show(); // Initialize all pixels to 'off'
}

int pixel = 0;
const int startDelay = 2000;
int baseDelay = startDelay;
void loop(){
  delay(baseDelay / 16);
  for(int i = 0; i < 40; i++){
    ring.setPixelColor(i, 0);
    int red = 0;
    int green = 0;
    int blue = 0;
    if(i == pixel){
       blue = 50;
       red = 255;
       green = 200;
    } else if (i == pixel - 1) {
      blue = 25;
       red = 255;
       green = 125;
    } else if (i == pixel - 2) {
      blue = 190;
    } else if (i == pixel - 3) {
      blue = 160;
    } else if (i == pixel - 4) {
      blue = 130;
    } else if (i == pixel - 5) {
      blue = 100;
    } else if (i == pixel - 6) {
      blue = 70;
    } else if (i == pixel - 7) {
      blue = 40;
    }else if (i == pixel - 8) {
      blue = 40;
    } else if (i <= pixel) {
//      blue = 50 + map((startDelay - baseDelay), 0, startDelay, 0, 255);
//       red = 20;
//       green = 20;
    }
    ring.setPixelColor(i, red, green, blue);
  }
  
  ring.show();
  //power++;
  pixel++;
  if(pixel > 48){
    pixel = 0;  
      baseDelay -= 100;
    if(baseDelay <= 0){
      baseDelay = startDelay;
    }
  }

}
