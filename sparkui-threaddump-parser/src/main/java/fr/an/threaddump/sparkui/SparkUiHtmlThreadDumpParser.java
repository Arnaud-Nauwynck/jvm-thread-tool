package fr.an.threaddump.sparkui;

import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import lombok.val;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;

public class SparkUiHtmlThreadDumpParser {

    /**
     * <tr id="thread_18_tr" class="accordion-heading" onclick="toggleThreadStackTrace(18, false)" onmouseover="onMouseOverAndOut(18)" onmouseout="onMouseOverAndOut(18)">
     *    <td id="18_td_id">18</td>
     *    <td id="18_td_name">Common-Cleaner</td>
     *    <td id="18_td_state">TIMED_WAITING</td>
     *    <td id="18_td_locking"></td>
     *    <td id="18_td_stacktrace" class="d-none">
     *          java.base@20.0.1/jdk.internal.misc.Unsafe.park(Native Method)<br/>
     *          java.base@20.0.1/java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:269)<br/>
     *          java.base@20.0.1/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:1847)<br/>java.base@20.0.1/java.lang.ref.ReferenceQueue.await(ReferenceQueue.java:71)<br/>java.base@20.0.1/java.lang.ref.ReferenceQueue.remove0(ReferenceQueue.java:143)<br/>java.base@20.0.1/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:218)<br/>java.base@20.0.1/jdk.internal.ref.CleanerImpl.run(CleanerImpl.java:140)<br/>java.base@20.0.1/java.lang.Thread.runWith(Thread.java:1636)<br/>java.base@20.0.1/java.lang.Thread.run(Thread.java:1623)<br/>java.base@20.0.1/jdk.internal.misc.InnocuousThread.run(InnocuousThread.java:186)
     *          </td>
     *</tr>
     * @param doc
     * @return
     */
    public ThreadDumpInfo htmlDocToThreadDump(Document doc) {
        ThreadDumpInfo res = new ThreadDumpInfo(0, 0, "");

        val body = doc.body();
        val tbody = body.getElementsByTag("tbody").first();
        val threadNodes = tbody.childNodes();
        for(val threadNode: threadNodes) {
            String threadIdNode = threadNode.attr("id");
            if (threadIdNode == null) {
                continue;
            }
            String htmlThreadId;
            if (threadIdNode.startsWith("thread_") && threadIdNode.endsWith("_tr") ) {
                htmlThreadId = threadIdNode.substring(7, threadIdNode.length()-3);
            } else {
                continue;
            }
            String threadName = null;
            String threadState = null;
            String threadLock = null;
            val threadStackTrace = new ArrayList<String>();

            val threadChildNodes = threadNode.childNodes();
            for(val threadChildNode: threadChildNodes) {
                String nodeId = threadChildNode.attr("id");
                if (nodeId.endsWith("_name")) {
                    threadName = threadChildNode.firstChild().outerHtml();
                } else if (nodeId.endsWith("_state")) {
                    Node node = threadChildNode.firstChild();
                    threadState = (node != null)? node.outerHtml() : null;
                } else if (nodeId.endsWith("_locking")) {
                    Node node = threadChildNode.firstChild();
                    threadLock = (node != null)? node.outerHtml() : null;
                } else if (nodeId.endsWith("_stacktrace")) {
                    val stackNodes = threadChildNode.childNodes();
                    if (stackNodes != null) {
                        for(val stackNode: stackNodes) {
                            if (stackNode instanceof TextNode) {
                                TextNode tnode = (TextNode) stackNode;
                                threadStackTrace.add(tnode.text());
                            } else {
                                // ignore
                            }
                        }
                    }
                } else {
                    // ignore
                }
            }
            if (threadName == null || threadStackTrace == null) {
                continue;
            }
            System.out.println("Thread name:" + threadName + " state:" + threadState + " stack:" + threadStackTrace);
        }

        return res;
    }
}
