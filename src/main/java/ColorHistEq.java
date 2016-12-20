import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ColorHistEq {

    private static Integer numBins = 256;

    //Use these labels to instantiate you timers.  You will need 8 invocations of now()
    static String[] labels = {"getRGB", "convert to HSB", "create brightness map", "probability array",
            "parallel prefix", "equalize pixels", "setRGB"};

    static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
        /**
         * IMPLEMENT SERIAL METHOD
         */
        Timer times = new Timer(labels);
        ColorModel colorModel = ColorModel.getRGBdefault();
        int w = image.getWidth();
        int h = image.getHeight();

        times.now();//initial

        int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
        int bucketLength = sourcePixelArray.length;

        times.now();// getRGB

        float[][] hsbPixelArray =
                Arrays.stream(sourcePixelArray)
                        .mapToObj(pixel -> (
                                (Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel),
                                        colorModel.getBlue(pixel), null)
                                )))
                        .toArray(float[][]::new);

        times.now();// hsb pixel array

        Map<Integer, Long> histMap = Arrays.stream(hsbPixelArray)
                .collect(Collectors.groupingBy(px -> (int) (px[2] * numBins), TreeMap::new, Collectors.counting()));

        times.now();//brightness map

        Long[] binVals = histMap.values().toArray(new Long[0]);

        times.now();// probability array

        Arrays.parallelPrefix(binVals, (x, y) -> x + y);

        double[] cumFreq = new double[binVals.length];

        for (int i = 0; i < binVals.length; i++)
            cumFreq[i] = (double) binVals[i] / bucketLength;

        times.now();// parallel prefix

        //Arrays.stream(cumFreq).forEach(System.out::println);

        int[] destPixelArray = Arrays.stream(hsbPixelArray)
                .mapToInt(pixel ->
                        Color.HSBtoRGB(pixel[0], pixel[1], (float) cumFreq[(int) (pixel[2] * (numBins-1))])
                ).toArray();

        times.now(); //equalize pixels

        newImage.setRGB(0, 0, w, h, destPixelArray, 0, w);

        times.now();//setRGB

        return times;
    }

    static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {

        Timer times = new Timer(labels);
        /**
         * IMPLEMENT PARALLEL METHOD
         */

        ColorModel colorModel = ColorModel.getRGBdefault();
        int w = image.getWidth();
        int h = image.getHeight();

        times.now();//initial

        int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
        int bucketLength = sourcePixelArray.length;

        times.now();// getRGB

        float[][] hsbPixelArray =
                Arrays.stream(sourcePixelArray)
                        .parallel()
                        .mapToObj(pixel -> (
                                (Color.RGBtoHSB(colorModel.getRed(pixel), colorModel.getGreen(pixel),
                                        colorModel.getBlue(pixel), null)
                                )))
                        .toArray(float[][]::new);

        times.now();// hsb pixel array

        Map<Integer, Long> histMap = Arrays.stream(hsbPixelArray).parallel()
                .collect(Collectors.groupingBy(px -> (int) (px[2] * numBins), TreeMap::new, Collectors.counting()));

        times.now();//brightness map

        Long[] binVals = histMap.values().toArray(new Long[0]);

        times.now();// probability array

        Arrays.parallelPrefix(binVals, (x, y) -> x + y);

        double[] cumFreq = new double[binVals.length];

        for (int i = 0; i < binVals.length; i++)
            cumFreq[i] = (double) binVals[i] / bucketLength;

        times.now();// parallel prefix

        //Arrays.stream(cumFreq).forEach(System.out::println);

        int[] destPixelArray = Arrays.stream(hsbPixelArray).parallel()
                .mapToInt(pixel ->
                        Color.HSBtoRGB(pixel[0], pixel[1], (float) cumFreq[(int) (pixel[2] * (numBins-1))])
                ).toArray();

        times.now(); //equalize pixels

        newImage.setRGB(0, 0, w, h, destPixelArray, 0, w);

        times.now();//setRGB

        return times;
    }
}
