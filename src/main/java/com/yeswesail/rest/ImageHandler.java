package com.yeswesail.rest;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

import net.coobird.thumbnailator.Thumbnails;


/*
 * @author mkyong
 *
 */
public class ImageHandler {
	@Context
	private ServletContext context;
	private ApplicationProperties prop = ApplicationProperties.getInstance();

	final Logger log = Logger.getLogger(this.getClass());
	private String contextPath = null;

	private int maxWidthSmall = 200;
	private int maxWidthMedium = 500;
	private int maxWidthLarge  = 1920;
	private int maxHeigthSmall = 150;
	private int maxHeigthMedium = 300;
	private int maxHeigthLarge = 1080;
	private int width = 150;
	private int heigth = 112;

	public ImageHandler() {
//		try {
//			contextPath = context.getResource("/").getPath();
//		}
//		catch (MalformedURLException e) 
//		{
//			contextPath = null;
//			log.warn("Exception " + e.getMessage() + " retrieving context path");	
//		}
		try 
		{
			contextPath = prop.getContext().getResource("/").getPath();
		}
		catch (MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving context path");
		}
	}

	public void resizeImage(String image){
		try{
			String imagePath = contextPath + File.separator + image.substring(image.indexOf("images"));
			BufferedImage originalImage = ImageIO.read(new File(imagePath));
			double ratio = originalImage.getHeight() / heigth;
			width = (int) (originalImage.getWidth() / ratio);
			int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

			String imageType = imagePath.substring(imagePath.lastIndexOf('.') + 1);
			imageType = "jpg";
			String tumbnailPath = imagePath.substring(0, imagePath.lastIndexOf('.') - 1) + "_tn." + imageType;
			BufferedImage resizedImage = new BufferedImage(width, heigth, type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, width, heigth, null);
			g.dispose();
			ImageIO.write(resizedImage, imageType, new File(tumbnailPath));
		}
		catch(IOException e){
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
	}

	public void createThumbnail(String image){
		String imagePath = null;
		BufferedImage originalImage = null;
		try{
			imagePath = contextPath + File.separator + image.substring(image.indexOf("images"));
			originalImage = ImageIO.read(new File(imagePath));
		}
		catch(IOException e){
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
		double ratio = originalImage.getHeight() / heigth;
		width = (int) (originalImage.getWidth() / ratio);
		String thumbnailPath = imagePath.substring(0, imagePath.lastIndexOf('.') - 1) + "_tn.jpg";
		try {
			Thumbnails.of(imagePath)
			  .size(width, heigth) 
			  .toFile(thumbnailPath);
		} catch (IOException e) {
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
	}

	private void doScale(int maxW, int maxH, BufferedImage originalImage, String imagePath, String suffix)
	{
		double ratio = 1.0;
		if ((originalImage.getHeight() > maxH) || 
				(originalImage.getWidth() > maxW))
		{
			if (originalImage.getHeight()/maxH > originalImage.getWidth()/maxW)
			{
				ratio = originalImage.getHeight() / maxH;
			}
			else
			{
				ratio = originalImage.getWidth() / maxW;
			}
		}
		
		heigth = (int) (originalImage.getHeight() / ratio);
		width = (int) (originalImage.getWidth() / ratio);
		String outPath = imagePath.substring(0, imagePath.lastIndexOf('.') - 1) + suffix + ".jpg";
		try {
			Thumbnails.of(imagePath)
			  .size(width, heigth) 
			  .toFile(outPath);
		} catch (IOException e) {
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
	}

	public void scaleImages(String image){
		String imagePath = null;
		BufferedImage originalImage = null;
		try{
			imagePath = contextPath + File.separator + image.substring(image.indexOf("images"));
			originalImage = ImageIO.read(new File(imagePath));
		}
		catch(IOException e){
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
		
		doScale(maxWidthSmall, maxHeigthSmall, originalImage, imagePath,"-small");
		doScale(maxWidthMedium, maxHeigthMedium, originalImage, imagePath, "-medium");
		doScale(maxWidthLarge, maxHeigthLarge, originalImage, imagePath, "-large");
	}
	/*
	private static BufferedImage resizeImageWithHint(BufferedImage originalImage, int type){

		BufferedImage resizedImage = new BufferedImage(width, heigth, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, heigth, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}
	*/
}
