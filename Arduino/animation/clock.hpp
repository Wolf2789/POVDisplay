#define MAX_STEPS 130
#define MAX_STEPS_HALF (MAX_STEPS/2)
#define MAX_STEPS_QUAT (MAX_STEPS_HALF/2)

extern Display;

int clock_hours = 0;
int clock_minutes = 0;
int clock_seconds = 0;
int clock_loop = 0;

int index_60[60] = {
  65,67,69,71,73,75,77,79,81,83,85,87,89,91,93,95,97,99,101,103,105,107,109,111,113,115,117,119,121,123,125,127,129,1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53
};
int index_12[12] = { 65,75,85,95,105,115,125,5,15,25,35,45 };

byte clock_frame[MAX_STEPS] {
  B00000011,B01000101,B01000101,B10000011,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  B00000011,B10000000,B10000111,B10000100,B00000011,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  B01000010,B11000111,B01000000,0,B01000010,B11000100,B01000011,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  B10000011,B00000100,B00000011,B00000100,B10000011,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
};

/*
void init() {
  // 6
  clock_frame[0] = B00100001;
  clock_frame[1] = B10100010;
  clock_frame[2] = B10100010;
  clock_frame[3] = B11000001;
  
  // 9
  clock_frame[MAX_STEPS_QUAT    ] = B10000011;
  clock_frame[MAX_STEPS_QUAT + 1] = B01000000;
  clock_frame[MAX_STEPS_QUAT + 2] = B11000011;
  clock_frame[MAX_STEPS_QUAT + 3] = B01000010;
  clock_frame[MAX_STEPS_QUAT + 4] = B10000001;
  
  // 12
  clock_frame[MAX_STEPS_HALF - 2] = B01000010;
  clock_frame[MAX_STEPS_HALF - 1] = B11000111;
  clock_frame[MAX_STEPS_HALF    ] = B11000111;
  clock_frame[MAX_STEPS_HALF + 1] = B01000000;
  clock_frame[MAX_STEPS_HALF + 2] = B00000000;
  clock_frame[MAX_STEPS_HALF + 3] = B01000010;
  clock_frame[MAX_STEPS_HALF + 4] = B11000100;
  clock_frame[MAX_STEPS_HALF + 5] = B11000101;
  clock_frame[MAX_STEPS_HALF + 6] = B01000011;
  
  // 3
  clock_frame[MAX_STEPS_HALF + MAX_STEPS_QUAT    ] = B10000011;
  clock_frame[MAX_STEPS_HALF + MAX_STEPS_QUAT + 1] = B00000100;
  clock_frame[MAX_STEPS_HALF + MAX_STEPS_QUAT + 2] = B00000111;
  clock_frame[MAX_STEPS_HALF + MAX_STEPS_QUAT + 3] = B00000100;
  clock_frame[MAX_STEPS_HALF + MAX_STEPS_QUAT + 4] = B10000011;

  int i;
  for (i = 0; i < 60; i++)
    index_60[i] = (MAX_STEPS_HALF + (i * (MAX_STEPS/60))) % MAX_STEPS;
  for (i = 0; i < 12; i++)
    index_12[i] = (MAX_STEPS_HALF + (i * (MAX_STEPS/12))) % MAX_STEPS;
}
*/

void doAnimation(int step) {
  // calculate time
  if (clock_loop++ >= 2500) {
    clock_loop = 0;
    clock_hours = (clock_hours + (clock_minutes / 59)) % 12;
    clock_minutes = (clock_minutes + (clock_seconds / 59)) % 60;
    clock_seconds = (clock_seconds + 1) % 60;
  }

  // show clock face
  Display::SetLeds(0, clock_frame[step]);
  Display::SetLeds(1, B00000000);

  if (step % 10 < 5)
    Display::TurnLeds(0, B00001000, ON);

  // show clock arms
  // hours
  if (step == index_12[clock_hours])
    Display::SetLeds(1, B11111100);
    
  // minutes
  if (step == index_60[clock_minutes]) {
    Display::SetLeds(1, B11111111);
    Display::TurnLeds(0, B00100000, ON);
  }
  
  // seconds
  if (step == index_60[clock_seconds]) {
    Display::SetLeds(1, B11111111);
    Display::TurnLeds(0, B11100001, ON);
  }
}
