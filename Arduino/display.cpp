#include "Arduino.h"
#include <Wire.h>
#include "display.h"

// each row corresponds to arrays defined below
byte Display::expander[2] = {
  0x21,
  0x38
};

byte Display::remap[][8] = {
  {3,2,1,0,7,6,5,4},
  {1,2,3,7,6,5,4,0}
};

byte Display::leds[] = {
  0, // each led is off -> 0 = B00000000
  0
};


void Display::SetLed(byte expander, byte led, LedState state) {
  expander = expander % 2;  // 0 - 1
  led = led % 7;            // 0 - 6 (we have 7 leds for each expander)
  
  if (state == ON) {
    Display::leds[expander] |= (1 << Display::remap[expander][led]);
  } else {
    Display::leds[expander] &= ~(1 << Display::remap[expander][led]);
  }
}

void Display::SetLed(byte led, LedState state) {
  Display::SetLed(led > 6 ? 1 : 0, led % 7, state);
}

void Display::SetLeds(byte expander, byte leds) {
  Display::leds[expander] = leds;
}

void Display::TurnLeds(byte expander, byte leds, LedState state) {
  if (state == ON)
    Display::leds[expander] |= leds;
  else
    Display::leds[expander] &= ~leds;
}

LedState Display::GetLed(byte led) {
  int expander = led > 6 ? 1 : 0;
  return (Display::leds[expander] & (1 << Display::remap[expander][led]));
}

void Display::Update() {
  for (byte i = 0; i < 2; i++) {
    Wire.beginTransmission(Display::expander[i]);
    Wire.write(~Display::leds[i]); // we're expecting 255 to be all on, but to expander it means all off, so we need to invert our value
    Wire.endTransmission();
  }
}
