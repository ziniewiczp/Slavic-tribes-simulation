import java.awt.Color;

import javax.swing.SwingUtilities;

public class Simulation {
    private static Population p1;
    private static Population p2;
    private static Population p3;
    private static Population p4;
    private static Population p5;
    private static Population p6;
    private static Population p7;
    private static MainWindow.MainFrame mw;
    private static Integer period = 1;
    public static MainBoard.Board MainB;
    static double[] qualificationArray;

    public static Population getPopulation(int _color) {
        switch (_color) {
        case 1:
            return p1;
        case 2:
            return p2;
        case 3:
            return p3;
        case 4:
            return p4;
        case 5:
            return p5;
        case 6:
            return p6;
        case 7:
            return p7;
        default:
            return p1;
        }
    }

    Integer getPeriod() {
        return period;
    }

    public static void main(String[] args) {
        try {
            MainBoard.MyImage PolandBackground = new MainBoard.MyImage("PolandHipso2_lowRes.bmp");
            MainBoard.MyImage PolandHipso = new MainBoard.MyImage("PolandHipso2_lowRes.bmp");
            MainBoard.MyImage PolandHydro = new MainBoard.MyImage("polandHydro_lowRes.bmp");
            MainBoard.MyImage PolandSoil = new MainBoard.MyImage("PolandSurface_lowRes.bmp");

            MainB = new MainBoard.Board(PolandHipso.GetWidth(), PolandHipso.GetHeight());
            MainB.LoadImageData(PolandBackground, MainBoard.dataType.initialize, false);
            MainB.LoadImageData(PolandHipso, MainBoard.dataType.altitude, true);
            MainB.LoadImageData(PolandHydro, MainBoard.dataType.isRiver, false);
            MainB.LoadImageData(PolandSoil, MainBoard.dataType.soil, false);
            MainB.ParseToRect(MainBoard.boardSize);
            qualificationArray = new double[8];

            p1 = new Population(MainB.boardRect[60][75], MainB.boardRect, 1000, new Color(255, 0, 0), "wislanie");
            p2 = new Population(MainB.boardRect[72][33], MainB.boardRect, 1000, new Color(255, 100, 150), "mazowszanie");
            p3 = new Population(MainB.boardRect[75][58], MainB.boardRect, 1000, new Color(100, 50, 200), "ledzianie");
            p4 = new Population(MainB.boardRect[35][40], MainB.boardRect, 1000, new Color(100, 0, 100), "polanie");
            p5 = new Population(MainB.boardRect[32][63], MainB.boardRect, 1000, new Color(200, 0, 200), "slezanie");
            p6 = new Population(MainB.boardRect[35][15], MainB.boardRect, 1000, new Color(0, 0, 255), "pomorzanie");
            p7 = new Population(MainB.boardRect[80][10], MainB.boardRect, 1000, new Color(0, 0, 0), "prusy");

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mw = new MainWindow.MainFrame(1200, 768, MainBoard.boardSize, MainB.GetRects(), MainB.GetPoints());
                    mw.setVisible(true);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void simulate() {
        period += 2;

        if (p1.getNbhoodRectList().size() != 0)
            p1.updateRectList(p1.findBestNbhoodDirection());
        if (p2.getNbhoodRectList().size() != 0)
            p2.updateRectList(p2.findBestNbhoodDirection());
        if (p3.getNbhoodRectList().size() != 0)
            p3.updateRectList(p3.findBestNbhoodDirection());
        if (p4.getNbhoodRectList().size() != 0)
            p4.updateRectList(p4.findBestNbhoodDirection());
        if (p5.getNbhoodRectList().size() != 0)
            p5.updateRectList(p5.findBestNbhoodDirection());
        if (p6.getNbhoodRectList().size() != 0)
            p6.updateRectList(p6.findBestNbhoodDirection());
        if (p7.getNbhoodRectList().size() != 0)
            p7.updateRectList(p7.findBestNbhoodDirection());

        try {
            Thread.sleep(75);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        mw.repaint();
    }
}