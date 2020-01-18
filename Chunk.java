package lab;
// the chunk holds the data read from the file, the size of that data, where in the file this data should be written,
// and what was the start of that range

public class Chunk {
    private byte[] data;
    private int size; //in bytes
    private long seek; //location in file
    private long startRange;

    public Chunk(byte[] i_Data, int i_Size, long i_Seek, long i_StartRange) {
        this.data = i_Data;
        this.size = i_Size;
        this.seek = i_Seek;
        this.startRange = i_StartRange;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSize() {
        return this.size;
    }

    public long getSeek() {
        return this.seek;
    }

    public long getStartRange() {
        return this.startRange;
    }
}
