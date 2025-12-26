package com.cortez.adventure_seasons.lib.config;

import java.io.Serializable;

public class Winter implements Serializable
{
    private int earlyLength;
    private int midLength;
    private int lateLength;

    public Winter() {
    }

    public Winter(int earlyLength, int midLength, int lateLength) {
        this.earlyLength = earlyLength;
        this.midLength = midLength;
        this.lateLength = lateLength;
    }

    public int getEarlyLength() {
        return earlyLength;
    }

    public void setEarlyLength(int earlyLength) {
        this.earlyLength = earlyLength;
    }

    public int getMidLength() {
        return midLength;
    }

    public void setMidLength(int midLength) {
        this.midLength = midLength;
    }

    public int getLateLength() {
        return lateLength;
    }

    public void setLateLength(int lateLength) {
        this.lateLength = lateLength;
    }
}
