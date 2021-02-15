package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.HashMap;



/**
 * 
 */
public class AttributeMapThreadLineInfo implements ThreadLineInfo {

    private HashMap<String,String> attributes = new HashMap<>(); 
    
    // -------------------------------------------------------------------------

    public AttributeMapThreadLineInfo() {
    }

    // ------------------------------------------------------------------------

    @Override
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseAttributeMapThreadLineInfo(this);
    }
    
    public HashMap<String, String> getAttributes() {
        return attributes;
    }
    public void put(String key, String value) {
        attributes.put(key, value);
    }

    public int size() {
        return attributes.size();
    }
    
}