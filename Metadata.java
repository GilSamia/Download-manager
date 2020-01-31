import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Metadata implements Serializable {
    public  List<Range> rangeList;
    private String fileName;
    private long bytesWritten;
    private int numOfThreads;
    private long fileSize;
    public static boolean isResumed;

    public Metadata(String i_fileName, long i_fileSize, int i_numOfThreads, List<Range> i_threadRangeList){
        this.rangeList = i_threadRangeList;
        this.bytesWritten = 0;
        this.fileSize = i_fileSize;
        this.fileName = i_fileName;
        this.numOfThreads = i_numOfThreads;
        this.isResumed = false;
    }

    public List<Range> getRangeList() {
        return this.rangeList;
    }

    public String getFileName() {
        return  this.fileName;
    }

    public long getBytesWritten() {
        return this.bytesWritten;
    }

    /**
     * This function creates a new metadata file if it does not exists.
     * @param i_fileName
     * @param i_fileSize
     * @param i_numOfThreads
     * @param i_threadRangeList
     * @return metadata file.
     */
    public static Metadata getMetadata(String i_fileName, long i_fileSize, int i_numOfThreads, List<Range> i_threadRangeList) {
        Metadata metadata;
        File metadataFile = new File(i_fileName + ".metadata");

        //check if we need to create new metadata object
        if (!metadataFile.exists()) {
            metadata = new Metadata(i_fileName, i_fileSize, i_numOfThreads, i_threadRangeList);
            try {
                //create the metadata file
                metadataFile.createNewFile();
            } catch (Exception e) {
                System.err.println("OOPS! Something went wrong with creating the metadata file.\n" + e);
            }

        //if the metadata exists
        } else {
            metadata = readMetadata(i_fileName + ".metadata");
            metadata.isResumed = true;
        }

        return metadata;
    }

    public void setRangeList(List<Range> i_threadRangeList) {
        this.rangeList = i_threadRangeList;
    }

    /**
     * This function reads from metadata file.
     * @param i_path
     * @return metadata
     */
    private static Metadata readMetadata(String i_path) {
        Metadata metadata = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(i_path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            metadata = (Metadata) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            return metadata;
        } catch (Exception e) {
            System.err.println("OOPS! Could not read metadata file. Please try again later.\n" + e);
            System.exit(1);
            return metadata;
        }
    }

    /**
     * This function writes data to metadata file.
     */
    private void writeToMetadata() {
        String tempPath = this.fileName + ".metadata.temp";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(tempPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();
            renameTempMetadataFile();
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with writing to metadata file. Please try again later." + e);
            System.exit(1);
        }
    }

    /**
     * This function renames the metadata file after writing, from .temp to .metadata
     */
    private void renameTempMetadataFile() {
        try {
            File tempFile = new File(this.fileName + ".metadata.temp");
            File curMetadataFile = new File(this.fileName + ".metadata");
            Files.move(Paths.get(tempFile.getAbsolutePath()), Paths.get(curMetadataFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with renaming temp metadata file.\nPlease try again later." + e);
        }
    }

    /**
     * This function updates the downloaded ranges after a chunk was written. it updates the range list with the new
     * range that we still need to read.
     * @param i_chunk
     */
    protected void updateDownloadedRanges(DataChunk i_chunk) {
        int chunkRangeIndex = i_chunk.getRangeIndex();

        Range metadataRange;
        Range chunkRange = i_chunk.getRange();
        long chunkStart = i_chunk.getSeek();
        long chunkEnd = chunkStart + i_chunk.getSize() - 1;
        long metadataRangeStart;
        long metadataRangeEnd;

        metadataRange = this.rangeList.get(chunkRangeIndex);
        metadataRangeStart = metadataRange.getStart();
        metadataRangeEnd = metadataRange.getEnd();
        if (chunkStart >= metadataRangeStart && chunkEnd <= metadataRangeEnd) {
            Range updatedRange = new Range(metadataRangeStart + i_chunk.getSize(), metadataRangeEnd);

            if (chunkStart == metadataRangeStart && updatedRange.getSize() > 0) {
                this.rangeList.set(chunkRangeIndex, updatedRange);
                this.bytesWritten += i_chunk.getSize();
            }
        }
        writeToMetadata();
    }

    /**
     * This function deletes the metadata file after we are done using it.
     */
    protected void deleteMetadataFile() {
        try {
            File metadataFile = new File(this.fileName + ".metadata");
            metadataFile.delete();
        } catch (Exception e) {
            System.err.println(
                    "OOPS! Something went wrong with deleting your old metadata file.\nPlease try again later.");
            System.exit(1);
        }
    }
}
