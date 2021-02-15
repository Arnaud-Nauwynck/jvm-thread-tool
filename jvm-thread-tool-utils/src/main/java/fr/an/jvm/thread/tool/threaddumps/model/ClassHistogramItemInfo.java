package fr.an.jvm.thread.tool.threaddumps.model;

/**
 * 
 */
public class ClassHistogramItemInfo implements ThreadItemInfoVisitable {

    private int rank;
    private int instanceCount;
    private long instanceByteSize;
    private String className;
    
    // ------------------------------------------------------------------------

    public ClassHistogramItemInfo() {
    }
    
    public ClassHistogramItemInfo(int rank, int instanceCount, long instanceByteSize, String className) {
        super();
        this.rank = rank;
        this.instanceCount = instanceCount;
        this.instanceByteSize = instanceByteSize;
        this.className = className;
    }

    // -------------------------------------------------------------------------

    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseClassHistogramItemInfo(this);        
    }

    // ------------------------------------------------------------------------

    public int getRank() {
        return rank;
    }
    
    public int getInstanceCount() {
        return instanceCount;
    }

    public long getInstanceByteSize() {
        return instanceByteSize;
    }

    public String getClassName() {
        return className;
    }

    public String toString() {
        return "ClassHistogramItemInfo["
            + "rank=" + rank
            + ", count=" + instanceCount
            + ", size=" + instanceByteSize
            + ", className=" + className
            + "]";
    }
    
}

