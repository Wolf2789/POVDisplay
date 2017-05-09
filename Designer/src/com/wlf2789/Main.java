package com.wlf2789;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JFrame {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.out.printf("Error: %s\n", e.getMessage());
            }
        }
        System.out.printf("ANGLE: %f\n", GUI.itemAngle);
        instance().init();
    }

    // Singleton
    private static Main _instance = null;
    public static Main instance() {
        if (_instance == null)
            _instance = new Main();
        return _instance;
    }


    public HashMap<Integer, String> data;
    public GUI gui;
    public static final int maxItems = 96;

    protected Main() {
        data = new HashMap<>();
    }

    public void init() {
        gui = new GUI();
    }

    public String data2string() {
        String result = "byte animation[][2] {" + (gui.userFriendly.isSelected() ? "\n" : "");

        String toInsert;
        switch ((String)gui.exportOptions.getSelectedItem()) {
            default: toInsert = ""; break;
            case "binary": toInsert = "{B00000000,B00000000}"; break;
            case "byte": toInsert = "{0,0}"; break;
        }

        for (int i = 0; i < maxItems; i++) {
            if (data.containsKey(i)) {
                switch ((String)gui.exportOptions.getSelectedItem()) {
                    default: toInsert = ""; break;
                    case "binary": {
                        toInsert = "{B";
                        toInsert += inverse(data.get(i).substring(0, 8));
                        toInsert += ",B";
                        toInsert += inverse(data.get(i).substring(8));
                        toInsert += "}";
                    } break;
                    case "byte": {
                        toInsert = "{";
                        toInsert += BinaryToInt(inverse(data.get(i).substring(0, 8)));
                        toInsert += ",";
                        toInsert += BinaryToInt(inverse(data.get(i).substring(8)));
                        toInsert += "}";
                    } break;
                }
            }
            result += (gui.userFriendly.isSelected() ? "  " : "") + toInsert + ( i < (maxItems - 1) ? "," : "") + (gui.userFriendly.isSelected() ? "\n" : "");
        }
        return result + "};";
    }


    // HELPERS
    public static int BinaryToInt(String b) {
        int result = 0;
        b = new StringBuilder(b).reverse().toString();
        for (int i = 0; i < b.length(); i++)
            result += (b.charAt(i) - 48) * Math.pow(2, i);
        return result;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static String inverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }



    public static void clearData() {
        instance().data.clear();
    }

    private static final Pattern pattern = Pattern.compile("([B0-9]+)\\s*,\\s*([B0-9]+)*");
    public static void loadData(File srcFile) {
        try(BufferedReader br = new BufferedReader(new FileReader(srcFile))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();

            clearData();
            Matcher m = pattern.matcher(everything);
            String row;
            String[] split;
            int i = 0;
            while (m.find()) {
                row = "";
                split = m.group(0).split("\\s*,\\s*");
                for (String s : split)
                    if (s.length() == 9)
                        row = s.substring(1) + row;
                    else
                        row = String.format("%8s", Integer.toBinaryString(Integer.parseInt(s))).replace(' ', '0') + row;
                row = inverse(row);
                instance().data.put(i, row);
                i++;
            }
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }
    }

    public static void saveData(File dstFile) {
        BufferedWriter writer = null;
        try {
            System.out.printf("Writing data to: %s\n", dstFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(dstFile));
            writer.write(dataExport());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {}
        }
    }

    public static String dataExport() {
        return instance().data2string();
    }


    public static void clearDataAt(int x) {
        if (x < 0 || x >= maxItems) return;
        if (instance().data.containsKey(x))
            instance().data.remove(x);
    }



    public final int remap[] = { 3,2,1,0,7,6,5,4, 9,10,11,15,14,13,12,8 };

    public static void setLed(int x, int y, boolean state) {
        if (x < 0 || x >= maxItems || y < 0 || y > 13) return;
        try {
            y = instance().remap[y > 6 ? y + 1 : y];
            String originalData = "0000000000000000";
            for (int i = x; i >= 0; i--)
                if (instance().data.containsKey(i)) {
                    originalData = instance().data.get(i);
                    break;
                }
            StringBuilder dataToPut = new StringBuilder(originalData);
            dataToPut.setCharAt(y, state ? '1' : '0');
            Main.instance().data.put(x, dataToPut.toString());
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }
    }

    public static boolean getLed(int x, int y) {
        if (x < 0 || x >= maxItems || y < 0 || y > 13) return false;
        try {
//            if (withTranslation)
//                x = instance().gui.translation + x;
            y = instance().remap[y > 6 ? y + 1 : y];
            for (int i = x; i >= 0; i--)
                if (instance().data.containsKey(i))
                    return instance().data.get(i).charAt(y) == '1';
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }
        return false;
    }


}
