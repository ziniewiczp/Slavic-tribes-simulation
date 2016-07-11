import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class MainWindow {
    private static Simulation s = new Simulation();

    public static class MainFrame extends JFrame {
        private static final long serialVersionUID = 1L;
        private MainBoard.Board.Rectangle[][] boardRectangle;
        private MainBoard.Board.Point[][] boardBackground;
        private Integer boardSize;
        private String tribe;

        public MainFrame(int width, int height, int _boardSize, MainBoard.Board.Rectangle[][] _boardRect,
                MainBoard.Board.Point[][] _boardBackground) {
            boardRectangle = _boardRect;
            boardBackground = _boardBackground;
            boardSize = _boardSize;

            JPanel background = new JPanel(new FlowLayout(FlowLayout.LEFT));

            // left panel on which map draws
            JPanel map = new JPanel();
            JComponent comp = new RandomRect();
            comp.setPreferredSize(new Dimension(800, 736));
            map.add(comp);
            map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            background.add(map);

            // right panel with buttons and information
            JPanel rightPanel = new JPanel(new GridLayout(3, 1));

            // panel displaying information about chosen rectangle
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));

            JLabel periodField = new JLabel();
            periodField.setText("Rok: 400 n.e.");

            JLabel infoField = new JLabel();
            infoField.setText("<html> Wspó³rzêdne wybranego punktu:" + "<br>x: " + ", y: " + "<br>Wysokoœæ n.p.m.: "
                    + "<br>Czy rzeka/jezioro: " + "<br>Gleba: " + "<br>Plemiê: " + "</html>");

            // Event used to get information about chosen pixel
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = (int) Math.floor(e.getX() - 10);
                    int y = (int) Math.floor(e.getY() - 30);
                    if ((x / boardSize) <= boardRectangle.length && y >= 30 && x > 10) {
                        switch (boardRectangle[x / boardSize][y / boardSize].WhatColor()) {
                        case 0:
                            tribe = "brak";
                            break;

                        case 1:
                            tribe = "Wiœlanie";
                            break;

                        case 2:
                            tribe = "Mazowszanie";
                            break;

                        case 3:
                            tribe = "Lêdzianie";
                            break;

                        case 4:
                            tribe = "Polanie";
                            break;

                        case 5:
                            tribe = "Œlê¿anie";
                            break;

                        case 6:
                            tribe = "Pomorzanie";
                            break;

                        case 7:
                            tribe = "Prusy";
                            break;
                        }

                        if ((e.getX() - 10) < boardBackground.length && (e.getY() - 30) < boardBackground[0].length) {
                            infoField.setText("<html> Wspó³rzêdne wybranego punktu:" + "<br>x: " + e.getX() + ", y: "
                                    + e.getY() + "<br>Wysokoœæ n.p.m.: " + boardBackground[x][y].GetAltitude()
                                    + "<br>Czy rzeka/jezioro: " + boardBackground[x][y].GetIsRiver() + "<br>Gleba: "
                                    + MainBoard.tableSoilData[boardBackground[x][y].GetSoilId()].name + "<br>Plemiê: "
                                    + tribe + "</html>");
                        }
                    }
                }
            });

            JButton startButton = new JButton();
            JButton pauseButton = new JButton();
            JButton resumeButton = new JButton();

            // executing simulation in seperated thread
            PausableSwingWorker<Void, Integer> worker = new PausableSwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    while (!isCancelled()) {
                        while (!isPaused()) {
                            s.simulate();

                            Integer i = s.getPeriod();
                            publish(i);
                        }
                    }

                    return null;
                }

                @Override
                // executed after calling publish() in doInBackground() used to update GUI
                protected void process(List<Integer> chunks) {
                    Integer value = chunks.get(chunks.size() - 1);

                    periodField.setText("Rok: " + (value / 4 + 400) + " n.e.");
                }
            };

            // actions of every button
            Action start = new AbstractAction("Start") {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    startButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(true);

                    worker.execute();
                }
            };

            Action pause = new AbstractAction("Pause") {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    worker.pause();
                }
            };

            Action resume = new AbstractAction("Resume") {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    worker.resume();
                }
            };

            JPanel buttonPanel = new JPanel();

            startButton.setAction(start);
            pauseButton.setAction(pause);
            resumeButton.setAction(resume);

            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);

            buttonPanel.add(startButton);
            buttonPanel.add(pauseButton);
            buttonPanel.add(resumeButton);

            infoPanel.add(infoField);
            rightPanel.add(periodField);
            rightPanel.add(infoPanel);
            rightPanel.add(buttonPanel);
            background.add(rightPanel);

            this.add(background);

            this.setPreferredSize(new Dimension(width, height));
            this.setSize(new Dimension(width, height));
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setResizable(false);
            this.setTitle("Symulacja rozwoju plemion s³owiañskich");
            this.setVisible(true);
            comp.repaint();

        }

        // drawing map, pixel by pixel
        public class RandomRect extends JComponent {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                for (int x = 0; x < boardBackground.length; x++) {
                    for (int y = 0; y < boardBackground[0].length; y++) {
                        Rectangle box = new Rectangle(x, y, 1, 1);
                        g2.setColor(boardBackground[x][y].GetColor());
                        g2.fill(box);
                    }
                }

                for (int x = 0; x < boardRectangle.length; x++) {
                    for (int y = 0; y < boardRectangle[0].length; y++) {
                        if (!boardRectangle[x][y].IsColorNull()) {
                            Rectangle box = new Rectangle(x * boardSize, y * boardSize, boardSize, boardSize);
                            g2.setColor(boardRectangle[x][y].GetColor());
                            g2.fill(box);
                        }
                    }
                }
            }
        }
    }

    /**
     * Extension of SwingWorker class, needed to pause and
     * resume thread, in defulat only terminating is allowed.
     */
    public abstract static class PausableSwingWorker<K, V> extends SwingWorker<K, V> {
        private volatile boolean isPaused;

        public final void pause() {
            if (!isPaused() && !isDone()) {
                isPaused = true;
                firePropertyChange("paused", false, true);
            }
        }

        public final void resume() {
            if (isPaused() && !isDone()) {
                isPaused = false;
                firePropertyChange("paused", true, false);
            }
        }

        public final boolean isPaused() {
            return isPaused;
        }
    }
}