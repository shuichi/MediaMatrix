package mediamatrix.db;

import mediamatrix.io.ChronoArchiveFileFilter;
import mediamatrix.io.MIDIFileFilter;
import java.io.File;

public class FileOpenScriptGenerator {

    public String delta(File dir) {
        final StringBuffer buff = new StringBuffer();
        buff.append("DELTA INDEX ***** [");
        buff.append(dir.getAbsolutePath());
        buff.append("] WITH [COLORSCHEME=CIS2]\n");
        return buff.toString();
    }

    public String generate(File dir) {
        final StringBuffer buff = new StringBuffer();
        File[] tempFiles = dir.listFiles(new ChronoArchiveFileFilter());
        if (tempFiles.length == 0) {
            tempFiles = dir.listFiles(new MIDIFileFilter());
            if (tempFiles.length == 0) {
                buff.append("ANALYZE BY [TYPE=IMAGE]\n");
                buff.append("QUERY BY\n");
                buff.append("     any() -> QUERY\n");
                buff.append("FROM [" + dir.getAbsolutePath() + "] @(NULL)\n");
                buff.append(CXMQLParser.RANK_BY + "\n");
                buff.append("     zero() -> SCORE\n");
            } else {
                buff.append("ANALYZE BY [TYPE=MUSIC,MODE=ABSOLUTE,RATIO=1]\n");
                buff.append("QUERY BY\n");
                buff.append("     any() -> QUERY\n");
                buff.append("FROM [" + dir.getAbsolutePath() + "] @(NULL)\n");
                buff.append(CXMQLParser.RANK_BY + "\n");
                buff.append("     zero() -> SCORE\n");
            }
        } else {
            buff.append("ANALYZE BY [TYPE=VIDEO]\n");
            buff.append("QUERY BY\n");
            buff.append("     any() -> QUERY\n");
            buff.append("FROM [" + dir.getAbsolutePath() + "] @(NULL)\n");
            buff.append(CXMQLParser.RANK_BY + "\n");
            buff.append("     zero() -> SCORE\n");
        }
        return buff.toString();
    }

    public String generate(File[] files) {
        final StringBuffer buff = new StringBuffer();
        File[] tempFiles = new ChronoArchiveFileFilter().filter(files);
        if (tempFiles.length == 0) {
            tempFiles = new MIDIFileFilter().filter(files);
            if (tempFiles.length == 0) {
                buff.append("GENERATE [");
                for (int i = 0; i < files.length; i++) {
                    buff.append(files[i].getAbsolutePath());
                    if (i + 1 < files.length) {
                        buff.append(",");
                    }
                }
                buff.append("] WITH [COLORSCHEME=CIS2]\n");
            } else {
                files = tempFiles;
                buff.append("QUERY BY\n");
                buff.append("     any() -> QUERY\n");
                buff.append("FROM [");
                for (int i = 0; i < files.length; i++) {
                    buff.append(files[i].getAbsolutePath());
                    if (i + 1 < files.length) {
                        buff.append(",");
                    }
                }
                buff.append("] @(NULL)\n");
                buff.append(CXMQLParser.RANK_BY + "\n");
                buff.append("     zero() -> SCORE\n");
            }
        } else {
            files = tempFiles;
            buff.append("QUERY BY\n");
            buff.append("     any() -> QUERY\n");
            buff.append("FROM [");
            files = new ChronoArchiveFileFilter().filter(files);
            for (int i = 0; i < files.length; i++) {
                buff.append(files[i].getAbsolutePath());
                if (i + 1 < files.length) {
                    buff.append(",");
                }
            }
            buff.append("] @(NULL)\n");
            buff.append(CXMQLParser.RANK_BY + "\n");
            buff.append("     zero() -> SCORE\n");
        }
        return buff.toString();
    }
}
