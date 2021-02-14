package fr.an.jvm.thread.tool.threaddumps.analyzer;

import java.util.Iterator;

import fr.an.jvm.thread.tool.threaddumps.model.DefaultThreadItemInfoVisitor;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;



/**
 * helper to remove MethodThreadLineInfo, using methodCaterories rules
 */
public class SystemThreadRemover extends DefaultThreadItemInfoVisitor { 

    // ------------------------------------------------------------------------

    public SystemThreadRemover() {
    }

    // -------------------------------------------------------------------------
    
    @Override
    public void caseThreadDumpInfo(ThreadDumpInfo p) {
        // no call super!
        for (Iterator<ThreadInfo> iter = p.getThreads().iterator(); iter.hasNext(); ) {
            ThreadInfo elt = iter.next();
            if (ThreadDumpUtils.isSystemThread(elt.getName())) {
                iter.remove();
            }
        }
    }
        
}
