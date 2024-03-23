package mediamatrix.action;

import java.io.IOException;
import mediamatrix.io.MIDIFileFilter;
import mediamatrix.utils.IOUtilities;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import mediamatrix.gui.DialogUtils;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaDatabaseTableModel;
import mediamatrix.gui.MunsellImagePanel;
import mediamatrix.gui.MusicMediaMatrixPanel;
import mediamatrix.munsell.ColorImpressionDataStore;

public class InspectorAction extends AbstractAction {

    private static final long serialVersionUID = 7644728210126590146L;
    private final MediaDatabaseTableModel model;
    private final JTable table;

    public InspectorAction(final JTable table, final MediaDatabaseTableModel model) {
        super("Inspector");
        this.model = model;
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int rows[] = table.getSelectedRows();
        if (rows.length > 0) {
            final File file = model.getAsFile(rows[0]);
            if (new MIDIFileFilter().accept(file)) {
                DialogUtils.showDialog(file.getName() + " - MediaMatrix Inspector", new MusicMediaMatrixPanel(file), table);
            } else {
                BufferedImage image;
                try {
                    if (model.isURL(rows[0])) {
                        byte[] buff = IOUtilities.download(model.getEntityAsURL(rows[0]));
                        image = ImageIO.read(new ByteArrayInputStream(buff));
                    } else {
                        image = ImageIO.read(model.getAsFile(rows[0]));
                    }
                    DialogUtils.showDialog(model.getValueAt(rows[0], 1) + " - MediaMatrix Inspector", new MunsellImagePanel(image, ColorImpressionDataStore.getColorImpressionKnowledge("CIS2")), table);
                } catch (IOException ex) {
                    ErrorUtils.showDialog(ex, table);
                }
            }
        }
    }
}
