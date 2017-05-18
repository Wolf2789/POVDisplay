#include <Wire.h>
#include "display.h"

// IR RELATED
int IR = 0;
#define IR_THRESHOLD 1100
#define IR_PIN_1 A1
#define IR_PIN_2 A2

// DISPLAY RELATED
int countdown = 0;
int frame = 0;
int angle = 0;


//#define CLOCK_MODE
#ifdef CLOCK_MODE
#include "clock.hpp"
#else
#include "animation.h"
#endif


void setup() {
//  Serial.begin(9600);
  Wire.begin();
}

void loop() {
  IR = analogRead(IR_PIN_1);
  IR += analogRead(IR_PIN_2);
  if (countdown > 0)
    countdown--;
  else if (IR > IR_THRESHOLD) {
    countdown = 20;
    angle = 0;
#ifndef CLOCK_MODE
    frame = (frame + 1) % MAX_FRAMES;
#endif
  }

  if (angle < MAX_STEPS) {
    // set display leds
#ifdef CLOCK_MODE
    doClock(angle);
#else
    Display::SetLeds(0, animation[frame][angle][0]);
    Display::SetLeds(1, animation[frame][angle][1]);
#endif

    // show display
    Display::Update();
    angle += 1;
  }
}
