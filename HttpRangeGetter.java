package lab;

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

    /**
     *This function download a range of data. It opens a connection, and reads all the bytes to read
     * in the given range. Then, the function will put the chunk data in the blocking queue.
     */
    public void downloadRange() {
        try {
            long threadRangeStart = this.threadRange.getStart();
            long threadRangeEnd = this.threadRange.getEnd();
            System.out.println("start downloading range (" + threadRangeStart + "-" + threadRangeEnd + ") from:" );

            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            connection.setRequestProperty("Range", "bytes=" + threadRangeStart + "-" + threadRangeEnd);
            connection.connect();

            int response = connection.getResponseCode();

            //The response is between 200 ok to 299 meaning the connection succeeded.
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
            }
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with downloading the Range data and putting the data chunks in the blocking queue.\n" + e);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        this.downloadRange();

    }
}
