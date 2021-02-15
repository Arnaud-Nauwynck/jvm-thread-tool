package fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.an.jvm.thread.tool.threaddumps.model.AttributeMapThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.LockThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.parser.ThreadFormatParser;



/**
 * 
 */
public class JRThreadFormatParser implements ThreadFormatParser {

    private static final Pattern threadPattern1 = Pattern.compile("\"([^\"]+)\"  id: (\\p{Alnum}+)  prio: (\\d+)  (\\w+), (\\w+), (\\w+)");
    private static final Pattern threadPattern2 = Pattern.compile("\"(.+?)\" prio=(.*?) id=(.*?) [pt]id=(.*?) ([^,]+?)(,.+)?$");
    private static final Pattern threadPattern3 = Pattern.compile("Thread-(.*?) \"(.*?)\" \\<(.*), priority=(.*?)(?:, (DAEMON))?\\> \\{");
    private static final Pattern methodCallPattern1 = Pattern.compile("\\s+at ([\\p{Alnum}$_./<>]+)\\(([^\\)]+)\\)@(\\p{Alnum}+)");
    private static final Pattern methodCallPattern2 = Pattern.compile("\t(.*?)\\(.*?\\).*?\\((.*?)\\)");
    private static final Pattern lockPattern = Pattern.compile("\\s+(\\^?--) (.*?): (.*?)@(.*?)\\[(.*?)\\]");
    private static final Pattern attributeSetPattern = Pattern.compile("\\s+([\\w_]+):\\s+(\\p{Alnum}+)");

    private static final String TL_END_LINE = "--- End of stack trace";
    private static final String T_NO_LINES = "No Java stack trace available";
    
    public JRThreadFormatParser() {
    }

    public ThreadInfo parseThread(String s) {
        ThreadInfo res = new ThreadInfo();
        Matcher matcher = threadPattern1.matcher(s);
        if(matcher.lookingAt()) {
            res.setName(matcher.group(1));
            res.setThreadId(matcher.group(2));
            res.setPriority(matcher.group(3));
            res.setState(matcher.group(4));
            res.setDaemon("DAEMON".equals(matcher.group(5)));
       } else if((matcher = threadPattern2.matcher(s)).lookingAt()) {
            res.setName(matcher.group(1));
            res.setPriority(matcher.group(2));
            res.setThreadId(matcher.group(4));
            res.setState(matcher.group(5));
            if(matcher.groupCount() < 6) {
                res.setDaemon(false);
            } else {
                String s1 = matcher.group(6);
                res.setDaemon(s1 != null ? s1.indexOf(", daemon") >= 0 : false);
            }
        } else if((matcher = threadPattern3.matcher(s)).lookingAt()) {
            res.setThreadId(matcher.group(1));
            res.setName(matcher.group(2));
            res.setState(matcher.group(3));
            res.setPriority(matcher.group(4));
            if(matcher.groupCount() < 5) {
               res.setDaemon(false);
            } else {
                String s2 = matcher.group(5);
                res.setDaemon(s2 != null ? s2.indexOf("DAEMON") >= 0 : false);
            }
        } else {
            System.err.println("parseThread faied on: '" + s + "'");
        }
        String name = res.getName();
        if(name == null || name.length() == 0)
            res.setName("--No Name--");
        return res;
    }

    public ThreadLineInfo parseThreadLine(String s) {
        ThreadLineInfo res;
        if (s == null)
            return null;
        Matcher matcher = null;
        if((matcher = methodCallPattern1.matcher(s)).lookingAt())
            res = extractMethodCall(matcher);
        else if((matcher = methodCallPattern2.matcher(s)).lookingAt())
            res = extractMethodCall(matcher);
        else if((matcher = lockPattern.matcher(s)).lookingAt()) {
            LockThreadLineInfo lockthreadlineinfo = new LockThreadLineInfo();
            if(matcher.group(1).charAt(0) == '^')
               lockthreadlineinfo.setType(LockThreadLineInfo.TYPE_LOCKED);
            else if(matcher.group(2).indexOf("Blocked") >= 0)
                lockthreadlineinfo.setType(LockThreadLineInfo.TYPE_ENTRY);
            else
                lockthreadlineinfo.setType(LockThreadLineInfo.TYPE_WAIT);
            lockthreadlineinfo.setClassName(matcher.group(3).replace('/', '.'));
            lockthreadlineinfo.setId(matcher.group(4));
            res = lockthreadlineinfo;
        } else if(s.indexOf(TL_END_LINE) > 0 || s.indexOf(T_NO_LINES) > 0) {
            res = null;
        } else {
            Matcher matcher1 = attributeSetPattern.matcher(s);
            AttributeMapThreadLineInfo attributemapthreadlineinfo = new AttributeMapThreadLineInfo();
            for(; matcher1.find(); attributemapthreadlineinfo.put(matcher1.group(1), matcher1.group(2))) {
            }
            res = (attributemapthreadlineinfo.size() != 0) ? ((ThreadLineInfo) attributemapthreadlineinfo) : null;
        }
        return res;
    }

    private ThreadLineInfo extractMethodCall(Matcher matcher) {
        MethodThreadLineInfo res = new MethodThreadLineInfo();
        String s = matcher.group(1);
        int i = s.lastIndexOf('.');
        res.setClassName(s.substring(0, i).replace('/', '.'));
        res.setMethodName(s.substring(i + 1));
        String s1 = matcher.group(2);
        i = s1.indexOf(':');
        if(i == -1) {
            res.setLocationClass(s1);
        } else {
            res.setLocationClass(s1.substring(0, i));
            res.setLocationLineNo(s1.substring(i + 1));
        }
        return res;
    }


}
