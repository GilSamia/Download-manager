package lab;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MetadataManager {
	private final String fileName;
	private long fileSize;
	private File file;
	private long[] startRangeArr;
	private long[] endRangeArr;
	private final int maxPercentage = 100;
	private int percentageCounter = 0;
	private List<Long> startRangeList;

	public MetadataManager(String url) throws IOException {
		this.fileName = createMetadataFileName(url);
		this.fileSize = getFileSize(url);
		this.file = createMetadataFile();
		this.startRangeList = createStartRangeList();
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
	
	public List<Long> getStartRangeList() {
		return this.startRangeList;
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
			String urlFileName = url.substring(fileNameIndex + 1, url.length());
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

		File metadataFile = new File(this.fileName);// this file has '.metadata' extension
		Path path = metadataFile.toPath();
		System.out.println(path);
		System.out.println(this.fileName);
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
			System.out.println("Ani po");
			return metadataFile;
		} catch (IOException e) {
			System.err.println("OOPS! Something went wrongaa!\n" + e);
			System.exit(1);
		}
		return null;
	}

	/**
	 * This is a helper function that initialize metadata file. The function also
	 * populate the start range array with values.
	 * 
	 * @param metadataFile: File
	 */
	private void initMetadataFile(File metadataFile) {
		try {
			RandomAccessFile raf = new RandomAccessFile(metadataFile, "rw");
			StringBuilder rangeSB = new StringBuilder();

			long percentage = this.fileSize / maxPercentage;
			long start;
			for (int i = 0; i < maxPercentage; i++) {
				start = percentage * i;
				rangeSB.append(Long.toString(start) + "\n");
				this.startRangeArr[i] = start;

				if (i != maxPercentage - 1) {
					this.endRangeArr[i] = start + percentage - 1;
				} else {
					this.endRangeArr[i] = this.fileSize;
				}
			}

			raf.writeBytes(rangeSB.toString());
			raf.close();

		} catch (IOException e) {
			System.err.println("OOPS! Could not initialize metadata file.\nPlease try again later.");
			System.exit(1);
		}
	}

	/**
	 * This function updates the start array and end array, according to the data we
	 * downloaded. It also updates the download process. The function also pronts
	 * download progress and success.
	 * 
	 * @param chunk:Chunk
	 */
	protected void updateDownloadedRanges(Chunk chunk) {
		int startChunkIndex = -1;
		for (int i = 0; i < this.startRangeArr.length; i++) {
			if (this.startRangeArr[i] == chunk.getStartRange()) {
				startChunkIndex = i;
				break;
			}
		}
		if (startChunkIndex == -1) {
			System.err.println("OOPS! Something terrible happend. We can't complete your downlad.\nPlease try again.");
		}
		long prevRangeStart = this.startRangeArr[startChunkIndex];
		long currRangeStart = prevRangeStart + chunk.getSize();

		// check if we read all the data in this range
		if (currRangeStart >= this.endRangeArr[startChunkIndex]) {
			this.percentageCounter++;
			System.out.println("Downloaded " + this.percentageCounter + "%");
		}

		if (isDownloadCompleted()) {
			System.out.println("Download succeeded!");
			this.deleteMetadata();
		} else {
			this.startRangeArr[startChunkIndex] = currRangeStart;
			updateMetadata();
		}
	}

	/**
	 * This function deletes the metadata file.
	 * 
	 */
	private void deleteMetadata() {
		try {
			if (!Files.deleteIfExists(this.file.toPath())) {
				System.err.println(
						"OOPS! Something went wrong with deleting your old metadata file.\nPlease try again later.");
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println(
					"OOPS! Something went wrong with deleting your old metadata file.\nPlease try again later.");
			System.exit(1);
		}

	}

	/**
	 * This function create start range list.
	 * 
	 */
	protected List<Long> createStartRangeList() {
		List<Long> StartRangeList = new ArrayList<>();
		try {
			RandomAccessFile raf = new RandomAccessFile(this.file, "rw");
			String start;
			int i = 0;
			System.out.println("Are you null?? " + raf.readLine());
			while ((start = raf.readLine()) != null) {
				System.out.println(i+ ". " + Long.parseLong(start));
				StartRangeList.add(Long.parseLong(start));
				i++;
			}
			raf.close();
			return StartRangeList;
		} catch (Exception e) {
			System.err.println("OOPS! Something went wrong while creating start ranges list.\nPlease try again again later.");
			System.exit(1);
			return null;
		}
	}

	/**
	 * This function updates the metadata file. First, it'll try to create a new
	 * metadata file, then it will wrap the file with a random access file, and then
	 * read the start range array and append the range to a string builder. The
	 * function then will check id we read all the data in this range. Last, the
	 * function will write the info to the random access file, and rename the temp
	 * metadata file, as required.
	 * 
	 */
	private void updateMetadata() {
		try {
			File tempMetadataFile = new File(this.fileName + ".temp");

			if (!tempMetadataFile.exists()) {
				// if the file doesn't exists, we will create it
				if (!tempMetadataFile.createNewFile()) {
					System.err.println(
							"OOPS! Something went wrong with creating temp metadata file.\nPlease try again later.");
					System.exit(1);
				}
			}

			RandomAccessFile raf = new RandomAccessFile(tempMetadataFile, "rw");
			StringBuilder rangeSB = new StringBuilder();

			long startRange;
			// read the start range array
			for (int i = 0; i < startRangeArr.length; i++) {
				startRange = this.startRangeArr[i];

				// check if we read all the data in this range
				if (startRange < this.endRangeArr[i]) {
					rangeSB.append(Long.toString(startRange) + "\n");
				}
			}

			raf.writeBytes(rangeSB.toString());
			raf.close();
			renameTempMetadataFile(tempMetadataFile);
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
	}

	/**
	 * This function renames the metadata file. It will only be called if a
	 * successful write of the chunk was performed.
	 * 
	 * @param file:File
	 */
	private void renameTempMetadataFile(File file) {
		try {
			Files.move(file.toPath(), this.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			System.err.println("OOPS! Something went wrong with renaming temp metadata file.\nPlease try again later.");
		}
	}

	/**
	 * This function checks if the download proccess is completed. If the download
	 * was completed, the function will return true.
	 * 
	 */
	protected boolean isDownloadCompleted() {
		return this.percentageCounter == this.maxPercentage;
	}
}
