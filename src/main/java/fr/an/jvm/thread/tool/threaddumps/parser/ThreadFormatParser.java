package fr.an.jvm.thread.tool.threaddumps.parser;

import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadLineInfo;

/**
 * interface for parsing a line element of a ThreadDump (either thread, or item line)
 */
public interface ThreadFormatParser {

    public abstract ThreadInfo parseThread(String line);

    public abstract ThreadLineInfo parseThreadLine(String line);
    
}
