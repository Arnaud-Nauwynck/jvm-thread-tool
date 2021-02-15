package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.List;

public class DefaultThreadItemInfoVisitor implements ThreadItemInfoVisitor {

    @Override
    public void caseThreadDumpList(ThreadDumpList p) {
        for (ThreadDumpInfo elt : p.getThreadDumps()) {
            elt.visit(this);
        }
    }

    @Override
    public void caseThreadDumpInfo(ThreadDumpInfo p) {
        for (ThreadInfo elt : p.getThreads()) {
            elt.visit(this);
        }
    }

    @Override
    public void caseThreadInfo(ThreadInfo p) {
        for (MethodThreadLineInfo elt : p.getStack()) {
            elt.visit(this);
        }
    }

    @Override
    public void caseMethodThreadLineInfo(MethodThreadLineInfo p) {
        List<ThreadLineInfo> infos = p.getInfos();
        if (infos != null && infos.size() != 0) {
            for (ThreadLineInfo elt : infos) {
                elt.visit(this);
            }
        }
    }

    @Override
    public void caseAttributeMapThreadLineInfo(AttributeMapThreadLineInfo info) {
    }

    @Override
    public void caseLockStandaloneInfo(LockStandaloneInfo info) {
    }

    @Override
    public void caseLockThreadLineInfo(LockThreadLineInfo info) {
    }

    @Override
    public void caseEliminatedThreadLineInfo(EliminatedThreadLineInfo info) {
    }
    
    @Override
    public void caseClassHistogramInfo(ClassHistogramInfo classHistogram) {
    }

    @Override
    public void caseClassHistogramItemInfo(ClassHistogramItemInfo classHistogram) {
    }

}
