#define MAX_FRAMES 4
#define MAX_STEPS 116
#define ANIMATION_SPEED 10

extern Display;
void doAnimation(int frame, int step) {
  Display::SetLeds(0, animation[frame][step][0]);
  Display::SetLeds(1, animation[frame][step][1]);
}
