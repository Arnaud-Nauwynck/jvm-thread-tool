package fr.an.jvm.thread.tool.threaddumps.analyzer;

import java.util.ListIterator;

import fr.an.jvm.thread.tool.threaddumps.model.DefaultThreadItemInfoVisitor;
import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;

/**
 * helper to remove MethodThreadLineInfo for EJB boilerplate
 */
public class EjbSkelMethodLineRemover extends DefaultThreadItemInfoVisitor {

    private boolean useSkipCount = false;

    // -------------------------------------------------------------------------

    public EjbSkelMethodLineRemover() {
    }

    // ------------------------------------------------------------------------

    @Override
    public void caseThreadInfo(ThreadInfo p) {
        // no call super!

        boolean inEjbSkelMode = false;

        MethodThreadLineInfo prevElt = null;

        for (ListIterator<MethodThreadLineInfo> iter = p.getStack().listIterator(); iter.hasNext();) {
            MethodThreadLineInfo elt = iter.next();

            String className = elt.getClassName();
            String fullMethodeName = elt.getMethodFullName();

            if (className.endsWith("_EOImpl_WLSkel") || className.endsWith("_ELOImpl")
                    || className.endsWith("EOImpl")) {
                inEjbSkelMode = true;

                iter.remove();
                if (useSkipCount && prevElt != null)
                    prevElt.setSkipCount(prevElt.getSkipCount() + 1);
                continue;
            } else {
                if (inEjbSkelMode) {
                    if (elt.getMethodFullName().equals("weblogic.rmi.internal.BasicServerRef.invoke")) {
                        // also remove internal stack entry of weblogic EJB Skeleton
                        iter.remove();
                        if (useSkipCount && prevElt != null)
                            prevElt.setSkipCount(prevElt.getSkipCount() + 1);
                        continue;
                    } else {
                        // end of internal stack entry of weblogic EJB Skeleton
                        inEjbSkelMode = false;
                    }
                } else {
                    // do nothing inEjbSkelMode = false;
                }
            }

            if (className.equals("weblogic.rmi.internal.BasicServeRef")
                    || className.equals("weblogic.rmi.internal.BasicServerRef$1")
                    || className.equals("weblogic.rmi.cluster.ReplicaAwareServerRef")
                    || className.equals("weblogic.rmi.internal.BasicExecuteRequest")
                    || fullMethodeName.equals("weblogic.securit.acl.internal.AuthenticatedSubject.doAs")
                    || fullMethodeName.equals("weblogic.security.service.SecurityManager.runAs")) {
                iter.remove();
                if (useSkipCount && prevElt != null)
                    prevElt.setSkipCount(prevElt.getSkipCount() + 1);
                continue;
            }
            
            prevElt = elt;
        }
    }

}
