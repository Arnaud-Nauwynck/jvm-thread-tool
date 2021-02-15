package fr.an.jvm.thread.tool.threaddumps.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class MethodThreadLineInfo implements ThreadLineInfo {

    private String className;
    private String methodName;
    private String locationClass;
    private String locationLineNo;

    private int skipCount;

    private List<ThreadLineInfo> infos = new ArrayList<>();

    // -------------------------------------------------------------------------

    public MethodThreadLineInfo() {
    }

    // -------------------------------------------------------------------------

    public void visit(ThreadItemInfoVisitor visitor) {
        visitor.caseMethodThreadLineInfo(this);
    }

    // ------------------------------------------------------------------------

    public String getMethodFullName() {
        return className + "." + methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLocationClass() {
        return locationClass;
    }

    public void setLocationClass(String p) {
        this.locationClass = p;
    }

    public String getLocationLineNo() {
        return locationLineNo;
    }

    public void setLocationLineNo(String p) {
        this.locationLineNo = p;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String p) {
        this.methodName = p;
    }

    public List<ThreadLineInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<ThreadLineInfo> p) {
        this.infos = p;
    }

    public void addLineInfo(ThreadLineInfo p) {
        if (infos == null)
            this.infos = new ArrayList<>();
        infos.add(p);
    }

    public void setSkipCount(int p) {
        this.skipCount = p;
    }

    public int getSkipCount() {
        return skipCount;
    }

    // override Object
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return className + ":" + methodName + "(" + locationClass + ":" + locationLineNo + ")";
    }

}
