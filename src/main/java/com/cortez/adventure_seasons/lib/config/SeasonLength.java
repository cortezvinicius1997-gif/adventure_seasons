package com.cortez.adventure_seasons.lib.config;

import java.io.Serializable;

public class SeasonLength implements Serializable
{
    private Spring spring;
    private Summer summer;
    private Autumn autumn;
    private Winter winter;

    public SeasonLength() {
    }

    public SeasonLength(Spring spring, Summer summer, Autumn autumn, Winter winter) {
        this.spring = spring;
        this.summer = summer;
        this.autumn = autumn;
        this.winter = winter;
    }

    public Spring getSpring() {
        return spring;
    }

    public void setSpring(Spring spring) {
        this.spring = spring;
    }

    public Summer getSummer() {
        return summer;
    }

    public void setSummer(Summer summer) {
        this.summer = summer;
    }

    public Autumn getAutumn() {
        return autumn;
    }

    public void setAutumn(Autumn autumn) {
        this.autumn = autumn;
    }

    public Winter getWinter() {
        return winter;
    }

    public void setWinter(Winter winter) {
        this.winter = winter;
    }
}
