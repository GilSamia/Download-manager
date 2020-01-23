package LabDm.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Metadata {
    private List<Range> rangeList;
    private String fileName;
    private long bytesWritten;
    private int numOfThreads;
    private long fileSize;
    public boolean isResumed;

    public Metadata(String i_fileName, long i_fileSize, int i_numOfThreads, List<Range> i_threadRangeList){
        this.rangeList = i_threadRangeList;
        this.bytesWritten = 0;
        this.fileSize = i_fileSize;
        this.fileName = i_fileName;
        this.numOfThreads = i_numOfThreads;
        this.isResumed = false;
    }

    public static Metadata getMetadata(String i_fileName, long i_fileSize, int i_numOfThreads, List<Range> i_threadRangeList) {
        Metadata metadata;
        File metadataFile = new File(i_fileName + ".metadata");

        //check if we need to create new metadata object
        if (!metadataFile.exists()) {
            metadata = new Metadata(i_fileName, i_fileSize, i_numOfThreads, i_threadRangeList);
            try {
                //create the metadata file
                metadataFile.createNewFile();

                //create the temp file too
                File tempMetadata = new File(i_fileName + ".metadata.temp");
                if(!tempMetadata.exists()) {
                    tempMetadata.createNewFile();
                }

            } catch (Exception e) {
                // TODO: error handling
            }

        //if the metadata exists
        } else {
            metadata = readMetadata(i_fileName + ".metadata");
            metadata.isResumed = true;
        }

        return metadata;
    }

    private static Metadata readMetadata(String i_path) {
        try {
            Metadata metadata;
            FileInputStream fileInputStream = new FileInputStream(i_path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            metadata = (Metadata) objectInputStream.readObject();
            objectInputStream.close();
            return metadata;
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
            return null;
        }
    }


    private void writeToMetadata() {
        String tempPath = this.fileName + ".metadata.temp";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(tempPath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            renameTempMetadataFile();
//            this.rangeCounter++;
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with creating temp file. Please try again later." + e);
            System.exit(1);
        }

    }


    private void renameTempMetadataFile() {
        try {
            File tempFile = new File(this.fileName + ".metadata.temp");
            File curMetadataFile = new File(this.fileName + ".metadata");
            Files.move(Paths.get(tempFile.getAbsolutePath()), Paths.get(curMetadataFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("OOPS! Something went wrong with renaming temp metadata file.\nPlease try again later." + e);
        }
    }


    protected void updateDownloadedRanges(DataChunk i_chunk) {
        Range metadataRange;
        Range chunkRange = i_chunk.getRange();
        long chunkStart = chunkRange.getStart();
        long chunkEnd = chunkRange.getEnd();
        long metadataRangeStart;
        long metadataRangeEnd;
        for (int i = 0; i < this.rangeList.size(); i++) {
            metadataRange = this.rangeList.get(i);
            metadataRangeStart = metadataRange.getStart();
            metadataRangeEnd = metadataRange.getEnd();

            if(chunkStart >= metadataRangeStart && chunkEnd <= metadataRangeEnd) {
                Range updatedRange = new Range(metadataRangeStart + i_chunk.getSize(), metadataRangeEnd);

                if(chunkStart == metadataRangeStart) {
                    this.rangeList.set(i, updatedRange);
                } else {
                    Range updatedRange1 = new Range(metadataRangeStart, chunkStart - 1);
                    Range updatedRange2 = new Range(chunkEnd + 1, metadataRangeEnd);
                    this.rangeList.remove(i);
                    this.rangeList.add(updatedRange1);
                    this.rangeList.add(updatedRange2);
                }

                if (updatedRange.getSize() < 1) {
                    this.rangeList.remove(i);
                }

            }
        }
        this.bytesWritten += i_chunk.getSize();
    }





// is it still needed??
    private List<Range> initRangeList() {
        List<Range> rangeList = new ArrayList<>();
        Range range;
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

            range = new Range(startRange, endRange);
            rangeList.add(range);
        }

        return rangeList;
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

}
