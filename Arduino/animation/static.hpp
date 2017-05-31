#define MAX_STEPS 130
#define ANIMATION_SPEED 10

extern Display;
void doAnimation(int step) {
  Display::SetLeds(0, animation[step][0]);
  Display::SetLeds(1, animation[step][1]);
}
