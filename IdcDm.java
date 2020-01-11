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
		System.out.println("Downloading...");

		if (numOfThreads > 1) {
			System.out.println(" using " + numOfThreads + " connections...");
		}
		System.out.println("before the big storm");
		DownloadManager.DownloadURL(url, numOfThreads);
		System.out.println("after the big storm");
	}
}

