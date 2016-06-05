package com.yeswesail.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class UploadFiles {
	final Logger log = Logger.getLogger(this.getClass());
	
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
	
	public boolean moveFiles(String contextPath, String uuid, String prefix)
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
}
