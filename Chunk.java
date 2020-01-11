package lab;

public class Chunk {

	
    private byte[] data;
    private int size; //in bytes
    private long offset; 
    private long range;

    public Chunk(byte[] data, int size, long offset, long start) {
    	this.data = data; // we may want to add an if cond to copy the content of data.
    	this.size = size;
    	this.offset = offset;
    	this.range = start;
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
    	return this.range;
    }
}
