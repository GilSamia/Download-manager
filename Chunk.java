package lab;

public class Chunk {

	
    private byte[] data;
    private int size; //in bytes
    private long offset; 
    private long startRange;

    public Chunk(byte[] i_data, int i_size, long i_offset, long i_startRange) {
    	this.data = i_data; // we may want to add an if cond to copy the content of data.
    	this.size = i_size;
    	this.offset = i_offset;
    	this.startRange = i_startRange;
    }
    
    public byte[] getData() {
    	return this.data;
    }
    
    public int getSize() {
    	return this.size;
    }
    
    public long getOffset() {
    	return this.offset;
    }
    
    public long getRange() {
    	return this.startRange;
    }
}
