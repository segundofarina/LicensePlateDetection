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
    private static List<Double> licenseRatios = Arrays.asList( (400.0/130.0) );
    public static void main(String[] args) throws IOException {
        Image image = new Image(new File(App.class.getClassLoader().getResource("IMG_2731.jpg").getPath()));

        Image bilateral = new BilateralFilter().apply(image);

        Image bilateralSobel = new Sobel().apply(bilateral);

        Histogram histogram = new Histogram(bilateralSobel);
        Histogram smoothed  = histogram.smooth();
        Histogram umbralized = smoothed.umbralize();

        List<Point> hoirziontalPoints = histogram.getIntervals(umbralized.getHorizontal());
        List<Point> verticalPoints = histogram.getIntervals(umbralized.getVertical());

        List<Image> licences = getPosibleLicenes(image,hoirziontalPoints,verticalPoints,licenseRatios);
        System.out.println("Posible licenses: "+licences.size());

        purgeDirectory(new File("./outImages/PosibleLicenses"));
        savePosibleImages(licences);


        int[][] newPixels = image.clonePixels();

        blackbars(newPixels, hoirziontalPoints);
        blackbarsVertical(newPixels,verticalPoints);
        Image blackImage = new Image(newPixels);




        Image license = image.subImage(new Point(125,843), new Point(460,702));
        Image licenseSobel = bilateralSobel.subImage(new Point(125,843), new Point(460,702));
        licenseSobel.saveImage(new File("./outImages/license.jpg"));

       Histogram licenseHistogram = new Histogram(licenseSobel).smooth();
       System.out.println("LICENSE HISTOGRAM");
       System.out.println(licenseHistogram.getHorizontal());
       System.out.println(licenseHistogram.getVertical());


       int[][] licenseNewPixels = license.clonePixels();
       blackbars(licenseNewPixels,licenseHistogram.getIntervals(licenseHistogram.umbralize().getHorizontal()));
       blackbarsVertical(licenseNewPixels, licenseHistogram.getIntervals(licenseHistogram.umbralize().getVertical()));

       Image licenseBlackImage = new Image(licenseNewPixels);

       licenseBlackImage.saveImage(new File("./outImages/LicenseBlackImage.jpg"));




        ImageIO.write(bilateral.getBufferedImage(), "jpg", new File("./outImages/bilateral.jpg"));

        ImageIO.write(bilateralSobel.getBufferedImage(), "jpg", new File("./outImages/bilateralSobel.jpg"));
        ImageIO.write(blackImage.getBufferedImage(), "jpg", new File("./outImages/blackimage.jpg"));

    }



    private static void savePosibleImages(List<Image> licences) {
        for(int i = 0; i < licences.size(); i++){
            try {
                ImageIO.write(licences.get(i).getBufferedImage(), "jpg", new File("./outImages/PosibleLicenses/license"+i+".jpg"));
            } catch (IOException e){
                System.out.println("Could not write image");
            }
        }
    }

    private static List<Image> getPosibleLicenes(Image image,  List<Point> horizontal, List<Point> vertical, List<Double> ratios){
        List<Image> images = new ArrayList<>();

        for(int hIndex = 1; hIndex < horizontal.size() ;hIndex++){
            for( int hIndex2 = hIndex; hIndex2 < horizontal.size(); hIndex2++){
                Point limitH = new Point(horizontal.get(hIndex-1).y,horizontal.get(hIndex2).x);

                for( int vIndex = 1; vIndex < vertical.size(); vIndex++){
                    for( int vIndex2 = vIndex; vIndex2 < vertical.size(); vIndex2++){
                        Point limitV = new Point(vertical.get(vIndex-1).y,vertical.get(vIndex2).x);
                        if(hasAspectRatio(limitH,limitV,ratios)){
                            images.add(image.subImage(limitH,limitV));
                            if(images.size() == 9){
                                System.out.println("Puntos " + limitH + limitV);

                            }
                        }
                    }
                }
            }
        }

        return images;
    }

    private static void purgeDirectory(File dir) {
        for (File file: dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

    private static boolean hasAspectRatio(Point limitH, Point limitV, List<Double> ratios) {
        double tolerance = 0.4;
        double ratio = (double) Math.abs(limitH.y - limitH.x) / (double) Math.abs(limitV.y - limitV.x);

        for(Double desired : ratios){
           if(isInRange(desired,tolerance,ratio)){
               System.out.println("Limit H: "+limitH+" Limit V: "+limitV+"Ratio: "+ratio + " Desired ratio " + desired);
               return true;
           }
        }
        return false;
    }

    private static boolean isInRange(double desired, double tolerance, double ratio){
        return (desired - tolerance) < ratio &&  ratio < (desired + tolerance);
    }

    private static void blackbars(int[][] pixels, List<Point> points){
        for(Point p : points){
            for(int x=p.x ; x<p.y; x++){
                for(int y = 0; y<pixels.length; y++){
                    pixels[y][x] = 0;
                }
            }
        }
    }
    private static void addBars (int [][] pixels, List<Integer> minimums){
        blackbars(pixels
        ,minimums.stream()
                .map(min -> new Point(min-4,min+4))
                .collect(Collectors.toList()));
    }
    private static void addBarsVertical(int [][] pixels, List<Integer> minimums){
        blackbarsVertical(pixels
                ,minimums.stream()
                        .map(min -> new Point(min-4,min+4))
                        .collect(Collectors.toList()));
    }
    private static void blackbarsVertical(int[][] pixels, List<Point> points){
        for(Point p : points){
            for(int y=p.x ; y<p.y; y++){
                for(int x = 0; x<pixels[0].length; x++){
                    pixels[y][x] = 0;
                }
            }
        }
    }



}