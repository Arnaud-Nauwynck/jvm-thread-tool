package fr.an.jvm.thread.tool.threaddumps.model;

public interface ThreadItemInfoVisitable {

    void visit(ThreadItemInfoVisitor visitor);
        
}
