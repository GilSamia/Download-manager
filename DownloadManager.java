package lab;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloadManager {
    private final String stringUrl;
    private int numOfThreads;
    public static final int chunkSize = 4096;  //1 chunk = 4KB
    private long fileSize;
    private String fileName;


    public DownloadManager(String i_Url, int i_NumOfThreads) {
        this.stringUrl = i_Url;
        this.fileSize = getFileSizeFromUrl(i_Url);
        this.numOfThreads = i_NumOfThreads;
        this.fileName = createFileName(i_Url);
    }


    /**
     *
     */
    protected void startDownload() {
    	System.out.println("File size: " + this.fileSize);

        int threshold = 5 * this.chunkSize; //TODO: explain why we chose 5?? is it enough?
        int optimalNumOfThreads = (int) this.fileSize / threshold;

        //if the user asked for too many threads, change to optimal num
        if (this.numOfThreads > optimalNumOfThreads) {
            this.numOfThreads = optimalNumOfThreads;
        }

        int numOfRanges = (int) Math.ceil(this.fileSize / this.chunkSize);
        MetadataManager metadataManager = MetadataManager.getMetadata(numOfRanges, this.fileName);
        BlockingQueue<Chunk> blockingQueue = new LinkedBlockingQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(this.numOfThreads);
        FileWriter fileWriter = new FileWriter(metadataManager, blockingQueue, this.fileName);
        Thread threadFileWriter = new Thread(fileWriter);
        threadFileWriter.start();

        long startRange;
        long endRange;
        long rangeSize = this.fileSize / this.numOfThreads;
        for (int i = 0; i < this.numOfThreads; i++) {
            startRange = i * rangeSize;
            endRange = startRange + rangeSize;

            //if the last thread has smaller range than the others
            if (i == this.numOfThreads - 1) {
                endRange = this.fileSize;
            }

            URL url = urlBuilder(this.stringUrl);
            Runnable runnable = new HttpRangeGetter(url, startRange, endRange , blockingQueue, metadataManager);
            executorService.execute(runnable);
        }

        executorService.shutdown();
    }


    /**
     *
     * @param i_Url
     * @return URL created by the given string. null in case of error
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
     * This function returns long file size for the given URL.
     * @param i_Url
     * @return the file size from the given URL
     */
    private long getFileSizeFromUrl(String i_Url) {
        try {
            URL url = new URL(i_Url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int response = connection.getResponseCode();
            //TODO: 201 202...??????
            if(response == 200) {
                return connection.getContentLength();
            }
            return 0;

        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
            return 0;
        }

    }


    private String createFileName(String i_Url) {
        int fileNameIndex = i_Url.lastIndexOf('/');
        //check if this is a valid address
        if (fileNameIndex > -1 && fileNameIndex < i_Url.length() - 1) {
            String urlFileName = i_Url.substring(fileNameIndex + 1);
            return urlFileName;
        } else {
            //TODO: add error and exit?????
            return null;
        }
    }


}
