package ar.edu.itba.ati.Model.Masks;

import ar.edu.itba.ati.Model.Image;
import ar.edu.itba.ati.Model.Mask;

public class BilateralFilter implements Mask {

    double spaceSigma;
    double colorSigma;


     int size;

    public BilateralFilter() {
        this.spaceSigma = 40;
        this.colorSigma = 30;
        this.size = 7;
    }

    public Image apply(Image originalImage) {
        Image xChannel = applySingleMaskTo(originalImage);

        return xChannel;
    }

    private Image applySingleMaskTo(Image originalImage) {
        int [][] newPixels = new int[originalImage.getHeight()][originalImage.getWidth()];

        for(int y = getBorderLength(); y < originalImage.getHeight() - getBorderLength(); y++) {
            for(int x = getBorderLength(); x < originalImage.getWidth() - getBorderLength(); x++) {
                newPixels[y][x] = applyMaskToPixel(x, y, originalImage);
            }
        }

        return new Image(newPixels);
    }

    private int applyMaskToPixel(int xCenter, int yCenter, Image originalImage){
        double upper = 0;
        double lower = 0;

        for(int y = 0; y < this.getMaskSize(); y++) {
            for(int x = 0; x < this.getMaskSize(); x++) {
                int xPos = xCenter + ( x - this.getBorderLength() );
                int yPos = yCenter + ( y - this.getBorderLength() );

                double wResult = wFunction(xCenter, yCenter,xPos,yPos,spaceSigma,colorSigma,originalImage);
                upper += originalImage.getPixel(xPos, yPos) * wResult;
                lower += wResult;
            }
        }

        return (int) (upper/lower);
    }

    private int getMaskSize() {
        return size;
    }

    private double wFunction(int i, int j, int k, int l, double spaceSigma, double colorSigma, Image original){
        double colorIJ = original.getPixel(i, j);
        double colorKL = original.getPixel(k,l);
        double exponent= - ( (i-k) * (i-k) + (j-l) * (j-l) ) / (2 * spaceSigma * spaceSigma) - (colorIJ - colorKL) * (colorIJ -  colorKL) / (2 * colorSigma * colorSigma);
        return Math.exp(exponent);
    }

    private int getBorderLength() {
        return (size - 1) / 2;
    }

}
