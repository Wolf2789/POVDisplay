package com.wlf2789;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class DataHelper {

    public static int AnimationSteps = 130;
    public static int AnimationSpeed = 1;
    public static boolean AnimationOutline = false;
    public static Object AnimationData;

    public static double itemAngle = 360.0 / AnimationSteps;

    public static String encodeString(String toEncode) { return Base64.getEncoder().withoutPadding().encodeToString(toEncode.getBytes()); }
    public static String decodeString(String toDecode) { return new String(Base64.getDecoder().decode(toDecode)); }
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    public static String inverseString(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public static String convertData(String s) {
        int value = 0;
        s = inverseString(s);
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == '1')
                value += Math.pow(2, RemapLED[i]);
        return String.format("0x%2s", Integer.toHexString(value)).replace(' ','0');
    }


    public static String loadString(String resourceName) {
        String result = "";
        try {
            BufferedInputStream in = (BufferedInputStream) MainWindow.class.getClassLoader().getResourceAsStream(resourceName);
            byte[] contents = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = in.read(contents)) != -1)
                result += new String(contents, 0, bytesRead);
        } catch (Exception e) {}
        return result;
    }

    public static ImageIcon loadImage(String resourceName) {
        try {
            return new ImageIcon(MainWindow.class.getClassLoader().getResource(resourceName));
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }
        return new ImageIcon();
    }


    public static void resizeImageData(LinkedList list, int newSize) {
        if (list.size() < newSize) {
            while (list.size() < newSize)
                list.addLast("00000000000000");
        } else if (list.size() >= newSize) {
            while (list.size() >= newSize)
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
            System.out.printf("Error: %s\n", e.getMessage());
        }
    }


    private static int RemapLED[] = { 3,2,1,0,7,6,5,4, 1,2,3,7,6,5,4,0 };
    public static String exportToArduino() {
        int AnimationType = (AnimationData instanceof String ? 0 : 1);

        // create header
        String result = loadString("code");

        // add animation start
        switch (AnimationType) {
            case 0:
                result = result.replace("{ANIMATION SETUP}",
                        loadString("animation/text/setup")
                            .replace("{ANIMATION TEXT}", AnimationData.toString())
                            .replace("{ANIMATION SPEED}", ""+(AnimationSpeed * 100))
                            .replace("{ANIMATION OUTLINE}", (AnimationOutline ? "TRUE" : "FALSE"))
                ).replace("{ANIMATION CODE}",
                        loadString("animation/text/code")
                );
                break;
            case 1: {
                LinkedList<String> list = (LinkedList)AnimationData;
                String data = "";
                for (int i = 0; i < list.size(); i++)
                    data +=
                            "  {" +
                                    convertData(list.get(i).substring(0, 7)) +
                                    "," +
                                    convertData(list.get(i).substring(7)) +
                                    "}" + (i < list.size()-1 ? ",\n" : "");

                result = result.replace("{ANIMATION SETUP}",
                        loadString("animation/image/setup")
                            .replace("{ANIMATION STEPS}", ""+AnimationSteps)
                            .replace("{ANIMATION DATA}", data)
                ).replace("{ANIMATION CODE}",
                        loadString("animation/image/code")
                );
            } break;
        }
        return result;
    }


    private static final Pattern pattern = Pattern.compile("(B{1}[0-1]{8}|0x[0-9a-fA-F]{2}|[0-9]+)\\,(B{1}[0-9]{8}|0x[0-9a-fA-F]{2}|[0-9]+)");
    public static void importFromArduino(String code) {

    }
}
