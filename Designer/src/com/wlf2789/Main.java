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
        instance().init();
    }

    // Singleton
    private static Main _instance = null;
    public static Main instance() {
        if (_instance == null)
            _instance = new Main();
        return _instance;
    }


    public HashMap<Integer, HashMap<Integer, String>> data;
    public GUI gui;
    public static int maxItems = 116;

    protected Main() {
        data = new HashMap<>();
        data.put(0, new HashMap<>());
    }

    public void init() {
        gui = new GUI();
    }

    public String data2string() {
        String result = "#define MAX_FRAMES "+ data.size() +"\n#define MAX_STEPS "+ maxItems +"\nextern Display;\nbyte animation[][MAX_STEPS][2] {";

        String toInsert;
        switch ((String)gui.exportOptions.getSelectedItem()) {
            default: toInsert = ""; break;
            case "binary": toInsert = "{B00000000,B00000000}"; break;
            case "byte": toInsert = "{0,0}"; break;
        }

        for (int f = 0; f < data.size(); f++) {
            result += "{\n";
            for (int i = 0; i < maxItems; i++) {
                if (data.get(f).containsKey(i)) {
                    switch ((String)gui.exportOptions.getSelectedItem()) {
                        default: toInsert = ""; break;
                        case "binary": {
                            toInsert = "{B";
                            toInsert += inverse(data.get(f).get(i).substring(0, 8));
                            toInsert += ",B";
                            toInsert += inverse(data.get(f).get(i).substring(8));
                            toInsert += "}";
                        } break;
                        case "byte": {
                            toInsert = "{";
                            toInsert += String.format("0x%2s", Integer.toHexString(BinaryToInt(inverse(data.get(f).get(i).substring(0, 8))))).replace(' ', '0');
                            toInsert += ",";
                            toInsert += String.format("0x%2s", Integer.toHexString(BinaryToInt(inverse(data.get(f).get(i).substring(8))))).replace(' ', '0');
                            toInsert += "}";
                        } break;
                    }
                }
                if (instance().gui.compact.isSelected())
                    result += " " + toInsert + (i < (maxItems - 1) ? "," : "\n");
                else
                    result += "  " + toInsert + (i < (maxItems - 1) ? ",\n" : "\n");
            }
            result += "}" + (f < (data.size() - 1) ? "," : "");
        }
        return result + "};\nvoid doAnimation(int frame, int step) {\n  Display::SetLeds(0, animation[frame][step][0];\n  Display::SetLeds(1, animation[frame][step][1];\n}";
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

    //*
    private static final Pattern pattern = Pattern.compile("(B{1}[0-1]{8}|0x[0-9a-fA-F]{2}|[0-9]+)\\,(B{1}[0-9]{8}|0x[0-9a-fA-F]{2}|[0-9]+)");
    public static void loadData(File srcFile) {
        try(BufferedReader br = new BufferedReader(new FileReader(srcFile))) {
            StringBuilder sb = new StringBuilder();
            br.readLine(); // omit frames count line
            String line = br.readLine();
            if (line != null) {
                maxItems = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
                br.readLine(); // omit array definition
                line = br.readLine();
            }
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();

            clearData();

            String[] framesToConvert = everything.split("\n\\}\\,\\{\n");
            String row;
            String[] split;
            int f = 0, i;
            for (String frameToConvert : framesToConvert) {
                instance().data.put(f, new HashMap<>());
                Matcher m = pattern.matcher(frameToConvert);
                i = 0;
                while (m.find()) {
                    row = "";
                    split = m.group(0).split("\\s*,\\s*");
                    for (String s : split)
                        if (s.length() == 9)
                            row = s.substring(1) + row;
                        else
                            row = String.format("%8s", Integer.toBinaryString(Integer.parseInt(s.substring(2), 16))).replace(' ', '0') + row;
                    row = inverse(row);
                    instance().data.get(f).put(i, row);
                    i++;
                }
                f++;
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


    public static void clearDataAt(int f, int x) {
        x = clamp(x, 0, maxItems-1);
        if (instance().data.containsKey(f))
            if (instance().data.get(f).containsKey(x))
                instance().data.get(f).remove(x);
    }


    public static void addFrame(int index) {
        if (getFramesCount() > 10) return;
        for (int i = instance().data.size(); i > index; i--)
            instance().data.put(i, instance().data.get(i - 1));
        instance().data.put(index, new HashMap<>());
        instance().data.get(index).put(0,"0000000000000000");
    }

    public static void duplicateFrame(int index) {
        if (getFramesCount() > 10) return;
        for (int i = instance().data.size(); i > index; i--)
            instance().data.put(i, instance().data.get(i - 1));
        instance().data.put(index + 1, new HashMap<>(instance().data.get(index)));
    }

    public static void delFrame(int index) {
        if (instance().data.size() > index) {
            for (int i = index; i < instance().data.size() - 1; i++)
                instance().data.put(i, instance().data.get(i + 1));
            instance().data.remove(instance().data.size() - 1);
        }
    }

    public static void swapFrames(int index1, int index2) {
        if (
            (index1 == index2) ||
            (index1 < 0) || (index1 >= instance().data.size()) ||
            (index2 < 0) || (index2 >= instance().data.size()) ||
            (!instance().data.containsKey(index1)) ||
            (!instance().data.containsKey(index2))
        ) return;
        HashMap<Integer, String> toSwap = instance().data.get(index1);
        instance().data.put(index1, instance().data.get(index2));
        instance().data.put(index2, toSwap);
    }

    public static void scrollFrame(int frame, int step) {
        if (!instance().data.containsKey(frame)) return;
        HashMap<Integer, String> frameToEdit = instance().data.get(frame);

        String backup = "";
        if (step < 0) {
            // backup first frame step
            for (int i : frameToEdit.keySet()) {
                backup = frameToEdit.get(i);
                break;
            }

            String current = backup;
            for (int i = 1; i < frameToEdit.size(); i++) {
                if (frameToEdit.containsKey(i))
                    current = frameToEdit.get(i);
                frameToEdit.put(i - 1, current);
            }
            frameToEdit.put(frameToEdit.size() - 1, backup);
        } else if (step > 0) {
            // backup last frame step
            for (int i : frameToEdit.keySet())
                backup = frameToEdit.get(i);

            String current = backup;
            for (int i = frameToEdit.size() - 2; i >= 0; i--) {
                if (frameToEdit.containsKey(i))
                    current = frameToEdit.get(i);
                frameToEdit.put(i + 1, current);
            }
            frameToEdit.put(0, backup);
        }

        if (step < -1 || step > 1)
            scrollFrame(frame, (step < 0 ? step + 1 : step - 1));
    }

    public static int getFramesCount() {
        return instance().data.size();
    }



    public final int remap[] = { 3,2,1,0,7,6,5,4, 9,10,11,15,14,13,12,8 };

    public static void setLed(int f, int x, int y, boolean state) {
        System.out.printf("f%d, x%d, y%d, s%d\n", f, x, y, state ? 1 : 0);
        if (! instance().data.containsKey(f)) return;

        x = clamp(x, 0, maxItems-1);
        y = clamp(y, 0, 13);

        try {
            y = instance().remap[y > 6 ? y + 1 : y];
            String originalData = "0000000000000000";
            for (int i = x; i >= 0; i--)
                if (instance().data.get(f).containsKey(i)) {
                    originalData = instance().data.get(f).get(i);
                    break;
                }
            StringBuilder dataToPut = new StringBuilder(originalData);
            dataToPut.setCharAt(y, state ? '1' : '0');
            instance().data.get(f).put(x, dataToPut.toString());
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }
    }

    public static boolean getLed(int f, int x, int y) {
        if (! instance().data.containsKey(f)) return false;

        x = clamp(x, 0, maxItems-1);
        y = clamp(y, 0, 13);

        try {
            for (int i = x; i >= 0; i--)
                if (instance().data.get(f).containsKey(i)) {
                    y = instance().remap[y > 6 ? y + 1 : y];
                    return instance().data.get(f).get(i).charAt(y) == '1';
                }
        } catch (Exception e) {
            System.out.printf("Error: %s\n", e.getMessage());
        }
        return false;
    }


}
