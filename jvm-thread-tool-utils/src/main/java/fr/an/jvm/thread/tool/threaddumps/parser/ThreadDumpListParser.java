package fr.an.jvm.thread.tool.threaddumps.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramItemInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl.IBMThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl.JRThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.parser.jvmimpl.SunThreadFormatParser;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReader;
import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramInfo;
import fr.an.jvm.thread.tool.threaddumps.model.LockStandaloneInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpList;

/**
 * 
 */
public class ThreadDumpListParser {

    private SourceReader sourcereader;
    private ThreadDumpParser threadDumpParser;

    private ThreadDumpList threadDumps;

    private int currentThreadDumpIndex = 0;

    protected String timeLine;

    protected int tdFinished = 0;

    // ------------------------------------------------------------------------

    public ThreadDumpListParser(ThreadDumpList threadDumps, SourceReader sourcereader) {
        this.threadDumps = threadDumps;
        this.sourcereader = sourcereader;
        this.threadDumpParser = new ThreadDumpParser(sourcereader);
    }

    public void parse() throws IOException {
        ThreadDumpInfo threaddump;
        while ((threaddump = extractDump()) != null) {
            ++currentThreadDumpIndex;
            threadDumps.addThreadDump(threaddump);
        }
    }

    protected ThreadDumpInfo extractDump() throws IOException {
        ThreadDumpJVMType threadDumpType = null;

        String s;
        for (;;) {
            if ((s = sourcereader.readLine()) == null)
                break;

            if (s.startsWith("<")) {
                int size = threadDumps.size();
                if (tdFinished < size) {
                    String extractTimeAfter = extractTime(s);
                    for (; tdFinished < size; tdFinished++) {
                        ThreadDumpInfo td = threadDumps.get(tdFinished);
                        if (td.getTimeAfter() == null) {
                            td.setTimeAfter(extractTimeAfter);
                        }
                    }
                }
                timeLine = s;
            }

            threadDumpType = ThreadDumpParser.isMatchStartDumpJVMType(s);
            if (threadDumpType != null) {
                break;
            }
        }
        if (threadDumpType == null) {
            return null;
        }

        long startSourceLine = sourcereader.getPosition();

        ThreadDumpInfo threaddump = new ThreadDumpInfo(startSourceLine, currentThreadDumpIndex, s);

        // println("found ThreadDump[" + currentThreadDumpIndex + "] at First line: " + startSourceLine);
        threaddump.setTimeBefore(extractTime(timeLine));

        threadDumpParser.readThreadDump(threadDumpType, threaddump);

        // long endSourceLine = threaddump.getEndSourceLine();
        // int threadCount = threaddump.getThreads().size();
        // String classHistoInfo = "";
        // if (threaddump.getClassHistogramInfo() != null) {
        //    classHistoInfo = ", class histo: " + threaddump.getClassHistogramInfo().getClassItems().size() + " classes";
        //}
        // println("Last line: " + endSourceLine + " (" + ((endSourceLine - startSourceLine) + 1L) + " lines, " + threadCount + " threads" + classHistoInfo + ")");

        return threaddump;
    }

    public static String extractTime(String s) {
        if (s == null)
            return null;
        String res = s.substring(1, s.indexOf('>'));
        return res;
    }

}
