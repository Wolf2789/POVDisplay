package com.wlf2789;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.File;

public class GUI extends JFrame {
    private JPanel guiRoot;
    private JButton bNew;
    private JButton bLoad;
    private JButton bPreviewData;
    private JButton bSave;
    private JPanel renderPanel;
    public JComboBox exportOptions;
    public JCheckBox userFriendly;
    private JButton bPreviewImage;
    private JComboBox cbDisplay;
    private Output output;

    // constants
    public static final int    tileSize  = 15;
    public static final double itemAngle = 360.0 / Main.maxItems;

    // variables
    private boolean previewImageMode  = false;
    public int renderWidth;
    public int renderHeight;
    public double   zoom              = 1.0;
    public Point    renderTranslation = new Point((int)(-Main.maxItems / 4 * tileSize), (int)(-9 * tileSize));

    public GUI() {
        setTitle("POVD Animator");
        setContentPane(guiRoot);
        pack();
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        createUIComponents();

        Rectangle bounds = getBounds();
        bounds.height = tileSize * 15 + 70;
        setBounds(bounds);
        setMinimumSize(new Dimension(bounds.width, bounds.height));

        bNew.addActionListener(e -> Main.clearData());
        bPreviewData.addActionListener(e -> output.show(Main.dataExport()));
        bPreviewImage.addActionListener(e -> previewImageMode = !previewImageMode);

        bLoad.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (fileChooser.showOpenDialog(GUI.this) == JFileChooser.APPROVE_OPTION)
                Main.loadData(fileChooser.getSelectedFile());
        });

        bSave.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (fileChooser.showSaveDialog(GUI.this) == JFileChooser.APPROVE_OPTION)
                Main.saveData(fileChooser.getSelectedFile());
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent componentEvent) {
                renderWidth = renderPanel.getWidth();
                renderHeight = renderPanel.getHeight();
            }
        });

        setVisible(true);
        output = new Output(this);

        Main.loadData(new File("/home/wlf/Arduino/Projects/POVDisplay/povd/animation.h"));
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
                Graphics2D g2 = (Graphics2D)g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                g2.setColor(Color.WHITE);
                g2.fill(getBounds());

                g2.translate((renderWidth / 2), (renderHeight / 2));
                g2.translate(-renderTranslation.getX(), -renderTranslation.getY());
//                g2.scale(zoom, zoom);


                if (previewImageMode) {
                    if (cbDisplay.getSelectedItem() == "Straight") {
                        g2.translate( -(Main.maxItems * 2), -28);
                        for (int x = 0; x < Main.maxItems; x++)
                            for (int y = 0; y < 14; y++) {
                                g2.setColor((y == 0 ? Color.GREEN : Color.RED));
                                if (Main.getLed(x, y))
                                    g2.fillOval(x*4, y*4, 4,4);
                            }
                    } else {
                        Point p;
                        for (int x = 0; x < Main.maxItems; x++)
                            for (int y = 0; y < 14; y++) {
                                g2.setColor((y == 0 ? Color.GREEN : Color.RED));
                                p = getPointAround(0, 30 + (14 - y) * tileSize, 0, 0, x * itemAngle);
                                if (Main.getLed(x, y))
                                    g2.fillOval(p.x, p.y, tileSize,tileSize);
                            }
                    }
                } else {
                    g2.setFont(g2.getFont().deriveFont((float)(7 * zoom)));
                    int size = (int)(tileSize * zoom);
                    if (cbDisplay.getSelectedItem() == "Straight") {
                        g2.translate(-(Main.maxItems / 2 * size), -(7.5 * size));
                        for (int x = 0; x < Main.maxItems; x++) {
                            g2.setColor(Color.BLACK);
                            g2.fillOval(x * size, -size, size,size);
                            g2.setColor(Color.WHITE);
                            g2.drawString(String.format("%3s", (x + 1)), (int)(x * size + 1 * zoom), -(int)(size * 0.4));
                            for (int y = 0; y < 14; y++) {
                                g2.setColor((y == 0 ? Color.GREEN : Color.RED));
                                if (Main.getLed(x, y))
                                    g2.fillOval(x*size, y*size, size, size);
                                else
                                    g2.drawOval(x*size, y*size, size, size);
                            }
                        }
                    } else {
                        Point p;
                        for (int x = 0; x < Main.maxItems; x++) {
                            p = getPointAround(0, (int)(20 * zoom * size), 0, 0, x * itemAngle);
                            g2.setColor(Color.BLACK);
                            g2.fillOval(p.x, p.y, size, size);
                            g2.setColor(Color.WHITE);
                            g2.drawString(String.format("%3s", (x + 1)), p.x + 2, (int)(p.y + size * 0.6));
                            for (int y = 0; y < 14; y++) {
                                p = getPointAround(0, (int)((19 - y) * zoom * size), 0, 0, x * itemAngle);
                                g2.setColor((y == 0 ? Color.GREEN : Color.RED));
                                if (Main.getLed(x, y))
                                    g2.fillOval(p.x, p.y, size, size);
                                else
                                    g2.drawOval(p.x, p.y, size, size);
                            }
                        }
                    }
                }
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

                // set leds if not in preview image mode
                if (!previewImageMode) {
                    int size = (int)(tileSize * zoom);
                    int x, y;
                    if (cbDisplay.getSelectedItem() == "Straight") {
                        x = -(int)(( renderWidth / 2) - renderTranslation.getX() - e.getX() - (Main.maxItems / 2 * size)) / size;
                        y = -(int)((renderHeight / 2) - renderTranslation.getY() - e.getY() - (7.5 * size)) / size;
                    } else {
                        x = -(int)(( renderWidth / 2) - renderTranslation.getX() - e.getX());
                        y = -(int)((renderHeight / 2) - renderTranslation.getY() - e.getY());
                        y = (int)Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2) ) / size;
                        System.out.printf("x,y: %d,%d\n", x, y);
                    }

                    switch (button) {
                        // set LED
                        case MouseEvent.BUTTON1:
                            Main.setLed(x, y, true);
                            break;
                        case MouseEvent.BUTTON3:
                            if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0)
                                Main.clearDataAt(x);
                            else
                                Main.setLed(x, y, false);
                            break;
                    }
                }

                if (button == MouseEvent.BUTTON2) {
                    switch (type) {
                        case 1:
                            oldPos = e.getPoint();
                            break;
                        case 2:
                            renderTranslation.move(
                                    (int)(renderTranslation.getX() + (oldPos.getX() - e.getX())),
                                    (int)(renderTranslation.getY() + (oldPos.getY() - e.getY()))
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
