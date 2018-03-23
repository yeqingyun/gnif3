package com.gionee.gnif3.query;

/**
 * Created by doit on 2016/4/19.
 */
public class Page {
    private int firstResult;
    private int maxResults;

    public Page(int firstResult, int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }
}
