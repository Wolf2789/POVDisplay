package com.wlf2789;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainWindow extends JFrame {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.printf("Error (main): %s\n", e.getMessage());
        }
        new MainWindow();
    }

    private JPanel uiRoot;
    private JTextPane uiAbout;
    private JButton uiExport;
    private JButton uiImport;

    private JPanel uiPanelChooseAnimation;
    private JComboBox uiAnimationChooser;

    private JPanel uiAnimationConfiguration;
    private JPanel uiText;
    private JTextField uiTextToScroll;
    private JSpinner uiAnimationSpeed;
    private JCheckBox uiAnimationOutline;
    private JButton uiOpenDesigner;

    private DesignerWindow designerWindow;

    public MainWindow() {
        setTitle("POV Display Configurator");
        setContentPane(uiRoot);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // initialize components
        uiAnimationSpeed.addChangeListener(changeEvent -> {
            if ((int)uiAnimationSpeed.getValue() < 1)
                uiAnimationSpeed.setValue(1);
            DataHelper.AnimationSpeed = (int)uiAnimationSpeed.getValue();
        });

        uiAnimationOutline.addChangeListener(changeEvent -> {
            DataHelper.AnimationOutline = uiAnimationOutline.isSelected();
        });

        uiAnimationChooser.setSelectedIndex(-1);
        uiAnimationChooser.addItemListener((ItemEvent itemEvent) -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                // show configuration panel if hidden and disable export button error tooltip if present
                uiAnimationConfiguration.setVisible(true);
                uiExport.setToolTipText(null);

                // hide configuration panels
                designerWindow.setVisible(false);
                uiText.setVisible(false);
                uiOpenDesigner.setVisible(false);

                // show only configuration panels we want
                switch (uiAnimationChooser.getSelectedIndex()) {
                    case 0:
                        uiText.setVisible(true);
                        DataHelper.AnimationData = uiTextToScroll.getText();
                        break;
                    case 1:
                        uiOpenDesigner.setVisible(true);
                        DataHelper.AnimationData = new LinkedList<>(Arrays.asList("00000000000000"));
                        DataHelper.reinitializeAnimationData((int)designerWindow.designerSteps.getValue());
                        break;
                }

                // resize window so we can see all components
                pack();
            }
        });

        uiImport.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            if (fileChooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                try(BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                    }
                    String arduino = sb.toString();
                    // determine animation type
                    uiAnimationChooser.setSelectedIndex( (arduino.indexOf("#define textToDisplay") > -1) ? 0 : 1 );
                    if (uiAnimationChooser.getSelectedIndex() == 0) {
                        // #### load scrolling text animation
                        // get animation outline
                        Matcher m = Pattern.compile("#define\\sANIMATION_OUTLINE\\s(.*)\\n").matcher(arduino);
                        while (m.find())
                            uiAnimationOutline.setSelected(Boolean.parseBoolean(m.group(1)));
                        // get animation speed
                        m = Pattern.compile("#define\\sANIMATION_SPEED\\s(.*)\\n").matcher(arduino);
                        while (m.find())
                            uiAnimationSpeed.setValue(Integer.parseInt(m.group(1))/100);
                        // get animation text
                        m = Pattern.compile("#define\\stextToDisplay\\s\"(.*)\"\\n").matcher(arduino);
                        while (m.find())
                            uiTextToScroll.setText(m.group(1));
                    } else {
                        // #### load image
                        // get image steps
                        Matcher m = Pattern.compile("#define\\sMAX_STEPS\\s(.*)\\n").matcher(arduino);
                        while (m.find())
                            designerWindow.designerSteps.setValue(Integer.parseInt(m.group(1)));
                        DataHelper.importFromArduino(arduino);
                    }
                } catch (Exception e) {
                    System.out.printf("Error (import): %s\n", e.getMessage());
                }
            }
        });

        uiExport.addActionListener(actionEvent -> {
            if (uiAnimationChooser.getSelectedIndex() < 0) {
                // set tooltip
                uiExport.setToolTipText("First choose start animation!");
                // and display it
                ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(uiExport,MouseEvent.MOUSE_MOVED,System.currentTimeMillis(),0,0,0,0,false));
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            if (fileChooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                if (uiAnimationChooser.getSelectedIndex() == 0)
                    DataHelper.AnimationData = uiTextToScroll.getText();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()));
                    writer.write(DataHelper.exportToArduino());
                    writer.close();
                } catch (Exception e) {
                    System.out.printf("Error (export): %s\n", e.getMessage());
                }
            }
        });

        uiOpenDesigner.addActionListener(actionEvent -> {
            designerWindow.setVisible(! designerWindow.isVisible());
        });

        // set default values
        uiAnimationSpeed.setValue(1);
        uiAbout.setText(
                DataHelper.loadString("about")
                        .replace("{INFO.PNG}",
                                this.getClass().getClassLoader().getResource("resources/image/info.png").toString()
                        )
        );

        pack();
        setVisible(true);

        designerWindow = new DesignerWindow(this);
    }
}
