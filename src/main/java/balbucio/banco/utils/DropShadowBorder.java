package balbucio.banco.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.awt.AlphaComposite;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import javax.swing.border.Border;

public class DropShadowBorder implements Border, Serializable {

	private static enum Position {
		TOP, TOP_LEFT, LEFT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT
	}

	private static final Map<Double, Map<Position, BufferedImage>> CACHE = new HashMap<Double, Map<Position, BufferedImage>>();

	private Color shadowColor;
	private int shadowSize;
	private float shadowOpacity;
	private int cornerSize;
	private boolean showTopShadow;
	private boolean showLeftShadow;
	private boolean showBottomShadow;
	private boolean showRightShadow;

	public DropShadowBorder() {
		this(Color.BLACK, 5);
	}

	public DropShadowBorder(Color shadowColor, int shadowSize) {
		this(shadowColor, shadowSize, .5f, 12, false, false, true, true);
	}

	public DropShadowBorder(boolean showLeftShadow) {
		this(Color.BLACK, 5, .5f, 12, false, showLeftShadow, true, true);
	}

	public DropShadowBorder(Color shadowColor, int shadowSize, float shadowOpacity, int cornerSize,
			boolean showTopShadow, boolean showLeftShadow, boolean showBottomShadow, boolean showRightShadow) {
		this.shadowColor = shadowColor;
		this.shadowSize = shadowSize;
		this.shadowOpacity = shadowOpacity;
		this.cornerSize = cornerSize;
		this.showTopShadow = showTopShadow;
		this.showLeftShadow = showLeftShadow;
		this.showBottomShadow = showBottomShadow;
		this.showRightShadow = showRightShadow;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
		/*
		 * 1) Get images for this border 2) Paint the images for each side of the border
		 * that should be painted
		 */
		Map<Position, BufferedImage> images = getImages((Graphics2D) graphics);

		Graphics2D g2 = (Graphics2D) graphics.create();

		try {
			// The location and size of the shadows depends on which shadows are being
			// drawn. For instance, if the left & bottom shadows are being drawn, then
			// the left shadow extends all the way down to the corner, a corner is drawn,
			// and then the bottom shadow begins at the corner. If, however, only the
			// bottom shadow is drawn, then the bottom-left corner is drawn to the
			// right of the corner, and the bottom shadow is somewhat shorter than before.

			int shadowOffset = 2; // the distance between the shadow and the edge

			Point topLeftShadowPoint = null;
			if (showLeftShadow || showTopShadow) {
				topLeftShadowPoint = new Point();
				if (showLeftShadow && !showTopShadow) {
					topLeftShadowPoint.setLocation(x, y + shadowOffset);
				} else if (showLeftShadow && showTopShadow) {
					topLeftShadowPoint.setLocation(x, y);
				} else if (!showLeftShadow && showTopShadow) {
					topLeftShadowPoint.setLocation(x + shadowSize, y);
				}
			}

			Point bottomLeftShadowPoint = null;
			if (showLeftShadow || showBottomShadow) {
				bottomLeftShadowPoint = new Point();
				if (showLeftShadow && !showBottomShadow) {
					bottomLeftShadowPoint.setLocation(x, y + height - shadowSize - shadowSize);
				} else if (showLeftShadow && showBottomShadow) {
					bottomLeftShadowPoint.setLocation(x, y + height - shadowSize);
				} else if (!showLeftShadow && showBottomShadow) {
					bottomLeftShadowPoint.setLocation(x + shadowSize, y + height - shadowSize);
				}
			}

			Point bottomRightShadowPoint = null;
			if (showRightShadow || showBottomShadow) {
				bottomRightShadowPoint = new Point();
				if (showRightShadow && !showBottomShadow) {
					bottomRightShadowPoint.setLocation(x + width - shadowSize, y + height - shadowSize - shadowSize);
				} else if (showRightShadow && showBottomShadow) {
					bottomRightShadowPoint.setLocation(x + width - shadowSize, y + height - shadowSize);
				} else if (!showRightShadow && showBottomShadow) {
					bottomRightShadowPoint.setLocation(x + width - shadowSize - shadowSize, y + height - shadowSize);
				}
			}

			Point topRightShadowPoint = null;
			if (showRightShadow || showTopShadow) {
				topRightShadowPoint = new Point();
				if (showRightShadow && !showTopShadow) {
					topRightShadowPoint.setLocation(x + width - shadowSize, y + shadowOffset);
				} else if (showRightShadow && showTopShadow) {
					topRightShadowPoint.setLocation(x + width - shadowSize, y);
				} else if (!showRightShadow && showTopShadow) {
					topRightShadowPoint.setLocation(x + width - shadowSize - shadowSize, y);
				}
			}

			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

			if (showLeftShadow) {
				Rectangle leftShadowRect = new Rectangle(x, topLeftShadowPoint.y + shadowSize, shadowSize,
						bottomLeftShadowPoint.y - topLeftShadowPoint.y - shadowSize);
				g2.drawImage(images.get(Position.LEFT), leftShadowRect.x, leftShadowRect.y, leftShadowRect.width,
						leftShadowRect.height, null);
			}

			if (showBottomShadow) {
				Rectangle bottomShadowRect = new Rectangle(bottomLeftShadowPoint.x + shadowSize,
						y + height - shadowSize, bottomRightShadowPoint.x - bottomLeftShadowPoint.x - shadowSize,
						shadowSize);
				g2.drawImage(images.get(Position.BOTTOM), bottomShadowRect.x, bottomShadowRect.y,
						bottomShadowRect.width, bottomShadowRect.height, null);
			}

			if (showRightShadow) {
				Rectangle rightShadowRect = new Rectangle(x + width - shadowSize, topRightShadowPoint.y + shadowSize,
						shadowSize, bottomRightShadowPoint.y - topRightShadowPoint.y - shadowSize);
				g2.drawImage(images.get(Position.RIGHT), rightShadowRect.x, rightShadowRect.y, rightShadowRect.width,
						rightShadowRect.height, null);
			}

			if (showTopShadow) {
				Rectangle topShadowRect = new Rectangle(topLeftShadowPoint.x + shadowSize, y,
						topRightShadowPoint.x - topLeftShadowPoint.x - shadowSize, shadowSize);
				g2.drawImage(images.get(Position.TOP), topShadowRect.x, topShadowRect.y, topShadowRect.width,
						topShadowRect.height, null);
			}

			if (showLeftShadow || showTopShadow) {
				g2.drawImage(images.get(Position.TOP_LEFT), topLeftShadowPoint.x, topLeftShadowPoint.y, null);
			}
			if (showLeftShadow || showBottomShadow) {
				g2.drawImage(images.get(Position.BOTTOM_LEFT), bottomLeftShadowPoint.x, bottomLeftShadowPoint.y, null);
			}
			if (showRightShadow || showBottomShadow) {
				g2.drawImage(images.get(Position.BOTTOM_RIGHT), bottomRightShadowPoint.x, bottomRightShadowPoint.y,
						null);
			}
			if (showRightShadow || showTopShadow) {
				g2.drawImage(images.get(Position.TOP_RIGHT), topRightShadowPoint.x, topRightShadowPoint.y, null);
			}
		} finally {
			g2.dispose();
		}
	}

