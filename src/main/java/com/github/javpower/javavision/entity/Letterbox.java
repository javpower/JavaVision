package com.github.javpower.javavision.entity;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Letterbox {

    private Size newShape = new Size(640, 640);
    private final double[] color = new double[]{114,114,114};
    private final Boolean auto = false;
    private final Boolean scaleUp = true;
    private Integer stride = 32;

    private double ratio;
    private double dw;
    private double dh;

    public double getRatio() {
        return ratio;
    }

    public double getDw() {
        return dw;
    }

    public Integer getWidth() {
        return (int) this.newShape.width;
    }

    public Integer getHeight() {
        return (int) this.newShape.height;
    }

    public double getDh() {
        return dh;
    }

    public void setNewShape(Size newShape) {
        this.newShape = newShape;
    }

    public void setStride(Integer stride) {
        this.stride = stride;
    }

    public Mat letterbox(Mat im) { // 调整图像大小和填充图像，使满足步长约束，并记录参数

        int[] shape = {im.rows(), im.cols()}; // 当前形状 [height, width]
        // Scale ratio (new / old)
        double r = Math.min(this.newShape.height / shape[0], this.newShape.width / shape[1]);
        if (!this.scaleUp) { // 仅缩小，不扩大（一且为了mAP）
            r = Math.min(r, 1.0);
        }
        // Compute padding
        Size newUnpad = new Size(Math.round(shape[1] * r), Math.round(shape[0] * r));
        double dw = this.newShape.width - newUnpad.width, dh = this.newShape.height - newUnpad.height; // wh 填充
        if (this.auto) { // 最小矩形
            dw = dw % this.stride;
            dh = dh % this.stride;
        }
        dw /= 2; // 填充的时候两边都填充一半，使图像居于中心
        dh /= 2;
        if (shape[1] != newUnpad.width || shape[0] != newUnpad.height) { // resize
            Imgproc.resize(im, im, newUnpad, 0, 0, Imgproc.INTER_LINEAR);
        }
        int top = (int) Math.round(dh - 0.1), bottom = (int) Math.round(dh + 0.1);
        int left = (int) Math.round(dw - 0.1), right = (int) Math.round(dw + 0.1);
        // 将图像填充为正方形
        Core.copyMakeBorder(im, im, top, bottom, left, right, Core.BORDER_CONSTANT, new org.opencv.core.Scalar(this.color));
        this.ratio = r;
        this.dh = dh;
        this.dw = dw;
        return im;
    }
}