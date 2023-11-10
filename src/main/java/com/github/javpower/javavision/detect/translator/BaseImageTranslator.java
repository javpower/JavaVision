package com.github.javpower.javavision.detect.translator;

import ai.djl.Model;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.*;
import ai.djl.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public abstract class BaseImageTranslator<T> implements Translator<Image, T> {

    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    private Image.Flag flag;
    private Pipeline pipeline;
    private Batchifier batchifier;

    /**
     * Constructs an ImageTranslator with the provided builder.
     *
     * @param builder the data to build with
     */
    public BaseImageTranslator(BaseBuilder<?> builder) {
        flag = builder.flag;
        pipeline = builder.pipeline;
        batchifier = builder.batchifier;
    }

    /** {@inheritDoc} */
    @Override
    public Batchifier getBatchifier() {
        return batchifier;
    }

    /**
     * Processes the {@link Image} input and converts it to NDList.
     *
     * @param ctx the toolkit that helps create the input NDArray
     * @param input the {@link Image} input
     * @return a {@link NDList}
     */
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray array = input.toNDArray(ctx.getNDManager(), flag);
        array = NDImageUtils.resize(array, 640, 640);
        array = array.transpose(2, 0, 1); // HWC -> CHW RGB -> BGR
//        array = array.expandDims(0);
        array = array.div(255f);
        return new NDList(array);
//        return pipeline.transform(new NDList(array));
    }

    protected static String getStringValue(Map<String, ?> arguments, String key, String def) {
        Object value = arguments.get(key);
        if (value == null) {
            return def;
        }
        return value.toString();
    }

    protected static int getIntValue(Map<String, ?> arguments, String key, int def) {
        Object value = arguments.get(key);
        if (value == null) {
            return def;
        }
        return (int) Double.parseDouble(value.toString());
    }

    protected static float getFloatValue(Map<String, ?> arguments, String key, float def) {
        Object value = arguments.get(key);
        if (value == null) {
            return def;
        }
        return (float) Double.parseDouble(value.toString());
    }

    protected static boolean getBooleanValue(Map<String, ?> arguments, String key, boolean def) {
        Object value = arguments.get(key);
        if (value == null) {
            return def;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * A builder to extend for all classes extending the {@link BaseImageTranslator}.
     *
     * @param <T> the concrete builder type
     */
    @SuppressWarnings("rawtypes")
    public abstract static class BaseBuilder<T extends BaseBuilder> {

        protected int width = 224;
        protected int height = 224;
        protected Image.Flag flag = Image.Flag.COLOR;
        protected Pipeline pipeline;
        protected Batchifier batchifier = Batchifier.STACK;

        /**
         * Sets the optional {@link Image.Flag} (default is {@link
         * Image.Flag#COLOR}).
         *
         * @param flag the color mode for the images
         * @return this builder
         */
        public T optFlag(Image.Flag flag) {
            this.flag = flag;
            return self();
        }

        /**
         * Sets the {@link Pipeline} to use for pre-processing the image.
         *
         * @param pipeline the pre-processing pipeline
         * @return this builder
         */
        public T setPipeline(Pipeline pipeline) {
            this.pipeline = pipeline;
            return self();
        }

        /**
         * Adds the {@link Transform} to the {@link Pipeline} use for pre-processing the image.
         *
         * @param transform the {@link Transform} to be added
         * @return this builder
         */
        public T addTransform(Transform transform) {
            if (pipeline == null) {
                pipeline = new Pipeline();
            }
            pipeline.add(transform);
            return self();
        }

        /**
         * Sets the {@link Batchifier} for the {@link Translator}.
         *
         * @param batchifier the {@link Batchifier} to be set
         * @return this builder
         */
        public T optBatchifier(Batchifier batchifier) {
            this.batchifier = batchifier;
            return self();
        }

        protected abstract T self();

        protected void validate() {
            if (pipeline == null) {
                throw new IllegalArgumentException("pipeline is required.");
            }
        }

        protected void configPreProcess(Map<String, ?> arguments) {
            if (pipeline == null) {
                pipeline = new Pipeline();
            }
            width = getIntValue(arguments, "width", 224);
            height = getIntValue(arguments, "height", 224);
            if (arguments.containsKey("flag")) {
                flag = Image.Flag.valueOf(arguments.get("flag").toString());
            }
            if (getBooleanValue(arguments, "centerCrop", false)) {
                addTransform(new CenterCrop());
            }
            if (getBooleanValue(arguments, "resize", false)) {
                addTransform(new Resize(width, height));
            }
            if (getBooleanValue(arguments, "toTensor", true)) {
                addTransform(new ToTensor());
            }
            String normalize = getStringValue(arguments, "normalize", "false");
            if ("true".equals(normalize)) {
                addTransform(new Normalize(MEAN, STD));
            } else if (!"false".equals(normalize)) {
                String[] tokens = normalize.split("\\s*,\\s*");
                if (tokens.length != 6) {
                    throw new IllegalArgumentException("Invalid normalize value: " + normalize);
                }
                float[] mean = {
                    Float.parseFloat(tokens[0]),
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2])
                };
                float[] std = {
                    Float.parseFloat(tokens[3]),
                    Float.parseFloat(tokens[4]),
                    Float.parseFloat(tokens[5])
                };
                addTransform(new Normalize(mean, std));
            }
            String range = (String) arguments.get("range");
            if ("0,1".equals(range)) {
                addTransform(a -> a.div(255f));
            } else if ("-1,1".equals(range)) {
                addTransform(a -> a.div(128f).sub(1));
            }
            if (arguments.containsKey("batchifier")) {
                batchifier = Batchifier.fromString((String) arguments.get("batchifier"));
            }
        }

        protected void configPostProcess(Map<String, ?> arguments) {}
    }

    /** A Builder to construct a {@code ImageClassificationTranslator}. */
    @SuppressWarnings("rawtypes")
    public abstract static class ClassificationBuilder<T extends BaseBuilder>
            extends BaseBuilder<T> {

        protected SynsetLoader synsetLoader;

        /**
         * Sets the name of the synset file listing the potential classes for an image.
         *
         * @param synsetArtifactName a file listing the potential classes for an image
         * @return the builder
         */
        public T optSynsetArtifactName(String synsetArtifactName) {
            synsetLoader = new SynsetLoader(synsetArtifactName);
            return self();
        }

        /**
         * Sets the URL of the synset file.
         *
         * @param synsetUrl the URL of the synset file
         * @return the builder
         */
        public T optSynsetUrl(String synsetUrl) {
            try {
                this.synsetLoader = new SynsetLoader(new URL(synsetUrl));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid synsetUrl: " + synsetUrl, e);
            }
            return self();
        }

        /**
         * Sets the potential classes for an image.
         *
         * @param synset the potential classes for an image
         * @return the builder
         */
        public T optSynset(List<String> synset) {
            synsetLoader = new SynsetLoader(synset);
            return self();
        }

        /** {@inheritDoc} */
        @Override
        protected void validate() {
            super.validate();
            if (synsetLoader == null) {
                synsetLoader = new SynsetLoader("synset.txt");
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void configPostProcess(Map<String, ?> arguments) {
            String synset = (String) arguments.get("synset");
            if (synset != null) {
                optSynset(Arrays.asList(synset.split(",")));
            }
            String synsetUrl = (String) arguments.get("synsetUrl");
            if (synsetUrl != null) {
                optSynsetUrl(synsetUrl);
            }
            String synsetFileName = (String) arguments.get("synsetFileName");
            if (synsetFileName != null) {
                optSynsetArtifactName(synsetFileName);
            }
        }
    }

    protected static final class SynsetLoader {

        private String synsetFileName;
        private URL synsetUrl;
        private List<String> synset;

        public SynsetLoader(List<String> synset) {
            this.synset = synset;
        }

        public SynsetLoader(URL synsetUrl) {
            this.synsetUrl = synsetUrl;
        }

        public SynsetLoader(String synsetFileName) {
            this.synsetFileName = synsetFileName;
        }

        public List<String> load(Model model) throws IOException {
            if (synset != null) {
                return synset;
            } else if (synsetUrl != null) {
                try (InputStream is = synsetUrl.openStream()) {
                    return Utils.readLines(is);
                }
            }
            return model.getArtifact(synsetFileName, Utils::readLines);
        }
    }
}