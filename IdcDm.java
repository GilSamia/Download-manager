package lab;

import java.util.concurrent.*;

public class IdcDm {

	/**
	 * Begin the download after accepting the user's args.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		int numOfThreads = 1;

		if (args.length < 1 || args.length > 2) {
			System.err.println("OPPS! Something went wrong.\nThe number of arguments is invalid.");
			System.exit(1);
		}
		String url = args[0];

		if (args.length == 2) {
			numOfThreads = Integer.parseInt(args[1]);
		}
		System.out.printf("Downloading");

		if (numOfThreads > 1) {
			System.out.printf(" using " + numOfThreads + " connections");
		}
		System.out.println("...");

		DownloadURL(url, numOfThreads);
	}

	/**
	 *
	 * @param url
	 * @param numOfThreads
	 */
	private static void DownloadURL(String url, int numOfThreads) {
		
		BlockingQueue<Chunk> blockingQueue = new LinkedBlockingQueue<>();
		
	
	
	
	}
}