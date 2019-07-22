package ar.edu.itba.ati;

/**
 * Hello world!
 */


import ar.edu.itba.ati.Model.GlobalThreshold;
import ar.edu.itba.ati.Model.Histogram;
import ar.edu.itba.ati.Model.Image;
import ar.edu.itba.ati.Model.Masks.BilateralFilter;
import ar.edu.itba.ati.Model.Masks.Sobel;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private static List<Double> licenseRatios = Arrays.asList((400.0 / 130.0));

    public static void main(String[] args) throws IOException {
        Image image = new Image(new File(App.class.getClassLoader().getResource("patentesJpgMed/IMG_2726.jpg").getPath()));

        Image bilateral = new BilateralFilter().apply(image);

        Image bilateralSobel = new Sobel().apply(bilateral);

        Histogram histogram = new Histogram(bilateralSobel);
        Histogram smoothed = histogram.smooth();
        Histogram umbralized = smoothed.umbralize();

        List<Point> hoirziontalPoints = getIntervals(umbralized.getHorizontal());
        List<Point> verticalPoints = getIntervals(umbralized.getVertical());

        Tuple licencesTuple = getPosibleLicenes(image,bilateralSobel, hoirziontalPoints, verticalPoints, licenseRatios);
        List<Image> licences = licencesTuple.licenses;
        List<Image> licencesSobel = licencesTuple.licensesSobel;
        System.out.println("Posible licenses: " + licences.size());

        purgeDirectory(new File("./outImages/PosibleLicenses"));
//        savePosibleImages(licences);


        int[][] newPixels = image.clonePixels();

        blackbars(newPixels, hoirziontalPoints);
        blackbarsVertical(newPixels, verticalPoints);
        Image blackImage = new Image(newPixels);

        ImageIO.write(bilateral.getBufferedImage(), "jpg", new File("./outImages/bilateral.jpg"));
        ImageIO.write(bilateralSobel.getBufferedImage(), "jpg", new File("./outImages/bilateralSobel.jpg"));
        ImageIO.write(blackImage.getBufferedImage(), "jpg", new File("./outImages/blackimage.jpg"));

        for (int i = 0; i < licences.size(); i++){
            findLicnese(licences.get(i), licencesSobel.get(i),"license"+i);
        }


    }

    private static void findLicnese(Image license, Image licenseSobel, String name){
        licenseSobel.saveImage(new File("./outImages/PosibleLicenses/"+name+".jpg"));

        Histogram licenseHistogram = new Histogram(licenseSobel).smooth();
        System.out.println("LICENSE HISTOGRAM");
        System.out.println(licenseHistogram.getHorizontal());
        System.out.println(licenseHistogram.getVertical());

        Histogram licenseHistUmbralized = licenseHistogram.umbralize();

        Image cutLicense = license.subImage(new Point(0, license.getWidth()), getMaximumSpectrum(getIntervals(licenseHistUmbralized.getVertical())));
        cutLicense.saveImage(new File("./outImages/PosibleLicenses/"+name+"cut.jpg"));
        Histogram cutHistogram = new Histogram(cutLicense).smooth().umbralize();

        int[][] licenseNewPixels = cutLicense.clonePixels();
        blackbars(licenseNewPixels, smoothIntervals(cutLicense.getHeight(),getIntervals(cutHistogram.getHorizontal())));
       // blackbarsVertical(licenseNewPixels, getIntervals(cutHistogram.getVertical()));

        Image licenseBlackImage = new Image(licenseNewPixels);

        licenseBlackImage.saveImage(new File("./outImages/PosibleLicenses/"+name+"cutBlackImage.jpg"));

    }
    private static List<Point> smoothIntervals(int heigth, List<Point> interval){
        List<Point> newInterval = new ArrayList<>();
        for(Point p : interval){
            if((double)p.y-p.x > 10){
                newInterval.add(p);
            }
        }
        return newInterval;
    }
    private static Point getMaximumSpectrum(List<Point> list) {
        Point p = new Point(0, 0);
        for (int i = 1; i < list.size(); i++) {
            int diff = list.get(i).x - list.get(i - 1).y;
            if (diff > p.y - p.x) {
                p = new Point(list.get(i - 1).y, list.get(i).x);
            }
        }
        return p;

    }

    private static List<Point> getIntervals(List<Integer> list) {
        int start = -1;
        List<Point> intervals = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == 0) {
                if (start == -1) {
                    start = i;
                }
            } else {
                if (start != -1) {
                    intervals.add(new Point(start, i));
                    start = -1;
                }
            }
        }

        if (start != -1) {
            intervals.add(new Point(start, list.size()));
        }
        return intervals;
    }

    private static void savePosibleImages(List<Image> licences) {
        for (int i = 0; i < licences.size(); i++) {
            try {
                ImageIO.write(licences.get(i).getBufferedImage(), "jpg", new File("./outImages/PosibleLicenses/license" + i + ".jpg"));
            } catch (IOException e) {
                System.out.println("Could not write image");
            }
        }
    }

    private static Tuple getPosibleLicenes(Image image,Image sobelImage, List<Point> horizontal, List<Point> vertical, List<Double> ratios) {
        List<Image> images = new ArrayList<>();
        List<Image> sobelImages = new ArrayList<>();

        for (int hIndex = 1; hIndex < horizontal.size(); hIndex++) {
            for (int hIndex2 = hIndex; hIndex2 < horizontal.size(); hIndex2++) {
                Point limitH = new Point(horizontal.get(hIndex - 1).y, horizontal.get(hIndex2).x);

                for (int vIndex = 1; vIndex < vertical.size(); vIndex++) {
                    for (int vIndex2 = vIndex; vIndex2 < vertical.size(); vIndex2++) {
                        Point limitV = new Point(vertical.get(vIndex - 1).y, vertical.get(vIndex2).x);
                        if (hasAspectRatio(limitH, limitV, ratios)) {
                            images.add(image.subImage(limitH, limitV));
                            sobelImages.add(sobelImage.subImage(limitH,limitV));
                        }
                    }
                }
            }
        }

        return new Tuple(images,sobelImages);
    }

    private static void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

    private static boolean hasAspectRatio(Point limitH, Point limitV, List<Double> ratios) {
        double tolerance = 0.5;
        double ratio = (double) Math.abs(limitH.y - limitH.x) / (double) Math.abs(limitV.y - limitV.x);

        for (Double desired : ratios) {
            if (isInRange(desired, tolerance, ratio)) {
                System.out.println("Limit H: " + limitH + " Limit V: " + limitV + "Ratio: " + ratio + " Desired ratio " + desired);
                return true;
            }
        }
        return false;
    }

    private static boolean isInRange(double desired, double tolerance, double ratio) {
        return (desired - tolerance) < ratio && ratio < (desired + tolerance);
    }

    private static void blackbars(int[][] pixels, List<Point> points) {
        for (Point p : points) {
            for (int x = p.x; x < p.y; x++) {
                for (int y = 0; y < pixels.length; y++) {
                    pixels[y][x] = 0;
                }
            }
        }
    }

    private static void addBars(int[][] pixels, List<Integer> minimums) {
        blackbars(pixels
                , minimums.stream()
                        .map(min -> new Point(min - 4, min + 4))
                        .collect(Collectors.toList()));
    }

    private static void addBarsVertical(int[][] pixels, List<Integer> minimums) {
        blackbarsVertical(pixels
                , minimums.stream()
                        .map(min -> new Point(min - 4, min + 4))
                        .collect(Collectors.toList()));
    }

    private static void blackbarsVertical(int[][] pixels, List<Point> points) {
        for (Point p : points) {
            for (int y = p.x; y < p.y; y++) {
                for (int x = 0; x < pixels[0].length; x++) {
                    pixels[y][x] = 0;
                }
            }
        }
    }

    private static class Tuple {
        List<Image> licenses;
        List<Image> licensesSobel;

        public Tuple(List<Image> licenses, List<Image> licensesSobel) {
            this.licenses = licenses;
            this.licensesSobel = licensesSobel;
        }
    }
}