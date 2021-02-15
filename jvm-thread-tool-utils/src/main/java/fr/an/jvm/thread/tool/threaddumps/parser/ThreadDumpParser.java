package fr.an.jvm.thread.tool.threaddumps.parser;

import fr.an.jvm.thread.tool.threaddumps.model.*;
import fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl.IBMThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl.JRThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl.SunThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReader;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class ThreadDumpParser {

    private static final String NL = "\n";

    private SourceReader sourcereader;

    // ------------------------------------------------------------------------

    public ThreadDumpParser(SourceReader sourcereader) {
        this.sourcereader = sourcereader;
    }

    public static ThreadDumpJVMType isMatchStartDumpJVMType(String line) {
        if (line.startsWith("Full thread dump")) {
            return ThreadDumpJVMType.DumpHotspot;
        }
        if (line.startsWith("===== FULL THREAD DUMP =")) {
            return ThreadDumpJVMType.DumpJRockit;
        }
        if (line.startsWith("0SECTION       TITLE subcomponent dump routine")) {
            return ThreadDumpJVMType.DumpIBM;
        }
        return null;
    }

    public ThreadDumpInfo readThreadDump(int dumpId, String dumpTitle) throws IOException {
        ThreadDumpJVMType threadDumpType;
        String s;

        for (;;) {
            if ((s = sourcereader.readLine()) == null) {
                return null;
            }

//            if (s.startsWith("<")) {
//                int size = threadDumps.size();
//                if (tdFinished < size) {
//                    String extractTimeAfter = extractTime(s);
//                    for (; tdFinished < size; tdFinished++) {
//                        ThreadDumpInfo td = threadDumps.get(tdFinished);
//                        if (td.getTimeAfter() == null) {
//                            td.setTimeAfter(extractTimeAfter);
//                        }
//                    }
//                }
//                timeLine = s;
//            }

            threadDumpType = isMatchStartDumpJVMType(s);
            if (threadDumpType != null) {
                break;
            }
        }
        if (threadDumpType == null) {
            return null;
        }

        long startSourcePosition = sourcereader.getPosition();

        ThreadDumpInfo threaddump = new ThreadDumpInfo(startSourcePosition, dumpId, s);

        // println("found ThreadDump[" + currentThreadDumpIndex + "] at First line: " + startSourceLine);
        // threaddump.setTimeBefore(extractTime(timeLine));

        readThreadDump(threadDumpType, threaddump);

        // long endSourceLine = threaddump.getEndSourceLine();
        // int threadCount = threaddump.getThreads().size();
        // String classHistoInfo = "";
        // if (threaddump.getClassHistogramInfo() != null) {
        //    classHistoInfo = ", class histo: " + threaddump.getClassHistogramInfo().getClassItems().size() + " classes";
        //}
        // println("Last line: " + endSourceLine + " (" + ((endSourceLine - startSourceLine) + 1L) + " lines, " + threadCount + " threads" + classHistoInfo + ")");

        return threaddump;
    }

    public void readThreadDump(ThreadDumpJVMType threadDumpType, ThreadDumpInfo threaddump) throws IOException {
        switch (threadDumpType) {
            case DumpHotspot:
                readHotspotThreadDump(threaddump);
                break;

            case DumpJRockit:
                readJRockitThreadDump(threaddump);
                break;

            case DumpIBM:
                readIBMThreadDump(threaddump);
                break;

            default:
                println("ERROR: UNKNOWN DUMP TYPE");
                break;
        }
    }

    private void readHotspotThreadDump(ThreadDumpInfo threaddump) throws IOException {
        ThreadFormatParserHelper tdParser = new ThreadFormatParserHelper(new SunThreadFormatParser(), threaddump);

        long endSourceLine = 0L;
        sourcereader.markPosition();
        String s;
        for (; (s = sourcereader.readLine()) != null; sourcereader.markPosition()) {
            if (s.length() == 0)
                continue;
            char c = s.charAt(0);
            if (c == '"') {
                tdParser.addThread(s);
                continue;
            }
            if (s.startsWith("   java.lang.Thread.State: ")) {
                // TODO
                continue;
            }
            if (s.equals("   No compile task")) {
                // OK ""C1 CompilerThread0" ..
                continue;
            }
            if (c == '\t') {
                tdParser.addThreadLine(s);
                continue;
            }
//            if (s.startsWith("\t- ")) {
//                tdParser.addThreadLine(s);
//                continue;
//            }
            if (s.startsWith("    \"")) {
                tdParser.addThread(s);
                continue;
            }
            if (s.startsWith("        ")) {
                tdParser.addThreadLine('\t' + s.substring(8));
                continue;
            }
            if (s.startsWith("Threads class SMR info:")) {
                // TODO
                s = sourcereader.readLine(); // "_java_thread_list=0x00007fadc40048d0, length=32, elements={"
                while(null != (s = sourcereader.readLine())) {
                    if (s.equals("}")) {
                        break;
                    }
                }
                continue;
            }

            if (!s.startsWith("----------------------------------"))
                break;
        }

        if (s != null
                && (s.startsWith("FOUND A JAVA LEVEL DEADLOCK:") || s.startsWith("Found one Java-level deadlock:"))) {
            println("Found deadlock information");
            StringBuilder sb = new StringBuilder(10000);
            sb.append(s).append(NL);
            for (;;) {
                if ((s = sourcereader.readLine()) == null)
                    break;
                sb.append(s).append(NL);
                if (!s.startsWith("Found ") || !s.endsWith("."))
                    continue;
                threaddump.setDeadlockInfo(sb.toString());
                endSourceLine = sourcereader.getPosition();
                break;
            }
            if (s == null) {
                println("ERROR: Deadlock info incomplete");
                sourcereader.resetPosition();
            }
        } else {
            sourcereader.resetPosition();
        }
        if (endSourceLine == 0L)
            endSourceLine = sourcereader.getPosition() - 1L;
        threaddump.setEndSourceLine(endSourceLine);

        // Added support for -XX:+PrintClassHistogram
        if (s != null && s.startsWith("num   #instances    #bytes  class name")) {
            // format:
            // num #intances #bytes class name
            // --------------------------------------
            // 1: 662507 45192144 [C
            // 2: 482345 32373408 [Ljava.lang.Object;
            // ...
            // Total 4909188 226140848

            println("Found ClassHistogram information");
            List<ClassHistogramItemInfo> items = new ArrayList<ClassHistogramItemInfo>();

            // skip line "num #instances #bytes class name"
            s = sourcereader.readLine();

            // skip line "--------------------------------------"
            for (; (s = sourcereader.readLine()) != null;) {
                if (s.equals("--------------------------------------")) {
                    break;
                }
            }

            // parse lines
            Pattern patternClassHistogramLine = Pattern.compile("\\s*([0-9]+):\\s*([0-9]+)\\s*([0-9]+)\\s*(.*)");
            for (; (s = sourcereader.readLine()) != null;) {
                if (s.startsWith("Total")) {
                    break;
                }
                Matcher m = patternClassHistogramLine.matcher(s);
                if (!m.matches()) {
                    continue;
                }

                String rankStr = m.group(1);
                String instanceCountStr = m.group(2);
                String instanceByteSizeStr = m.group(3);
                String className = m.group(4);

                int rank = Integer.parseInt(rankStr);
                int instanceCount = Integer.parseInt(instanceCountStr);
                long instanceByteSize = Long.parseLong(instanceByteSizeStr);

                ClassHistogramItemInfo item = new ClassHistogramItemInfo(rank, instanceCount, instanceByteSize,
                        className);
                items.add(item);
            }

            int totalInstanceCount = 0;
            long totalInstanceByteSize = 0L;
            Pattern patternClassHistogramTotal = Pattern.compile("Total\\s*([0-9]+)\\s*([0-9]+)");
            Matcher matchTotal = patternClassHistogramTotal.matcher(s);
            if (matchTotal.matches()) {
                String instanceCountStr = matchTotal.group(1);
                String instanceByteSizeStr = matchTotal.group(2);
                totalInstanceCount = Integer.parseInt(instanceCountStr);
                totalInstanceByteSize = Long.parseLong(instanceByteSizeStr);
            }

            ClassHistogramInfo ch = new ClassHistogramInfo(totalInstanceCount, totalInstanceByteSize, items);
            threaddump.setClassHistogramInfo(ch);
        }

    }

    private void readJRockitThreadDump(ThreadDumpInfo threaddump) throws IOException {
        long endSourceLine = 0L;
        ThreadFormatParserHelper tdParser = new ThreadFormatParserHelper(new JRThreadFormatParser(), threaddump);

        String s;
        if ((s = sourcereader.readLine()) != null) {
            println("JRockit Thread Dump time: " + s);
            threaddump.setPreDumpInfo(s);
        }
        for (;;) {
            if ((s = sourcereader.readLine()) == null)
                break;
            if (s.length() == 0)
                continue;
            char c = s.charAt(0);
            if (c == '"') {
                tdParser.addThread(s);
                continue;
            }
            if (s.startsWith("Thread-0x")) {
                tdParser.addThread(s);
                continue;
            }
            if (s.startsWith("    ")) {
                tdParser.addThreadLine(s);
                continue;
            }
            if (s.startsWith("  ")) {
                tdParser.addThreadLine(s);
                continue;
            }
            if (s.startsWith("\t")) {
                tdParser.addThreadLine(s);
                continue;
            }
            if (s.startsWith("=====================")) {
                threaddump.setPostDumpInfo(s);
                break;
            }
            if (s.startsWith("===== END OF HREAD DUMP")) {
                threaddump.setPostDumpInfo(s);
                break;
            }
            if (s.equals("}"))
                continue;
            print("Unexpected line in JRockit Threadump at line ");
            print(String.valueOf(sourcereader.getPosition()));
            print(": ");
            println(s);
            println("Aborting the parse");
            break;
        }
        if (s == null)
            println("JRockit Threaddump incomplete at the end of the file");
        if (endSourceLine == 0L)
            endSourceLine = sourcereader.getPosition();
        threaddump.setEndSourceLine(endSourceLine);
    }

    private void readIBMThreadDump(ThreadDumpInfo threaddump) throws IOException {
        ThreadFormatParserHelper tdParser = new ThreadFormatParserHelper(new IBMThreadFormatParser(), threaddump);
        long endSourceLine = 0L;

        LinkedList<LockStandaloneInfo> standaloneLocks = new LinkedList<>();
        HashMap<String, LockStandaloneInfo> hashmap = new HashMap<>();

        String s;
        label0: do {
            label1: do {
                if ((s = sourcereader.readLine()) == null)
                    break label0;
                if (s.startsWith("3XMTHREADINFO")) {
                    tdParser.addThread(s.substring(14).trim());
                    continue;
                }
                if (s.startsWith("4XESTACKTRACE")) {
                    tdParser.addThreadLine(s.substring(14).trim());
                    continue;
                }
                if (s.startsWith("2LKMONINUSE")) {
                    String s1 = s.trim();
                    String s2 = sourcereader.readLine().trim();
                    if (!(s = sourcereader.readLine()).startsWith("3LK")) {
                        print("Unexpected monitor line in IBM Threadump at line ");
                        print(sourcereader.getResourceName());
                        print("#");
                        print(String.valueOf(sourcereader.getPosition()));
                        println(":");
                        println(s);
                        println("Aborting the parse");
                        break label0;
                    }
                    sourcereader.markPosition();
                    LinkedList<String> linkedlist1 = new LinkedList<String>();
                    String s4;
                    for (; (s4 = sourcereader.readLine()).startsWith("3LKWAITNOTIFY");) {
                        linkedlist1.add(s4.trim());
                    }
                    sourcereader.resetPosition();
                    LinkedList<LockStandaloneInfo> linkedlist2 = IBMThreadFormatParser.parseLockStructure(s1, s2,
                            linkedlist1);
                    standaloneLocks.addAll(linkedlist2);
                    for (Iterator<LockStandaloneInfo> iterator = linkedlist2.iterator();;) {
                        LockStandaloneInfo lockstandaloneinfo1;
                        do {
                            if (!iterator.hasNext())
                                continue label1;
                            lockstandaloneinfo1 = (LockStandaloneInfo) iterator.next();
                        } while (lockstandaloneinfo1.isThreadResolved());
                        hashmap.put(lockstandaloneinfo1.getThreadId(), lockstandaloneinfo1);
                    }
                }
                if (!s.startsWith("2LKFLATMON"))
                    continue label0;
                Pattern pattern = Pattern.compile("ident (.*?) .*\\(([^)]+)\\) ee");
                Matcher matcher = pattern.matcher(s);
                if (!matcher.find())
                    throw new IllegalStateException("Unexpected 2LKFLATMON line: " + s);
                String s3 = matcher.group(1);
                if (hashmap.containsKey(s3)) {
                    LockStandaloneInfo lockstandaloneinfo = (LockStandaloneInfo) hashmap.get(s3);
                    lockstandaloneinfo.setThreadId(matcher.group(2));
                    lockstandaloneinfo.setThreadResolved(true);
                }
            } while (true);
        } while (!s.startsWith("NULL           ---------------------- END OF DUMP"));
        threaddump.addAllStandaloneLock(standaloneLocks);
        endSourceLine = sourcereader.getPosition();
        threaddump.setEndSourceLine(endSourceLine);
    }

    public static String extractTime(String s) {
        if (s == null)
            return null;
        String res = s.substring(1, s.indexOf('>'));
        return res;
    }

    private static void print(String s) {
        System.out.print(s);
    }

    private static void println(String s) {
        System.out.println(s);
    }

}
