package lab;

// this object will keep a list of indices (start of ranges) left to read.
//after a chunk is read and written that chunk's range will be deleted from the list and
// the metadata file will be updated.

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;

public class MetadataManager implements Serializable {
    private final String fileName;
    private static String destPath;
    private final int numOfRanges;
    private List<Long> startRangeList;
    private int rangeCounter;
//    private long fileSize;
//    private File file;
//    private final int maxPercentage = 100;
//    private int percentageCounter = 0;

    public MetadataManager(int i_NumOfRanges, String i_FileName) {
        this.fileName = i_FileName + ".metadata";
        MetadataManager.destPath = createMetadataPath(this.fileName);
//        this.file = createMetadataFile();
        this.startRangeList = createStartRangeList(i_NumOfRanges);
        this.numOfRanges = i_NumOfRanges;
        this.rangeCounter = 0;
    }

    public List<Long> getStartRangeList() {
        return this.startRangeList;
    }

    public int getRangeCounter() {
        return this.rangeCounter;
    }

    public int getNumOfRanges() {
        return this.numOfRanges;
    }

    public static String getDestPath(){
    	return MetadataManager.destPath;
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
        String path = fileName;
        return path;
    }


    /**
     *
     * @param i_NumOfRanges
     * @param i_MetadataFileName
     * @return
     */
    protected static  MetadataManager getMetadata(int i_NumOfRanges, String i_MetadataFileName) {
        MetadataManager metadata;
        String path = createMetadataPath(i_MetadataFileName) + ".metadata";
        System.out.println("Before creating the file");
        File metadataFile = new File(i_MetadataFileName + ".metadata");

        // check if we need to create a new metadata object
        if (!metadataFile.exists()) {
        	System.out.println("In the if");

            metadata = new MetadataManager(i_NumOfRanges, i_MetadataFileName);
			try {
				metadataFile.createNewFile();
			} catch (IOException e) {
				System.err.println("OOPS! Could not create metadata file" + e);
			}
            
            String tempPath = getDestPath() + ".temp";
        	File tempFile = new File(tempPath);
			try {
				tempFile.createNewFile();
			} catch (IOException e) {
				System.err.println("OOPS! Could not create metadata TEMP file" + e);
			}
        } 
        
        //the metadata object exists so we just need to read it
        else {
        	System.out.println("In the else");
            metadata = readMetadata(path);
        }
        System.out.println("After creating the file");
        return metadata;
    }

     /**
     *
     * @param i_Path
     * @return the saved metadata object.
     */
    private static MetadataManager readMetadata(String i_Path) {
        try {
        	System.out.println("Read metadata file method");
            MetadataManager metadata;
            FileInputStream fileInputStream = new FileInputStream(i_Path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            System.out.println("the stream was created");
            
            metadata = (MetadataManager) objectInputStream.readObject();
            System.out.println("Read!!!! metadata: " + metadata);
            objectInputStream.close();
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
        System.out.println("tempPath: " + tempPath);
        try {        	
            FileOutputStream fileOutputStream = new FileOutputStream(tempPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            System.out.println("after writing the metadata");
            renameTempMetadataFile(tempPath);
            this.rangeCounter++;
            System.out.println("after renaming the metadata, rangeConter = " + this.rangeCounter);
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with creating temp file. Please try again later." + e);
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
            System.out.println("Path in the renaming mathod i_Path: " + i_Path);
            File curMetadataFile = new File(MetadataManager.destPath);
            Files.move(Paths.get(tempFile.getAbsolutePath()), Paths.get(curMetadataFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with renaming temp metadata file.\nPlease try again later." + e);
        }
    }



    /**
     * This function deletes the metadata file.
     *
     */
    protected void deleteMetadata() {
        try {
        	System.out.println("in delete");
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
