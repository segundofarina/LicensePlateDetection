package ar.edu.itba.ati.Model;

public class GlobalThreshold {

    public Image applyGlobalThreshold(Image image) {
        int prevThreshold = 0;
        int threshold = 128;

        int dT = 1;

        while(Math.abs(prevThreshold - threshold) > dT) {
            int m1 = 0, n1 = 0;
            int m2 = 0, n2 = 0;

            for(int i = 0; i < image.getWidth(); i++) {
                for(int j = 0; j < image.getHeight(); j++) {
                    if(image.getPixel(i,j) <= threshold) {
                        m1 += image.getPixel(i,j);
                        n1++;
                    } else {
                        m2 += image.getPixel(i,j);
                        n2++;
                    }
                }
            }

            m1 /= n1;
            m2 /= n2;

            prevThreshold = threshold;
            threshold = (m1 + m2) / 2;
        }

        return applyThreshold(threshold, image);
    }

    public static Image applyThreshold(int threshold, Image image){
        int [][] newPixels =  new int[image.getHeight()][image.getWidth()];
        for(int i = 0; i < image.getWidth(); i++) {
            for(int j = 0; j < image.getHeight(); j++) {
                if(image.getPixel(i,j)<= threshold) {
                    newPixels[j][i] = 0;
                } else {
                    newPixels[j][i] = 255;
                }
            }
        }
        return new Image(newPixels);
    }

}

