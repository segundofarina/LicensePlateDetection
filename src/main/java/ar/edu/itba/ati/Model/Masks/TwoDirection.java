package ar.edu.itba.ati.Model.Masks;


import ar.edu.itba.ati.Model.Image;
import ar.edu.itba.ati.Model.Mask;

public abstract class TwoDirection implements Mask {

    private static int MASK_SIZE = 3;
    private static int MASK_BORDER = (MASK_SIZE - 1) / 2;


    @Override
    public Image apply(Image originalImage) {
        Image xChannel = applySingleMaskTo(originalImage, generateXMask());
        Image yChannel = applySingleMaskTo(originalImage, generateYMask());

        return joinMasks(xChannel, yChannel);
    }

    public Image applySingleMaskTo(Image originalImage, double[][] poundedMask) {
        int [][] newPixels = new int[originalImage.getHeight()][originalImage.getWidth()];

        for(int y = MASK_BORDER; y < originalImage.getHeight() - MASK_BORDER; y++) {
            for(int x = MASK_BORDER; x < originalImage.getWidth() - MASK_BORDER; x++) {
                newPixels[y][x] = applyMaskToPixel(x, y, originalImage, poundedMask);
            }
        }

        return  new Image(newPixels);
    }

    public int applyMaskToPixel(int xCenter, int yCenter, Image originalImage, double[][] poundedMask) {
        double newColor = 0;
        for(int y = 0; y < MASK_SIZE; y++) {
            for(int x = 0; x < MASK_SIZE; x++) {
                int xPos = xCenter + ( x - MASK_BORDER );
                int yPos = yCenter + ( y - MASK_BORDER );

                newColor += originalImage.getPixel(xPos, yPos) * poundedMask[y][x];
            }
        }

        return (int)newColor;
    }

    public Image joinMasks(Image image1, Image image2) {
        int [][] newPixels = new int[image1.getHeight()][image1.getWidth()];

        for(int y = MASK_BORDER; y < image1.getHeight() - MASK_BORDER; y++) {
            for(int x = MASK_BORDER; x < image1.getWidth() - MASK_BORDER; x++) {
                int image1Color = image1.getPixel(x, y);
                int image2Color = image2.getPixel(x, y);

                int color = (int) Math.sqrt( image1Color * image1Color + image2Color * image2Color );
                newPixels[y][x] =  color;
            }
        }

        return new Image(newPixels);
    }


    public abstract double[][] generateXMask();

    public abstract double[][] generateYMask();
}