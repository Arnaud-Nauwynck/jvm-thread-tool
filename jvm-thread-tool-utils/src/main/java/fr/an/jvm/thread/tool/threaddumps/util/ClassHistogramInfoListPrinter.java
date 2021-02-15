package fr.an.jvm.thread.tool.threaddumps.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramItemInfo;


/**
 * 
 */
public class ClassHistogramInfoListPrinter {

    private PrintWriter out;
    private boolean printByteSize;
    private boolean printIncremental;    

    // -------------------------------------------------------------------------

    public ClassHistogramInfoListPrinter(PrintWriter out, boolean printByteSize, boolean printIncremental) {
        this.out = out;
        this.printByteSize = printByteSize;
        this.printIncremental = printIncremental;
    }
    
    // -------------------------------------------------------------------------
    
    public static void dumpClassHistoCsvFile(File outputFile, List<ClassHistogramInfo> ls, boolean printByteSize, boolean printIncremental) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(outputFile));
            System.out.println("Generatin Class Histogram Csv file: " + outputFile);

            ClassHistogramInfoListPrinter printer = new ClassHistogramInfoListPrinter(out, printByteSize, printIncremental);
            printer.print(ls);
            
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        } finally  {
            if (out != null) try { out.close(); } catch(Exception ex) {} 
        }
    }
    
    public void print(List<ClassHistogramInfo> ls) {
        // first pass to collect all class => columns
        List<String> columnClasses = new ArrayList<String>();
        for(ClassHistogramInfo ch : ls) {
            for (ClassHistogramItemInfo chitem : ch.getClassItems()) {
                String className = chitem.getClassName();
                if (!columnClasses.contains(className)) { // OPTIM? not efficient..
                    columnClasses.add(className);
                }
            }
        }

        
        out.print("dump/class; ");
        for(String c : columnClasses) {
            out.print("\"" + c + "\"");
            out.print(';');
        }
        out.println();
        
        int dumpIndex = 0;
       ClassHistogramInfo prevCh = null;
        for(ClassHistogramInfo ch : ls) {
            out.print(Integer.toString(dumpIndex));
            out.print(';');
            for(String c : columnClasses) {
                // find corresponding ClassHistogramItemInfo in dump
                ClassHistogramItemInfo chitem = ch.findByClassName(c);
                if (chitem != null) {
                    ClassHistogramItemInfo prevChitem = null;
                    if (printIncremental && prevCh != null) {
                        prevChitem = prevCh.findByClassName(c);
                    }
                        
                    if (printByteSize) {
                        long value;
                        if (printIncremental && prevCh != null) {
                            long prevValue = (prevChitem!=null)? prevChitem.getInstanceByteSize() : 0;
                            value = chitem.getInstanceByteSize() - prevValue;
                        } else {
                            value = chitem.getInstanceByteSize();
                        }
                        out.print(Long.toString(value));
                    } else {
                        int value;
                        if (printIncremental && prevCh != null) {
                            int prevValue = (prevChitem!=null)? prevChitem.getInstanceCount() : 0;
                        value = chitem.getInstanceCount() - prevValue;
                        } else {
                            value = chitem.getInstanceCount();
                        }
                        out.print(Integer.toString(value));
                    }
                } else {
                    out.print(0);
                }
                out.print(';');
           }
            
            out.println();
            dumpIndex++;
            prevCh = ch;
        }
    }
}
