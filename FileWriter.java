
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class FileWriter implements Runnable {
    private Metadata metadata;
    private BlockingQueue blockingQueue;
    private boolean isFileCreated;
    private long fileSize;

    public FileWriter(Metadata i_metadata, BlockingQueue i_blockingQueue, long i_fileSize) {
        this.metadata = i_metadata;
        this.blockingQueue = i_blockingQueue;
        this.isFileCreated = false;
        this.fileSize = i_fileSize;
    }

    /**
     * This function writes the data chunks to disk.
     */
    private void writeDataChunks() {
        try{
            String fileName = this.metadata.getFileName();
            //for the first write.
            if(!this.isFileCreated) {
                this.createFile(fileName);
            }

            File file = new File(fileName);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");

            long bytesWritten = this.metadata.getBytesWritten();
            int percentage = (int) (((double) bytesWritten / this.fileSize) * 100);
            int curPercentage = percentage;

            while (this.fileSize > bytesWritten) {
                DataChunk chunk = (DataChunk) this.blockingQueue.take();
                raf.seek(chunk.getSeek());
                raf.write(chunk.getData(), 0, chunk.getSize());

                this.metadata.updateDownloadedRanges(chunk);
                bytesWritten+=chunk.getSize();
               // bytesWritten = this.metadata.getBytesWritten();
                percentage =(int) (((double) bytesWritten / this.fileSize) * 100);
                if(curPercentage != percentage) {
                    if(percentage != 100) {
                        System.out.println("bytes written: "+bytesWritten);
                        System.out.println("file size: "+this.fileSize);
                        System.out.println("Downloaded " + percentage + "%");
                    }
                    curPercentage = percentage;
                }
            }
            System.out.println("at the end:"+metadata.getBytesWritten());
            if (this.fileSize == bytesWritten && percentage == 100) {
                System.out.println("Finished downloading");
                this.metadata.deleteMetadataFile();
                this.isFileCreated = true;
                System.out.println("download succeeded!");
            } else {
                System.out.println("OOPS! Something went wrong. Did not write all of the bytes!");
            }
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with writing the data to disk. Please try again later.\n" + e);
            System.exit(1);
        }
    }

    /**
     * This function creates a new file if it does not exists already.
     * @param i_fileName
     */
    private void createFile(String i_fileName) {
        try {
            File file = new File(i_fileName);
            if(!file.exists()){
                file.createNewFile();
            }
        } catch (Exception e) {
            System.err.println("OOPS! Could not create a new file. Please try again later\n" + e);
            System.exit(1);
        }
    }

    @Override
    public void run(){
        writeDataChunks();
    }
}
