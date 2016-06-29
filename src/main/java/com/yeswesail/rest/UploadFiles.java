package com.yeswesail.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPart;

public class UploadFiles {
	final static Logger log = Logger.getLogger(UploadFiles.class);
	final static ApplicationProperties prop = ApplicationProperties.getInstance();
	
	public void uploadMultipartFiles(HttpServletRequest request, String contextPath, 
									 int eventId, int startFrom, String uuid) throws Exception
	{
		final FileItemFactory factory = new DiskFileItemFactory();
		final ServletFileUpload fileUpload = new ServletFileUpload(factory);
		try
		{
			/*
			 * parseRequest returns a list of FileItem
			 * but in old (pre-java5) style
			 */
			final List<FileItem> items = fileUpload.parseRequest(request);
			log.trace(items.size() + " elements in the request");

			int imageRef = startFrom;
			if (items != null)
			{
				// Create the folder to host files under the temp directory
				Path path = Paths.get(contextPath + File.separator + "temp" + File.separator + uuid);
				Files.createDirectories(path);
				log.trace("Files will be saved in folder '" + path + "'");
				final Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext())
				{
					final FileItem item = (FileItem) iter.next();
					
					String extension = item.getName().substring(item.getName().lastIndexOf("."));
					final String itemName = eventId + "_" + imageRef++ + extension;
					log.trace("Moving the file: '" + itemName + "'");		    		
			    	item.write(new File(path + File.separator + itemName));
				}
			}
		}
		catch (FileUploadException fue)
		{
			fue.printStackTrace();
			throw(fue);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(e);
		}
	}
	
	public static boolean moveFiles(String contextPath, String uuid, String prefix)
	{
		boolean errorMoving = false;
		File srcDir = new File(contextPath + File.separator + "temp" + File.separator + uuid);
		if(srcDir.isDirectory()) 
		{
		    File[] dirContent = srcDir.listFiles();
		    String destPath = contextPath + File.separator + "images/events";
		    for(int i = 0; i < dirContent.length; i++) 
		    {
				if (!dirContent[i].renameTo(new File(destPath + File.separator + prefix + dirContent[i].getName())))
				{
					log.warn("Error moving file '" + dirContent[i].getName() + "' to '" + destPath);
					errorMoving = true;
				}
		    }
		    try 
		    {
				FileUtils.deleteDirectory(srcDir);
			}
		    catch (IOException e) 
		    {
		    	log.warn("Exception '" + e.getMessage() + "' removing the src directory '" + contextPath + "'");
			}
		}
		return errorMoving;
	}
	
	public static boolean newMoveFiles(String fromPath, String toPath, String prefix, boolean overwrite)
	{
		boolean errorMoving = false;
    	if (overwrite)
    	{
    		// Remove all file with the given prefix from the destination directory
    		File toDir = new File(toPath);
    		if(toDir.isDirectory()) 
    		{
    		    File[] dirContent = toDir.listFiles();
			    for(int i = 0; i < dirContent.length; i++) 
			    {
			    	if (dirContent[i].isFile() && dirContent[i].getName().startsWith(prefix))
			    		dirContent[i].delete();
			    }
    		}
    	}
    	
		File srcDir = new File(fromPath);
		if(srcDir.isDirectory()) 
		{
		    File[] dirContent = srcDir.listFiles();
		    try 
		    {
			    for(int i = 0; i < dirContent.length; i++) 
			    {
			    	Files.move(Paths.get(dirContent[i].getPath()), 
			    			   Paths.get(toPath + File.separator + dirContent[i].getName()), 
			    			   java.nio.file.StandardCopyOption.REPLACE_EXISTING);
//			    	if (!dirContent[i].renameTo(new File(toPath)))
//					{
//						log.warn("Error moving file '" + dirContent[i].getName() + "' to '" + toPath);
//						errorMoving = true;
//					}
			    }
				FileUtils.deleteDirectory(srcDir);
			}
		    catch (IOException e) 
		    {
		    	log.warn("Exception '" + e.getMessage() + "' removing the src directory '" + fromPath + "'");
			}
		}
		return errorMoving;
	}

	public static ArrayList<String> getExistingFilesPath(String prefix, String path)
	{
		File directory = new File(path);
        File[] fList = directory.listFiles();
        ArrayList<String> imageURLs = new ArrayList<>();
        for (File file : fList)
        {
            if (!file.isFile() || (!file.getName().startsWith(prefix)))
            	continue;
            
            imageURLs.add(prop.getWebHost() + file.getPath().substring(file.getPath().indexOf("/images/")));
        }
        return(imageURLs);
	}

	public static boolean uploadFiles(BodyPart part, String destPath, 
							   String prefix, String token, int index)
	{
		byte[] buf = part.getEntityAs(byte[].class);
		String tempDir = destPath + File.separator + token + File.separator;
		try 
		{
			Files.createDirectories(Paths.get(tempDir));
		}
		catch (IOException e1) 
		{
			return(false);
		}
		
		OutputStream out;
		try {
			String extension = part.getContentDisposition().getFileName()
									.substring(part.getContentDisposition().getFileName().lastIndexOf("."));
//			out = new FileOutputStream(new File(tempDir + ));
			out = new FileOutputStream(new File(tempDir + prefix + index + extension));
			out.write(buf);
			out.flush();
			out.close();
		} 
		catch (FileNotFoundException e) 
		{
			log.error("Exception FileNotFoundException on '" +
					  part.getContentDisposition().getFileName() + "'");
			return(false);
		} 
		catch (IOException e) {
			log.error("Exception IOException on '" +
					  part.getContentDisposition().getFileName() + "'");
			return(false);
		}

		return(true);
	}
}
