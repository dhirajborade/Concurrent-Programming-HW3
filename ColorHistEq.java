package cop5618;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ColorHistEq {

	// Use these labels to instantiate you timers. You will need 8 invocations
	// of now()
	static String[] labels = { "getRGB", "convert to HSB", "create brightness map", "probability array",
			"parallel prefix", "equalize pixels", "setRGB" };

	static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
		Timer times = new Timer(labels);
		/**
		 * IMPLEMENT SERIAL METHOD
		 */

		ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight();

		times.now();
		int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		times.now(); // getRGB

		times.now(); // convert to HSB
		boolean hsbHistogram = true;
		float histogram[][] = makeHistogram(image, hsbHistogram);
		normalizeHistogram(histogram);
		times.now(); // create brightness map
		times.now(); // probability array
		times.now(); // parallel prefix
		times.now(); // equalize pixels
		times.now(); // setRGB
		return times;
	}

	static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {
		Timer times = new Timer(labels);
		/**
		 * IMPLEMENT PARALLEL METHOD
		 */
		return times;
	}

	/**
	 * <p>
	 * Creates a histogram from an image. Returned is an array with min(image
	 * width, 256) elements and 3 channels (eg an hist[256][3]).
	 * </p>
	 *
	 * <p>
	 * In an rgb histogram hist[][0] is red, hist[][1] is green, hist[][2] blue.
	 * In an hsb histogram hist[][0] is hue, hist[][1] is saturation, hist[][2]
	 * brightness. In an image with width >= 256 a particular bin, eg
	 * hist[42][2], counts the number of pixels in the image which have a
	 * blue/brightness value of 42. In smaller images these are scaled
	 * appropriately.
	 * </p>
	 *
	 * @param source
	 *            image to compute the histogram from
	 * @param hsbHistogram
	 *            produces a HSB histogram if true, else RGB histogram
	 * @return 2D histogram array
	 **/
	public static float[][] makeHistogram(BufferedImage source, boolean hsbHistogram) {
		int width = source.getWidth();
		int height = source.getHeight();

		// Scale the number of histogram boxes if our image is smaller than
		// 256 pixels wide.
		double scale;
		if (width < 256) {
			scale = (double) 256 / width;
		} else {
			// just limit it to 256 bins
			scale = 1;
		}

		// Allocate the bins, 3 channels for rgb or hsb
		float histogram[][] = new float[Math.min(width, 256)][3];

		for (int i = 0; i < histogram.length; i++) {
			histogram[i][0] = 0;
			histogram[i][1] = 0;
			histogram[i][2] = 0;
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int red = (source.getPixelRed(x, y) >= 0) ? source.getPixelRed(x, y) : 256 + source.getPixelRed(x, y);
				int green = (source.getPixelGreen(x, y) >= 0) ? source.getPixelGreen(x, y)
						: 256 + source.getPixelGreen(x, y);
				int blue = (source.getPixelBlue(x, y) >= 0) ? source.getPixelBlue(x, y)
						: 256 + source.getPixelBlue(x, y);
				float[] pixelArray = { red, green, blue };
				if (hsbHistogram) {
					int pix = source.getPixel(x, y);
					pixelArray = Color.RGBtoHSB(red, green, blue, null);
					pixelArray[0] = pixelArray[0] * 255;
					pixelArray[1] = pixelArray[1] * 255;
					pixelArray[2] = pixelArray[2] * 255;
				}
				for (int k = 0; k < 3; k++) {
					int bin = (int) (pixelArray[k] / scale);
					histogram[bin][k]++;
				}
			}
		}
		return histogram;
	}

	private static void normalizeHistogram(float[][] histogram) {

		if (histogram == null)
			return;

		int numBins = histogram.length;
		int numChannels = histogram[0].length;

		// find maximum column total (ie. r+g+b)
		float maxColTotal = 0;
		float colTotal;
		for (int w = 0; w < numBins; w++) {
			colTotal = 0;
			for (int c = 0; c < numChannels; c++) {
				colTotal += histogram[w][c];
			}
			if (colTotal > maxColTotal) {
				maxColTotal = colTotal;
			}
		}

		// normalize histogram based on the maximum column total
		for (int w = 0; w < numBins; w++) {
			for (int c = 0; c < numChannels; c++) {
				histogram[w][c] /= maxColTotal;
			}
		}
	}
}
