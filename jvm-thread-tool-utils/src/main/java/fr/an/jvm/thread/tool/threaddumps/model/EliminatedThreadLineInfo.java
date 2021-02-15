package fr.an.jvm.thread.tool.threaddumps.model;

/**
 * 
 */
public class EliminatedThreadLineInfo implements ThreadLineInfo {

    public static final String TYPE_LOCKED = "eliminated scalar";

    // -------------------------------------------------------------------------

    private String type;
    private String eliminated;
    private String className;

    // -------------------------------------------------------------------------
    
    public EliminatedThreadLineInfo() {
    }

    public EliminatedThreadLineInfo(String type, String eliminated, String className) {
        this();
        this.type = type;
        this.eliminated = eliminated;
        this.className = className;
    }



    // -------------------------------------------------------------------------

    /** implements ThreadLineInfoVisitabe */
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseEliminatedThreadLineInfo(this);
    }

    // -------------------------------------------------------------------------

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getEliminated() {
        return eliminated;
    }
    
    public void setEliminated(String eliminated) {
        this.eliminated = eliminated;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    // -------------------------------------------------------------------------

    public String toString() {
        return "eliminated <" + type + "> (a " + eliminated + ") at " + className;
    }
    
}