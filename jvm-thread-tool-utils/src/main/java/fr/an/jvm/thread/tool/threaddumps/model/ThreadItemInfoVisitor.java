package fr.an.jvm.thread.tool.threaddumps.model;



/**
 * Visitor design pattern for ThreadLineInfoVisitable sub-class hierarcy
 */
public interface ThreadItemInfoVisitor {

    void caseThreadDumpList(ThreadDumpList list);

    void caseThreadDumpInfo(ThreadDumpInfo list);
    
    void caseThreadInfo(ThreadInfo info);

    void caseMethodThreadLineInfo(MethodThreadLineInfo info);

    void caseAttributeMapThreadLineInfo(AttributeMapThreadLineInfo info);
    
    void caseLockThreadLineInfo(LockThreadLineInfo info);
    void caseEliminatedThreadLineInfo(EliminatedThreadLineInfo info);
    
    void caseLockStandaloneInfo(LockStandaloneInfo info);

    void caseClassHistogramInfo(ClassHistogramInfo classHistogram);
    void caseClassHistogramItemInfo(ClassHistogramItemInfo classHistogram);


}
