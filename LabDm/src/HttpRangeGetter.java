package LabDm.src;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class HttpRangeGetter implements Runnable {
    private Metadata metadata;
    private BlockingQueue blockingQueue;
    private URL url;
    private Range threadRange;

    public HttpRangeGetter(URL i_url, Range i_range, Metadata i_metadata, BlockingQueue i_blockingQueue) {
        this.metadata = i_metadata;
        this.blockingQueue = i_blockingQueue;
        this.url = i_url;
        this.threadRange = i_range;
    }
    

    public void downloadRange() {
        try {
            long threadRangeStart = this.threadRange.getStart();
            long threadRangeEnd = this.threadRange.getEnd();
            System.out.println("downloading range" + threadRangeStart + "-" + threadRangeEnd); //TODO: print like in example

            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            connection.setRequestProperty("Range", "bytes=" + threadRangeStart + "-" + threadRangeEnd);
            connection.connect();

            int response = connection.getResponseCode();
            if(response / 100 == 2) {
                InputStream inputStream = connection.getInputStream();

                int chunkSize = DownloadManager.chunkSize;
                long bytesRead = 0;
                long bytesToRead = threadRangeEnd - threadRangeStart + 1;

               // while there are bytes left to read in this range
                while (bytesRead < bytesToRead) {
                    byte [] data = new byte[chunkSize];
                    long seekPosition = threadRangeStart + bytesRead;

                    //read the data to chunkData and returns the size of data in bytes.
                    int sizeRead = inputStream.read(data);

                    // this is the end of file
                    if(sizeRead == -1) {
                        break;
                    }

                    bytesRead += sizeRead;
                    DataChunk dataChunk = new DataChunk(data, sizeRead, seekPosition, this.threadRange);
                    this.blockingQueue.put(dataChunk);
                }













             //   List<Range> rangeList = this.metadata.getRangeList();

//                for (int i = 0; i < rangeList.size(); i++) {
//                    Range metadataRange = rangeList.get(i);
//                    long metadataRangeStart = metadataRange.getStart();
//
//                    // if the range of this thread matches the ranges in metadata
//                    if (metadataRangeStart >= threadRangeStart && metadataRangeStart <= threadRangeEnd) {
//                        int chunkSize = DownloadManager.chunkSize;
//                        long bytesRead = 0;
//                        long bytesToRead = threadRangeEnd - metadataRangeStart + 1;
//
//                        //while there are bytes left to read in this range
//                        while (bytesRead < bytesToRead) {
//                            byte [] data = new byte[chunkSize];
//                            long seekPosition = threadRangeStart + bytesRead;
//                            //read the data to chunkData and returns the size of data in bytes.
//                            int sizeRead = inputStream.read(data);
//
//                            // this is the end of file
//                            if(sizeRead == -1) {
//                                break;
//                            }
//
//                            bytesRead += sizeRead;
//                            DataChunk dataChunk = new DataChunk(data, sizeRead, seekPosition, this.threadRange);
//                            this.blockingQueue.put(dataChunk);
//                        }
//                    }
//
//                }


            }


        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

    }



    @Override
    public void run() {
        this.downloadRange();

    }
}
