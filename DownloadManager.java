package lab;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
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
	        	List<Long> startRangesList = metadataManager.getStartRangeList();
	        	System.out.println("startRangesList: " + startRangesList);
	        	
    			for (int i = 0; i < startRangesList.size(); i++) {
    				long start = startRangesList.get(i);
    				Runnable runnable = new HttpRangeGetter(i_Url, start, start + 100 , blockingQueue);
					//TODO: CHANGE THE END VARIABLE!!!!!!!!!!!!!!!!!!!!
					executorService.execute(runnable);
					System.out.println("Download manager, i=" + i + ", start=" + start);
    			}

    			
    			executorService.shutdown();
	        }
			catch (Exception e) {
				System.err.println("OOPS! Something went very wrong.\nPlease try again later.\n" + e);
			}
			finally{
			System.out.println("Have a magical eve!");
			}

		} 
		catch (IOException e) {
			System.err.println("OOPS! Something went very wrong.\nPlease try again later.\n" + e);
		}
		finally{
		System.out.println("Have a magical day!");
		}
	}
}









