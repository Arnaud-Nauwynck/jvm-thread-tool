package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ClassHistogramInfo implements ThreadItemInfoVisitable {

    private int totalInstanceCount;
    private long totalInstanceByteSize;
    
    private List<ClassHistogramItemInfo> classItems = new ArrayList<>();
    
    // -------------------------------------------------------------------------

    public ClassHistogramInfo() {
    }
    
    public ClassHistogramInfo(int totalInstanceCount, long totalInstanceByteSize, List<ClassHistogramItemInfo> classItems) {
        super();
        this.totalInstanceCount = totalInstanceCount;
        this.totalInstanceByteSize = totalInstanceByteSize;
        this.classItems.addAll(classItems);
    }
    
    //-------------------------------------------------------------------------

    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseClassHistogramInfo(this);        
    }

    
    public int getTotalInstanceCount() {
        return totalInstanceCount;
    }

    public long getTotalInstanceByteSize() {
        return totalInstanceByteSize;
    }

    public List<ClassHistogramItemInfo> getClassItems() {
        return classItems;
    }
    
    public ClassHistogramItemInfo findByClassName(String className) {
        ClassHistogramItemInfo res = null;
        for (ClassHistogramItemInfo elt : classItems) { // OPTIM? not efficient
            if (elt.getClassName().equals(className)) {
                res = elt;
                break;
            }
        }
        return res;
    }
    
}
