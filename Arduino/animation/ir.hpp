extern Display;
void doAnimation() {
  Display::SetLeds(0, 0);
  Display::SetLeds(1, 0);
  Display::SetLed(IR*(1100/14), ON);
}

