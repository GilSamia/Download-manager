package lab;

// this object will keep a list of indices (start of ranges) left to read.
//after a chunk is read and written that chunk's range will be deleted from the list and
// the metadata file will be updated.

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;

public class MetadataManager {
    private final String fileName;
    private String destPath;
    private final int numOfRanges;
    private List<Long> startRangeList;
    private int rangeCounter;
//    private long fileSize;
//    private File file;
//    private final int maxPercentage = 100;
//    private int percentageCounter = 0;

    public MetadataManager(int i_NumOfRanges, String i_FileName) {
        this.fileName = i_FileName + ".metadata";
        this.destPath = createMetadataPath(this.fileName);
//        this.file = createMetadataFile();
        this.startRangeList = createStartRangeList(i_NumOfRanges);
        this.numOfRanges = i_NumOfRanges;
        this.rangeCounter = 0;
    }

//    public String getFileName() {
//        return this.fileName;
//    }
//
//    public long getFileSize() {
//        return this.fileSize;
//    }
//
//    public File getFile() {
//        return this.file;
//    }
//
    public List<Long> getStartRangeList() {
        return this.startRangeList;
    }

    public int getRangeCounter() {
        return this.rangeCounter;
    }

    public int getNumOfRanges() {
        return this.numOfRanges;
    }



    /**
     *
     * @param i_NumOfRanges
     * @return a list of all ranges start indices
     */
    private static List<Long> createStartRangeList(long i_NumOfRanges) {
        List<Long> rangeList = new ArrayList<>();

        for (int i = 0; i < i_NumOfRanges; i++) {
            long start = (long) i * DownloadManager.chunkSize;
            rangeList.add(start);
        }

        return rangeList;
    }


    /**
     *
     * @param fileName
     * @return
     */
    private static String createMetadataPath(String fileName) {
        String path = "downloads" + System.getProperty("file.separator") + fileName;
        return path;
    }


    /**
     *
     * @param i_NumOfRanges
     * @param i_MetadataFileName
     * @return
     */
    protected static MetadataManager getMetadata(int i_NumOfRanges, String i_MetadataFileName) {
        MetadataManager metadata;
        String path = createMetadataPath(i_MetadataFileName);
        File metadataFile = new File(path).getAbsoluteFile();

        // check if we need to create a new metadata object
        if (!metadataFile.exists()) {
            metadata = new MetadataManager(i_NumOfRanges, i_MetadataFileName);
        } else {
            //the metadata object exists so we just need to read it
            metadata = readMetadata(path);
        }
        return metadata;

    }


    /**
     *
     * @param i_Path
     * @return the saved metadata object.
     */
    private static MetadataManager readMetadata(String i_Path) {
        try {
            MetadataManager metadata;
            FileInputStream fileInputStream = new FileInputStream(i_Path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            metadata = (MetadataManager) objectInputStream.readObject();
            return metadata;
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
            return null;
        }
    }


    /**
     *
     */
    private void writeToMetadata() {
        String tempPath = this.destPath + ".temp";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(tempPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            renameTempMetadataFile(tempPath);
            this.rangeCounter++;


        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

    }


    /**
     *
     * @param i_Path
     */
    private void renameTempMetadataFile(String i_Path) {
        try {
            File tempFile = new File(i_Path);
            File curMetadataFile = new File(this.destPath).getAbsoluteFile();

            Files.move(tempFile.toPath(), curMetadataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with renaming temp metadata file.\nPlease try again later.");
        }
    }



    /**
     * This function deletes the metadata file.
     *
     */
    protected void deleteMetadata() {
        try {
            File metadataFile = new File(this.destPath);
            metadataFile.delete();
        } catch (Exception e) {
            System.err.println(
                    "OOPS! Something went wrong with deleting your old metadata file.\nPlease try again later.");
            System.exit(1);
        }

    }

    protected void updateDownloadedRanges(Chunk chunk) {
        this.startRangeList.remove(chunk.getStartRange());
        writeToMetadata();
    }



}
