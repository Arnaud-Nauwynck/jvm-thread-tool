package fr.an.jvm.thread.tool.threaddumps.model;


/**
 * 
 */
public class LockStandaloneInfo extends LockThreadLineInfo {

    private String threadId;
    private boolean threadResolved;

    // -------------------------------------------------------------------------

    public LockStandaloneInfo() {
        threadResolved = false;
    }

    // -------------------------------------------------------------------------

    @Override
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseLockStandaloneInfo(this);
    }

    // ------------------------------------------------------------------------

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String p) {
        this.threadId = p;
    }

    public boolean isThreadResolved() {
        return threadResolved;
    }

    public void setThreadResolved(boolean p) {
        this.threadResolved = p;
    }

    
}
