package lab;

import java.io.Serializable;

public class Range implements Serializable {
    private long start;
    private long end;

    public Range(long i_Start, long i_End) {
        this.start = i_Start;
        this.end = i_End;
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public long getSize() {return this.end - this.start + 1;}
}