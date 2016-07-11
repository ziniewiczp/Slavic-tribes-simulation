import java.awt.Color;
import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;

//every object of this class is separated tribe, e.g. wislanie
public class Population {
    private String name;
    private Integer size;
    private ArrayList<MainBoard.Board.Rectangle> rectList;
    private ArrayList<MainBoard.Board.Rectangle> nbhoodRectList;
    private double[][] neighbourSurroundBonusQualification;
    private MainBoard.Board.Rectangle[][] boardRect;
    private Color color;

    Population(MainBoard.Board.Rectangle _rect, MainBoard.Board.Rectangle[][] _boardRect, Integer _size, Color _color,
            String _name) {
        this.name = _name;
        this.rectList = new ArrayList<MainBoard.Board.Rectangle>();
        this.nbhoodRectList = new ArrayList<MainBoard.Board.Rectangle>();

        _rect.SetColor(_color);
        this.rectList.add(_rect);
        this.size = _size;
        this.color = _color;
        this.boardRect = _boardRect;
        this.neighbourSurroundBonusQualification = new double[boardRect.length][boardRect[0].length];
        updateNbhoodList(_rect, this.color);
    }

    Integer getSize() {
        return this.size;
    }

    ArrayList<MainBoard.Board.Rectangle> getRectList() {
        return this.rectList;
    }

    ArrayList<MainBoard.Board.Rectangle> getNbhoodRectList() {
        return this.nbhoodRectList;
    }

    Color getColor() {
        return this.color;
    }

    void setSize(Integer _size) {
        this.size = _size;
    }

    // searching for the best development direction from the neighbourhood list
    MainBoard.Board.Rectangle findBestNbhoodDirection() {
        int bestNbhoodRect = 0;
        int x, y, xBest = 0, yBest = 0;
        for (int i = 0; i < (nbhoodRectList.size()); i++) {
            x = nbhoodRectList.get(i).i;
            y = nbhoodRectList.get(i).j;
            xBest = nbhoodRectList.get(bestNbhoodRect).i;
            yBest = nbhoodRectList.get(bestNbhoodRect).j;
            if (nbhoodRectList.get(i).qualification
                    + neighbourSurroundBonusQualification[x][y] > nbhoodRectList.get(bestNbhoodRect).qualification
                            + neighbourSurroundBonusQualification[xBest][yBest]) {
                bestNbhoodRect = i;
            }
        }
        neighbourSurroundBonusQualification[xBest][yBest] = 0;
        return nbhoodRectList.get(bestNbhoodRect);
    }

    double countQualification(MainBoard.Board.Rectangle _rect) {
        /**
         * couting qualification for every rect (done while adding to the
         * neighbourhood list)
         * 
         * Weights: - soil - 0.6 - rivers - 0.3 - height - 0.1
         * 
         * ad. soils. Possible points 1-11 (11 - most fertile soil) ad. rivers.
         * Possible points 0 or 11 ad. height. Possible points 1-11 (11 - the
         * lowest heights; up to 227.27 m above sea level)
         * 
         * When there's no data about soil whole qualification equals -1. That
         * makes it almost impossible to reach over map borders.
         */

        double tmp = 0, tmp_altitude = 0, qualification = 0;
        tmp = 0.6 * (MainBoard.tableSoilData[_rect.soilId].fertility);
        qualification += tmp;

        if (_rect.isRiver) {
            tmp = 0.3 * 11;
            qualification += tmp;
        }

        for (int i = 11; tmp_altitude < _rect.altitude; i++) {
            tmp_altitude = tmp_altitude + 227.27;
            if (_rect.altitude <= tmp_altitude) {
                tmp = 0.1 * i;
                qualification += tmp;
            }
        }

        if (MainBoard.tableSoilData[_rect.soilId].fertility == -1) {
            qualification = -1;
        }
        return qualification;
    }

