// the chunk holds the data read from the file, the size of that data, where in the file this data should be written,
// and what was the start of that range

public class DataChunk {
    private byte[] data;
    private int size; //in bytes
    private long seekPosition; //location in file
    private Range range;
    private int rangeIndex;

    public DataChunk(byte[] i_Data, int i_Size, long i_SeekPos, Range i_Range, int i_RangeIndex) {
        this.data = i_Data;
        this.size = i_Size;
        this.seekPosition = i_SeekPos;
        this.range = i_Range;
        this.rangeIndex = i_RangeIndex;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSize() {
        return this.size;
    }

    public long getSeek() {
        return this.seekPosition;
    }

    public Range getRange() {
        return this.range;
    }

    public int getRangeIndex() { return this.rangeIndex; }
}
