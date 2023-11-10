package com.github.javpower.javavision.detect.translator;

import ai.djl.Model;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslatorContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ObjectDetectionTranslator extends BaseImageTranslator<DetectedObjects> {

    protected float threshold;
    private SynsetLoader synsetLoader;
    protected List<String> classes;
    protected double imageWidth;
    protected double imageHeight;

    /**
     * Creates the {@link ObjectDetectionTranslator} from the given builder.
     *
     * @param builder the builder for the translator
     */
    protected ObjectDetectionTranslator(ObjectDetectionBuilder<?> builder) {
        super(builder);
        this.threshold = builder.threshold;
        this.synsetLoader = builder.synsetLoader;
        this.imageWidth = builder.imageWidth;
        this.imageHeight = builder.imageHeight;
    }

    /** {@inheritDoc} */
    @Override
    public void prepare(TranslatorContext ctx) throws IOException {
        Model model = ctx.getModel();
        if (classes == null) {
            classes = synsetLoader.load(model);
        }
    }

    /** The base builder for the object detection translator. */
    @SuppressWarnings("rawtypes")
    public abstract static class ObjectDetectionBuilder<T extends ObjectDetectionBuilder>
            extends ClassificationBuilder<T> {

        protected float threshold = 0.2f;
        protected double imageWidth;
        protected double imageHeight;

        /**
         * Sets the threshold for prediction accuracy.
         *
         * <p>Predictions below the threshold will be dropped.
         *
         * @param threshold the threshold for the prediction accuracy
         * @return this builder
         */
        public T optThreshold(float threshold) {
            this.threshold = threshold;
            return self();
        }

        /**
         * Sets the optional rescale size.
         *
         * @param imageWidth the width to rescale images to
         * @param imageHeight the height to rescale images to
         * @return this builder
         */
        public T optRescaleSize(double imageWidth, double imageHeight) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            return self();
        }

        /**
         * Get resized image width.
         *
         * @return image width
         */
        public double getImageWidth() {
            return imageWidth;
        }

        /**
         * Get resized image height.
         *
         * @return image height
         */
        public double getImageHeight() {
            return imageHeight;
        }

        /** {@inheritDoc} */
        @Override
        protected void configPostProcess(Map<String, ?> arguments) {
            super.configPostProcess(arguments);
            if (getBooleanValue(arguments, "rescale", false)) {
                optRescaleSize(width, height);
            }
            threshold = getFloatValue(arguments, "threshold", 0.2f);
        }
    }
}