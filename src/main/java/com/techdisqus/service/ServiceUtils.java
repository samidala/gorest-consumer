package com.techdisqus.service;

public class ServiceUtils {

    /**
     * counts the no of batches
     * @param count
     * @return
     */
    public static int getIterationCount(int count) {
        int itr = count / 100;
        itr = itr + (count % 100 == 0 ? 0 : 1);
        return itr;
    }
}
