package fr.an.jvm.thread.tool.threaddumps.model;

/**
 * 
 */
public class LockThreadLineInfo implements ThreadLineInfo {

    public static final String TYPE_LOCKED = "locked";
    public static final String TYPE_WAIT = "waiting on";
    public static final String TYPE_ENTRY = "waiting to lock";

    public static String lookupType(String s) {
        s = s.trim();
        if (s.equals(TYPE_LOCKED) || s.equals(TYPE_WAIT) || s.equals(TYPE_ENTRY))
            return s;
        else
            return s + "(unknown)";
    }

    // -------------------------------------------------------------------------

    private String id;
    private String type;
    private String className;

    // -------------------------------------------------------------------------
    
    public LockThreadLineInfo() {
    }

    
    // -------------------------------------------------------------------------

    /** implements ThreadLineInfoVisitabe */
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseLockThreadLineInfo(this);
    }

    // -------------------------------------------------------------------------
    
    public String getClassName() {
        return className;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type){
        this.type = type;
    }
    
    // -------------------------------------------------------------------------

    public String toString() {
        return className + ";" + id + " (" + type + ")";
    }

    
}