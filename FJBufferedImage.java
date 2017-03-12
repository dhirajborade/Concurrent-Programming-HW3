package cop5618;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;

public class FJBufferedImage extends BufferedImage {

	private class RGBSet extends RecursiveAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int xStart, yStart, w, h, offset, scansize;
		int[] rgbArray;

		public RGBSet(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) {
			this.xStart = xStart;
			this.yStart = yStart;
			this.w = w;
			this.h = h;
			this.rgbArray = rgbArray;
			this.offset = offset;
			this.scansize = scansize;
		}

		protected void compute() {
			if (h <= 3) {
				FJBufferedImage.super.setRGB(xStart, yStart, w, h, rgbArray, offset, scansize);
			} else {
				int height = h / 3;
				int yStart2 = yStart + height;
				int yStart3 = yStart + (height * 2);
				int yEnd2 = height;
				int yEnd3 = h - (2 * height);
				int offset2 = offset + (height * scansize);
				int offset3 = offset + (height * scansize * 2);

				ForkJoinPool.commonPool().invoke(new RGBSet(xStart, yStart, w, height, rgbArray, offset, scansize));
				ForkJoinPool.commonPool().invoke(new RGBSet(xStart, yStart2, w, yEnd2, rgbArray, offset2, scansize));
				ForkJoinPool.commonPool().invoke(new RGBSet(xStart, yStart3, w, yEnd3, rgbArray, offset3, scansize));
			}
		}
	}

	private class RGBGet extends RecursiveAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int xStart, yStart, w, h, offset, scansize;
		int[] rgbArray;

		public RGBGet(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) {
			this.xStart = xStart;
			this.yStart = yStart;
			this.w = w;
			this.h = h;
			this.rgbArray = rgbArray;
			this.offset = offset;
			this.scansize = scansize;
		}

		protected void compute() {
			if (h <= 3) {
				FJBufferedImage.super.getRGB(xStart, yStart, w, h, rgbArray, offset, scansize);
			} else {
				int height = h / 3;
				int yStart2 = yStart + height;
				int yStart3 = yStart + (height * 2);
				int yEnd2 = height;
				int yEnd3 = h - (2 * height);
				int offset2 = offset + (height * scansize);
				int offset3 = offset + (height * scansize * 2);

				ForkJoinPool.commonPool().invoke(new RGBGet(xStart, yStart, w, height, rgbArray, offset, scansize));
				ForkJoinPool.commonPool().invoke(new RGBGet(xStart, yStart2, w, yEnd2, rgbArray, offset2, scansize));
				ForkJoinPool.commonPool().invoke(new RGBGet(xStart, yStart3, w, yEnd3, rgbArray, offset3, scansize));
			}
		}
	}

	/** Constructors */

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
	 * 
	 * @param source
	 * @return
	 */
	public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source) {
		Hashtable<String, Object> properties = null;
		String[] propertyNames = source.getPropertyNames();
		if (propertyNames != null) {
			properties = new Hashtable<String, Object>();
			for (String name : propertyNames) {
				properties.put(name, source.getProperty(name));
			}
		}
		return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(),
				properties);
	}

	@Override
	public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) {
		/**** IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER *****/
		new RGBSet(xStart, yStart, w, h, rgbArray, offset, scansize).compute();
	}

	@Override
	public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize) {
		/**** IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER *****/
		new RGBGet(xStart, yStart, w, h, rgbArray, offset, scansize).compute();
		return null;
	}
}