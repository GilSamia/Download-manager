package lab;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.*;

public class DownloadManager {

	/**
	 *
	 * @param url
	 * @param numOfThreads
	 */
	protected static void DownloadURL(String i_Url, int numOfThreads) {

		BlockingQueue<Chunk> blockingQueue = new LinkedBlockingQueue<>();
		
		try {
			MetadataManager metadataManager = new MetadataManager(i_Url);
			FileWriter fileWriter = new FileWriter(metadataManager, blockingQueue);
			Thread threadFileWriter = new Thread(fileWriter);
	        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
		
	        try {
	        	threadFileWriter.start();
	        	for() {
	        		
	        	}
	        }
		} 
		catch (IOException e) {
			System.err.println("OOPS! Something went very wrong.\nPlease try again later.\n" + e);
			e.printStackTrace();
		}		
	}

}
