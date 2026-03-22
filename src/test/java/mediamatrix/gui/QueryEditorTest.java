package mediamatrix.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QueryEditorTest {

    @BeforeAll
    static void setHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void highlightsKeywordsAtDocumentOffsetsForCrLfText()
            throws InvocationTargetException, InterruptedException, BadLocationException {
        final QueryEditor editor = onEdt(QueryEditor::new);
        onEdt(() -> editor.setText("GENERATE A\r\nFROM [abc]\r\nQUERY BY B\r\n"));
        flushEdt();

        final StyledDocument document = editor.getStyledDocument();
        assertEquals("GENERATE A\nFROM [abc]\nQUERY BY B\n", document.getText(0, document.getLength()));

        assertStyled(document, 11, "F", true, false);
        assertStyled(document, 15, " ", false, false);
        assertStyled(document, 22, "Q", true, false);
        assertStyled(document, 30, " ", false, false);
    }

    @Test
    void underlinesBracketContentsAfterFromForCrLfText()
            throws InvocationTargetException, InterruptedException, BadLocationException {
        final QueryEditor editor = onEdt(QueryEditor::new);
        onEdt(() -> editor.setText("GENERATE A\r\nFROM [abc]\r\n"));
        flushEdt();

        final StyledDocument document = editor.getStyledDocument();
        assertStyled(document, 17, "a", false, true);
        assertStyled(document, 18, "b", false, true);
        assertStyled(document, 19, "c", false, true);
        assertStyled(document, 16, "[", false, false);
        assertStyled(document, 20, "]", false, false);
    }

    private static void assertStyled(StyledDocument document, int offset, String expectedChar,
            boolean bold, boolean underline) throws BadLocationException {
        assertEquals(expectedChar, document.getText(offset, 1));
        assertEquals(bold, StyleConstants.isBold(document.getCharacterElement(offset).getAttributes()));
        assertEquals(underline, StyleConstants.isUnderline(document.getCharacterElement(offset).getAttributes()));
    }

    private static void flushEdt() throws InvocationTargetException, InterruptedException {
        onEdt(() -> {
        });
    }

    private static void onEdt(ThrowingRunnable action) throws InvocationTargetException, InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                action.run();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return;
        }
        final Exception[] thrown = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                action.run();
            } catch (Exception ex) {
                thrown[0] = ex;
            }
        });
        if (thrown[0] != null) {
            throw new RuntimeException(thrown[0]);
        }
    }

    private static <T> T onEdt(ThrowingSupplier<T> supplier) throws InvocationTargetException, InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                return supplier.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        final Object[] result = new Object[1];
        final Exception[] thrown = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                result[0] = supplier.get();
            } catch (Exception ex) {
                thrown[0] = ex;
            }
        });
        if (thrown[0] != null) {
            throw new RuntimeException(thrown[0]);
        }
        @SuppressWarnings("unchecked")
        final T value = (T) result[0];
        return value;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