	private Map<Position, BufferedImage> getImages(Graphics2D g2) {
		// first, check to see if an image for this size has already been rendered
		// if so, use the cache. Else, draw and save
		Map<Position, BufferedImage> images = CACHE
				.get(shadowSize + (shadowColor.hashCode() * .3) + (shadowOpacity * .12));// TODO do a real hash
		if (images == null) {
			images = new HashMap<Position, BufferedImage>();

			/*
			 * To draw a drop shadow, I have to: 1) Create a rounded rectangle 2) Create a
			 * BufferedImage to draw the rounded rect in 3) Translate the graphics for the
			 * image, so that the rectangle is centered in the drawn space. The border
			 * around the rectangle needs to be shadowWidth wide, so that there is space for
			 * the shadow to be drawn. 4) Draw the rounded rect as shadowColor, with an
			 * opacity of shadowOpacity 5) Create the BLUR_KERNEL 6) Blur the image 7) copy
			 * off the corners, sides, etc into images to be used for drawing the Border
			 */
			int rectWidth = cornerSize + 1;
			RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, rectWidth, rectWidth, cornerSize, cornerSize);
			int imageWidth = rectWidth + shadowSize * 2;
			BufferedImage image = GraphicsUtilities.createCompatibleTranslucentImage(imageWidth, imageWidth);
			Graphics2D buffer = (Graphics2D) image.getGraphics();

			try {
				buffer.setPaint(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(),
						(int) (shadowOpacity * 255)));
//	                buffer.setColor(new Color(0.0f, 0.0f, 0.0f, shadowOpacity));
				buffer.translate(shadowSize, shadowSize);
				buffer.fill(rect);
			} finally {
				buffer.dispose();
			}

			float blurry = 1.0f / (shadowSize * shadowSize);
			float[] blurKernel = new float[shadowSize * shadowSize];
			for (int i = 0; i < blurKernel.length; i++) {
				blurKernel[i] = blurry;
			}
			ConvolveOp blur = new ConvolveOp(new Kernel(shadowSize, shadowSize, blurKernel));
			BufferedImage targetImage = GraphicsUtilities.createCompatibleTranslucentImage(imageWidth, imageWidth);
			((Graphics2D) targetImage.getGraphics()).drawImage(image, blur, -(shadowSize / 2), -(shadowSize / 2));

