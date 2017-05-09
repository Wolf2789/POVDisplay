
#include <Wire.h>
#include "animation.h"
#include "display.h"

void setup() {
  Serial.begin(9600);
  Wire.begin();
}

#define IR_THRESHOLD 400

int IR = 0;
int countdown = 0;
int angle = 0;
bool state = true;

void loop() {
    if (countdown > 0)
      countdown--;
    else {
      IR = analogRead(A1); // IR > 850
      if (IR > IR_THRESHOLD) {
        countdown = 20;
        state = false;
        angle = 0;
      }
    }
    
    Display::SetLeds(0, animation[angle][0]);
    Display::SetLeds(1, animation[angle][1]);
    Display::Update();
    
    angle = (angle + 1) % 96;
    delayMicroseconds(250);
}
