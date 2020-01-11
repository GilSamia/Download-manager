package lab;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MetadataManager {
	private final String fileName;
	private long fileSize;
	private File file;
	private final int maxPercentage = 100;
	private int percentageCounter = 0;

	public MetadataManager(String url) throws IOException {
		this.fileName = createMetadataFileName(url);
		this.fileSize = getFileSize(url);
		this.file = createMetadataFile();
	}

	public String getFileName() {
		return this.fileName;
	}

	public long getFileSize() {
		return this.fileSize;
	}

	public File getFile() {
		return this.file;
	}

	/**
	 * This function creates the metadata file name, using the given URL and the
	 * file name in it.
	 *
	 * @param url: String
	 */
	private static String createMetadataFileName(String url) throws MalformedURLException {
		int fileNameIndex = url.lastIndexOf('/');

		// check if this is a legal address
		if (fileNameIndex > -1 && fileNameIndex < url.length() - 1) {
			String urlFileName = (new URL(url)).getFile();
			return urlFileName + ".metadata";
		} else {
			return null;
		}
	}

	/**
	 * This function returns long file size for the given URL.
	 *
	 * @param url: String
	 */
	private long getFileSize(String i_Url) throws IOException {
		URL url = new URL(i_Url);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		int response = connection.getResponseCode();

		// Check if the HTTP request succeeded
		if (response == 200) {
			return connection.getContentLengthLong();
		}
		return 0;
	}

	/**
	 * This function creates metadata file. First, we create metadata file object,
	 * and a temp metadata file, later we initialize the metadata random accesses
	 * file.
	 */
	private File createMetadataFile() {

		File metadataFile = new File(this.fileName);// this file had '.metadata' extension
		Path path = metadataFile.toPath();
		Path resolvedPath = path.resolve(".temp");

		// delete all '.temp' files. -> These are files that were not converted
		// to '.metadata' due to app crash.
		try {
			Files.deleteIfExists(resolvedPath);
			if (!metadataFile.exists()) {
				boolean isCreated = metadataFile.createNewFile();

				// initialize metadata file
				if (isCreated) {
					initMetadataFile(metadataFile);
				} else {
					System.err.println("OOPS! Could not create file!\nPlease try again later.");
					System.exit(1);
				}
			}
			return metadataFile;
		} catch (IOException e) {
			System.err.println("OOPS! Something went wrong!\n" + e);
			System.exit(1);
		}
		return null;
	}

	/**
	 * This is a helper function that initialize metadata file.
	 * 
	 * @param metadataFile: File
	 */
	private void initMetadataFile(File metadataFile) {
		try {
			RandomAccessFile raf = new RandomAccessFile(metadataFile, "rw");
			StringBuilder rangeSB = new StringBuilder();

			Long percentage = this.fileSize / maxPercentage;
			Long start;
			Long end;

			for (int i = 0; i < maxPercentage; i++) {
				start = percentage * i;
				end = start + percentage - 1;
				if (i == maxPercentage - 1 && end != this.fileSize) {
					end = this.fileSize;
				}
				String currentRange = Long.toString(start) + ',' + Long.toString(end);
				rangeSB.append(currentRange + "\n");
			} // for loop

			raf.writeBytes(rangeSB.toString());
			raf.close();

		} catch (IOException e) {
			System.err.println("OOPS! Could not initialize metadata file.\nPlease try again later.");
			System.exit(1);
		}
	}
	
	protected boolean isDownloadCompleted() {
		return this.percentageCounter == this.maxPercentage;
	}
}
