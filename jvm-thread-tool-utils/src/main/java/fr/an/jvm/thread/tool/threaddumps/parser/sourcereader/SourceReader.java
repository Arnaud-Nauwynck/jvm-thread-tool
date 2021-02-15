package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;

import java.io.IOException;

/**
 * 
 */
public interface SourceReader {

    public String getResourceName();

    public long getPosition();

    public void markPosition() throws IOException;

    public void resetPosition() throws IOException;

    public String readLine() throws IOException;

    public void close();

    public void resetPositionLine(String line);
    
}
