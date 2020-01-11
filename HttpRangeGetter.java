package lab;

import java.io.File;
import java.io.*;
import java.net.*;

/**
 * This class manages the downloading process of a single file
 */
public class HttpRangeGetter {
	static int chunkSize = 1024;
	static int bufferBegining = 0;
	
	/**
	 * Determines the download file given from the URL
	 * 
	 * @param address
	 */
	public static String determineFile(String address) throws MalformedURLException {
		int fileNameIndex = address.lastIndexOf('/');
		
		//check if this is a legal address
		if (fileNameIndex > -1 && fileNameIndex < address.length() - 1) 
		{
			return (new URL(address)).getFile();
		} 
		else 
		{
			return null;
		}
	}

	/**
	 * Determine which range of the given file to download
	 * 
	 * @param adress
	 * @param localFileName
	 */	
	public static void download(String address, String localFileName) {
		InputStream input = null;
		OutputStream output = null;
		URLConnection connection = null;

		try {
			URL url = new URL(address);
			connection = url.openConnection();
			input = connection.getInputStream();
			output = new BufferedOutputStream(new FileOutputStream(localFileName));

			int numOfBytesRead;
			byte[] buffer = new byte[chunkSize];
			long numWritten = 0;
			
			//While the end of the stream hasn't been reached
			while ((numOfBytesRead = input.read(buffer)) != -1) 
			{
				output.write(buffer, bufferBegining, numOfBytesRead);
				numWritten += numOfBytesRead;
			}

			//System.out.println(localFileName + "\t" + numWritten);
		}
		catch (Exception e) 
		{
			System.err.println("OOPS! Something went wrong.\n" + e); 
		} 
		finally 
		{
			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} 
			catch (IOException e) 
			{
				System.err.println("OOPS! Something went wrong with closing the stream.\n" + e); 

			}
		}
	}
}