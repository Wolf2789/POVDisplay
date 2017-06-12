package com.wlf2789;

import java.io.BufferedInputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHelper {

    public static int AnimationSteps = 130;
    public static int AnimationSpeed = 1;
    public static boolean AnimationOutline = false;
    public static Object AnimationData;

    public static double itemAngle = 360.0 / AnimationSteps;

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    public static String inverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }


    // RESOURCES
    public static String loadString(String resourceName) {
        String result = "";
        try {
            BufferedInputStream in = new BufferedInputStream(MainWindow.class.getClassLoader().getResourceAsStream("resources/"+ resourceName));
            byte[] contents = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(contents)) != -1)
                result += new String(contents, 0, bytesRead);
        } catch (Exception e) {
            System.out.printf("Error (resource): %s\n", e.getMessage());
        }
        return result;
    }


    // DATA OPERATIONS
    public static boolean isLedOn(int x, int y) {
        LinkedList data = (LinkedList)AnimationData;
        if (data.get(x) != null)
            return ((String)data.get(x)).charAt(y) == '1';
        else
            return false;
    }

    public static void setLed(int x, int y, boolean state) {
        x = clamp(x, 0, AnimationSteps-1);
        y = clamp(y, 0, 13);
        try {
            LinkedList data = (LinkedList)AnimationData;
            String originalData = "00000000000000";
            for (int i = x; i >= 0; i--)
                if (data.get(i) != null) {
                    originalData = (String)data.get(i);
                    break;
                }
            StringBuilder dataToPut = new StringBuilder(originalData);
            dataToPut.setCharAt(y, state ? '1' : '0');
            data.set(x, dataToPut.toString());
            AnimationData = data;
        } catch (Exception e) {
            System.out.printf("Error (setLed): %s\n", e.getMessage());
        }
    }

    private static void resizeImageData(LinkedList list, int newSize) {
        if (list.size() < newSize) {
            while (list.size() < newSize)
                list.addLast("00000000000000");
        } else if (list.size() > newSize) {
            while (list.size() > newSize)
                list.removeLast();
        }
    }

    public static void reinitializeAnimationData(int size) {
        AnimationSteps = size;
        itemAngle = 360.0 / size;
        if (AnimationData instanceof LinkedList) {
            LinkedList list = (LinkedList)AnimationData;
            resizeImageData(list, size);
        }
    }


    // ARDUINO CODE RELATED
    private static int RemapLED[] = { 3,2,1,0,7,6,5,4, 1,2,3,7,6,5,4,0 };

    public static String convertToHEX(String input, int offset) {
        int value = 0;
        for (int i = 0; i < input.length(); i++)
            if (input.charAt(i) == '1')
                value += Math.pow(2, RemapLED[offset + i]);
        return String.format("0x%2s", Integer.toHexString(value)).replace(' ','0');
    }

    // EXPORT TO ARDUINO CODE
    private static String loadAnimationSetup(int AnimationType) {
        switch (AnimationType) {
            case 0:
                return loadString("animation/text/setup")
                        .replace("{ANIMATION TEXT}", AnimationData.toString())
                        .replace("{ANIMATION SPEED}", ""+(AnimationSpeed * 100))
                        .replace("{ANIMATION OUTLINE}", (AnimationOutline ? "TRUE" : "FALSE"));
            case 1: {
                LinkedList<String> list = (LinkedList)AnimationData;
                String data = "";
                for (int i = 0; i < list.size(); i++) {
                    data += "  {";
                    data += convertToHEX(list.get(i).substring(0, 7) + "0", 0);
                    data += ",";
                    data += convertToHEX(list.get(i).substring(7) + "0", 8);
                    data += "}" + (i < list.size() - 1 ? ",\n" : "");
                }
                return loadString("animation/image/setup")
                        .replace("{ANIMATION STEPS}", ""+AnimationSteps)
                        .replace("{ANIMATION DATA}", data);
            }
        }
        return "";
    }

    private static String loadAnimationCode(int AnimationType) {
        switch (AnimationType) {
            case 0 : return loadString("animation/text/code");
            case 1 : return loadString("animation/image/code");
        }
        return "";
    }

    public static String exportToArduino() {
        int AnimationType = (AnimationData instanceof String ? 0 : 1);
        return loadString("code")
                .replace("{ANIMATION SETUP}",
                        loadAnimationSetup(AnimationType))
                .replace("{ANIMATION CODE}",
                        loadAnimationCode(AnimationType));
    }

    // IMPORT FROM ARDUINO CODE
    public static String convertToBinary(String input) {
        int i;
        if (input.startsWith("0x"))
            i = Integer.parseInt(input.substring(2), 16);
        else
            i = Integer.parseInt(input);
        return String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
    }

    private static String getImageData(String input) {
        String binary;
        if (input.length() == 9)
            binary = input.substring(1);
        else
            binary = convertToBinary(input);
        return inverseString(binary);
    }

    private static String loadImageData(String input) {
        StringBuilder result = new StringBuilder("00000000000000");
        if (input.length() == 16) {
            for (int i = 0; i < 7; i++)
                result.setCharAt(i, input.charAt(RemapLED[i]));
            input = input.substring(8);
            for (int i = 0; i < 7; i++)
                result.setCharAt(i+7, input.charAt(RemapLED[i+8]));
        }
        return result.toString();
    }

    public static void importFromArduino(String code) {
        int i = 0;
        LinkedList<String> list = (LinkedList)AnimationData;
        Matcher m = Pattern.compile("\\{(B[0-1]{8}|0x[0-9a-fA-F]{2}|[0-9]+)\\s*,\\s*(B[0-9]{8}|0x[0-9a-fA-F]{2}|[0-9]+)}").matcher(code);
        while (m.find() && i < AnimationSteps)
            list.set(i++, loadImageData(getImageData(m.group(1)) + getImageData(m.group(2))));
    }
}
