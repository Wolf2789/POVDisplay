#define MAX_FRAMES 14
#define ANIMATION_SPEED 3
extern Display;

bool direction = true;
void doAnimation(int frame) {
  Display::SetLeds(0, 0);
  Display::SetLeds(1, 0);
  
  if (frame_countdown == 0)
    if (frame == 13)
      direction = !direction;
  Display::SetLed((direction ? 13 - frame : frame ), ON);
}

