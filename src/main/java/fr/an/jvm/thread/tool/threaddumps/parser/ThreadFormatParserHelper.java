package fr.an.jvm.thread.tool.threaddumps.parser;

import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;

/**
 * 
 */
public class ThreadFormatParserHelper {

    ThreadFormatParser parser;

    ThreadDumpInfo currentThreadDump;
    ThreadInfo currentThread;
    
    // ------------------------------------------------------------------------

    public ThreadFormatParserHelper(ThreadFormatParser parser, ThreadDumpInfo currentThreadDump) {
        this.parser = parser;
        this.currentThreadDump = currentThreadDump;
        this.currentThread = null;
    }

    // -------------------------------------------------------------------------
    
    public void addThread(String s) {
        currentThread = parser.parseThread(s);
        currentThreadDump.addThread(currentThread);
    }

    public void addThreadLine(String s) {
        ThreadLineInfo threadlineinfo = parser.parseThreadLine(s);
        if (threadlineinfo != null) {
            if (threadlineinfo instanceof MethodThreadLineInfo) {
                currentThread.addLineStack((MethodThreadLineInfo)threadlineinfo);
            } else {
                // currentThread.addLineInfo(threadlineinfo);
                MethodThreadLineInfo meth = currentThread.getStackTop();
                if (meth != null) {
                    meth.addLineInfo(threadlineinfo);
                }
            }
       }
    }

}
