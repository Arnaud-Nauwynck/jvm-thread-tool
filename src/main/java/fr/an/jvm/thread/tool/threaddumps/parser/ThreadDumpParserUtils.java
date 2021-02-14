package fr.an.jvm.thread.tool.threaddumps.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpList;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.BufferedSourceReader;
import fr.an.jvm.thread.tool.threaddumps.parser.sourcereader.SourceReader;

public final class ThreadDumpParserUtils {

    /** private to force all static */
    private ThreadDumpParserUtils() {}

    public static ThreadDumpInfo parseThreadDump(String text) {
        ThreadDumpList threadDumpList = new ThreadDumpList();
        SourceReader sourceReader = new BufferedSourceReader(new BufferedReader(new StringReader(text)));
        ThreadDumpListParser parser = new ThreadDumpListParser(threadDumpList, sourceReader); 
        try {
            parser.parse();
        } catch(IOException ex) {
            throw new RuntimeException("should not occur: in-memory io", ex);
        } finally {
            if (sourceReader != null) sourceReader.close();
        }
        List<ThreadDumpInfo> tmpres = threadDumpList.getThreadDumps();
        return (!tmpres.isEmpty())? tmpres.get(0) : null;
    }
    
}
