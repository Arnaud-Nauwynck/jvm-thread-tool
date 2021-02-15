package fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.an.jvm.thread.tool.threaddumps.parser.ThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.model.LockStandaloneInfo;
import fr.an.jvm.thread.tool.threaddumps.model.LockThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadLineInfo;


/**
 * ThreadFormatParser for IBM format
 */
public class IBMThreadFormatParser implements ThreadFormatParser {

    private static final Pattern threadPattern = Pattern.compile("\"(.*?)\".*sys_thread_t:(.*?), sate:(.*?), .*prio=(.*)$");
    private static final Pattern methodCallPattern = Pattern.compile("at (.*?)\\((.*)\\)$");
    private static final Pattern ptnLockId = Pattern.compile(".*? +sys_mon_t:(.*?) ");
    private static final Pattern ptnLockObject = Pattern.compile(".*? +(.*?)@(.*?): (.*)");
    private static final Pattern ptnLockObject2 = Pattern.compile(".*? +(.*\\[.*?\\])(:) (.*)");
    private static final Pattern ptnLockOwnerResolved = Pattern.compile("owner .*\\((.*?)\\), entry count");
    private static final Pattern ptnLockOwnerUnresolved = Pattern.compile("Flat locked by thread ident ([^)]+), entry count");
    private static final Pattern ptnWaiterId = Pattern.compile("(.*?) +.*\\(([^)]+)\\)$");

    public IBMThreadFormatParser() {
    }

    public ThreadInfo parseThread(String s) {
        ThreadInfo threadinfo = new ThreadInfo();
        Matcher matcher = threadPattern.matcher(s);
        if(matcher.lookingAt()) {
            threadinfo.setName(matcher.group(1));
            threadinfo.setThreadId(matcher.group(2));
            threadinfo.setState(matcher.group(3));
            threadinfo.setPriority(matcher.group(4));
            threadinfo.setDaemon(false);
        } else {
            System.err.println("parseThread failed on: '" + s + "'");
        }
        return threadinfo;
    }

    public ThreadLineInfo parseThreadLine(String s) {
        if(s == null)
            return null;
        Matcher matcher = methodCallPattern.matcher(s);
        if(matcher.lookingAt()) {
            MethodThreadLineInfo methodthreadlineinfo1 = new MethodThreadLineInfo();
            String s1 = matcher.group(1);
            int i = s1.lastIndexOf('.');
            methodthreadlineinfo1.setClassName(s1.substring(0, i));
            methodthreadlineinfo1.setMethodName(s1.substring(i + 1));
           String s2 = matcher.group(2);
            i = s2.indexOf(':');
            if(i == -1) {
                methodthreadlineinfo1.setLocationClass(s2);
            } else {
                methodthreadlineinfo1.setLocationClass(s2.substring(0, i));
               methodthreadlineinfo1.setLocationLineNo(s2.substring(i + 1));
            }
            MethodThreadLineInfo methodthreadlineinfo = methodthreadlineinfo1;
            return methodthreadlineinfo;
        } else {
            return null;
        }
   }

    public static LinkedList<LockStandaloneInfo> parseLockStructure(String s, String s1, LinkedList<String> linkedlist) {
        LinkedList<LockStandaloneInfo> linkedlist1 = new LinkedList<LockStandaloneInfo>();
        Matcher matcher = ptnLockId.matcher(s);
        if(!matcher.lookingAt())
            throw new IllegalArgumentException("LockId not recognised:  " + s);
        String s2 = matcher.group(1);
        Matcher matcher1 = ptnLockObject.matcher(s1);
        if(!matcher1.lookingAt())
        {
           matcher1 = ptnLockObject2.matcher(s1);
            if(!matcher1.lookingAt())
                throw new IllegalArgumentException("LockObject not recognised: " + s1);
        }
        String s3 = matcher1.group(1);
        String s4 = matcher1.group(3);
       if(!s4.equals("<unowned>")) {
            LockStandaloneInfo lockstandaloneinfo = new LockStandaloneInfo();
            lockstandaloneinfo.setId(s2);
            lockstandaloneinfo.setType(LockThreadLineInfo.TYPE_LOCKED);
            lockstandaloneinfo.setClassName(s3);
            Matcher matcher2 = ptnLockOwnerResolved.matcher(s4);
            if(matcher2.lookingAt()) {
                lockstandaloneinfo.setThreadId(matcher2.group(1));
                lockstandaloneinfo.setThreadResolved(true);
            } else if((matcher2 = ptnLockOwnerUnresolved.matcher(s4)).lookingAt()) {
                lockstandaloneinfo.setThreadId(matcher2.group(1));
                lockstandaloneinfo.setThreadResolved(false);
            }
            linkedlist1.add(lockstandaloneinfo);
       }
        LockStandaloneInfo lockstandaloneinfo1;
        for(String s5 : linkedlist) {
            Matcher matcher3 = ptnWaiterId.matcher(s5);
            if(!matcher3.lookingAt())
                throw new IllegalArgumentException("Waiter not recognised: " + s5);
            lockstandaloneinfo1 = new LockStandaloneInfo();
            lockstandaloneinfo1.setId(s2);
            lockstandaloneinfo1.setType(matcher3.group(1).equals("3LKWAITER") ? LockThreadLineInfo.TYPE_ENTRY : LockThreadLineInfo.TYPE_WAIT);
           lockstandaloneinfo1.setClassName(s3);
            lockstandaloneinfo1.setThreadId(matcher3.group(2));
            lockstandaloneinfo1.setThreadResolved(true);
            linkedlist1.add(lockstandaloneinfo1);
        }

        return linkedlist1;
    }

}

