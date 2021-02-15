package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.util.DefaultThreadDumpPrinter;

public class ThreadInfo implements ThreadItemInfoVisitable {

    private String name;
    private String priority;
    private String threadId;
    private String state;
    private boolean daemon;

    private List<MethodThreadLineInfo> stack = new ArrayList<>();

    // -------------------------------------------------------------------------

    public ThreadInfo() {
    }

    // ------------------------------------------------------------------------

    @Override
    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseThreadInfo(this);
    }

    // -------------------------------------------------------------------------

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean p) {
        this.daemon = p;
    }

    public String getName() {
        return name;
    }

    public void setName(String p) {
        this.name = p;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String p) {
        this.priority = p;
    }

    public String getState() {
        return state;
    }

    public void setState(String p) {
        this.state = p;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String p) {
        this.threadId = p;
    }

    public List<MethodThreadLineInfo> getStack() {
        return stack;
    }

    public List<MethodThreadLineInfo> getStackDisplay() {
        List<MethodThreadLineInfo> res = new ArrayList<>(stack);
        Collections.reverse(res);
        return res;
    }

    public MethodThreadLineInfo getStackTop() {
        if (stack == null || stack.size() == 0)
            return null;
        int size = stack.size();
        return stack.get(size - 1);
    }

    public MethodThreadLineInfo getStackNthTopEntry(int index) {
        if (stack == null || index >= stack.size())
            return null;
        int size = stack.size();
        return stack.get(size - 1 - index);
    }

    public MethodThreadLineInfo getStackBottomEntry() {
        if (stack == null || stack.size() == 0)
            return null;
        return stack.get(0);
    }

    public MethodThreadLineInfo getStackNthBottomEntry(int index) {
        if (stack == null || index >= stack.size())
            return null;
        return stack.get(index);
    }

    // public void setStack(List<MethodThreadLineInfo> p) {
    // this.stack = p;
    // }

    public void addLineStack(MethodThreadLineInfo p) {
        if (stack == null)
            this.stack = new ArrayList<>();
        stack.add(0, p);
    }

    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return DefaultThreadDumpPrinter.print(this);
//        return "\"" + name + "\" pri:" + priority + " threadID:" + threadId + " state:\"" + state + "\" daemon:"
//                + (daemon ? "yes" : "no");
    }

}
