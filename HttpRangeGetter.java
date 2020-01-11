package lab;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

public class HttpRangeGetter implements Runnable {
	private static final int chunkSize = 1024;
	private final URL url;
	private final Long start; // The begining of this range
	private final Long end; // The ending of this range
	private final BlockingQueue<Chunk> blockingQueue;

	public HttpRangeGetter(String i_Url, Long i_Start, Long i_End, BlockingQueue i_BlockingQueue) {
		this.url = urlBuilder(i_Url);
		this.start = i_Start;
		this.end = i_End;
		this.blockingQueue = i_BlockingQueue;
	}

	/**
	 * build a URL address from a String
	 * 
	 * @param i_Url: String
	 */
	private URL urlBuilder(String i_Url) {
		try {
			return new URL(i_Url);
		} catch (IOException e) {
			System.err.println("OOPS! Could not convert given address to a legal URL address.\n" + e);
			System.exit(1);
			return null;
		}
	}

	/**
	 * Determine which range of the given file to download
	 * 
	 */
	public void rangeDownloader() {
		try {
			HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

			// TODO: Do we need to get 201, 202..??
			if (connection.getResponseCode() == 200) {

				InputStream inputStream = connection.getInputStream();

				long bytesRead = 0;
				long bytesToRead = this.end - this.start + 1;

				// While I can read
				while (bytesRead < bytesToRead) {
					byte[] chunkData = new byte[chunkSize];
					Long seek = this.start + bytesRead;
					int sizeRead = inputStream.read(chunkData); // The size of what we read now in bytes

					//End of the file
					if (sizeRead == -1)
						break;

					bytesRead += sizeRead;

					Chunk chunk = new Chunk(chunkData, sizeRead, seek, this.start);
					this.blockingQueue.put(chunk);
				}
				
			inputStream.close();
			connection.disconnect();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	@Override
	public void run() {
		try {
			this.rangeDownloader();
		}
		catch(Exception e) {
			System.err.println("OOPS! Something went wrong.\n" + e);
			System.exit(1);
		}		
	}
}