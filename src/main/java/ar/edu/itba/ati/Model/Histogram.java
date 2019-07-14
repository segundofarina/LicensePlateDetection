package ar.edu.itba.ati.Model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Histogram {
    int  threshold = 10;
    private Image image;

    private List<Integer> horizontal;
    private List<Integer> vertical;

    public Histogram (Image image){
        this.image = image;
        this.horizontal = calcHorizontal(image);
        this.vertical = calcVertical(image);
    }

    public List<Integer> calcHorizontal(Image image){
        List<Integer> list = new ArrayList<>();
        for(int x = 0; x < image.getWidth(); x++){
            int count = 0;
           for(int y = 1; y < image.getHeight(); y++){
               int diff = Math.abs(image.getPixel(x,y)-image.getPixel(x,y-1));
               if(diff > threshold){
                   count++;
               }
           }
           list.add(count);
        }
        return list;
    }
    public List<Integer> calcVertical(Image image){
        List<Integer> list = new ArrayList<>();
        for(int y = 0; y< image.getHeight(); y++){
            int count = 0;
            for(int x=1; x< image.getWidth(); x++){
                int diff = Math.abs(image.getPixel(x,y) - image.getPixel(x-1,y));
                if(diff > threshold){
                    count++;
                }
            }
            list.add(count);
        }
        return list;
    }


    public List<Integer> getHorizontal() {
        return horizontal;
    }

    public List<Integer> getVertical() {
        return vertical;
    }

    public List<Integer> getUmbralized(List<Integer> list){
//        int threshold = 150;
        int threshold = getThreshold(list);
        return smooth(
                list.stream()
                .map(num ->  num > threshold ? num : 0)
                .collect(Collectors.toList()));
    }

    private int getThreshold(List<Integer> list){
        int prevThreshold = 0;
        int threshold = 128;

        int dT = 1;

        while(Math.abs(prevThreshold - threshold) > dT) {
            int m1 = 0, n1 = 1;
            int m2 = 0, n2 = 1;

            for(int i = 0; i < list.size(); i++) {
                    if(list.get(i) <= threshold) {
                        m1 += list.get(i);
                        n1++;
                    } else {
                        m2 += list.get(i);
                        n2++;
                    }
                }

            m1 /= n1;
            m2 /= n2;

            prevThreshold = threshold;
            threshold = (m1 + m2) / 2;
        }
    return threshold;
    }


    public List<Integer> smooth(List<Integer> list){
        List<Integer> newList = new ArrayList<>();
        for(int i= 0; i< 3; i++){
            newList.add(0);
        }
        for (int i = 3 ; i < list.size()-3 ; i++){
            int sum = 0;
            for(int j = -3 ; j< 4 ; j++){
                sum+=list.get(i+j)* gauss(j);
            }
            newList.add(sum);
        }
        for(int i = list.size()-3 ; i< list.size(); i++){
            newList.add(0);
        }
        return newList;
    }

    public double gauss(int j){
        double sigma = 2;
       return  1/(Math.sqrt(Math.PI*2) * sigma) * Math.exp(-(j*j) / (2* sigma* sigma));
    }

    public List<Point> getIntervals(List<Integer> list){
       int start= -1;
       List<Point> intervals = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i) == 0){
                if(start == -1){
                    start = i;
                }
            }else{
                if(start!= -1){
                    intervals.add(new Point(start,i));
                    start=-1;
                }
            }
        }
        return intervals;
    }

}
