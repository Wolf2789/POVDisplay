package com.wlf2789;

import javax.swing.*;
import java.awt.*;

public class Output extends JFrame {
    private JFrame parent;
    private JTextArea console;
    private JPanel outputRoot;

    public Output(JFrame parent) {
        this.parent = parent;

        setTitle("POVD Animator");
        setLocationRelativeTo(parent);
        setContentPane(outputRoot);
        pack();
        setSize(200, 350);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        setVisible(false);
    }

    public void show(String msg) {
        console.setText(msg);
        if (! isVisible())
            setVisible(true);
        Point loc = parent.getLocation();
        setLocation(
                loc.x + parent.getWidth() + 5,
                loc.y + (parent.getHeight()/2) - (getHeight()/2)
        );
    }
}
