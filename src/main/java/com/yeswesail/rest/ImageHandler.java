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

	final private int maxWidthSmall = 200;
	final private int maxWidthMedium = 500;
	final private int maxWidthLarge  = 1920;
	final private int maxHeigthSmall = 150;
	final private int maxHeigthMedium = 300;
	final private int maxHeigthLarge = 1080;
	private Double width = 150.0;
	private Double heigth = 112.0;

	public ImageHandler() {
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
			width = originalImage.getWidth() / ratio;
			int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

			String imageType = imagePath.substring(imagePath.lastIndexOf('.') + 1);
			imageType = "jpg";
			String tumbnailPath = imagePath.substring(0, imagePath.lastIndexOf('.') - 1) + "_tn." + imageType;
			BufferedImage resizedImage = new BufferedImage(width.intValue(), heigth.intValue(), type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, width.intValue(), heigth.intValue(), null);
			g.dispose();
			ImageIO.write(resizedImage, imageType, new File(tumbnailPath));
		}
		catch(IOException e){
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
	}

	public void createThumbnail(String image){
		if (image.endsWith("-large.jpg") || image.endsWith("-medium.jpg") || image.endsWith("-small.jpg"))
		{
			return;
		}
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
		width = originalImage.getWidth() / ratio;
		String thumbnailPath = imagePath.substring(0, imagePath.lastIndexOf('.') - 1) + "_tn.jpg";
		try {
			Thumbnails.of(imagePath)
			  .size(width.intValue(), heigth.intValue()) 
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
		
		heigth = originalImage.getHeight() / ratio;
		width = originalImage.getWidth() / ratio;
		log.debug("Resizing from " + originalImage.getWidth() + "x" +
				  originalImage.getHeight() + " to " + width.intValue() + "x" + heigth.intValue());
		String outPath = imagePath.substring(0, imagePath.lastIndexOf('.')) + suffix + ".jpg";
		try {
			Thumbnails.of(imagePath)
			  .size(width.intValue(), heigth.intValue()) 
			  .toFile(outPath);
		} catch (IOException e) {
			log.warn("Exception " + e.getMessage() + " resizing image");
		}
	}

	public void scaleImages(String image){
		if (image.endsWith("-large.jpg") || image.endsWith("-medium.jpg") || image.endsWith("-small.jpg"))
		{
			return;
		}
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
}
