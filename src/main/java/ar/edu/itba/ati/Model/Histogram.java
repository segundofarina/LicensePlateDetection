package ar.edu.itba.ati.Model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Histogram {
    int  threshold = 50;
    private Image image;

    private List<Integer> horizontal;
    private List<Integer> vertical;

    private double [] licenseRatios = { (120.0 /50) , (110.0/35) };

    public Histogram (Image image){
        this.image = image;
        this.horizontal = calcHorizontal(image);
        this.vertical = calcVertical(image);
    }

    public Histogram (Image image, List<Integer> horizontal, List<Integer> vertical) {
        this.image = image;
        this.horizontal = horizontal;
        this.vertical = vertical;

    }

    public List<Integer> calcHorizontal(Image image){
        List<Integer> list = new ArrayList<>();
        for(int x = 0; x < image.getWidth(); x++){
            int count = 0;
           for(int y = 1; y < image.getHeight(); y++){
               int diff = Math.abs(image.getPixel(x,y)-image.getPixel(x,y-1));
               if(diff > threshold){
                   count += diff;
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
                    count+=diff;
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

    public Histogram umbralize(){
        return new Histogram(image,umbralize(horizontal), umbralize(vertical));
    }


    private List<Integer> umbralize(List<Integer> list){
        int threshold = (int) (list.stream().reduce(0, Integer::sum) / list.size() * 1);
//        int threshold = getThreshold(list);
        System.out.println("threshold is " + threshold + " max is " + list.stream().max(Integer::compareTo).orElse(0) );
        return list.stream()
                .map(num ->  num > threshold ? num : 0)
                .collect(Collectors.toList());
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

    public Histogram smooth(){
        return new Histogram(image,smooth(horizontal),smooth(vertical));
    }

    private List<Integer> smooth(List<Integer> list){
        List<Integer> newList = new ArrayList<>();
        int maskSize = 15;
        for(int i= 0; i< maskSize/2; i++){
            newList.add(0);
        }
        for (int i = maskSize/2 ; i < list.size()-maskSize/2 ; i++){
            int sum = 0;
            for(int j = -maskSize/2 ; j< maskSize/2 +1 ; j++){
                sum+=list.get(i+j)* gauss(j);
            }
            newList.add(sum);
        }
        for(int i = list.size()-maskSize/2 ; i< list.size(); i++){
            newList.add(0);
        }
        return newList;
    }

    public double gauss(int j){
        double sigma = 15;
        return  1/(Math.sqrt(Math.PI*2) * sigma) * Math.exp(-(j*j) / (2* sigma* sigma));
    }


    public List<Integer> getPoints (List<Integer> list){
//        int minimum = list.get(0);
        int globalMax = list.stream().max(Integer::compareTo).orElse(0);
        int lastMax = 0;
        List<Integer> minimums = new ArrayList<>();
        boolean goingUp = true;
        for(int i = 0; i < list.size() - 1; i++){
           int diff = list.get(i+1) - list.get(i);
           if(diff > 0 && !goingUp){
               // Empiezo a subir
               goingUp = true;
//               if(lastMax - list.get(i) > globalMax * 0.05) {
                   minimums.add(i);
//               }
           } else if( diff < 0 && goingUp){
               // empiezo a bajar
               goingUp = false;
               lastMax = list.get(i);
           }
        }
        return minimums;
    }



}
