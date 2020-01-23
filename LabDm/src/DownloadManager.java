package LabDm.src;

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
        this.url = i_Url.get(0); //TODO:maybe delete
        this.fileSize = getFileSizeFromUrl();
        this.fileName = getFileName(i_Url.get(0).toString());
        this.threadRangeList = divideFileToRanges();
    }

    protected void startDownload() {
        int threshold = 5 * this.chunkSize; //TODO: explain why we chose 5?? is it enough?
        int optimalNumOfThreads = (int) this.fileSize / threshold;

        //if the user asked for too many threads, change to optimal num
        if (this.numOfThreads > optimalNumOfThreads) {
            this.numOfThreads = optimalNumOfThreads;
        }

        BlockingQueue<DataChunk> blockingQueue = new LinkedBlockingQueue<>();

        // TODO: file size and num of threads might not be needed in metadata
        Metadata metadata = Metadata.getMetadata(this.fileName, this.fileSize, this.numOfThreads, this.threadRangeList);
        FileWriter fileWriter = new FileWriter(metadata, blockingQueue, this.fileSize);
        Thread fileWriterThread = new Thread(fileWriter);
        ExecutorService executor = Executors.newFixedThreadPool(this.numOfThreads);

        List<Range> metadataRangeList = metadata.getRangeList();

        // if we are in resume, reCalc the ranges
        if(metadata.isResumed) {
            long totalFileSize = 0;
            //calc the total size left to download
            for (int i = 0; i < metadataRangeList.size(); i++) {
                totalFileSize += metadataRangeList.get(i).getSize();
            }

            //size for each thread to download
            long sizeForThread = totalFileSize / this.numOfThreads;
            long curRangeSize;
            List<Range> updatedRangeList = new ArrayList<>();

            Range newRange;
            Range metadataRange;
            for (int i = 0; i < numOfThreads; i++) {
                curRangeSize = sizeForThread;
                for (int j = 0; j < metadataRangeList.size(); j++) {
                    metadataRange = metadataRangeList.get(j);
                    if(metadataRange.getSize() <= curRangeSize) {
                        newRange = new Range(metadataRange.getStart(), metadataRange.getEnd());
                        updatedRangeList.add(newRange);
                    } else {
                        newRange = new Range(metadataRange.getStart(), metadataRange.getStart() + curRangeSize);
                    }
                    curRangeSize -= newRange.getSize();
                }
            }
            this.threadRangeList = updatedRangeList;
        }


        fileWriterThread.start();
        for (int i = 0; i < this.threadRangeList.size(); i++) {
            if (i < this.urlList.size()) {
                this.url = this.urlList.get(i);
            } else {
                this.url = this.urlList.get(i % this.urlList.size());
            }

            System.out.println("open conn " + this.url);
            Runnable httpRangeGetter = new HttpRangeGetter(this.url, this.threadRangeList.get(i), metadata, blockingQueue);
            executor.execute(httpRangeGetter);
        }
        executor.shutdown();
    }




    private long getFileSizeFromUrl() {
        try {
            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            int response = connection.getResponseCode();

            if(response / 100 == 2) {
                return connection.getContentLength();
            }

            return 0;

        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
            return 0;
        }

    }

//    private URL createUrl(String i_url) {
//        try{
//            URL fileUrl = new URL(i_url);
//            return fileUrl;
//        } catch (Exception e) {
//            System.err.println(e);
//            System.exit(1);
//            return null;
//        }
//    }

    private String getFileName(String i_url) {
        int fileNameIndex = i_url.lastIndexOf('/');
        //check if this is a valid address
        if (fileNameIndex > -1 && fileNameIndex < i_url.length() - 1) {
            String urlFileName = i_url.substring(fileNameIndex + 1);
            System.out.println("file name" + urlFileName);
            return urlFileName;
        } else {
            //TODO: add error and exit?????
            return null;
        }
    }



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
