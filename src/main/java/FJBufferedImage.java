import java.awt.image.*;
import java.util.Hashtable;
import java.util.concurrent.RecursiveAction;

public class FJBufferedImage extends BufferedImage {

    /**Constructors*/

    public FJBufferedImage(int width, int height, int imageType) {
        super(width, height, imageType);
    }

    public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
        super(width, height, imageType, cm);
    }

    public FJBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
                           Hashtable<?, ?> properties) {
        super(cm, raster, isRasterPremultiplied, properties);
    }


    /**
     * Creates a new FJBufferedImage with the same fields as source.
     * @param source
     * @return
     */
    public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source){
        Hashtable<String,Object> properties=null;
        String[] propertyNames = source.getPropertyNames();
        if (propertyNames != null) {
            properties = new Hashtable<String,Object>();
            for (String name: propertyNames){properties.put(name, source.getProperty(name));}
        }
        return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(), properties);
    }

    private class getRGBAction extends RecursiveAction {

        int xStart;
        int yStart;
        int w;
        int h;
        int[] rgbArray;
        int offset;
        int scansize;

        public getRGBAction(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) {
            this.xStart = xStart;
            this.yStart = yStart;
            this.w = w;
            this.h = h;
            this.rgbArray = rgbArray;
            this.offset = offset;
            this.scansize = scansize;
        }

        @Override
        protected void compute() {
            if (h > 50) {
                int split = h / 2;
                invokeAll(new getRGBAction(xStart, yStart, w, split, rgbArray, offset, scansize),
                        new getRGBAction(xStart, yStart + split, w, h - split, rgbArray, offset + (split * scansize), scansize));
            } else {
                FJBufferedImage.super.getRGB(xStart, yStart, w, h, rgbArray, offset, scansize);
            }
        }
    }

    private class setRGBAction extends RecursiveAction {

        int xStart;
        int yStart;
        int w;
        int h;
        int[] rgbArray;
        int offset;
        int scansize;

        public setRGBAction(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) {
            this.xStart = xStart;
            this.yStart = yStart;
            this.w = w;
            this.h = h;
            this.rgbArray = rgbArray;
            this.offset = offset;
            this.scansize = scansize;
        }

        @Override
        protected void compute() {
            if(h > 50){
                int split = h/2;
                invokeAll(new setRGBAction(xStart, yStart, w, split, rgbArray, offset, scansize),
                        new setRGBAction(xStart, yStart + split, w, h - split, rgbArray, offset + (split * scansize), scansize));
            } else{
                FJBufferedImage.super.setRGB(xStart, yStart, w, h, rgbArray, offset, scansize);
            }
        }
    }

    @Override
    public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
        /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
        new getRGBAction(xStart, yStart, w, h, rgbArray, offset, scansize).compute();
        return rgbArray;
    }

    @Override
    public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
        /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
        new setRGBAction(xStart, yStart, w, h, rgbArray, offset, scansize).compute();
    }
}
