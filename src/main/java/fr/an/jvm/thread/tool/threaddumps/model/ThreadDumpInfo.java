package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.util.DefaultThreadDumpPrinter;

/**
 * 
 */
public class ThreadDumpInfo implements ThreadItemInfoVisitable {

    private List<String> headerComments = new ArrayList<>();

    private String timeBefore;
    private String timeAfter;
    private int dumpId;
    private long startSourceLine;
    private long endSourceLine;
    
    private List<ThreadInfo> threads = new ArrayList<>();
    
    private String dumpTitle;
    private String deadlockInfo;
    private String preDumpInfo;
    private String postDumpInfo;
    private int skipInactiveThread;

    private LinkedList<LockStandaloneInfo> standaloneLocks = new LinkedList<>();

    private ClassHistogramInfo classHistogramInfo;
    
    // -------------------------------------------------------------------------

    public ThreadDumpInfo(long startSourceLine, int dumpId, String dumpTitle) {
        this.startSourceLine = startSourceLine;
        this.dumpId = dumpId;
        this.dumpTitle = dumpTitle;
    }

    // ------------------------------------------------------------------------

    @Override
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseThreadDumpInfo(this);
    }

    public String getDeadlockInfo() {
        return deadlockInfo;
    }

    public void setDeadlockInfo(String deadlockInfo) {
        this.deadlockInfo = deadlockInfo;
    }

    public int getDumpId() {
        return dumpId;
    }

    public void setDumpId(int dumpId) {
        this.dumpId = dumpId;
    }

    public String getDumpTitle() {
        return dumpTitle;
    }

    public void setDumpTitle(String p) {
        this.dumpTitle = p;
    }

    public long getEndSourceLine() {
        return endSourceLine;
    }

    public void setEndSourceLine(long p) {
        this.endSourceLine = p;
    }

    public String getPostDumpInfo() {
        return postDumpInfo;
    }

    public void setPostDumpInfo(String p) {
        this.postDumpInfo = p;
    }

    public String getPreDumpInfo() {
        return preDumpInfo;
    }

    public void setPreDumpInfo(String p) {
        this.preDumpInfo = p;
    }

    public LinkedList<LockStandaloneInfo> getStandaloneLocks() {
        return standaloneLocks;
    }

    public void setStandaloneLocks(LinkedList<LockStandaloneInfo> p) {
        this.standaloneLocks = p;
    }

    public void addStandaloneLock(LockStandaloneInfo standaloneLock) {
        if (standaloneLocks == null) this.standaloneLocks = new LinkedList<>();
        standaloneLocks.add(standaloneLock);
    }

    public void addAllStandaloneLock(Collection<LockStandaloneInfo> p) {
        if (standaloneLocks == null) this.standaloneLocks = new LinkedList<>();
        standaloneLocks.addAll(p);
    }
    
    public long getStartSourceLine() {
        return startSourceLine;
    }

    public void setStartSourceLine(long p) {
        this.startSourceLine = p;
    }

    public List<ThreadInfo> getThreads() {
        return threads;
    }

    public void setThreads(List<ThreadInfo> p) {
        this.threads = p;
    }

    public void addThread(ThreadInfo p) {
        if (threads == null) threads = new ArrayList<>();
        threads.add(p);
    }

    public String getTimeAfter() {
        return timeAfter;
    }

    public void setTimeAfter(String p) {
        this.timeAfter = p;
    }

    public String getTimeBefore() {
        return timeBefore;
    }

    public void setTimeBefore(String p) {
        this.timeBefore = p;
    }
    
    public int getSkipInactiveThread() {
        return skipInactiveThread;
    }

    public void setSkipInactiveThread(int p) {
        this.skipInactiveThread = p;
    }

    public ClassHistogramInfo getClassHistogramInfo() {
        return classHistogramInfo;
    }

    public void setClassHistogramInfo(ClassHistogramInfo p) {
        this.classHistogramInfo = p;
    }


    public List<String> getHeaderComments() {
        return headerComments;
    }    
    
    public void addHeaderComment(String p) {
        headerComments.add(p);
    }

    public void addAllHeaderComments(Collection<String> p) {
        headerComments.addAll(p);
    }

    @Override
    public String toString() {
        return DefaultThreadDumpPrinter.print(this);
    }
    
    
}