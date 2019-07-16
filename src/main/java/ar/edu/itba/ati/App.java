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
import java.util.List;

public class App {

    public static void main(String[] args) throws IOException {

        Image image = new Image(new File(App.class.getClassLoader().getResource("patentesJpgSmall/IMG_2736.jpg").getPath()));
//        Image image = new Image(new File(App.class.getClassLoader().getResource("bmw.jpg").getPath()));

        Image bilateral = new BilateralFilter().apply(image);

//        Image bilateralBinarized = new GlobalThreshold().applyGlobalThreshold(bilateral);

        Image bilateralSobel = new Sobel().apply(bilateral);

        Histogram histogram = new Histogram(bilateralSobel);

        System.out.println(histogram.getHorizontal());
        System.out.println(histogram.getVertical());

        System.out.println(histogram.smooth(histogram.getHorizontal()));

        System.out.println(histogram.getUmbralized(histogram.getHorizontal()));
        System.out.println(histogram.getUmbralized(histogram.getVertical()));

        List<Point> hoirziontalPoints = histogram.getIntervals(histogram.getUmbralized(histogram.getHorizontal()));
        List<Point> verticalPoints = histogram.getIntervals(histogram.getUmbralized(histogram.getVertical()));

        int[][] newPixels = image.clonePixels();

        blackbars(newPixels, hoirziontalPoints);
        blackbarsVertical(newPixels,verticalPoints);
        Image blackImage = new Image(newPixels);

        ImageIO.write(bilateral.getBufferedImage(), "jpg", new File("./outImages/bilateral.jpg"));

        ImageIO.write(bilateralSobel.getBufferedImage(), "jpg", new File("./outImages/bilateralSobel.jpg"));
        ImageIO.write(blackImage.getBufferedImage(), "jpg", new File("./outImages/blackimage.jpg"));

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