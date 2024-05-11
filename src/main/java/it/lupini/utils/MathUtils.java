package it.lupini.utils;

import java.util.Collections;
import java.util.List;

public class MathUtils {

    private MathUtils() {}

    public static int getMaxVal(List<Integer> list) {
        int i;
        if(list.isEmpty() ) return 0;
        int max = list.get(0);
        for (i = 1; i < list.size(); i++) {
            if (max < list.get(i)) max = list.get(i);
        }

        return max;

    }

    public static int getAvgVal(List<Integer> list){
        int sum = 0;

        if(list.isEmpty() ) return 0;

        for(Integer v : list){
            sum += v;
        }
        return sum/ list.size();

    }


    public static float median(List<Float> array){
        float median;

        Collections.sort(array);

        int size = array.size();
        if (size % 2 == 0) {
            median = (array.get((size / 2) - 1) + array.get(size / 2)) / 2;
        } else {
            median = array.get(size / 2);
        }

        return median;
    }
}
