#ifndef _DISPLAY_H
#define _DISPLAY_H

enum LedState {
  OFF = 0,
  ON = 1
};

class Display {
  private:
    // expanders addresses
    static byte expander[];
    
    /*
     * LEDS RELATED
     * Remapping      - if leds order is different than expected
     * Actual States  - data that is sent to expanders
     */
    static byte leds[];
    static byte remap[][8];
  public:
    static void Update();
    static void SetLed(byte expander, byte led, LedState state);
    static void SetLed(byte led, LedState state);
    static void SetLeds(byte expander, byte leds);
    static LedState GetLed(byte led);
};

#endif
