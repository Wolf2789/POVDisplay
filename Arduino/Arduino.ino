// ANIM_TYPE CAN BE:  TEXT | CLOCK | IR | SCANNER | STATIC | GIF
//#define ANIM_TYPE TEXT
//#define textToDisplay "POV Display by Dawid Bugajski"

//#define ANIM_TYPE STATIC
//#define ANIM_OPTION "robotyka"
//#define ANIM_OPTION "lego"

//#define ANIM_TYPE GIF
//#define ANIM_OPTION "scan"




#include <Wire.h>
#include "display.h"

// IR RELATED
int IR = 0;
int IR_countdown = 0;
#define IR_THRESHOLD 900
#define IR_PIN_1 A2
#define IR_PIN_2 A1

// ANIMATION RELATED
#define ANIM_FILE(folder,file) <animation/##folder/##file.h>

#ifdef ANIM_TYPE
#if(ANIM_TYPE == TEXT)
#include "animation/text.hpp"

#elif(ANIM_TYPE == CLOCK)
#include "animation/clock.hpp"

#elif(ANIM_TYPE == IR)
#include "animation/ir.hpp"

#elif(ANIM_TYPE == SCANNER)
#include "animation/scanner.h"

#elif(ANIM_TYPE == STATIC)
#include ANIM_FILE("static",ANIM_OPTION)
#include "animation/static.hpp"

#elif(ANIM_TYPE == GIF)
#include ANIM_FILE("gif",ANIM_OPTION)
#include "animation/gif.hpp"
#endif
#endif

// DISPLAY RELATED
bool doReset = true;
int currentStep = 0;
#ifdef MAX_FRAMES
int currentFrame = 0;
int frame_countdown = 10;
#endif

void setup() {
  Wire.begin();
}

void loop() {
  IR = analogRead(IR_PIN_1);
  //IR += analogRead(IR_PIN_2);
  if (IR_countdown > 0)
    IR_countdown--;
  else {
    if (IR > IR_THRESHOLD)
      doReset = true;
    else if (doReset) {
      doReset = false;
      currentStep = 0;
      IR_countdown = 20;

      // animate
      #ifdef MAX_FRAMES
      if (frame_countdown > 0)
        frame_countdown--;
      else {
        frame_countdown = ANIMATION_SPEED;
        currentFrame = (currentFrame + 1) % MAX_FRAMES;
      }
      #endif
    }
  }

#ifdef MAX_STEPS
  if (currentStep < MAX_STEPS) {
#endif
    // set display leds
    #ifdef MAX_STEPS
      #ifdef MAX_FRAMES
        doAnimation(currentFrame, currentStep);
      #else
        doAnimation(currentStep);
      #endif
    #else
      #ifdef MAX_FRAMES
        doAnimation(currentFrame);
      #else
        doAnimation();
      #endif
    #endif
    
#ifdef MAX_STEPS
#ifdef NO_LOOP
    if ((currentStep += 1) >= MAX_STEPS) {
      Display::SetLeds(0, 0);
      Display::SetLeds(1, 0);
    }
#endif
#endif

    // show display
    Display::Update();
    
#ifdef MAX_STEPS
  }
#endif
}
