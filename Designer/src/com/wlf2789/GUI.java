package com.wlf2789;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class GUI extends JFrame {
    private JPanel guiRoot;
    private JButton bNew;
    private JButton bLoad;
    private JButton bPreviewData;
    private JButton bSave;
    private JPanel renderPanel;
    private JButton bPreviewImage;
    private JComboBox cbDisplay;
    private JList<Integer> lFrames;
    private JButton bAddU;
    private JButton bDel;
    private Output output;
    public JComboBox exportOptions;
    public JCheckBox compact;
    private JButton bAddD;
    private JButton bCloneU;
    private JButton bCloneD;
    private JTextField tfAdd;
    private JTextField tfClone;
    private JButton bMoveU;
    private JButton bMoveD;
    private JButton bScrollL;
    private JButton bScrollR;
    private JTextField tfMove;
    private JTextField tfScroll;

    // constants
    public static final int    tileSize  = 15;
    public static final double itemAngle = 360.0 / Main.maxItems;

    // variables
    private boolean previewImageMode  = false;
    public int renderWidth;
    public int renderHeight;
    public double   zoom              = 1.0;
    public Point    renderTranslation = new Point(0, 0);

    public GUI() {
        setTitle("POVD Animator");
        setContentPane(guiRoot);
        pack();
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        createUIComponents();

        Rectangle bounds = getBounds();
        bounds.width = 800;
        bounds.height = 300;
        setBounds(bounds);
        setMinimumSize(new Dimension(bounds.width, bounds.height));

        bNew.addActionListener(e -> {
            Main.clearData();
            updateFramesList();
        });
        bPreviewData.addActionListener(e -> output.show(Main.dataExport()));
        bPreviewImage.addActionListener(e -> previewImageMode = !previewImageMode);

        bLoad.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (fileChooser.showOpenDialog(GUI.this) == JFileChooser.APPROVE_OPTION)
                Main.loadData(fileChooser.getSelectedFile());
            updateFramesList();
        });

        bSave.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (fileChooser.showSaveDialog(GUI.this) == JFileChooser.APPROVE_OPTION)
                Main.saveData(fileChooser.getSelectedFile());
        });

        bAddU.addActionListener(e -> {
            int i = getSelectedFrame();
            if (i < 0)
                i = 0;
            Main.addFrame(i);
            updateFramesList();
            lFrames.setSelectedIndex(i);
        });

        bAddD.addActionListener(e -> {
            int i = getSelectedFrame()+1;
            if (i < 0)
                i = 0;
            Main.addFrame(i);
            updateFramesList();
            lFrames.setSelectedIndex(i);
        });

        bCloneU.addActionListener(e -> {
            int i = getSelectedFrame();
            if (i >= 0) {
                Main.duplicateFrame(i);
                updateFramesList();
                lFrames.setSelectedIndex(i);
            }
        });

        bCloneD.addActionListener(e -> {
            int i = getSelectedFrame();
            if (i >= 0) {
                Main.duplicateFrame(i);
                updateFramesList();
                lFrames.setSelectedIndex(i + 1);
            }
        });

        bMoveU.addActionListener(e -> {
            int i = getSelectedFrame();
            if (i > 0) {
                i -= 1;
                Main.swapFrames(i+1, i);
                updateFramesList();
                lFrames.setSelectedIndex(i);
            }
        });

        bMoveD.addActionListener(e -> {
            int i = getSelectedFrame();
            if (i < Main.getFramesCount() - 1) {
                i += 1;
                Main.swapFrames(i-1, i);
                updateFramesList();
                lFrames.setSelectedIndex(i);
            }
        });

        bScrollL.addActionListener(e -> {
            int i = getSelectedFrame();
            if (Main.instance().data.containsKey(i))
                Main.scrollFrame(i, -1);
        });

        bScrollR.addActionListener(e -> {
            int i = getSelectedFrame();
            if (Main.instance().data.containsKey(i))
                Main.scrollFrame(i, +1);
        });

        bDel.addActionListener(e -> {
            int i = getSelectedFrame();
            if (i >= 0) {
                Main.delFrame(lFrames.getSelectedIndex());
                updateFramesList();
                lFrames.setSelectedIndex(i >= lFrames.getModel().getSize() ? i - 1 : i);
            }
        });

        setVisible(true);
        output = new Output(this);

        /*
        Main.loadData(new File("/home/wlf/Coding/INZYNIERKA/POVDisplay/Arduino/animation.h"));
        updateFramesList();
        lFrames.setSelectedIndex(0);
        */
    }

    public void updateFramesList() {
        DefaultListModel<Integer> model = (DefaultListModel)lFrames.getModel();
        model.clear();
        for(int index : Main.instance().data.keySet())
            model.addElement(index);
        lFrames.setModel(model);
    }

    public int getSelectedFrame() {
        return lFrames.getSelectedIndex();
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

            int currentRenderedFrame = 0;

            @Override protected void paintComponent(Graphics g) {
                renderWidth = getWidth();
                renderHeight = getHeight();

                Graphics2D g2 = (Graphics2D)g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                g2.setColor(Color.WHITE);
                g2.fill(new Rectangle2D.Double(0, 0, renderWidth, renderHeight));

                AffineTransform savedTransform = g2.getTransform();
                g2.translate((renderWidth / 2), (renderHeight / 2));
                g2.translate(renderTranslation.getX(), renderTranslation.getY());

                if (previewImageMode) {
                    if (cbDisplay.getSelectedItem() == "Straight") {
                        g2.translate( -(Main.maxItems * 2), -28);
                        for (int x = 0; x < Main.maxItems; x++)
                            for (int y = 0; y < 14; y++) {
                                g2.setColor((y == 0 ? Color.GREEN : Color.RED));
                                if (Main.getLed(currentRenderedFrame, x, y))
                                    g2.fillOval(x*4, y*4, 4,4);
                            }
                    } else {
                        Point p;
                        for (int x = 0; x < Main.maxItems; x++)
                            for (int y = 0; y < 14; y++) {
                                g2.setColor((y == 0 ? Color.GREEN : Color.RED));
                                p = getPointAround(0, 30 + (14 - y) * tileSize, 0, 0, x * itemAngle);
                                if (Main.getLed(currentRenderedFrame, x, y))
                                    g2.fillOval(p.x, p.y, tileSize,tileSize);
                            }
                    }
                    currentRenderedFrame = (currentRenderedFrame + 1) % Main.getFramesCount();
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
                                if (Main.getLed(getSelectedFrame(), x, y))
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
                                if (Main.getLed(getSelectedFrame(), x, y))
                                    g2.fillOval(p.x, p.y, size, size);
                                else
                                    g2.drawOval(p.x, p.y, size, size);
                            }
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

                // set leds if not in preview image mode
                if (!previewImageMode) {
                    int size = (int)(tileSize * zoom);
                    int x, y;
                    if (cbDisplay.getSelectedItem() == "Straight") {
                        x = -(int)(( renderWidth / 2) + renderTranslation.getX() - e.getX() - (Main.maxItems / 2 * size)) / size;
                        y = -(int)((renderHeight / 2) + renderTranslation.getY() - e.getY() - (7.5 * size)) / size;
                    } else {
                        x = Math.abs((int)(( renderWidth / 2) + renderTranslation.getX() - e.getX()));
                        y = Math.abs((int)((renderHeight / 2) + renderTranslation.getY() - e.getY()));
                        System.out.printf("--- START\ne x,y: %d,%d\no x,y: %d,%d\n", e.getX(),e.getY(), x,y);
                        y = (int)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / size;
                        System.out.printf("o   y: %d\n", y);
                    }

                    switch (button) {
                        // set LED
                        case MouseEvent.BUTTON1:
                            Main.setLed(getSelectedFrame(), x, y, true);
                            break;
                        case MouseEvent.BUTTON3:
                            if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0)
                                Main.clearDataAt(getSelectedFrame(), x);
                            else
                                Main.setLed(getSelectedFrame(), x, y, false);
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
