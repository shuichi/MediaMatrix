/* MediaMatrix -- A Programable Database Engine for Multimedia
 * Copyright (C) 2008-2010 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mediamatrix;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import mediamatrix.db.FFMpegShotSet;
import mediamatrix.gui.AboutDialog;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaMatrixDatabaseFrame;
import mediamatrix.utils.FileNameUtilities;

public class MediaMatrix {

    private MediaMatrix(){}
    
    public static void main(final String[] args) {
        FlatLightLaf.setup();

        for (String res : new String[]{"ColorEmotion_VIZ", "DominantEmotion_VIZ", "EmergentEmotion_VIZ", "Pitch_VIZ", "Tonality_VIZ"}) {
            final File aFile = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), res + ".cxmql");
            if (!aFile.exists()) {
                try {
                    final URL url = MediaMatrix.class.getResource("/mediamatrix/resources/cxmql/" + res + ".cxmql");
                    final StringWriter output;
                    try (Reader input = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                        output = new StringWriter();
                        char[] buffer = new char[4096];
                        int n = 0;
                        while (-1 != (n = input.read(buffer))) {
                            output.write(buffer, 0, n);
                        }
                    }
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile), "UTF-8"))) {
                        writer.write(output.toString());
                    }
                } catch (IOException ex) {
                }
            }
        }

        try {
            if (System.getProperty("ffmpeg") == null) {
                System.setProperty("ffmpeg", FFMpegShotSet.findExecutable("ffmpeg").getAbsolutePath());
            }
            System.setProperty("ffmpeg.version", FFMpegShotSet.version());
        } catch (IOException | InterruptedException ex) {
        }

        java.awt.EventQueue.invokeLater(() -> {
            try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                final MediaMatrixDatabaseFrame frame = new MediaMatrixDatabaseFrame();
                FlatLaf.updateUI();
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.setAboutHandler(e -> new AboutDialog(frame, true).setVisible(true));
                    desktop.setQuitHandler((e, r) -> {
                        frame.storePrefs();
                        frame.dispose();
                        System.exit(0);
                    });
                } catch (Exception ignored) {
                }
                frame.setVisible(true);
            } catch (Exception ex) {
                ErrorUtils.showDialog(ex);
            }
        });
    }
}
