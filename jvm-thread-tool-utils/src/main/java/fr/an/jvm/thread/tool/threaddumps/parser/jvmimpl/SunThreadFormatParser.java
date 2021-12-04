package fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.an.jvm.thread.tool.threaddumps.model.EliminatedThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.LockThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.parser.ThreadFormatParser;

/**
 * 
 */
public class SunThreadFormatParser implements ThreadFormatParser {

    private static final Pattern threadPattern = Pattern
            .compile("\"([^\"]+)\" (#\\S+ )?(daemon )?(prio=(\\S+) )?(os_prio=\\S+ )?"
                    + "(cpu=(\\d+[,\\.]\\d*)ms )?"
                    + "(elapsed=(\\d+[,\\.]\\d*)s )?"
                    + "tid=(\\S+) nid=\\S+ (runnable |waiting on condition )?(.*)");

    private static final Pattern methodPattern = Pattern.compile("\\s+at ([\\p{Alnum}$_.<>]+)\\(([^\\)]*)\\)");
    private static final Pattern methodLambdaPattern = Pattern.compile("\\s+at ([\\p{Alnum}$_.<>]+)/.*");

    private static final Pattern lockPattern = Pattern
            .compile("\t- (waiting to lock|waiting on|locked|parking to wait for\\s*) <(\\p{Alnum}+)> \\(a ([^\\)]+)\\)");
    private static final String waitingOnNoObjectRef ="\t- waiting on <no object reference available>";

    // example: - eliminated <owner is scalar replaced> (a java.io.DataInputStream)    at org.eclipse.jdi.internal.connect.PacketReceiveManager.readAvailablePacket(PacketReceiveManager.java:300)
    private static final Pattern eliminatedScalarPattern = Pattern
            .compile("\t- eliminated <([^>]*)> \\(a ([^\\)]*)\\)(\\s+at (.*))?");
    
    
    public SunThreadFormatParser() {
    }

    public ThreadInfo parseThread(String s) {
        ThreadInfo res = new ThreadInfo();
        Matcher matcher = threadPattern.matcher(s);
        if (matcher.lookingAt()) {
            res.setName(matcher.group(1));
            String s1 = matcher.group(3);
            res.setDaemon(s1 != null);
            res.setPriority(matcher.group(5));
            res.setThreadId(matcher.group(11));
            res.setState(matcher.group(12));
        } else {
            System.err.println("parseThread failed on: '" + s + "'");
        }
        return res;
    }

    public ThreadLineInfo parseThreadLine(String s) {
        ThreadLineInfo res;
        Matcher matcher = methodPattern.matcher(s);
        if (matcher.lookingAt()) {
            MethodThreadLineInfo res2 = new MethodThreadLineInfo();
            String s1 = matcher.group(1);
            int i = s1.lastIndexOf('.');
            res2.setClassName(s1.substring(0, i));
            res2.setMethodName(s1.substring(i + 1));
            String s2 = matcher.group(2);
            i = s2.indexOf(':');
            if (i == -1) {
                if (s2.length() == 0)
                    res2.setLocationClass("Unknown");
                else
                    res2.setLocationClass(s2);
            } else {
                res2.setLocationClass(s2.substring(0, i));
                res2.setLocationLineNo(s2.substring(i + 1));
            }
            res = res2;
        } else if ((matcher = methodLambdaPattern.matcher(s)).matches()) {
            MethodThreadLineInfo res2 = new MethodThreadLineInfo();
            String s1 = matcher.group(1);
            res2.setClassName(s1);
            // res2.setMethodName("Lambda$" + matcher.group(2));
            // TODO ..
            res = res2;
        } else if ((matcher = lockPattern.matcher(s)).lookingAt()) {
            LockThreadLineInfo res2 = new LockThreadLineInfo();
            res2.setType(LockThreadLineInfo.lookupType(matcher.group(1)));
            res2.setId(matcher.group(2));
            res2.setClassName(matcher.group(3));
            res = res2;
        } else if (waitingOnNoObjectRef.equals(s)) {
            LockThreadLineInfo res2 = new LockThreadLineInfo();
            res2.setType(LockThreadLineInfo.TYPE_WAIT);
            res = res2;
        } else if (s.startsWith("\t- eliminated")) {
        	matcher = eliminatedScalarPattern.matcher(s);
        	if (matcher.matches()) {
	            String type = matcher.group(1), eliminated = matcher.group(2);
	            String className = matcher.group(3);
	            String at = matcher.group(4);
	            EliminatedThreadLineInfo res2 = new EliminatedThreadLineInfo(type, eliminated, className);
	            res = res2;
        	} else {
                System.err.println("Unknown - eliminated line: '" + s + "'");
        		res = new MethodThreadLineInfo();
        	}
        } else {
            System.err.println("Unknown line: '" + s + "'");
            res = new MethodThreadLineInfo();
        }
        return res;
    }

}
