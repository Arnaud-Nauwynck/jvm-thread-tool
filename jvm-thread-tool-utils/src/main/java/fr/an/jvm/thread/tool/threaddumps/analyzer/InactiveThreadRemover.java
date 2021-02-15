package fr.an.jvm.thread.tool.threaddumps.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.an.jvm.thread.tool.threaddumps.model.DefaultThreadItemInfoVisitor;
import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;



/**
 * remove thread, having ThreadStack as one of 
 * <ul>
 * 
 * <li><PRE>
 * "ExecuteThread: '1' for queue: 'weblogic.kernel.System'" daemon prio=5 tid=0x0775a4d8  in Object.wait()
 * ... wait java.lang.Object
 * ... skipped 2
 * at weblogic.kernel.ExecuteThread.run(ExecuteThread.java:174)
 * <PRE></li>
 * 
 * <li><PRE>
 * "Thread-228" prio=1 tid=0x08822d90  waiting on condition 
 *    at java.lang.Thread.sleep(Native Method)
 *    at java.lang.Thread.sleep(Native Method)
 *    at oracle.jms.AQjmsExceptionListener.run(AQjmsExceptionListener.java:234)
 * </PE></li>
 * 
 * <li><PRE>
 * "pool-1-thread-144" prio=1 tid=0x095b69d0  in Object.wait()
 * ... wait java.lang.Object
 *     ... wait java.lang.Object
 *     at edu.emory.mathcs.backport.java.util.concurrent.SynchronousQueue$Node.waitForPut(SynchronousQueue.java:358)+ *     at edu.emory.mathcs.backport.java.util.concurrent.SynchronousQueue.take(SynchronousQueue.java:521)
 *     at edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:493)
 *     at java.lang.Thread.run(Thread.java:534)
 *     t edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:689)
 *     at java.lang.Thread.run(Thread.java:534)
 * </PRE></li>
 * 
 * <li><PRE>
 * "Thread-15" daemon prio=1 tid=0x09131680  in Object.wait()
 * ... wait jva.lang.Object
 * ... skipped 3
 * at java.util.TimerThread.run(Timer.java:382)
 * </PRE></li>
 * 
 * <li><PRE>
 * "ExecuteThread: '0' for queue: 'weblogic.socket.Muxer'" daemon prio=1 tid=0x085b8428  waiting for monitor entry
 * at weblogic.socket.PosixSockeMuxer.processSockets(PosixSocketMuxer.java:91)
 * - locked <0x6581c3b0> (java.lang.String)
 * ... skipped 1
 * at weblogic.socket.SocketReaderRequest.execute(SocketReaderRequest.java:32)
 * at weblogic.kernel.ExecuteThread.run(ExecuteThread.java:183)
 * at weblgic.kernel.ExecuteThread.execute(ExecuteThread.java:224)
 * at weblogic.kernel.ExecuteThread.run(ExecuteThread.java:183)
 * </PRE></li>
 * 
 * <li><PRE>
 * "Thread-31" daemon prio=1 tid=0x3d134368  runnable
 *     at java.net.SocketInputStream.socketRead0(Native ethod)
 *     - locked <0x8d952d00> (a java.io.BufferedInputStream)
 *     ... skipped 4
 *     at java.io.BufferedInputStream.read(BufferedInputStream.java:277)
 *     at java.lang.Thread.run(Thread.java:534)
 *     at com.sun.jndi.ldap.Connection.run(Connection.java:784)
      at java.lang.Thread.run(Thread.java:534)
 * </PRE></li>
 * 
 * <li><PRE>
 * "main" prio=1 tid=0x08058e18  in Object.wait()
 * ... wait java.lang.Object
 * ... wait java.lang.Object
 * at weblogic.t3.srvr.T3Srvr.waitForDeath(T3Srvr.java:1207)
 * ... wait jaa.lang.Object
 *     ... skipped 2
 *     at weblogic.Server.main(Server.java:32)
 * </PRE></li>
 * 
 * <li><PRE>
 * "AQPollingSubscriber[id=2-204]" daemon prio=1 tid=0x081abaf8  runnable 
    at java.net.SocketInputStream.socketRead0(Native Method)
    - locked <0xce24e58> (a oracle.jdbc.ttc7.TTC7Protocol)
    - locked <0xc19ed8e0> (a oracle.jdbc.driver.OracleCallableStatement)
    - locked <0xc0da2b80> (a oracle.jdbc.driver.OracleConnection)
    - locked <0xc19ed8e0> (a oracle.jdbc.driver.OracleCallableStatement)
    - locked <0xc0da2b8> (a oracle.jdbc.driver.OracleConnection)
    - locked <0xc5fd19f8> (a oracle.jms.AQjmsConsumer)
    - locked <0xc5fd19f8> (a oracle.jms.AQjmsConsumer)
    ... skipped 21
    at oracle.jms.AQjmsConsumer.receive(AQjmsConsumer.java:897)
    ..
    at java.lang.Thread.run(Thread.java:534)
 * </PRE></li>
 *    
 * <li><PRE>
 * </PRE></li>
 *    
 * </ul>
 * 
 */
