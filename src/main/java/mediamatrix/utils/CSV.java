package mediamatrix.utils;

import java.util.ArrayList;
import java.util.List;

public class CSV {
    
    public static final char DEFAULT_SEP = ',';
    protected char fieldSep;
    protected List<String> list = new ArrayList<String>();
    

    public CSV() {
        this(DEFAULT_SEP);
    }

    
    public CSV(char sep) {
        fieldSep = sep;
    }
    
    
    public List<String> parse(String line) {
        StringBuffer sb = new StringBuffer();
        list.clear(); // discard previous, if any
        int i = 0;
        
        if (line.length() == 0) {
            list.add(line);
            return list;
        }
        
        do {
            sb.setLength(0);
            if (i < line.length() && line.charAt(i) == '"')
                i = advQuoted(line, sb, ++i); // skip quote
            else
                i = advPlain(line, sb, i);
            list.add(sb.toString());
            i++;
        } while (i < line.length());
        
        return list;
    }
    
    /** advQuoted: quoted field; return index of next separator */
    protected int advQuoted(String s, StringBuffer sb, int i) {
        int j;
        int len = s.length();
        for (j = i; j < len; j++) {
            if (s.charAt(j) == '"' && j + 1 < len) {
                if (s.charAt(j + 1) == '"') {
                    j++; // skip escape char
                } else if (s.charAt(j + 1) == fieldSep) { //next delimeter
                    j++; // skip end quotes
                    break;
                }
            } else if (s.charAt(j) == '"' && j + 1 == len) { // end quotes at
                // end of line
                break; //done
            }
            sb.append(s.charAt(j)); // regular character.
        }
        return j;
    }
    
    /** advPlain: unquoted field; return index of next separator */
    protected int advPlain(String s, StringBuffer sb, int i) {
        int j;
        
        j = s.indexOf(fieldSep, i); // look for separator
        if (j == -1) { // none found
            sb.append(s.substring(i));
            return s.length();
        } else {
            sb.append(s.substring(i, j));
            return j;
        }
    }
}