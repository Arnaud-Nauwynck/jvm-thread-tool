package fr.an.jvm.thread.tool.threaddumps.parser.sourcereader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 */
public class DirectoryLNReader implements SourceReader {

   File parentDirectory;
    String fileNameList[];
    
    long baseOffsetPos;
    LineNumberReader currentReader;
    int currentFileIdx;
    int markFileIdx;
    String readAheadLine;
    
    // -----------------------------------------------------------------------
    
    public DirectoryLNReader(File parentDirectory, final String filenamePrefix) {
        this.currentFileIdx = -1;
        this.parentDirectory = parentDirectory;
        
        String[] tmpFileNameList;
        if (filenamePrefix != null) {
            tmpFileNameList = parentDirectory.list((dir, name) -> name.startsWith(filenamePrefix));
        } else {
           // all child files in directory!
            tmpFileNameList = parentDirectory.list();
        }
        
        // sort... 
        Arrays.sort(tmpFileNameList, new FilenameIndexComparator());
        this.fileNameList = tmpFileNameList;
        
    }
    
    // ------------------------------------------------------------------------
    
    private static class StringIndexPair {
        public String str;
        public int index;
        
        public static StringIndexPair parse(String name) {
           StringIndexPair res = new StringIndexPair();
            res.str = name;
            int indexLastDot = name.lastIndexOf('.');
            if (indexLastDot != -1) {
                String suffixStr = name.substring(indexLastDot+1, name.length());
               int suffixValue = -1;
                try {
                    suffixValue = Integer.parseInt(suffixStr);
                } catch(Exception ex) {
                    // not an int suffix!
                    // ignore, no rethrow
                }
               if (suffixValue >= 0) {
                    res.str = name.substring(0, indexLastDot);
                    res.index = suffixValue;
                }
            }
            return res;
        }
    }
    
    private static class FilenameIndexComparator implements Comparator<String> {
        public int compare(String str1, String str2) {
            StringIndexPair p1 = StringIndexPair.parse(str1);
            StringIndexPair p2 = StringIndexPair.parse(str2);
            int res = p1.str.compareTo(p2.str);
            if (res == 0) {
                res = (p1.index == p2.index) ? 0 : (p1.index < p2.index) ? -1 : +1;
            }
            return res;
        }
        
    }
    
    public String getResourceName() {
        try {
            return parentDirectory.getCanonicalPath();
        } catch(IOException ex) {
            return parentDirectory.getPath();
        }
    }

    public long getPosition() {
        return currentReader != null ? baseOffsetPos + currentReader.getLineNumber() : -1L;
    }

    public void markPosition() throws IOException {
        if(currentReader != null) {
            markFileIdx = currentFileIdx;
            currentReader.mark(currentReader.getLineNumber()); // ??? mark(bufferSize);
        }
    }

    public void resetPosition() throws IOException {
        if(currentReader == null)
            throw new IOException("No current file");
        if(markFileIdx != currentFileIdx) {
            throw new IOException("File cannot be reset. The mark is in the old file.");
        } else {
           currentReader.reset();
            return;
        }
    }

    public void resetPositionLine(String line) {
        this.readAheadLine = line;
    }
    
    public String readLine() throws IOException {
        String res;
        if (readAheadLine != null) {
            res = readAheadLine;
            readAheadLine = null;
        } else if(currentReader == null) {
            res = null;
            while(findNextReader()) {
                String s = currentReader.readLine();
                if (s != null) {
                    res = s;
                    break;
                }
            }
        } else {
            String s1;
            for(s1 = currentReader.readLine(); s1 == null && findNextReader(); s1 = currentReader.readLine()) {
            }
           res = s1;
        }

        return res;
    }

    private boolean findNextReader() {
        close();
        int i = fileNameList.length;
        if(currentFileIdx >= i - 1) {
            currentFileIdx = i;
            return false;
        }
        for (;;) {
           if(++currentFileIdx >= i)
                break; /* Loop/switch isn't completed */
            String s = fileNameList[currentFileIdx];
            try {
                File file = new File(parentDirectory, s);
                if (file.isDirectory()) {
                    System.out.println("Skipping directory (not recursive): " + s);
                    continue;
                }
                
                if (!file.canRead()) {
                    System.out.println("Skipping unreadable file: " + s);
                    continue;
                }
                
                System.out.println("Accepted entry: " + s);
                currentReader = new LineNumberReader(new FileReader(file));
                return true;
            } catch(FileNotFoundException filenotfoundexception) {
                filenotfoundexception.printStackTrace();
                System.out.println("Skipping entry: " + s);
                continue; /* Loop/switch isn't completed */
            }
        }
        return false;
    }

    public void close() {
        if(currentReader != null) {
            baseOffsetPos += currentReader.getLineNumber();
           try {
                currentReader.close();
            } catch(IOException ioexception) {
                ioexception.printStackTrace();
            }
        }
        currentReader = null;
    }

}