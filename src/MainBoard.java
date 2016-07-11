import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

public class MainBoard {
    static Integer boardSize = 8;
    public static Board.Soil[] tableSoilData;

    public enum dataType {
        // initialize is used to read colors and display and initialize two-dimensional array of Points
        initialize, altitude, isRiver, soil
    }

    // class used to load images
    public static class MyImage {
        private BufferedImage imageData;
        private Color[][] pixelData;
        private Integer pixelDataWidth;
        private Integer pixelDataHeight;

        public MyImage(String imageSrc) throws IOException {
            imageData = ImageIO.read(new File(imageSrc));
            Integer imageWidth = imageData.getWidth();
            Integer imageHeight = imageData.getHeight();
            pixelData = new Color[imageWidth][imageHeight];
            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    pixelData[x][y] = new Color(imageData.getRGB(x, y));
                }
            }
            pixelDataWidth = pixelData.length;
            pixelDataHeight = pixelData[0].length;
        }

        Color[][] GetImageData() {
            return pixelData;
        }

        Integer GetWidth() {
            return pixelDataWidth;
        }

        Integer GetHeight() {
            return pixelDataHeight;
        }

        Color GetPixel(int x, int y) {
            return pixelData[x][y];
        }
    }

    public static class Board implements Serializable {
        private static final long serialVersionUID = 2627716761922438084L;
        Integer boardWidth;
        Integer boardHeight;
        Rectangle[][] boardRect;
        static HashMap<dataType, HashMap<Color, Integer>> legendMap;
        private Point[][] points;

        Board(int width, int height) {
            boardWidth = width;
            boardHeight = height;
            boardRect = new Rectangle[(int) Math.floor(width / boardSize)][(int) Math.floor(height / boardSize)];
            legendMap = new HashMap<dataType, HashMap<Color, Integer>>();
            MakeSoilData();
            MakeAltitudeLegend();
            MakeSoilLegend();
        }

        class Soil {
            int id;
            String name;
            int fertility;

            Soil(int _id, String _name, int _fertility) {
                id = _id;
                name = _name;
                fertility = _fertility;
            };

        }

        static class Point implements Serializable {
            private static final long serialVersionUID = 6263019856176508636L;
            int altitude;
            boolean isRiver;
            Color color;
            int soilId;

            Point(Color _color) {
                color = _color;
            }

            void SetAltitude(int _altitude) {
                altitude = _altitude;
            }

            void SetSoilId(int _id) {
                soilId = _id;
            }

            void SetIsRiver(boolean _isRiver) {
                isRiver = _isRiver;
            }

            Color GetColor() {
                return color;
            }

            int GetSoilId() {
                return soilId;
            }

            int GetAltitude() {
                return altitude;
            }

            boolean GetIsRiver() {
                return isRiver;
            }
        }

        Point[][] GetPoints() {
            return points;
        }

        class Rectangle extends Point {
            private static final long serialVersionUID = 1L;
            int size, i, j;
            double qualification;

            Rectangle(int _size, int _altitude, boolean _isRiver, Color _color, int _soilId, int _i, int _j) {
                super(_color);
                super.SetAltitude(_altitude);
                super.SetIsRiver(_isRiver);
                super.SetSoilId(_soilId);
                size = _size;
                this.i = _i;
                this.j = _j;
                this.qualification = -1;
            }

            int GetSize() {
                return size;
            }

            public boolean IsColorNull() {
                if (color == null)
                    return true;
                else
                    return false;
            }

            public void SetColor(Color _color) {
                color = _color;
            }

            void addPointsToQualification(double _p) {
                this.qualification += _p;
            }

            public int WhatColor() {
                if (IsColorNull())
                    return 0;

                if (color.equals(new Color(255, 0, 0)))
                    return 1;

                if (color.equals(new Color(255, 100, 150)))
                    return 2;

                if (color.equals(new Color(100, 50, 200)))
                    return 3;

                if (color.equals(new Color(100, 0, 100)))
                    return 4;

                if (color.equals(new Color(200, 0, 200)))
                    return 5;

                if (color.equals(new Color(0, 0, 255)))
                    return 6;

                if (color.equals(new Color(0, 0, 0)))
                    return 7;

                return 0;
            }

        }

        Rectangle[][] GetRects() {
            return boardRect;
        }

        void ParseToRect(int _rectSize) throws FileNotFoundException {
            int rectSize = _rectSize;
            int rectSurf = _rectSize * _rectSize;
            int iMax = points.length / rectSize;
            int jMax = points[0].length / rectSize;
            Integer[] soilTab = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            int dominantSoil = 0;
            int dominantSoilId = 0;
            System.out.println(iMax + " " + jMax);
            int avrgAltitude = 0;
            int sumAltitude = 0;
            int isRiverCounter = 0;
            boolean isRiver = false;
            for (int i = 0; i < iMax; i++) {
                for (int j = 0; j < jMax; j++) {
                    for (int x = 0; x < rectSize; x++) {
                        for (int y = 0; y < rectSize; y++) {
                            sumAltitude += points[x + i * rectSize][y + j * rectSize].GetAltitude();
                            soilTab[points[x + i * rectSize][y + j * rectSize].GetSoilId()] += 1;
                            if (points[x + i * rectSize][y + j * rectSize].GetIsRiver())
                                isRiverCounter++;
                            if (x == rectSize - 1 && y == rectSize - 1) {
                                // calculating avarage height _rectSize^2
                                avrgAltitude = (int) Math.floor(sumAltitude / (rectSurf));
                                
                                // if over 1/4 points contains rivers/lakes then rect will be taken as "river"
                                if (isRiverCounter > (int) Math.floor((rectSurf) / 4)) {
                                    isRiver = true;
                                } else {
                                    isRiver = false;
                                }
                                
                                // searching for dominant soil
                                for (int w = 0; w < soilTab.length; w++) {
                                    if (dominantSoil < soilTab[w]) {
                                        dominantSoil = soilTab[w];
                                        dominantSoilId = w;
                                    }
                                }

                                boardRect[i][j] = new Rectangle(_rectSize, avrgAltitude, isRiver, null, dominantSoilId, i, j);
                                sumAltitude = 0;
                                avrgAltitude = 0;
                                isRiverCounter = 0;
                                dominantSoil = 0;
                                dominantSoilId = 0;
                                for (int e = 0; e < soilTab.length; e++) {
                                    soilTab[e] = 0;
                                }
                            }
                        }
                    }
                }
            }
        }

        void LoadImageData(MyImage img, dataType data, boolean findClosest) throws IOException {
            // Loading data based on analysing every pixel and legend
            if (dataType.initialize == data)
                points = new Point[img.GetWidth()][img.GetHeight()];
            for (int y = 0; y < img.GetHeight(); y++) {
                for (int x = 0; x < img.GetWidth(); x++) {
                    // creating and initializing new point
                    switch (data) {
                        case initialize:
                            points[x][y] = new Point(img.GetPixel(x, y));
                            break;
                        case altitude:
                            if (legendMap.get(dataType.altitude).containsKey(img.GetPixel(x, y))) {
                                points[x][y].SetAltitude(legendMap.get(dataType.altitude).get(img.GetPixel(x, y)));
                            } else {
                                points[x][y].SetAltitude(0);
                            }
                            break;
                        case isRiver:
                            if (img.GetPixel(x, y).equals(new Color(255, 255, 255))) {
                                points[x][y].SetIsRiver(false);
                            } else {
                                points[x][y].SetIsRiver(true);
                            }
                            break;
                        case soil:
                            if (legendMap.get(dataType.soil).containsKey(img.GetPixel(x, y))) {
                                points[x][y].SetSoilId(legendMap.get(dataType.soil).get(img.GetPixel(x, y)));
                            } else {
                                points[x][y].SetSoilId(11);
                            }
                            break;
                    }

                }
            }
            
            // averaging missing values of height above sea level
            if (findClosest) {
                for (int q = 0; q < 5; q++) {
                    for (int y = 0; y < img.GetHeight(); y++) {
                        for (int x = 0; x < img.GetWidth(); x++) {
                            if (points[x][y].GetAltitude() == 0) {
                                points[x][y].SetAltitude(findClosest(x, y, img));
                            }
                        }
                    }
                }
            }

        }

        // method used to finding missing height values and averaging them
        int findClosest(int x, int y, MyImage img) {
            int newAltitude;
            int counter = 0;
            int index = 0;
            int index2 = 0;
            int range = 2;
            if (x < range)
                x += range;
            if (y < range)
                y += range;
            if (x > img.GetWidth() - range)
                x -= range;
            if (y > img.GetHeight() - range)
                y -= range;
            int[] tempAltitudes = new int[(int) Math.pow((range * 2), 2)];
            Random generator = new Random();
            for (int i = x - range; i < x + range; i++) {
                for (int j = y - range; j < y + range; j++) {
                    if (i != x || j != y) {
                        if (points[i][j].GetAltitude() == 0) {
                            counter++;
                        } else {
                            tempAltitudes[index] = points[i][j].GetAltitude();
                            index++;
                        }
                    }
                }
            }
            if (counter >= 3) {
                newAltitude = 0;
            } else {
                index2 = generator.nextInt(index);
                newAltitude = tempAltitudes[index2];
            }
            return newAltitude;
        }


        private void MakeAltitudeLegend() {
            HashMap<Color, Integer> LegendAltitudeMap = new HashMap<Color, Integer>();
            LegendAltitudeMap.put(new Color(133, 93, 69), 2500);
            LegendAltitudeMap.put(new Color(146, 108, 77), 2000);
            LegendAltitudeMap.put(new Color(161, 124, 85), 1500);
            LegendAltitudeMap.put(new Color(175, 141, 92), 1000);
            LegendAltitudeMap.put(new Color(192, 162, 100), 700);
            LegendAltitudeMap.put(new Color(210, 184, 106), 500);
            LegendAltitudeMap.put(new Color(234, 220, 112), 400);
            LegendAltitudeMap.put(new Color(255, 249, 116), 300);
            LegendAltitudeMap.put(new Color(218, 233, 122), 200);
            LegendAltitudeMap.put(new Color(180, 201, 124), 150);
            LegendAltitudeMap.put(new Color(130, 167, 122), 100);
            LegendAltitudeMap.put(new Color(94, 139, 115), 50);
            LegendAltitudeMap.put(new Color(61, 117, 108), 0);
            legendMap.put(dataType.altitude, LegendAltitudeMap);
        }

        private void MakeSoilLegend() {
            HashMap<Color, Integer> LegendSoilMap = new HashMap<Color, Integer>();
            LegendSoilMap.put(new Color(255, 255, 26), 0);      // bielicowe
            LegendSoilMap.put(new Color(224, 191, 128), 1);     // plowe
            LegendSoilMap.put(new Color(178, 102, 26), 2);      // brunatne w³aœciwe
            LegendSoilMap.put(new Color(206, 131, 102), 3);     // brunatne kwaœne
            LegendSoilMap.put(new Color(124, 133, 135), 4);     // Czarnoziemy
            LegendSoilMap.put(new Color(102, 101, 64), 5);      // Czarne ziemie
            LegendSoilMap.put(new Color(101, 190, 166), 6);     // Mady
            LegendSoilMap.put(new Color(133, 163, 37), 7);      // Gleby bagienne
            LegendSoilMap.put(new Color(157, 101, 178), 8);     // Redziny
            LegendSoilMap.put(new Color(179, 181, 155), 9);     // Gleby inicjalne i s³abo wykszta³cone
            LegendSoilMap.put(new Color(216, 1, 12), 10);       // Gleby antropogeniczne

            legendMap.put(dataType.soil, LegendSoilMap);
        }

        private void MakeSoilData() {
            tableSoilData = new Soil[12];
            tableSoilData[0] = new Soil(0, "bielicowe", 5);
            tableSoilData[1] = new Soil(1, "plowe", 7);
            tableSoilData[2] = new Soil(2, "brunatne w³aœciwe", 8);
            tableSoilData[3] = new Soil(3, "brunatne kwaœne", 2);
            tableSoilData[4] = new Soil(4, "Czarnoziemy", 11);
            tableSoilData[5] = new Soil(5, "Czarne ziemie", 9);
            tableSoilData[6] = new Soil(6, "Mady", 10);
            tableSoilData[7] = new Soil(7, "Gleby bagienne", 4);
            tableSoilData[8] = new Soil(8, "Rêdziny", 6);
            tableSoilData[9] = new Soil(9, "Gleby inicjalne i s³abo wykszta³cone", 3);
            tableSoilData[10] = new Soil(10, "Gleby antropogeniczne", 1);
            tableSoilData[11] = new Soil(11, "brak danych", -1);
        }

        public void PrintOut() {
            Integer iMax = points.length;
            Integer jMax = points[0].length;
            for (int i = 0; i < iMax; i++) {
                for (int j = 0; j < jMax; j++) {
                    System.out.println("X:" + i + " Y:" + j);
                    if (points[i][j].GetAltitude() != 0)
                        System.out.println("X:" + i + " Y:" + j + " -> " + points[i][j].GetColor() + " | "
                                + points[i][j].GetAltitude() + " | " + points[i][j].GetIsRiver());
                }
            }
        }

        void SerializeSer() {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            System.out.println("Serializuje do .ser");
            try {
                fos = new FileOutputStream("Europe.ser");   // creating output stream
                oos = new ObjectOutputStream(fos);
                oos.writeObject(points);
                oos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oos != null)
                        oos.close();
                } catch (IOException e) {
                }
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                }
            }
        }

        void DeserializeSer() {
            System.out.println("Deserializuje z pliku .ser");
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream("Europe.ser");    // creating input stream
                ois = new ObjectInputStream(fis);
                points = (Point[][]) ois.readObject();      // deserializing object
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException e) {}
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {}
            }
        }
    }
}