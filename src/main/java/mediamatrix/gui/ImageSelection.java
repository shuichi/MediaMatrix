package mediamatrix.gui;

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ImageSelection implements Transferable, ClipboardOwner {

    protected Image data;

    public ImageSelection(Image image) {
        this.data = image;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (DataFlavor.imageFlavor.equals(flavor)) {
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        this.data = null;
    }
}
