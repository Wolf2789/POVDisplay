package com.wlf2789;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;

public class DesignerWindow extends JFrame {

    private JPanel designerRoot;
    private JPanel renderPanel;
    public JPanel designerConfigPanel;
    public JSpinner designerSteps;
    private JButton bLeft;
    private JButton bRight;
    private JButton bUp;
    private JButton bDown;
    private JButton bPreview;

    private JFrame parent;
    public int renderWidth;
    public int renderHeight;
    public static final int tileSize  = 15;
    private boolean previewMode       = false;
    public double   zoom              = 1.0;
    public Point    renderTranslation = new Point(0, 0);


    public DesignerWindow(JFrame parent) {
        this.parent = parent;

        setTitle("POV Display Designer");
        setContentPane(designerRoot);
        setLocationRelativeTo(parent);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        designerSteps.setValue(130);
        designerSteps.addChangeListener(changeEvent -> {
            if ((int)designerSteps.getValue() < 1)
                designerSteps.setValue(1);
            DataHelper.reinitializeAnimationData((int) designerSteps.getValue());
        });

        bLeft.addActionListener(actionEvent -> {
            LinkedList data = (LinkedList)DataHelper.AnimationData;
            String backup = (String)data.getFirst();
            for (int i = 1; i < data.size(); i++)
                data.set(i-1, data.get(i));
            data.set(data.size()-1, backup);
        });

        bRight.addActionListener(actionEvent -> {
            LinkedList data = (LinkedList)DataHelper.AnimationData;
            data.add(0, data.getLast());
            data.removeLast();
        });

        bUp.addActionListener(actionEvent -> {
            LinkedList data = (LinkedList)DataHelper.AnimationData;
            for (int i = 0; i < data.size(); i++) {
                String s = (String)data.get(i);
                data.set(i, s.substring(1)+s.charAt(0));
            }
        });

        bDown.addActionListener(actionEvent -> {
            LinkedList data = (LinkedList)DataHelper.AnimationData;
            for (int i = 0; i < data.size(); i++) {
                String s = (String)data.get(i);
                data.set(i, s.charAt(s.length() - 1) + s.substring(0, s.length() - 1));
            }
        });

        bPreview.addActionListener(actionEvent -> previewMode = !previewMode);
    }

    @Override
    public void setVisible(boolean b) {
        zoom = 1.0;
        previewMode = false;
        renderTranslation = new Point(0, 0);

        super.setVisible(b);
        pack();
        setSize(getWidth()*3, getHeight()+(tileSize * 20));
        setLocationRelativeTo(parent);
    }