			int x = 1;
			int y = 1;
			int w = shadowSize;
			int h = shadowSize;
			images.put(Position.TOP_LEFT, getSubImage(targetImage, x, y, w, h));
			x = 1;
			y = h;
			w = shadowSize;
			h = 1;
			images.put(Position.LEFT, getSubImage(targetImage, x, y, w, h));
			x = 1;
			y = rectWidth;
			w = shadowSize;
			h = shadowSize;
			images.put(Position.BOTTOM_LEFT, getSubImage(targetImage, x, y, w, h));
			x = cornerSize + 1;
			y = rectWidth;
			w = 1;
			h = shadowSize;
			images.put(Position.BOTTOM, getSubImage(targetImage, x, y, w, h));
			x = rectWidth;
			y = x;
			w = shadowSize;
			h = shadowSize;
			images.put(Position.BOTTOM_RIGHT, getSubImage(targetImage, x, y, w, h));
			x = rectWidth;
			y = cornerSize + 1;
			w = shadowSize;
			h = 1;
			images.put(Position.RIGHT, getSubImage(targetImage, x, y, w, h));
			x = rectWidth;
			y = 1;
			w = shadowSize;
			h = shadowSize;
			images.put(Position.TOP_RIGHT, getSubImage(targetImage, x, y, w, h));
			x = shadowSize;
			y = 1;
			w = 1;
			h = shadowSize;
			images.put(Position.TOP, getSubImage(targetImage, x, y, w, h));

