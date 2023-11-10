package com.github.javpower.javavision.entity;

import java.text.DecimalFormat;


public class ODResult {
    private final Integer batchId;
    private final Float x0;
    private final Float y0;
    private final Float x1;
    private final Float y1;
    private final Integer clsId;
    private final Float score;

    public ODResult(float[] x) {
        this.batchId = (int) x[0];
        this.x0 = x[1];
        this.y0 = x[2];
        this.x1 = x[3];
        this.y1 = x[4];
        this.clsId = (int) x[5];
        this.score = x[6];
    }

    public Integer getBatchId() {
        return batchId;
    }

    public Float getX0() {
        return x0;
    }

    public Float getY0() {
        return y0;
    }

    public Float getX1() {
        return x1;
    }

    public Float getY1() {
        return y1;
    }

    public Integer getClsId() {
        return clsId;
    }

    public Float setX0(Float x0) {
        return this.x0;
    }

    public Float setY0(Float y0) {
        return this.y0;
    }

    public Float setX1(Float x1) {
        return this.x1;
    }

    public Float setY1(Float y1) {
        return this.y1;
    }



    public String getScore() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(this.score);
    }

    @Override
    public String toString() {
        return " batchId=" + batchId +
                " \t x0=" + x0 +
                " \t y0=" + y0 +
                " \t x1=" + x1 +
                " \t y1=" + y1 +
                " \t clsId=" + clsId +
                " \t score=" + getScore();
    }
}