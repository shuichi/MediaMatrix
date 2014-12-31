package mediamatrix.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class QueryEditor extends JTextPane {

    private static final long serialVersionUID = 1480602384993179798L;
    private static final String[] KEYWORDS = {"GENERATE", "WITH",
        "QUERY BY", "ANALYZE BY", "VISUALIZE BY", "FROM", "RANK BY"};
    private static final String[] RESERVED = {"TARGET", "QUERY", "SCORE",
        "ColorImpression", "Tonality", "RESULT", "NULL", "MODE", "RATIO",
        "EACH_ColorImpression", "EACH_Tonality", "TYPE", "COLORSCHEME",
        "width", "height", "threshold"};

    public QueryEditor() {
        super();
        setText("# Please Input CXMQL here.\n");
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        final FontMetrics fm = getFontMetrics(getFont());
        final int charWidth = fm.charWidth('m');
        final int tabLength = charWidth * 4;
        final TabStop[] tabs = new TabStop[10];
        for (int j = 0; j < tabs.length; j++) {
            tabs[j] = new TabStop((j + 1) * tabLength);
        }
        final SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setTabSet(attrs, new TabSet(tabs));
        getStyledDocument().setParagraphAttributes(0, getDocument().getLength(), attrs, false);

        getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                setHighlight();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setHighlight();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void setHighlight() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final String text = getText();
                final StyledDocument doc = getStyledDocument();
                doc.setCharacterAttributes(0, text.length(), new SimpleAttributeSet(), true);

                final SimpleAttributeSet attr2 = new SimpleAttributeSet();
                StyleConstants.setForeground(attr2, new Color(10, 100, 10));
                StyleConstants.setBold(attr2, true);
                for (int i = 0; i < RESERVED.length; i++) {
                    int pos = text.indexOf(RESERVED[i]);
                    while (pos > -1) {
                        doc.setCharacterAttributes(pos, RESERVED[i].length(), attr2, true);
                        pos = text.indexOf(RESERVED[i], pos + RESERVED[i].length());
                    }
                }

                final SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setBold(attr, true);
                StyleConstants.setForeground(attr, Color.BLUE);
                for (int i = 0; i < KEYWORDS.length; i++) {
                    int pos = text.indexOf(KEYWORDS[i]);
                    while (pos > -1) {
                        doc.setCharacterAttributes(pos, KEYWORDS[i].length(), attr, true);
                        pos = text.indexOf(KEYWORDS[i], pos + KEYWORDS[i].length());
                    }
                }
                
                final SimpleAttributeSet attr3 = new SimpleAttributeSet();
                StyleConstants.setUnderline(attr3, true);
                StyleConstants.setForeground(attr3, new Color(100, 10, 10));
                int pos = text.indexOf("FROM");
                doc.setCharacterAttributes(text.indexOf("[", pos) + 1, text.indexOf("]", pos) - text.indexOf("[", pos) - 1, attr3, true);
            }
        });
    }
}
