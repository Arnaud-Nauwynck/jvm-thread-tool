package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.ArrayList;
import java.util.List;

public class ThreadDumpList implements ThreadItemInfoVisitable {

    private List<ThreadDumpInfo> threadDumps = new ArrayList<>();

    // -------------------------------------------------------------------------

    public ThreadDumpList() {
    }

    // -------------------------------------------------------------------------

    /** imlements ThreadItemInfoVisitable */
    @Override
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseThreadDumpList(this);
    }

    // -------------------------------------------------------------------------

    public List<ThreadDumpInfo> getThreadDumps() {
        return threadDumps;
    }

    public void setThreadDumps(List<ThreadDumpInfo> p) {
        this.threadDumps = p;
    }

    public void addThreadDump(ThreadDumpInfo p) {
        if (threadDumps == null) {
            this.threadDumps = new ArrayList<ThreadDumpInfo>();
        }
        threadDumps.add(p);
    }

    public int size() {
        return threadDumps.size();
    }

    public ThreadDumpInfo get(int index) {
        return threadDumps.get(index);
    }

}
