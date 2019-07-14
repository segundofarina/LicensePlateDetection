package ar.edu.itba.ati.Model.Masks;

import ar.edu.itba.ati.Model.Image;
import ar.edu.itba.ati.Model.Mask;

public class Sobel  extends TwoDirection {

    @Override
    public double[][] generateXMask() {
        return new double[][] {
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}
        };
    }

    @Override
    public double[][] generateYMask() {
        return new double[][] {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
    }
}
