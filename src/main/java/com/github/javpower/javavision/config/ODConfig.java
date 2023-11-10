package com.github.javpower.javavision.config;

import java.util.*;

public final class ODConfig {


    public static final Integer lineThicknessRatio = 333;
    public static final Double fontSizeRatio = 1080.0;

    private static final List<String> default_names = new ArrayList<>(Arrays.asList(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train",
            "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter",
            "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear",
            "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase",
            "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat",
            "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle",
            "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut",
            "cake", "chair", "couch", "potted plant", "bed", "dining table", "toilet",
            "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave",
            "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors",
            "teddy bear", "hair drier", "toothbrush"));


    private static final List<String> names = new ArrayList<>(Arrays.asList(
            "no_helmet", "helmet"));

    private final Map<String, double[]> colors;

    public ODConfig() {
        this.colors = new HashMap<>();
        default_names.forEach(name->{
            Random random = new Random();
            double[] color = {random.nextDouble()*256, random.nextDouble()*256, random.nextDouble()*256};
            colors.put(name, color);
        });
    }

    public String getName(int clsId) {
        return names.get(clsId);
    }

    public double[] getColor(int clsId) {
        return colors.get(getName(clsId));
    }

    public double[] getNameColor(String Name){
        return colors.get(Name);
    }

    public double[] getOtherColor(int clsId) {
        return colors.get(default_names.get(clsId));
    }
}