package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.ArrayList;
import java.util.List;

/**
 * helper class to present Class Histo by transposing => line=classes/column=dump
 */
public class ClassHistoDiffList {

    private String className;
    private List<Long> values = new ArrayList<Long>();
    
    // ------------------------------------------------------------------------

    public ClassHistoDiffList(String className) {
        super();
        this.className = className;
    }

    public boolean isNegligibleDiffList(long maxValue, int maxCount) {
        boolean res = true;
        int count = 0;
        int size = values.size();
        int index = 0;
        for(Long value : values) {
            if (value.longValue() > maxValue) {
                if (index == size-1) {
                    res = false;
                    break;
                }
                count++;
                if (count > maxCount) {
                    res = false;
                    break;
                }
            }
            index++;
        }
        return res;
    }

    public String getClassName() {
        return className;
    }

    public List<Long> getValues() {
        return values;
    }
    
    public void addValue(long value) {
        values.add(Long.valueOf(value));
    }
    
}