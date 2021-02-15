package fr.an.jvm.thread.tool.threaddumps.util;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import fr.an.jvm.thread.tool.threaddumps.model.ClassHistoDiffList;
import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ClassHistogramItemInfo;


/**
 * 
 */
public class ClassHistogramInfoListIncrPrinter {

    private PrintWriter out;
    private boolean printByteSize;
    
    private boolean printDiffToFirst = true; // otherwise diff to previous
    
    private long maxDiffForNegligible = 10;
    private int maxCountForNegligible = 0;
    
    public ClassHistogramInfoListIncrPrinter(PrintWriter out, boolean printByteSize, long maxDiffForNegligible) {
        this.out = out;
        this.printByteSize = printByteSize;
        this.maxDiffForNegligible = maxDiffForNegligible;
    }
    
    // ------------------------------------------------------------------------
    
    public static void dumpClassHistoCsvFile(File outputFile, List<ClassHistogramInfo> ls, boolean printByteSize) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(outputFile));
            System.out.println("Generating Class Histogram Csv file: " + outputFile);
            
            long maxDiffForNegligible = (printByteSize)? 100 : 5;
            ClassHistogramInfoListIncrPrinter printer = new ClassHistogramInfoListIncrPrinter(out, printByteSize, maxDiffForNegligible);
            List<ClassHistoDiffList> classDiffList = printer.computeClassDiffList(ls);
            
            List<ClassHistoDiffList> classDiffList2 = printer.simplifyRemoveClassDiffList(classDiffList);

            // TODO sort List<ClasHistoDiffList> classDiffList3 = printer.sortClassDiffList(classDiffList2);

            printer.print(classDiffList2);
            
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        } finally  {
            if (out != null) try { out.close(); } catch(Exception ex) {} 
        }
    }
    
    private List<ClassHistoDiffList> simplifyRemoveClassDiffList(List<ClassHistoDiffList> classDiffList) {
        List<ClassHistoDiffList> res = classDiffList;
        res = removeJvmInternalClassesDiffList(res);
        res = removeNegligibleClassDiffList(res);
        res = removeFinalLowerInitialClassDiffList(res);
        
        return res;
    }


    public List<ClassHistoDiffList> computeClassDiffList(List<ClassHistogramInfo> ls) {
        // first pass to collect all class => olumns
        List<String> columnClasses = new ArrayList<String>();
        for(ClassHistogramInfo ch : ls) {
            for (ClassHistogramItemInfo chitem : ch.getClassItems()) {
                String className = chitem.getClassName();
                if (!columnClasses.contains(className)) { // OPTIM? not efficient..
                    columnClasses.add(className);
                }
            }
        }

        // allocate result list
        List<ClassHistoDiffList> res = new ArrayList<ClassHistoDiffList>();  
        for(String c : columnClasses) {
            ClassHistoDiffList entry = new ClassHistoDiffList(c);
            res.add(entry);
        }

        // fill result diff
        ClassHistogramInfo firstCh = null;
        ClassHistogramInfo prevCh = null;
        for(ClassHistogramInfo ch : ls) {
            if (prevCh == null) {
                firstCh = ch;
                prevCh = ch;
                continue;
            }
            
            int colIndex = 0;
            for(String c : columnClasses) {
                ClassHistoDiffList resElt = res.get(colIndex);

                ClassHistogramInfo refCh = (printDiffToFirst)? firstCh : prevCh;
               
                // find corresponding ClassHistogramItemInfo in dump
                ClassHistogramItemInfo refChItem = refCh.findByClassName(c);
                ClassHistogramItemInfo chItem = ch.findByClassName(c);
                
                long diffValue;
                if (printByteSize) {
                long prevValue = (refChItem!=null)? refChItem.getInstanceByteSize() : 0;
                    long newValue = (chItem!=null)? chItem.getInstanceByteSize() : 0;
                    diffValue = newValue - prevValue; 
                } else {
                    int prevValue = (refChItem!=null)? refChItem.getInstanceCount() : 0;
                    int newValue = (chItem!=null)? chItem.getInstanceCount() : 0;
                    diffValue = newValue - prevValue; 
                }

                resElt.addValue(diffValue);
                
                colIndex++;    
            }
            
               prevCh = ch;
        }
        
       return res;
    }
    
    
    public List<ClassHistoDiffList> removeNegligibleClassDiffList(List<ClassHistoDiffList> ls) {
        List<ClassHistoDiffList> res = new ArrayList<ClassHistoDiffList>();
        for(ClassHistoDiffList elt : ls) {
            if (!elt.isNegligibleDiffList(maxDiffForNegligible, maxCountForNegligible)) {
                res.add(elt);
            }
        }
        return res;
    }
    

    private List<ClassHistoDiffList> removeJvmInternalClassesDiffList(List<ClassHistoDiffList> ls) {
        List<ClassHistoDiffList> res = new ArrayList<ClassHistoDiffList>();
        for(ClassHistoDiffList elt : ls) {
            String c = elt.getClassName(); 
            if (! (
                    (c.startsWith("<") && c.indexOf("Klass") != -1)
                    || c.indexOf("java.lang.reflect") != -1
                    || c.indexOf("jva.lang.Class") != -1
                    || c.indexOf("java.lang.Package") != -1
                    || c.indexOf("java.io.ObjectStreamClass") != -1
                    || c.indexOf("Ljava.io.ObjectStreamField") != -1
                    || c.indexOf("org.apache.log4j.Logger") != -1                    
                    )) {
               res.add(elt);
            }
        }
        return res;
    }
    
    public List<ClassHistoDiffList> removeFinalLowerInitialClassDiffList(List<ClassHistoDiffList> ls) {
        List<ClassHistoDiffList> res = new ArrayList<ClassHistoDiffList>();
        for(ClassHistoDiffList elt : ls) {
            List<Long> values = elt.getValues();
            long initialValue = values.get(0);
            long finalValue = values.get(values.size() - 1);
            if (finalValue > initialValue) {
                res.add(elt);
            }
        }
        return res;
    }
    
   public void print(List<ClassHistoDiffList> ls) {
        
        out.print("class/dump;");
        if (ls.size() != 0) {
            ClassHistoDiffList firstLine = ls.get(0);
            int size = firstLine.getValues().size();
            for(int i = 0; i < size; i++) {
                out.print("diff" + (i+1) + ";");
            }
        }
        out.println();
                
        for(ClassHistoDiffList classDiffList : ls) {
            out.print("\"" + classDiffList.getClassName() + "\"");
            out.print(';');
            
            for(Long diff : classDiffList.getValues()){
                out.print(Long.toString(diff));
                out.print(';');
            }

            out.println();
        }
        out.println();
    }
}