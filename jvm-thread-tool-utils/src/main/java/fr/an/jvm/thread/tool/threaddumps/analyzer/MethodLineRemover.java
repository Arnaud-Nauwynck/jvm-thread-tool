package fr.an.jvm.thread.tool.threaddumps.analyzer;

import java.util.List;
import java.util.ListIterator;

import fr.an.jvm.thread.tool.threaddumps.model.DefaultThreadItemInfoVisitor;
import fr.an.jvm.thread.tool.threaddumps.model.MethodThreadLineInfo;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadInfo;



/**
 * helper to remove MethodThreadLineInfo, using methodCaterories rules
 */
public class MethodLineRemover extends DefaultThreadItemInfoVisitor { 

    private List<MethodCategory> methCategories;
    
    // -------------------------------------------------------------------------

    public MethodLineRemover(List<MethodCategory> methCategories) {
        this.methCategories = methCategories;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void caseThreadInfo(ThreadInfo p) {
        // no call super!
        
        MethodThreadLineInfo prevElt = null; 
        
        for (ListIterator<MethodThreadLineInfo> iter = p.getStack().listIterator(); iter.hasNext(); ) {
            MethodThreadLineInfo elt = iter.next();
            
            MethodCategory rule = findMethodLineRule(elt);
            
            switch(rule.getPrintMode()) {
            case ALL:
                break;
            
            case NONE:
                iter.remove();
                if (prevElt != null) {
                    prevElt.setSkipCount(prevElt.getSkipCount() + 1);
                }
                break;
            
            case START_END_ONLY: {
                MethodThreadLineInfo beginFragmentElt = elt;
                
                int skipCount = 0;
                MethodThreadLineInfo endElt = elt;
                                
                for (; iter.hasNext(); ) {
                    elt = iter.next();
                    
                    if (rule.matchesFollowing(elt)) {
                        skipCount++;
                        iter.remove();
                    } else {
                        break;
                    }
                    
//                    MethodCategory currRule = findMethodLineRule(elt);
//                    if (currRule == rule) {
//                        skipCount++;
//                    iter.remove();
//                    } else {
//                        break;
//                    }
                    
                    endElt = elt;
                }
                // restore last elt of fragment!
                // skipCount--; 
                iter.add(endElt);
                
                beginFragmentElt.setSkipCount(beginFragmentElt.getSkipCount() + skipCount);
            
                } break;
            }
            
            prevElt = elt;
        }
    }

    /** internal */
    private MethodCategory findMethodLineRule(MethodThreadLineInfo p) {
        MethodCategory res = null;
        for (MethodCategory elt : methCategories) {
            if (elt.matches(p)) {
                res = elt;
            break;
            }
        }
        if (res == null) {
            res = MethodCategory.EMPTY_RULE;
        }
        return res;
    }    
        
}
