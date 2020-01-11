package lab;

import java.net.MalformedURLException;

public class DownloadManager {

	public static void main(String[] args) throws MalformedURLException {
		
		for (int i = 0; i < args.length; i++) {
			String address = args[i];
			String url = HttpRangeGetter.determineFile(address);
			if (url != null) {
				HttpRangeGetter.download(address, url);				
			}
			else {
				System.err.println("Could not get local file name: " + address);
			}
		}
	}
}
