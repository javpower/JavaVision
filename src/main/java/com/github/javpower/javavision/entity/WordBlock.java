package com.github.javpower.javavision.entity;

import ai.djl.modality.cv.output.Point;
import lombok.Data;

import java.util.List;

@Data
public class WordBlock {
    private String text;
    private float[] charScores;
    private List<Point> boxPoint;
}