    private void createUIComponents() {
        renderPanel = new JPanel() {
            private Point getPointAround(int X, int Y, int centerX, int centerY, double angle) {
                angle = Math.toRadians(angle);
                double sin = Math.sin(angle);
                double cos= Math.cos(angle);
                int DX = X - centerX;
                int DY = Y - centerY;
                return new Point(
                        (int)(DX * cos - DY * sin + centerX),
                        (int)(DX * sin + DY * cos + centerY)
                );
            }

            @Override protected void paintComponent(Graphics g) {
                renderWidth = getWidth();
                renderHeight = getHeight();

                Graphics2D g2 = (Graphics2D)g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                g2.setColor(Color.LIGHT_GRAY);
                g2.fill(new Rectangle2D.Double(0, 0, renderWidth, renderHeight));

                AffineTransform savedTransform = g2.getTransform();
                g2.translate((renderWidth / 2), (renderHeight / 2));

                int size = (int)(tileSize * zoom);
                g2.setFont(g2.getFont().deriveFont((float)(7 * zoom)));
                if (previewMode) {
                    Point p;
                    g2.translate(0, -size/2);
                    for (int x = 0; x < DataHelper.AnimationSteps; x++) {
                        p = getPointAround(0, (int)(20 * zoom * size), 0, 0, x * DataHelper.itemAngle);
                        g2.setColor(Color.BLACK);
                        g2.fillOval(p.x, p.y, size, size);
                        g2.setColor(Color.WHITE);
                        g2.drawString(String.format("%3s", (x + 1)), p.x + 2, (int)(p.y + size * 0.6));
                        for (int y = 0; y < 14; y++) {
                            p = getPointAround(0, (int)((19 - y) * zoom * size), 0, 0, x * DataHelper.itemAngle);
                            g2.setColor((y == 0 ? Color.decode("#33FF00") : Color.decode("#FF2200")));
                            if (DataHelper.isLedOn(x, y))
                                g2.fillOval(p.x, p.y, size, size);
                            else
                                g2.drawOval(p.x, p.y, size, size);
                        }
                    }
                } else {
                    g2.translate(renderTranslation.getX(), renderTranslation.getY());
                    g2.translate(-(DataHelper.AnimationSteps / 2 * size), -(6.5 * size));
                    for (int x = 0; x < DataHelper.AnimationSteps; x++) {
                        g2.setColor(Color.BLACK);
                        g2.fillOval(x * size, -size, size, size);
                        g2.setColor(Color.WHITE);
                        g2.drawString(String.format("%3s", (x + 1)), (int) (x * size + 1 * zoom), -(int) (size * 0.4));
                        for (int y = 0; y < 14; y++) {
                            g2.setColor((y == 0 ? Color.decode("#33FF00") : Color.decode("#FF2200")));
                            if (DataHelper.isLedOn(x, y))
                                g2.fillOval(x * size, y * size, size, size);
                            else
                                g2.drawOval(x * size, y * size, size, size);
                        }
                    }
                }

                // render border
                g2.setTransform(savedTransform);
                g2.setColor(Color.BLACK);
                g2.drawRect(0, 0, renderWidth-1, renderHeight-1);
                repaint();
            }
        };

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private int button = 0;
            private Point oldPos = null;

            /** CONTROLS
             Left mouse - set
             Right mouse - remove\n
             Right mouse + ctrl - remove row
             Middle mouse - move around
             Mouse wheel - zoom
             Mouse wheel + ctrl - faster zoom
             */
            private void doMouse(int type, MouseEvent e) {
                if (type == 1)
                    button = e.getButton();

                int size = (int)(tileSize * zoom);
                int x = -(int)(( renderWidth / 2) + renderTranslation.getX() - e.getX() - ((int)designerSteps.getValue() / 2 * size)) / size;
                int y = -(int)((renderHeight / 2) + renderTranslation.getY() - e.getY() - (6.5 * size)) / size;

                if(!previewMode)
                    switch (button) {
                        // set LED
                        case MouseEvent.BUTTON1:
                            DataHelper.setLed(x, y, true);
                            break;
                        case MouseEvent.BUTTON3:
                            DataHelper.setLed(x, y, false);
                            break;
                    }

                if (button == MouseEvent.BUTTON2) {
                    switch (type) {
                        case 1:
                            oldPos = e.getPoint();
                            break;
                        case 2:
                            renderTranslation.move(
                                    (int)(renderTranslation.getX() + (e.getX() - oldPos.getX())),
                                    (int)(renderTranslation.getY() + (e.getY() - oldPos.getY()))
                            );
                            oldPos = e.getPoint();
                            break;
                    }
                }
            }

            @Override public void mousePressed(MouseEvent e) { doMouse(1, e); }
            @Override public void mouseDragged(MouseEvent e) { doMouse(2, e); }
            @Override public void mouseClicked(MouseEvent e) { doMouse(1, e); }
            @Override public void mouseReleased(MouseEvent e) { doMouse(3, e); }

            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                zoom -= e.getWheelRotation() * ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0 ? zoom/2 : 0.05);
                if (zoom <= 0)
                    zoom = 0.05;
            }
        };
        renderPanel.addMouseListener(mouseAdapter);
        renderPanel.addMouseMotionListener(mouseAdapter);
        renderPanel.addMouseWheelListener(mouseAdapter);
    }
}