    int checkIfSurrounded(MainBoard.Board.Rectangle _rect, Color _color) {
        // increasing qualification while there are allied rects in
        // neighbourhood and decreasing when there are enemies around.
        // in case of being totally surrounded by enemy - enemy takes over this
        // rect
        int countSameRects = 0;
        int countForeignRects = 0;
        int returnInt = 0;
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if ((x != 0) || (y != 0)) {
                    if (rectList.contains(boardRect[_rect.i + x][_rect.j + y])) {
                        if (boardRect[_rect.i + x][_rect.j + y].GetColor().equals(_color))
                            countSameRects++;
                    }

                    if (!Simulation.MainB.GetRects()[_rect.i + x][_rect.j + y].IsColorNull()) {
                        if (!Simulation.MainB.GetRects()[_rect.i + x][_rect.j + y].GetColor().equals(_color)) {
                            countForeignRects++;
                        }
                    }
                }
            }
        }
        if (countForeignRects == 4)
            returnInt -= 4;
        if (countForeignRects == 5)
            returnInt -= 6;
        if (countForeignRects == 6)
            returnInt -= 9;
        if (countForeignRects == 7)
            returnInt -= 11;
        if (countForeignRects == 8) {
            returnInt -= 30;
            this.rectList.remove(_rect); // removing rect totally surrounded by
                                         // enemies
        }
        if (countSameRects == 3)
            returnInt += 2;
        else if (countSameRects == 4)
            returnInt += 3;
        else if (countSameRects == 5)
            returnInt += 4;
        else if (countSameRects == 6)
            returnInt += 5;
        else if (countSameRects == 7)
            returnInt += 7;
        else if (countSameRects == 8)
            returnInt += 11;
        return returnInt;
    }

    void updateRectList(MainBoard.Board.Rectangle _rect) {
        // increasing area controlled by the tribe
        _rect.color = new Color(255, 0, 0);
        this.rectList.add(_rect);
        this.updateNbhoodList(_rect, this.color);
    }

    void updateNbhoodList(MainBoard.Board.Rectangle _rect, Color _color) {
        /**
         * updating neighbourhood list of provided tribe, gets recently added
         * rect and checks his neighbourhood if some neighbour is not yet in the
         * neigbourhood list of the tribe it's added
         */

        int ColorVici = 0;

        // removing rect from neighbourhood list (now it should be in rectList)
        if (nbhoodRectList.contains(boardRect[_rect.i][_rect.j])) {
            nbhoodRectList.remove(boardRect[_rect.i][_rect.j]);
        }

        for (int ii = -1; ii < 2; ii++) {
            for (int jj = -1; jj < 2; jj++) {
                if ((jj != 0) || (ii != 0)) {
                    if (_rect.i + ii > 0            // checks if rect doesn't reach over map boundaries (horizontally)
                            && _rect.j + jj > 0     // checks if rect doesn't reach over map boundaries (vertically)
                            && (!rectList.contains(boardRect[_rect.i + ii][_rect.j + jj])))     // checks if rect isn't already added to the tribe area
                    {

                        if (boardRect[_rect.i + ii][_rect.j + jj].IsColorNull()
                                && boardRect[_rect.i + ii][_rect.j + jj].qualification != -1) {
                            // changing qualification in accordance to neighbourhood
                            neighbourSurroundBonusQualification[_rect.i + ii][_rect.j + jj] = checkIfSurrounded(
                                    boardRect[_rect.i + ii][_rect.j + jj], this.color);
                        }

                        // checks if rect isn't taken by some tribe
                        if (!Simulation.MainB.GetRects()[_rect.i + ii][_rect.j + jj].IsColorNull()) {
                            if (!Simulation.MainB.GetRects()[_rect.i + ii][_rect.j + jj].GetColor().equals(_color)) {
                                if ((Simulation.qualificationArray[boardRect[_rect.i][_rect.j].WhatColor()]
                                        - Simulation.qualificationArray[boardRect[_rect.i + ii][_rect.j + jj]
                                                .WhatColor()] > 600)) {
                                    
                                    // if overall qualification of a tribe is greater than the opposite tribe's rect is
                                    // added to the neighbourhood list
                                    ColorVici = boardRect[_rect.i + ii][_rect.j + jj].WhatColor();
                                    Simulation.getPopulation(ColorVici).rectList
                                            .remove(boardRect[_rect.i + ii][_rect.j + jj]);
                                    Simulation.qualificationArray[ColorVici] -= _rect.qualification;
                                    nbhoodRectList.add(boardRect[_rect.i + ii][_rect.j + jj]);
                                }
                            }
                        }

                        if (!nbhoodRectList.contains(boardRect[_rect.i + ii][_rect.j + jj])) {
                            // if rect isn't taken by any tribe it is added to the neighbourhood list
                            if (boardRect[_rect.i + ii][_rect.j + jj].IsColorNull()) {
                                boardRect[_rect.i + ii][_rect.j + jj].qualification = countQualification(
                                        boardRect[_rect.i + ii][_rect.j + jj]);
                                if (boardRect[_rect.i + ii][_rect.j + jj].qualification != -1) {
                                    nbhoodRectList.add(boardRect[_rect.i + ii][_rect.j + jj]);
                                }
                            }
                        }
                    }

                }
            }
        }
        boardRect[_rect.i][_rect.j].color = _color;
        Simulation.qualificationArray[_rect.WhatColor()] += _rect.qualification;
    }
}