			image.flush();
			CACHE.put(shadowSize + (shadowColor.hashCode() * .3) + (shadowOpacity * .12), images); // TODO do a real
																									// hash
		}
		return images;
	}

	/**
	 * Returns a new BufferedImage that represents a subregion of the given
	 * BufferedImage. (Note that this method does not use
	 * BufferedImage.getSubimage(), which will defeat image acceleration strategies
	 * on later JDKs.)
	 */
	private BufferedImage getSubImage(BufferedImage img, int x, int y, int w, int h) {
		BufferedImage ret = GraphicsUtilities.createCompatibleTranslucentImage(w, h);
		Graphics2D g2 = ret.createGraphics();

		try {
			g2.drawImage(img, 0, 0, w, h, x, y, x + w, y + h, null);
		} finally {
			g2.dispose();
		}

		return ret;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		int top = showTopShadow ? shadowSize : 0;
		int left = showLeftShadow ? shadowSize : 0;
		int bottom = showBottomShadow ? shadowSize : 0;
		int right = showRightShadow ? shadowSize : 0;
		return new Insets(top, left, bottom, right);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	public boolean isShowTopShadow() {
		return showTopShadow;
	}

	public boolean isShowLeftShadow() {
		return showLeftShadow;
	}

	public boolean isShowRightShadow() {
		return showRightShadow;
	}

	public boolean isShowBottomShadow() {
		return showBottomShadow;
	}

	public int getShadowSize() {
		return shadowSize;
	}

	public Color getShadowColor() {
		return shadowColor;
	}

	public float getShadowOpacity() {
		return shadowOpacity;
	}

	public int getCornerSize() {
		return cornerSize;
	}

	public void setShadowColor(Color shadowColor) {
		this.shadowColor = shadowColor;
	}

	public void setShadowSize(int shadowSize) {
		this.shadowSize = shadowSize;
	}

	public void setShadowOpacity(float shadowOpacity) {
		this.shadowOpacity = shadowOpacity;
	}

	public void setCornerSize(int cornerSize) {
		this.cornerSize = cornerSize;
	}

	public void setShowTopShadow(boolean showTopShadow) {
		this.showTopShadow = showTopShadow;
	}

	public void setShowLeftShadow(boolean showLeftShadow) {
		this.showLeftShadow = showLeftShadow;
	}

	public void setShowBottomShadow(boolean showBottomShadow) {
		this.showBottomShadow = showBottomShadow;
	}

	public void setShowRightShadow(boolean showRightShadow) {
		this.showRightShadow = showRightShadow;
	}

	public static class GraphicsUtilities {
		private GraphicsUtilities() {
		}

		// Returns the graphics configuration for the primary screen
		private static GraphicsConfiguration getGraphicsConfiguration() {
			return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		}

		private static boolean isHeadless() {
			return GraphicsEnvironment.isHeadless();
		}

		/**
		 * Converts the specified image into a compatible buffered image.
		 *
		 * @param img the image to convert
		 * @return a compatible buffered image of the input
		 */
		public static BufferedImage convertToBufferedImage(Image img) {
			BufferedImage buff = createCompatibleTranslucentImage(img.getWidth(null), img.getHeight(null));
			Graphics2D g2 = buff.createGraphics();

			try {
				g2.drawImage(img, 0, 0, null);
			} finally {
				g2.dispose();
			}

			return buff;
		}

		/**
		 * <p>
		 * Returns a new <code>BufferedImage</code> using the same color model as the
		 * image passed as a parameter. The returned image is only compatible with the
		 * image passed as a parameter. This does not mean the returned image is
		 * compatible with the hardware.
		 * </p>
		 *
		 * @param image the reference image from which the color model of the new image
		 *              is obtained
		 * @return a new <code>BufferedImage</code>, compatible with the color model of
		 *         <code>image</code>
		 */
		public static BufferedImage createColorModelCompatibleImage(BufferedImage image) {
			ColorModel cm = image.getColorModel();
			return new BufferedImage(cm, cm.createCompatibleWritableRaster(image.getWidth(), image.getHeight()),
					cm.isAlphaPremultiplied(), null);
		}

		/**
		 * <p>
		 * Returns a new compatible image with the same width, height and transparency
		 * as the image specified as a parameter. That is, the returned BufferedImage
		 * will be compatible with the graphics hardware. If this method is called in a
		 * headless environment, then the returned BufferedImage will be compatible with
		 * the source image.
		 * </p>
		 *
		 * @see Transparency
		 * @see #createCompatibleImage(int, int)
		 * @see #createCompatibleImage(BufferedImage, int, int)
		 * @see #createCompatibleTranslucentImage(int, int)
		 * @see #loadCompatibleImage(URL)
		 * @see #toCompatibleImage(BufferedImage)
		 * @param image the reference image from which the dimension and the
		 *              transparency of the new image are obtained
		 * @return a new compatible <code>BufferedImage</code> with the same dimension
		 *         and transparency as <code>image</code>
		 */
		public static BufferedImage createCompatibleImage(BufferedImage image) {
			return createCompatibleImage(image, image.getWidth(), image.getHeight());
		}

		/**
		 * <p>
		 * Returns a new compatible image of the specified width and height, and the
		 * same transparency setting as the image specified as a parameter. That is, the
		 * returned <code>BufferedImage</code> is compatible with the graphics hardware.
		 * If the method is called in a headless environment, then the returned
		 * BufferedImage will be compatible with the source image.
		 * </p>
		 *
		 * @see Transparency
		 * @see #createCompatibleImage(BufferedImage)
		 * @see #createCompatibleImage(int, int)
		 * @see #createCompatibleTranslucentImage(int, int)
		 * @see #loadCompatibleImage(URL)
		 * @see #toCompatibleImage(BufferedImage)
		 * @param width  the width of the new image
		 * @param height the height of the new image
		 * @param image  the reference image from which the transparency of the new
		 *               image is obtained
		 * @return a new compatible <code>BufferedImage</code> with the same
		 *         transparency as <code>image</code> and the specified dimension
		 */
		public static BufferedImage createCompatibleImage(BufferedImage image, int width, int height) {
			return isHeadless() ? new BufferedImage(width, height, image.getType())
					: getGraphicsConfiguration().createCompatibleImage(width, height, image.getTransparency());
		}

		/**
		 * <p>
		 * Returns a new opaque compatible image of the specified width and height. That
		 * is, the returned <code>BufferedImage</code> is compatible with the graphics
		 * hardware. If the method is called in a headless environment, then the
		 * returned BufferedImage will be compatible with the source image.
		 * </p>
		 *
		 * @see #createCompatibleImage(BufferedImage)
		 * @see #createCompatibleImage(BufferedImage, int, int)
		 * @see #createCompatibleTranslucentImage(int, int)
		 * @see #loadCompatibleImage(URL)
		 * @see #toCompatibleImage(BufferedImage)
		 * @param width  the width of the new image
		 * @param height the height of the new image
		 * @return a new opaque compatible <code>BufferedImage</code> of the specified
		 *         width and height
		 */
		public static BufferedImage createCompatibleImage(int width, int height) {
			return isHeadless() ? new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
					: getGraphicsConfiguration().createCompatibleImage(width, height);
		}

		/**
		 * <p>
		 * Returns a new translucent compatible image of the specified width and height.
		 * That is, the returned <code>BufferedImage</code> is compatible with the
		 * graphics hardware. If the method is called in a headless environment, then
		 * the returned BufferedImage will be compatible with the source image.
		 * </p>
		 *
		 * @see #createCompatibleImage(BufferedImage)
		 * @see #createCompatibleImage(BufferedImage, int, int)
		 * @see #createCompatibleImage(int, int)
		 * @see #loadCompatibleImage(URL)
		 * @see #toCompatibleImage(BufferedImage)
		 * @param width  the width of the new image
		 * @param height the height of the new image
		 * @return a new translucent compatible <code>BufferedImage</code> of the
		 *         specified width and height
		 */
		public static BufferedImage createCompatibleTranslucentImage(int width, int height) {
			return isHeadless() ? new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
					: getGraphicsConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		}

		/**
		 * <p>
		 * Returns a new compatible image from a stream. The image is loaded from the
		 * specified stream and then turned, if necessary into a compatible image.
		 * </p>
		 *
		 * @see #createCompatibleImage(BufferedImage)
		 * @see #createCompatibleImage(BufferedImage, int, int)
		 * @see #createCompatibleImage(int, int)
		 * @see #createCompatibleTranslucentImage(int, int)
		 * @see #toCompatibleImage(BufferedImage)
		 * @param in the stream of the picture to load as a compatible image
		 * @return a new translucent compatible <code>BufferedImage</code> of the
		 *         specified width and height
		 * @throws IOException if the image cannot be read or loaded
		 */
		public static BufferedImage loadCompatibleImage(InputStream in) throws IOException {
			BufferedImage image = ImageIO.read(in);
			if (image == null)
				return null;
			return toCompatibleImage(image);
		}

		/**
		 * <p>
		 * Returns a new compatible image from a URL. The image is loaded from the
		 * specified location and then turned, if necessary into a compatible image.
		 * </p>
		 *
		 * @see #createCompatibleImage(BufferedImage)
		 * @see #createCompatibleImage(BufferedImage, int, int)
		 * @see #createCompatibleImage(int, int)
		 * @see #createCompatibleTranslucentImage(int, int)
		 * @see #toCompatibleImage(BufferedImage)
		 * @param resource the URL of the picture to load as a compatible image
		 * @return a new translucent compatible <code>BufferedImage</code> of the
		 *         specified width and height
		 * @throws IOException if the image cannot be read or loaded
		 */
		public static BufferedImage loadCompatibleImage(URL resource) throws IOException {
			BufferedImage image = ImageIO.read(resource);
			return toCompatibleImage(image);
		}

		/**
		 * <p>
		 * Return a new compatible image that contains a copy of the specified image.
		 * This method ensures an image is compatible with the hardware, and therefore
		 * optimized for fast blitting operations.
		 * </p>
		 *
		 * <p>
		 * If the method is called in a headless environment, then the returned
		 * <code>BufferedImage</code> will be the source image.
		 * </p>
		 *
		 * @see #createCompatibleImage(BufferedImage)
		 * @see #createCompatibleImage(BufferedImage, int, int)
		 * @see #createCompatibleImage(int, int)
		 * @see #createCompatibleTranslucentImage(int, int)
		 * @see #loadCompatibleImage(URL)
		 * @param image the image to copy into a new compatible image
		 * @return a new compatible copy, with the same width and height and
		 *         transparency and content, of <code>image</code>
		 */
		public static BufferedImage toCompatibleImage(BufferedImage image) {
			if (isHeadless()) {
				return image;
			}

			if (image.getColorModel().equals(getGraphicsConfiguration().getColorModel())) {
				return image;
			}

			BufferedImage compatibleImage = getGraphicsConfiguration().createCompatibleImage(image.getWidth(),
					image.getHeight(), image.getTransparency());
			Graphics g = compatibleImage.getGraphics();

			try {
				g.drawImage(image, 0, 0, null);
			} finally {
				g.dispose();
			}

			return compatibleImage;
		}

		/**
		 * <p>
		 * Returns a thumbnail of a source image. <code>newSize</code> defines the
		 * length of the longest dimension of the thumbnail. The other dimension is then
		 * computed according to the dimensions ratio of the original picture.
		 * </p>
		 * <p>
		 * This method favors speed over quality. When the new size is less than half
		 * the longest dimension of the source image,
		 * {@link #createThumbnail(BufferedImage, int)} or
		 * {@link #createThumbnail(BufferedImage, int, int)} should be used instead to
		 * ensure the quality of the result without sacrificing too much performance.
		 * </p>
		 *
		 * @see #createThumbnailFast(BufferedImage, int, int)
		 * @see #createThumbnail(BufferedImage, int)
		 * @see #createThumbnail(BufferedImage, int, int)
		 * @param image   the source image
		 * @param newSize the length of the largest dimension of the thumbnail
		 * @return a new compatible <code>BufferedImage</code> containing a thumbnail of
		 *         <code>image</code>
		 * @throws IllegalArgumentException if <code>newSize</code> is larger than the
		 *                                  largest dimension of <code>image</code> or
		 *                                  &lt;= 0
		 */
		public static BufferedImage createThumbnailFast(BufferedImage image, int newSize) {
			float ratio;
			int width = image.getWidth();
			int height = image.getHeight();

			if (width > height) {
				if (newSize >= width) {
					throw new IllegalArgumentException("newSize must be lower than" + " the image width");
				} else if (newSize <= 0) {
					throw new IllegalArgumentException("newSize must" + " be greater than 0");
				}

				ratio = (float) width / (float) height;
				width = newSize;
				height = (int) (newSize / ratio);
			} else {
				if (newSize >= height) {
					throw new IllegalArgumentException("newSize must be lower than" + " the image height");
				} else if (newSize <= 0) {
					throw new IllegalArgumentException("newSize must" + " be greater than 0");
				}

				ratio = (float) height / (float) width;
				height = newSize;
				width = (int) (newSize / ratio);
			}

			BufferedImage temp = createCompatibleImage(image, width, height);
			Graphics2D g2 = temp.createGraphics();

			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
			} finally {
				g2.dispose();
			}

			return temp;
		}

		/**
		 * <p>
		 * Returns a thumbnail of a source image.
		 * </p>
		 * <p>
		 * This method favors speed over quality. When the new size is less than half
		 * the longest dimension of the source image,
		 * {@link #createThumbnail(BufferedImage, int)} or
		 * {@link #createThumbnail(BufferedImage, int, int)} should be used instead to
		 * ensure the quality of the result without sacrificing too much performance.
		 * </p>
		 *
		 * @see #createThumbnailFast(BufferedImage, int)
		 * @see #createThumbnail(BufferedImage, int)
		 * @see #createThumbnail(BufferedImage, int, int)
		 * @param image     the source image
		 * @param newWidth  the width of the thumbnail
		 * @param newHeight the height of the thumbnail
		 * @return a new compatible <code>BufferedImage</code> containing a thumbnail of
		 *         <code>image</code>
		 * @throws IllegalArgumentException if <code>newWidth</code> is larger than the
		 *                                  width of <code>image</code> or if
		 *                                  code>newHeight</code> is larger than the
		 *                                  height of <code>image</code> or if one of
		 *                                  the dimensions is &lt;= 0
		 */
		public static BufferedImage createThumbnailFast(BufferedImage image, int newWidth, int newHeight) {
			if (newWidth >= image.getWidth() || newHeight >= image.getHeight()) {
				throw new IllegalArgumentException(
						"newWidth and newHeight cannot" + " be greater than the image" + " dimensions");
			} else if (newWidth <= 0 || newHeight <= 0) {
				throw new IllegalArgumentException("newWidth and newHeight must" + " be greater than 0");
			}

			BufferedImage temp = createCompatibleImage(image, newWidth, newHeight);
			Graphics2D g2 = temp.createGraphics();

			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
			} finally {
				g2.dispose();
			}

			return temp;
		}

		/**
		 * <p>
		 * Returns a thumbnail of a source image. <code>newSize</code> defines the
		 * length of the longest dimension of the thumbnail. The other dimension is then
		 * computed according to the dimensions ratio of the original picture.
		 * </p>
		 * <p>
		 * This method offers a good trade-off between speed and quality. The result
		 * looks better than
		 * {@link #createThumbnailFast(BufferedImage, int)} when the new
		 * size is less than half the longest dimension of the source image, yet the
		 * rendering speed is almost similar.
		 * </p>
		 *
		 * @see #createThumbnailFast(BufferedImage, int, int)
		 * @see #createThumbnailFast(BufferedImage, int)
		 * @see #createThumbnail(BufferedImage, int, int)
		 * @param image   the source image
		 * @param newSize the length of the largest dimension of the thumbnail
		 * @return a new compatible <code>BufferedImage</code> containing a thumbnail of
		 *         <code>image</code>
		 * @throws IllegalArgumentException if <code>newSize</code> is larger than the
		 *                                  largest dimension of <code>image</code> or
		 *                                  &lt;= 0
		 */
		public static BufferedImage createThumbnail(BufferedImage image, int newSize) {
			int width = image.getWidth();
			int height = image.getHeight();

			boolean isTranslucent = image.getTransparency() != Transparency.OPAQUE;
			boolean isWidthGreater = width > height;

			if (isWidthGreater) {
				if (newSize >= width) {
					throw new IllegalArgumentException("newSize must be lower than" + " the image width");
				}
			} else if (newSize >= height) {
				throw new IllegalArgumentException("newSize must be lower than" + " the image height");
			}

			if (newSize <= 0) {
				throw new IllegalArgumentException("newSize must" + " be greater than 0");
			}

			float ratioWH = (float) width / (float) height;
			float ratioHW = (float) height / (float) width;

			BufferedImage thumb = image;
			BufferedImage temp = null;

			Graphics2D g2 = null;

			try {
				int previousWidth = width;
				int previousHeight = height;

				do {
					if (isWidthGreater) {
						width /= 2;
						if (width < newSize) {
							width = newSize;
						}
						height = (int) (width / ratioWH);
					} else {
						height /= 2;
						if (height < newSize) {
							height = newSize;
						}
						width = (int) (height / ratioHW);
					}

					if (temp == null || isTranslucent) {
						if (g2 != null) {
							// do not need to wrap with finally
							// outer finally block will ensure
							// that resources are properly reclaimed
							g2.dispose();
						}
						temp = createCompatibleImage(image, width, height);
						g2 = temp.createGraphics();
						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					}
					g2.drawImage(thumb, 0, 0, width, height, 0, 0, previousWidth, previousHeight, null);

					previousWidth = width;
					previousHeight = height;

					thumb = temp;
				} while (newSize != (isWidthGreater ? width : height));
			} finally {
				if (g2 != null) {
					g2.dispose();
				}
			}

			if (width != thumb.getWidth() || height != thumb.getHeight()) {
				temp = createCompatibleImage(image, width, height);
				g2 = temp.createGraphics();

				try {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(thumb, 0, 0, width, height, 0, 0, width, height, null);
				} finally {
					g2.dispose();
				}

				thumb = temp;
			}

			return thumb;
		}

		/**
		 * <p>
		 * Returns a thumbnail of a source image.
		 * </p>
		 * <p>
		 * This method offers a good trade-off between speed and quality. The result
		 * looks better than
		 * {@link #createThumbnailFast(BufferedImage, int)} when the new
		 * size is less than half the longest dimension of the source image, yet the
		 * rendering speed is almost similar.
		 * </p>
		 *
		 * @see #createThumbnailFast(BufferedImage, int)
		 * @see #createThumbnailFast(BufferedImage, int, int)
		 * @see #createThumbnail(BufferedImage, int)
		 * @param image     the source image
		 * @param newWidth  the width of the thumbnail
		 * @param newHeight the height of the thumbnail
		 * @return a new compatible <code>BufferedImage</code> containing a thumbnail of
		 *         <code>image</code>
		 * @throws IllegalArgumentException if <code>newWidth</code> is larger than the
		 *                                  width of <code>image</code> or if
		 *                                  code>newHeight</code> is larger than the
		 *                                  height of
		 *                                  <code>image or if one the dimensions is not &gt; 0</code>
		 */
		public static BufferedImage createThumbnail(BufferedImage image, int newWidth, int newHeight) {
			int width = image.getWidth();
			int height = image.getHeight();

			boolean isTranslucent = image.getTransparency() != Transparency.OPAQUE;

			if (newWidth >= width || newHeight >= height) {
				throw new IllegalArgumentException(
						"newWidth and newHeight cannot" + " be greater than the image" + " dimensions");
			} else if (newWidth <= 0 || newHeight <= 0) {
				throw new IllegalArgumentException("newWidth and newHeight must" + " be greater than 0");
			}

			BufferedImage thumb = image;
			BufferedImage temp = null;

			Graphics2D g2 = null;

			try {
				int previousWidth = width;
				int previousHeight = height;

				do {
					if (width > newWidth) {
						width /= 2;
						if (width < newWidth) {
							width = newWidth;
						}
					}

					if (height > newHeight) {
						height /= 2;
						if (height < newHeight) {
							height = newHeight;
						}
					}

					if (temp == null || isTranslucent) {
						if (g2 != null) {
							// do not need to wrap with finally
							// outer finally block will ensure
							// that resources are properly reclaimed
							g2.dispose();
						}
						temp = createCompatibleImage(image, width, height);
						g2 = temp.createGraphics();
						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					}
					g2.drawImage(thumb, 0, 0, width, height, 0, 0, previousWidth, previousHeight, null);

					previousWidth = width;
					previousHeight = height;

					thumb = temp;
				} while (width != newWidth || height != newHeight);
			} finally {
				if (g2 != null) {
					g2.dispose();
				}
			}

			if (width != thumb.getWidth() || height != thumb.getHeight()) {
				temp = createCompatibleImage(image, width, height);
				g2 = temp.createGraphics();

				try {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g2.drawImage(thumb, 0, 0, width, height, 0, 0, width, height, null);
				} finally {
					g2.dispose();
				}

				thumb = temp;
			}

			return thumb;
		}

		/**
		 * <p>
		 * Returns an array of pixels, stored as integers, from a
		 * <code>BufferedImage</code>. The pixels are grabbed from a rectangular area
		 * defined by a location and two dimensions. Calling this method on an image of
		 * type different from <code>BufferedImage.TYPE_INT_ARGB</code> and
		 * <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.
		 * </p>
		 *
		 * @param img    the source image
		 * @param x      the x location at which to start grabbing pixels
		 * @param y      the y location at which to start grabbing pixels
		 * @param w      the width of the rectangle of pixels to grab
		 * @param h      the height of the rectangle of pixels to grab
		 * @param pixels a pre-allocated array of pixels of size w*h; can be null
		 * @return <code>pixels</code> if non-null, a new array of integers otherwise
		 * @throws IllegalArgumentException is <code>pixels</code> is non-null and of
		 *                                  length &lt; w*h
		 */
		public static int[] getPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
			if (w == 0 || h == 0) {
				return new int[0];
			}

			if (pixels == null) {
				pixels = new int[w * h];
			} else if (pixels.length < w * h) {
				throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");
			}

			int imageType = img.getType();
			if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
				Raster raster = img.getRaster();
				return (int[]) raster.getDataElements(x, y, w, h, pixels);
			}

			// Unmanages the image
			return img.getRGB(x, y, w, h, pixels, 0, w);
		}

		/**
		 * <p>
		 * Writes a rectangular area of pixels in the destination
		 * <code>BufferedImage</code>. Calling this method on an image of type different
		 * from <code>BufferedImage.TYPE_INT_ARGB</code> and
		 * <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.
		 * </p>
		 *
		 * @param img    the destination image
		 * @param x      the x location at which to start storing pixels
		 * @param y      the y location at which to start storing pixels
		 * @param w      the width of the rectangle of pixels to store
		 * @param h      the height of the rectangle of pixels to store
		 * @param pixels an array of pixels, stored as integers
		 * @throws IllegalArgumentException is <code>pixels</code> is non-null and of
		 *                                  length &lt; w*h
		 */
		public static void setPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
			if (pixels == null || w == 0 || h == 0) {
				return;
			} else if (pixels.length < w * h) {
				throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");
			}

			int imageType = img.getType();
			if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_RGB) {
				WritableRaster raster = img.getRaster();
				raster.setDataElements(x, y, w, h, pixels);
			} else {
				// Unmanages the image
				img.setRGB(x, y, w, h, pixels, 0, w);
			}
		}

		/**
		 * Clears the data from the image.
		 *
		 * @param img the image to erase
		 */
		public static void clear(Image img) {
			Graphics g = img.getGraphics();

			try {
				if (g instanceof Graphics2D) {
					((Graphics2D) g).setComposite(AlphaComposite.Clear);
				} else {
					g.setColor(new Color(0, 0, 0, 0));
				}

				g.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
			} finally {
				g.dispose();
			}
		}

		/**
		 * Draws an image on top of a component by doing a 3x3 grid stretch of the image
		 * using the specified insets.
		 */
		public static void tileStretchPaint(Graphics g, JComponent comp, BufferedImage img, Insets ins) {

			int left = ins.left;
			int right = ins.right;
			int top = ins.top;
			int bottom = ins.bottom;

			// top
			g.drawImage(img, 0, 0, left, top, 0, 0, left, top, null);
			g.drawImage(img, left, 0, comp.getWidth() - right, top, left, 0, img.getWidth() - right, top, null);
			g.drawImage(img, comp.getWidth() - right, 0, comp.getWidth(), top, img.getWidth() - right, 0,
					img.getWidth(), top, null);

			// middle
			g.drawImage(img, 0, top, left, comp.getHeight() - bottom, 0, top, left, img.getHeight() - bottom, null);

			g.drawImage(img, left, top, comp.getWidth() - right, comp.getHeight() - bottom, left, top,
					img.getWidth() - right, img.getHeight() - bottom, null);

			g.drawImage(img, comp.getWidth() - right, top, comp.getWidth(), comp.getHeight() - bottom,
					img.getWidth() - right, top, img.getWidth(), img.getHeight() - bottom, null);

			// bottom
			g.drawImage(img, 0, comp.getHeight() - bottom, left, comp.getHeight(), 0, img.getHeight() - bottom, left,
					img.getHeight(), null);
			g.drawImage(img, left, comp.getHeight() - bottom, comp.getWidth() - right, comp.getHeight(), left,
					img.getHeight() - bottom, img.getWidth() - right, img.getHeight(), null);
			g.drawImage(img, comp.getWidth() - right, comp.getHeight() - bottom, comp.getWidth(), comp.getHeight(),
					img.getWidth() - right, img.getHeight() - bottom, img.getWidth(), img.getHeight(), null);
		}
	}
}
