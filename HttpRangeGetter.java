package lab;

// the HttpRangeGetter will get a range from the DownloadManager, open connection and
// read the given range into the chunk data.
//puts the chunk in the queue.

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class HttpRangeGetter implements Runnable {
    private final int chunkSize = 4096;  //1 chunk = 4KB
    private final URL url;
    private final Long start; // The beginning of this range
    private final Long end; // The ending of this range
    private final BlockingQueue<Chunk> blockingQueue;
    private MetadataManager metadataManager;

    public HttpRangeGetter(URL i_Url, Long i_Start, Long i_End, BlockingQueue i_BlockingQueue, MetadataManager i_MetadataManager){
        this.url = i_Url;
        this.start = i_Start;
        this.end = i_End;
        this.blockingQueue = i_BlockingQueue;
        this.metadataManager = i_MetadataManager;
        
    }

    public void rangeDownloader() {
        try {
            System.out.println("Before the connection. end = " + this.end);

        	HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            connection.setRequestProperty("Range", "bytes=" + this.start + "-" + this.end);
            connection.connect();

            //TODO: check status 201 202...?
            int response = connection.getResponseCode();
            System.out.println("response : " + response);
            if (response >= 200 && response < 300) {

                InputStream inputStream = connection.getInputStream();
        		List<Long> startRangeList = this.metadataManager.getStartRangeList();
                
        		for (int i = 0; i < startRangeList.size(); i++) {

        			long startRange = startRangeList.get(i);
        			//if the given list ranges matches the thread range then read the file
        			if (startRange <= this.end && startRange >= this.start) {

                        long bytesRead = 0;
                        long bytesToRead = chunkSize;

        				//while there are bytes left to read                
                        while (bytesRead < bytesToRead) {
                            byte [] chunkData = new byte[chunkSize];
                            long seek = this.start + bytesRead;
                            int sizeRead = inputStream.read(chunkData); //read the data to chunkData and returns the size of data in bytes.

                            // this is the end of file
                            if(sizeRead == -1) {
                                break;
                            }
                            
                            bytesRead += sizeRead;
                            Chunk chunk = new Chunk(chunkData, sizeRead, seek, this.start);
                            this.blockingQueue.put(chunk);
                        }
        			}
        		}

                inputStream.close();
                connection.disconnect();
            }
            
        } catch (Exception e){
            System.err.println(e);
            System.exit(1);
        }
    }


    @Override
    public void run(){
    	this.rangeDownloader();
    	System.out.println("HTTP Range Getter Runner");
    }
}