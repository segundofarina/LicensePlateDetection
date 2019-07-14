package ar.edu.itba.ati.Model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Image {

    private final int height;
    private final int width;
    private final int[][] pixels;

    public Image(int[][] pixels) {
        this.width = pixels[0].length;
        this.height = pixels.length;
        this.pixels = pixels;
    }

    public Image( File file) throws IOException {
        BufferedImage inputImage = ImageIO.read(file);
        int[][] result = convertToArrayLocation(toGrayscale(inputImage));
        this.width = result[0].length;
        this.height = result.length;
        this.pixels = result;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

   public int getPixel(int x, int y){
        return pixels[y][x];
   }

    public BufferedImage getBufferedImage(){
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster writableRaster = bufferedImage.getRaster();

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int color = pixels[y][x];
                int value = color <= 128 ? color : color - 256;
                writableRaster.setSample(x, y, 0, value);
            }
        }

        return bufferedImage;
    }

    private static BufferedImage toGrayscale( BufferedImage colorImage) {
        final int width = colorImage.getWidth();
        final int height = colorImage.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = image.getGraphics();
        g.drawImage(colorImage, 0, 0, null);
        g.dispose();
        return image;
    }


    private static int[][] convertToArrayLocation(BufferedImage inputImage) {

        final byte[] pixels = ((DataBufferByte) inputImage.getRaster()
                .getDataBuffer()).getData(); // get pixel value as single array from buffered Image
        final int width = inputImage.getWidth(); //get image width value
        final int height = inputImage.getHeight(); //get image height value
        int[][] result = new int[height][width]; //Initialize the array with height and width
        //this loop allocates pixels value to two dimensional array
        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
            int argb = (int) pixels[pixel];

            if (argb < 0) { //if pixel value is negative, change to positive //still weird to me
                argb += 256;
            }

            result[row][col] = argb;
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }
        return result; //return the result as two dimensional array
    }

    public int[][] clonePixels(){
        int[][] newPixels = new int[height][width];
        for(int y = 0 ; y<height ; y++){
            newPixels[y] = Arrays.copyOf(pixels[y],width);
        }
        return newPixels;
    }
}
