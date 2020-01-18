package lab;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class FileWriter implements Runnable {
    private final BlockingQueue<Chunk> blockingQueue;
    private MetadataManager metadata;
    private String fileName;
    private int downloadPercent;
    private boolean isFileCreated;


    public FileWriter(MetadataManager i_metadata, BlockingQueue<Chunk> i_blockingQueue, String i_FileName) {
        this.blockingQueue = i_blockingQueue;
        this.metadata = i_metadata;
        this.fileName = i_FileName;
        this.downloadPercent = (this.metadata.getRangeCounter() / this.metadata.getNumOfRanges()) * 100;
        this.isFileCreated = false;
    }


    private void createFile() {
        try {
            File file = new File(this.fileName);
            if(!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

    }

    private void writeChunk() {
        try {

            if (!this.isFileCreated) {
                createFile();
                this.isFileCreated = true;
            }

            File file = new File(this.fileName);
            RandomAccessFile writer = new RandomAccessFile(file, "rw");

            while (this.downloadPercent != 100) { //TODO: maybe take from metadata??
                Chunk chunk = blockingQueue.take();
                writer.seek(chunk.getSeek());
                writer.write(chunk.getData(), 0, chunk.getSize());
                this.metadata.updateDownloadedRanges(chunk);
                writer.close();
                System.out.println("Downloaded " + this.downloadPercent + "%");  //TODO: do we need to write 0-100?
            }
        } catch (Exception e) {
            System.err.println(
                    "OOPS! Could not write the current chunk, or close the writer.\nPlease try again later." + e);
            System.exit(1);
        }
    }

    @Override
    public void run(){
        this.writeChunk();
       if(this.downloadPercent == 100 ) { //TODO: maybe take from metadata??
           this.metadata.deleteMetadata();
           System.out.println("Download succeeded");
       } else {
           System.err.println("did not download all file ranges");
       }
    }


}
