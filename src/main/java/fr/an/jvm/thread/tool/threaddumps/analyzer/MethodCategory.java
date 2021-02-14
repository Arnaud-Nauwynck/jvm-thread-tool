package fr.an.jvm.thread.tool.threaddumps.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;

/**
 * TODO should implements push/pop StateAutomaton !!
 */
public class MethodCategory {

    public static final MethodCategory EMPTY_RULE = new MethodCategory("", PrintMode.ALL);

    public static final List<MethodCategory> DEFAULT_RULES;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    static {
        List<MethodCategory> tmp = new ArrayList<MethodCategory>();

        // tmp.add(new MethodCategory("com.", PrintMode.ALL));

        tmp.add(new MethodCategory("sun.reflect.GeneratedMethodAccessor", PrintMode.NONE));

        tmp.add(new MethodCategory("oracle.toplink.", PrintMode.START_END_ONLY,
                new String[] { "oracle.jdbc.", "weblogic.", "java.util.", "java.lang." }));

        tmp.add(new MethodCategory("oracle.jms.AQjmsConsumer", PrintMode.START_END_ONLY,
                new String[] {
                        // "racle.jdbc.driver.OraclePreparedStatement",
                        // "oracle.jdbc.ttc7.MAREngine.unmarshalUB1",
                        // "oracle.jdbc.",
                        // "oracle.sql.",
                        // "oracle.net.",
                        // "oracle.jms",
                        "oracle.", "java.net.", "java.util." }));

        tmp.add(new MethodCategory("oracle.jms.AQjmsExceptionListener", PrintMode.START_END_ONLY,
                new String[] { "oracle.jdbc.", "oracle.sql.", "oracle.net.ns.", "java.net.", "java.util." }));

        tmp.add(new MethodCategory("oracle.jdbc.", PrintMode.START_END_ONLY, new String[] { "java.util." }));

        tmp.add(new MethodCategory("weblogic.kernel.ExecuteThread.run", PrintMode.START_END_ONLY,
                new String[] {
                        // "weblogic.kernel.ExecuteThread.waitForRequest",
                        // "weblogic.socket.SockeReaderRequest",
                        "weblogic.", "javax.servlet.", "java.lang.Object.wait", "java.net.", "java.util.",
                        "java.lang.reflect.", "sun.reflect." }));

        tmp.add(new MethodCategory("weblogic.jms.client.JMSSession.execute", PrintMode.START_END_ONLY,
                new String[] { "weblogic.ejb20.internal.MDListener." }));

        tmp.add(new MethodCategory("weblogic.jdbc.", PrintMode.START_END_ONLY,
                new String[] { "oracle.jdbc.", "java.util." }));

        tmp.add(new MethodCategory("weblogicservlet.", PrintMode.START_END_ONLY,
                new String[] { "javax.servlet.", "weblogic.", "java.lang.reflect.", "sun.reflect." }));

        tmp.add(new MethodCategory("weblogic.webservice.", PrintMode.START_END_ONLY,
                new String[] { "java.langreflect.", "sun.reflect." }));

        tmp.add(new MethodCategory("weblogic.", PrintMode.START_END_ONLY,
                new String[] {
                        // "com.bea.utils.misc.ProcessManager",
                        "com.bea.", "java.lang.ClassLoader", "sun.misc.URLClassPath", "sun.misc.auncher$AppClassLoader",
                        "java.net.URLClassLoader", "sun.misc.URLClassPath", "java.security.AccessController",
                        "java.util.jar.JarFile", "java.util.zip.ZipFile", "java.util."

                }));

        tmp.add(new MethodCategory("com.bea.", PrintMode.START_END_ONLY));

        tmp.add(new MethodCategory("java.lang.ClassLoader.loadClassInternal", PrintMode.START_END_ONLY,
                new String[] { "com.bea.utils.misc.ProcessFile.loadFileNew", "java.lang.ClassLoader.loadClass(",
                        "sun.misc.Launher$AppClassLoader.loadClass(" }));

        tmp.add(new MethodCategory("sun.misc.URLClassPath", PrintMode.START_END_ONLY,
                new String[] { "java.net.URLClassLoader", "sun.misc.URLClassPath", "java.security.AccessController",
                        "java.util.jar.JarFile", "java.util.zip.ZipFile", "sun.misc.Launcher$AppClassLoader", }));

        tmp.add(new MethodCategory("java.util.TimerThread.run(", PrintMode.START_END_ONLY,
                new String[] { "java.util.TimerThread.mainLoop(", "java.lang.Object.wait(" }));

        tmp.add(new MethodCategory("javax.servlet.http.HttpServlet.service", PrintMode.START_END_ONLY,
                new String[] { "weblogic.servlet.",
                // "weblogic.webservice."
                }));

        tmp.add(new MethodCategory("org.apache.commons.collections.", PrintMode.START_END_ONLY));

        tmp.add(new MethodCategory("com.", PrintMode.START_END_ONLY));
        tmp.add(new MethodCategory("sun.", PrintMode.START_END_ONLY));
        tmp.add(new MethodCategory("java.", PrintMode.START_END_ONLY));

        DEFAULT_RULES = Collections.unmodifiableList(tmp);
    }

    // -------------------------------------------------------------------------

    private String prefix;

    private String[] followingPrefix;

    private PrintMode printMode;

    // ------------------------------------------------------------------------

    public MethodCategory(String prefix, PrintMode printMode, String[] followingPrefix) {
        this.prefix = prefix;
        this.printMode = printMode;
        this.followingPrefix = (followingPrefix != null) ? followingPrefix : EMPTY_STRING_ARRAY;
    }

    public MethodCategory(String prefix, PrintMode printMode) {
        this(prefix, printMode, EMPTY_STRING_ARRAY);
    }

    // -------------------------------------------------------------------------

    public boolean matchesClassName(String className) {
        return className.startsWith(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public PrintMode getPrintMode() {
        return printMode;
    }

    public boolean matches(MethodThreadLineInfo p) {
        String tmp = p.getClassName();
        if (tmp == null)
            return false;
        if (p.getMethodName() != null) {
            tmp += "." + p.getMethodName();
        }
        boolean res = tmp.startsWith(prefix);
        return res;
    }

    public boolean matchesFollowing(MethodThreadLineInfo p) {
        String tmp = p.getClassName();
        if (tmp == null)
            return false;
        if (p.getMethodName() != null) {
            tmp += "." + p.getMethodName();
        }
        boolean res = tmp.startsWith(prefix);
        if (!res) {
            int size = followingPrefix.length;
            for (int i = 0; i < size; i++) {
                if (tmp.startsWith(followingPrefix[i])) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MethodCategory[" + "'" + prefix + "'" + ", mode=" + printMode);
        if (followingPrefix != null && followingPrefix.length != 0) {
            int size = followingPrefix.length;
            sb.append(", followingClassPrefix=[");
            for (int i = 0; i < size; i++) {
                sb.append(followingPrefix[i]);
                if (i + 1 < size)
                    sb.append(",");
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

}