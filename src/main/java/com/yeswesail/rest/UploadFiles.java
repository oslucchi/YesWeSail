package com.yeswesail.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPart;

public class UploadFiles {
	final static Logger log = Logger.getLogger(UploadFiles.class);
	final static ApplicationProperties prop = ApplicationProperties.getInstance();
	
	
	public static boolean moveFiles(String fromPath, String toPath, String prefix, boolean overwrite)
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
		String contextPath = "";
		try {
			contextPath = prop.getContext().getResource("/").getPath();
		} 
		catch (MalformedURLException e) {
			// TODO Error handling
		}
		
		if (!path.startsWith(contextPath))
		{
			path = contextPath + File.separator + path;
		}
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

	private static ArrayList<String> uploadFiles(BodyPart part, String destPath, 
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
			return(null);
		}
		
		ArrayList<String> uploaded = new ArrayList<String>();
		
		OutputStream out;
		try {
			String extension = part.getContentDisposition().getFileName()
									.substring(part.getContentDisposition().getFileName().lastIndexOf("."));
			out = new FileOutputStream(new File(tempDir + prefix + index + extension));
			out.write(buf);
			out.flush();
			out.close();
			uploaded.add(prefix + index + extension);
		} 
		catch (FileNotFoundException e) 
		{
			log.error("Exception FileNotFoundException on '" +
					  part.getContentDisposition().getFileName() + "'");
			return(null);
		} 
		catch (IOException e) {
			log.error("Exception IOException on '" +
					  part.getContentDisposition().getFileName() + "'");
			return(null);
		}

		return(uploaded);
	}
	
	private static boolean isAcceptable(String[] acceptableTypes, String requestedType)
	{
		for(String type : acceptableTypes)
		{
			if (requestedType.compareTo(type) == 0)
				return true;
		}
		return false;
	}

	private static int startUploafFromIndex(String destPath, String prefix, boolean overwrite)
	{
		ArrayList<String> images = UploadFiles.getExistingFilesPath(prefix, destPath);
		int lastIndex = -1;
		if (!overwrite)
		{
			// Checking if any existing file to preserve and marking the starting sequence number for 
			// this file set to upload
			int pos;
			int a = 0;
			for(String fName : images)
			{
				pos = fName.lastIndexOf("_") + 1;
				fName = fName.substring(pos);
				a = Integer.parseInt(fName.substring(0, fName.lastIndexOf(".")));
				if (lastIndex < a)
					lastIndex = a;
			}
		}
		else
		{
			File directory = new File(destPath);
	        File[] fList = directory.listFiles();
	        for (File file : fList)
	        {
	            if (!file.isFile() || (!file.getName().startsWith(prefix)))
	            	continue;
	            file.delete();
	        }
		}
		lastIndex++;
		return lastIndex;
	}
	
	@SuppressWarnings("unchecked")
	public static Object[] uploadBodyPart(
			List<BodyPart> parts, 
			String partName,
			String token,
			String resource,
			String prefix,
			String[] acceptableTypes,
			int languageId,
			boolean overwrite)

	{
		Object[] results = new Object[3];
		results[2] = new ArrayList<String>();

		String destPath = null;
		try 
		{
			destPath = prop.getContext().getResource(resource).getPath();
		}
		catch (MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving context path");
			((ArrayList<String>) results[2]).add(
					(String) Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, e, 
												   languageId, "generic.uploadFileFormatError").getEntity());
			return results;
		}

		ArrayList<String> uploaded = new ArrayList<String>();
		ArrayList<String> rejected  = new ArrayList<String>();
		ArrayList<String> rejectionMsg  = new ArrayList<String>();

		int lastIndex = startUploafFromIndex(destPath, prefix, overwrite);
		// uploading the files into temp dir
		for(BodyPart part : parts)
		{
			if ((part.getContentDisposition() == null) ||
				!part.getContentDisposition().getParameters().get("name").startsWith(partName))
				continue;
			
			if(isAcceptable(acceptableTypes, part.getMediaType().getType() + "/" + part.getMediaType().getSubtype()))
			{
				uploaded.addAll(UploadFiles.uploadFiles(part, destPath, prefix, token, lastIndex++));
			}
			else if (part.getMediaType().getType().compareTo("text") != 0)
			{
				rejected.add(part.getContentDisposition().getFileName());
				rejectionMsg.add(LanguageResources.getResource(languageId, "generic.uploadFileFormatError"));
			}		
		}
		for(int i = 0; i < uploaded.size(); i++)
		{
			uploaded.set(i, prop.getWebHost() + resource + File.pathSeparator + uploaded.get(i));
		}
		results[0] = uploaded;
		results[1] = rejected;
		results[2] = rejectionMsg;
		return results;
	}

	public static Response uploadFromRestRequest(
								List<BodyPart> parts, 
								String token,
								String resource,
								String prefix,
								String[] acceptableTypes,
								int languageId,
								boolean overwrite)
	{
		// getting destination path from context
		String destPath = null;
		try 
		{
			destPath = prop.getContext().getResource(resource).getPath();
		}
		catch (MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving context path");
 			return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, e, languageId, "generic.uploadFileFormatError");
		}

		int lastIndex = startUploafFromIndex(destPath, prefix, overwrite);

		ArrayList<String> rejected = new ArrayList<>();
		// uploading the files into temp dir
		for(BodyPart part : parts)
		{
			if(isAcceptable(acceptableTypes, part.getMediaType().getType() + "/" + part.getMediaType().getSubtype()))
			{
				UploadFiles.uploadFiles(part, destPath, prefix, token, lastIndex++);
			}
			else if (part.getMediaType().getType().compareTo("text") != 0)
			{
				rejected.add(part.getContentDisposition().getFileName());
			}		
		}
		
		// moving to the final destination
		UploadFiles.moveFiles(destPath + File.separator + token, destPath, prefix, false);

		if (rejected.size() != 0)
		{
 			Utils jsonizer = new Utils();
 			jsonizer.addToJsonContainer("rejectionMessage", 
									 LanguageResources.getResource(languageId, 
											 					   "generic.uploadFileFormatError"),
									 true);
 			jsonizer.addToJsonContainer("rejectedList", 
									  rejected.toArray(new String[rejected.size()]), false);
			return Response.status(Response.Status.NOT_ACCEPTABLE)
 					.entity(jsonizer.jsonize())
					.build();
		}
		else
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}
	}
}