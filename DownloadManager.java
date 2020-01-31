
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloadManager {
    private long fileSize;
    public static final int chunkSize = 1024 * 4; //1 chunk = 4kb
    private int numOfThreads;
    private List<URL> urlList;
    private URL url;
    private String fileName;
    private List<Range> threadRangeList;

    public DownloadManager(List<URL> i_Url, int i_NumOfThreads) {
        this.numOfThreads = i_NumOfThreads;
        this.urlList = i_Url;
        this.url = i_Url.get(0);
        this.fileSize = getFileSizeFromUrl();
        this.fileName = getFileName(i_Url.get(0).toString());
        this.threadRangeList = divideFileToRanges();
    }

    /**
     * this function manages the downloading. is calculates a optimal number of threads as a function of chunk size
     * and creates threads accordingly. The function creates blocking queue, metadata file and a file writer..
     *
     * This function is also in charge to resume download if it was interrupted.
     */
    protected void startDownload() {
        int threshold = 5 * this.chunkSize; //threshold is a multiplication of the chunk size.
        int optimalNumOfThreads = (int) this.fileSize / threshold;

        //if the user asked for too many threads, change to optimal num
        if (this.numOfThreads > optimalNumOfThreads) {
            this.numOfThreads = optimalNumOfThreads;
        }
        BlockingQueue<DataChunk> blockingQueue = new LinkedBlockingQueue<>();

        // TODO: file size and num of threads might not be needed in metadata
        //I think this is helpful... I let you decide..:)
        Metadata metadata = Metadata.getMetadata(this.fileName, this.fileSize, this.numOfThreads, this.threadRangeList);
        FileWriter fileWriter = new FileWriter(metadata, blockingQueue, this.fileSize);
        Thread fileWriterThread = new Thread(fileWriter);
        ExecutorService executor = Executors.newFixedThreadPool(this.numOfThreads);

        List<Range> metadataRangeList = metadata.getRangeList();

        // if we are in resume, reCalc the ranges
        if(metadata.isResumed) {
            //System.out.println("resumed!");
            metadataRangeList = metadata.rangeList;
            //calc the total size left to download
            long totalFileSize = this.fileSize - metadata.getBytesWritten();

            //size for each thread to download
            long sizeForThread = (long) Math.ceil(totalFileSize / this.numOfThreads);
            long curRangeSize;
            Range newRange;
            Range metadataRange;
            List<Range> updatedRangeList = new ArrayList<>();

            for (int i = 0; i < metadataRangeList.size(); i++) {
                metadataRange = metadataRangeList.get(i);
                curRangeSize = metadataRange.getSize();

                while (curRangeSize > 0) {
                    if(sizeForThread >= curRangeSize) {
                        newRange = new Range(metadataRange.getEnd() - curRangeSize + 1, metadataRange.getEnd());
                    } else {
                        newRange = new Range(metadataRange.getEnd() - curRangeSize + 1, metadataRange.getEnd() - curRangeSize + sizeForThread);
                    }
                    updatedRangeList.add(newRange);
                    curRangeSize -= sizeForThread;
                }

            }

            this.threadRangeList = updatedRangeList;

        }

        metadata.setRangeList(this.threadRangeList);
        fileWriterThread.start();
        for (int i = 0; i < this.threadRangeList.size(); i++) {
            this.url = this.urlList.get(i % this.urlList.size());
            Runnable httpRangeGetter = new HttpRangeGetter(this.url, this.threadRangeList.get(i), metadata, blockingQueue, i);
            executor.execute(httpRangeGetter);
        }
        executor.shutdown();
    }

    /**
     * This function creates HTTP URL connection and get the connection length
     * @return connectionLength
     */
    private long getFileSizeFromUrl() {
        long connectionLength = 0;
        try {
            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            int response = connection.getResponseCode();

            //The response is between 200 ok to 299 meaning the connection succeeded.
            if(response / 100 == 2) {
                return connection.getContentLength();
            }
            return connectionLength;
        } catch (Exception e) {
            System.err.println("OOPS! Could not open Http URL Connections. Please try again later.\n" + e);
            System.exit(1);
            return connectionLength;
        }
    }

    /**
     * This function get the file name from the given URL.
     * @param i_url: string of the URL
     * @return
     */
    private String getFileName(String i_url) {
        String urlFileName = null;
        int fileNameIndex = i_url.lastIndexOf('/');
        //check if this is a valid address
        if (fileNameIndex > -1 && fileNameIndex < i_url.length() - 1) {
            urlFileName = i_url.substring(fileNameIndex + 1);
            System.out.println("file name" + urlFileName);
            return urlFileName;
        } else {
            System.err.println("OOPS! Could not create file name from the given URL.\n");
            System.exit(1);
            return urlFileName;
        }
    }

    /**
     * This function devides the file into ranges. each thread will get a range to download.
     * @return threadRangeList
     */
    private List<Range> divideFileToRanges() {
        // divide the file size into ranges for the thread - this is based on original file size
        List<Range> threadRangeList = new ArrayList<>();
        Range threadRange;
        long startRange;
        long endRange;
        long rangeSize = this.fileSize / this.numOfThreads;
        for (int i = 0; i < this.numOfThreads; i++) {
            startRange = i * rangeSize;
            endRange = startRange + rangeSize - 1;

            //if the last thread has smaller range than the others
            if (i == this.numOfThreads - 1) {
                endRange = this.fileSize;
            }
            threadRange = new Range(startRange, endRange);
            threadRangeList.add(threadRange);
        }
        return threadRangeList;
    }
}