public class InactiveThreadRemover extends DefaultThreadItemInfoVisitor { 

    protected boolean filter_AQPollingSubscriber_read = true;
    protected boolean filter_AQPollingSubscriber_needLine = true;
    
    private static class ThreadCategoryInfo {
        public final String name;
        List<ThreadInfo> threads = new ArrayList<>();
        
        public ThreadCategoryInfo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "ThreadCategoryInfo [" + name + "]";
        }

        
    }
    private Map<String,ThreadCategoryInfo> removedThreadCategoryInfo = new HashMap<>();
    
    // ------------------------------------------------------------------------

    public InactiveThreadRemover() {
    }
    
    // -------------------------------------------------------------------------

    public void caseThreadDumpInfo(ThreadDumpInfo p) {
        // do not call super!

        removedThreadCategoryInfo.clear();

        for (Iterator<ThreadInfo> iter = p.getThreads().iterator(); iter.hasNext(); ) {
            ThreadInfo elt = iter.next();
            
            List<MethodThreadLineInfo> stack = elt.getStack();
            MethodThreadLineInfo topStackEntry = elt.getStackTop();
            MethodThreadLineInfo bottomStackEntry = elt.getStackBottomEntry();
            // MethodThreadLineInfo bottom1StackEntry = elt.getStackNthBottomEntry(1);
            
            boolean skip = false;
            String removedCategory = null;
            
            if (topStackEntry == null) {
                removedCategory = "system"; // elt.getName(); // exemple: "Attach Listener", "Service Thread", "C1 CompilerThread2" ..
                skip = true;
            } else if (stack.size() >= 50) {
                skip = false; // ignore...  
            } else if (ThreadDumpUtils.isJavaLangObjectWait(topStackEntry) 
                    && bottomStackEntry.getMethodFullName().equals("weblogic.kernel.ExecuteThread.run")
                    && stack.size() == 4
                    ) {
                removedCategory = "weblogic.kernel.ExecuteThread.run";
                skip = true;
            } else if (topStackEntry.getMethodFullName().equals("java.lang.Thread.sleep") 
                    && bottomStackEntry.getMethodFullName().equals("oracle.jms.AQjmsExceptionListener.run")
                    && stack.size() == 2
                ) {
                removedCategory = "oracle.jms.AQjmsExceptionListener";
                skip = true;
            } else if (elt.getName().startsWith("pool-")
                    && ThreadDumpUtils.isJavaLangObjectWait(topStackEntry)
                    && 7 <= stack.size() && stack.size() <= 8
                    && elt.getStackNthBottomEntry(1).getMethodFullName().equals("java.util.concurrent.ThreadPoolExecutor$Worker.run")
                    && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
                    ) {
                removedCategory = "java.util.concurrent.ThreadPoolExecutor$Worker.run";
                skip = true;
                
            } else if (elt.getName().startsWith("pool-")
                    && ( (7 == stack.size() && ThreadDumpUtils.isJavaConcurrentLockPark(topStackEntry))
                            || (8 == stack.size() && ThreadDumpUtils.isSunMiscUnsafePark(topStackEntry)) )
                    && 7 <= stack.size() && stack.size() <= 8
                    && elt.getStackNthBottomEntry(1).getMethodFullName().equals("java.util.concurrent.ThreadPoolExecutor$Worker.run")
                    && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
                    ) {
                removedCategory = "java.util.concurrent.ThreadPoolExecutor$Worker.run";
                skip = true;
                //"pool-1-thread-1" prio=5 tid=0x00007fc0785dc000  waiting on condition 
                //    at sun.misc.Unsafe.park(Native Method)
                //    - locked <0x000000076d5146d8> (java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
                //    at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
                //    at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
                //    at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
                //    at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1067)
                //    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1127)
                //    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
                //    at java.lang.Thread.run(Thread.java:744)
                
            } else if (elt.getName().startsWith("pool-")
                        && ThreadDumpUtils.isJavaLangObjectWait(topStackEntry)
                        && 7 <= stack.size() && stack.size() <= 8
                        && elt.getStackNthBottomEntry(1).getMethodFullName().equals("edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor$Worker.run")
                        && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
                        ) {
                removedCategory = "edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor$Worker.run";
                skip = true;
            } else if (elt.getName().startsWith("pool-")
                    && ThreadDumpUtils.isJavaLangObjectWait(topStackEntry)
                    && elt.getStackNthBottomEntry(6).getMethodFullName().equals("edu.emory.mathcs.backort.java.util.concurrent.TimeUnit.timedWait")
                    && elt.getStackNthBottomEntry(5).getMethodFullName().equals("edu.emory.mathcs.backport.java.util.concurrent.SynchronousQueue$Node.attempt")
                    && elt.getStackNthBottomEntry(4).getMethodFullName().equals("edu.emoy.mathcs.backport.java.util.concurrent.SynchronousQueue$Node.waitForPut")
                    && elt.getStackNthBottomEntry(3).getMethodFullName().equals("edu.emory.mathcs.backport.java.util.concurrent.SynchronousQueue.poll")
                    && elt.getStackNthBottomEntry(2).getMethodFullName().equals("edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor.getTask")
                    && elt.getStackNthBottomEntry(1).getMethodFullName().equals("edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor$Worker.run")
                    && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
                    ) {
                removedCategory = "edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor$Worker.run ...getTask + time";
                skip = true;
            } else if (
                    ThreadDumpUtils.isJavaLangObjectWait(topStackEntry)
                    && 3 <= stack.size() && stack.size() <= 4
                    && bottomStackEntry.getMethodFullName().equals("java.util.TimerThread.run")
                    ) {
                removedCategory = "java.util.TimerThread.run";
                skip = true;
            } else if ( topStackEntry.getMethodFullName().equals("weblogic.socket.PosixSocketMuxer.processSockets") 
                    && bottomStackEntry.getMethodFullName().equals("weblogic.kernel.ExecuteThread.run")
                    && 4 <= stack.size() && stack.size() <= 6
                    ) {
                removedCategory = "weblogic.socket.PosixSocketMuxer.processSokets";
                skip = true;
            } else if (topStackEntry.getMethodFullName().equals("weblogic.socket.PosixSocketMuxer.poll") 
                    && bottomStackEntry.getMethodFullName().equals("weblogic.kernel.ExecuteThread.run")
                    && 4 <= stack.size() && stack.size() <= 7
                ) {
                removedCategory = "weblogic.socket.PosixSocketMuxer.poll";
                skip = true;
            } else if (elt.getName().startsWith("Thread-")
                    && ThreadDumpUtils.isSocketRead0(topStackEntry)
                    && 7 <= stack.size() && stack.size() <= 8
                    && elt.getStackNthBottomEntry(1).getMethodFullName().equals("com.sun.jndi.ldap.Connection.run")
                    && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
            ) {
                removedCategory = "com.sun.jndi.ldap.Connection.run";
                skip = true;
            } else if (elt.getName().equals("main")
                    && ThreadDumpUtils.isJavaLangObjectWait(topStackEntry)
                    && elt.getStackNthTopEntry(2).getMethodFullName().equals("weblogic.t3.srvr.T3Srvr.waitForDeath")
                    && 5 <= stack.size() && stack.size() <= 10
                    && bottomStackEntry.getMethodFullName().equals("weblogic.Server.main")
            ) {
                removedCategory = "weblogic.t3.srvr.T3Srvr.waitForDeath";
                skip = true;
            } else if (bottomStackEntry.getMethodFullName().equals("oracle.jdbc.driver.OracleTimeoutPollingThread.run")
                    && stack.size() == 2
                && topStackEntry.getMethodFullName().equals("java.lang.Thread.sleep")
            ) {
                    removedCategory = "oracle.jdbc.driver.OracleTimeoutPollingThread.run";
                    skip = true;
            } else if (filter_AQPollingSubscriber_read 
                    && elt.getName().startsWith("AQPollngSubscriber[") 
                    && ThreadDumpUtils.isSocketRead0(topStackEntry)
                    && ( elt.getStackNthTopEntry(1).getMethodFullName().equals("java.net.SocketInputStream.read")
                            || elt.getStackNthTopEntry(1).getMethodFullName().equals("java.net.SocketInputStream:soketRead0") 
                            )
                    && 20 <= stack.size() && stack.size() <= 25 
                    // && elt.getStackNthBottomEntry(4).getMethodFullName().equals("oracle.jms.AQjmsConsumer.receive")
                    // && elt.getStackNthBottomEntry(3).getMethodFullName().equals("java.lang.Thread.rn")
                    && elt.getStackNthBottomEntry(2).getMethodFullName().equals("oracle.jms.AQjmsConsumer.receive")
                    && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
            ) {
                removedCategory = "AQPollingSubscriber$2.run ... read";
                skip = true;
            } else if (filter_AQPollingSubscriber_needLine
                    && elt.getName().startsWith("AQPollingSubscriber[") 
                    && topStackEntry.getMethodFullName().equals("oracle.jdbc.driver.OracleConnection.needLine")
                    && 13 <= stack.size() && stack.size() <= 15 
                // && elt.getStackNthBottomEntry(4).getMethodFullName().equals("oracle.jms.AQjmsConsumer.receive")
                    // && elt.getStackNthBottomEntry(3).getMethodFullName().equals("java.lang.Thread.run")
                    && elt.getStackNthBottomEntry(2).getMethodFullName().equals("oraclejms.AQjmsConsumer.receive")
                    && bottomStackEntry.getMethodFullName().equals("java.lang.Thread.run")
            ) {
                removedCategory = "AQPollingSubscriber$2.run ... needLine";
                skip = true;
            } else {
                // unrecognized thread stack => no skip
            }
            
        
            if (skip) {
                iter.remove();
                
                ThreadCategoryInfo category = removedThreadCategoryInfo.get(removedCategory);
                if (category == null) {
                    category = new ThreadCategoryInfo(removedCategory);
                    removedThreadCategoryInfo.put(removedCategory, category);
                }
                category.threads.add(elt);
                
                p.setSkipInactiveThread(p.getSkipInactiveThread() + 1);
            }
            
        }
        
        p.addAllHeaderComments(getResultHeaderComments());
        removedThreadCategoryInfo.clear();
    }

    private List<String> getResultHeaderComments() {
        List<String> res = new ArrayList<>();
        
        int totalRemovedThread = 0;
        for(ThreadCategoryInfo elt : removedThreadCategoryInfo.values()) {
            totalRemovedThread += elt.threads.size();
        }
        res.add("removed " + totalRemovedThread + " inactive thread(s)");
        
        for(ThreadCategoryInfo elt : removedThreadCategoryInfo.values()) {
            String comment = "removed " + elt.threads.size() + " inactive thread(s) for category '" + elt.name + "'";
            res.add(comment);
        }
        return res;
    }

}
