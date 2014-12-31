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

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import mediamatrix.db.FFMpegShotSet;
import mediamatrix.gui.ErrorUtils;
import mediamatrix.gui.MediaMatrixDatabaseFrame;
import mediamatrix.utils.FileNameUtilities;

public class MediaMatrix {

    public static void main(final String[] args) {

        for (String res : new String[]{"ColorEmotion_VIZ", "DominantEmotion_VIZ", "EmergentEmotion_VIZ", "Pitch_VIZ", "Tonality_VIZ"}) {
            final File aFile = new File(FileNameUtilities.getApplicationSubDirectory("CXMQL"), res + ".cxmql");
            if (!aFile.exists()) {
                try {
                    final URL url = MediaMatrix.class.getResource("/mediamatrix/resources/cxmql/" + res + ".cxmql");
                    final Reader input = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                    final StringWriter output = new StringWriter();
                    char[] buffer = new char[4096];
                    int n = 0;
                    while (-1 != (n = input.read(buffer))) {
                        output.write(buffer, 0, n);
                    }
                    input.close();
                    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile), "UTF-8"));
                    writer.write(output.toString());
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            if (System.getProperty("ffmpeg") == null) {
                System.setProperty("ffmpeg", FFMpegShotSet.findExecutable("ffmpeg").getAbsolutePath());
            }
            System.setProperty("ffmpeg.version", FFMpegShotSet.version());
        } catch (Exception ex) {
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (System.getProperty("os.name").indexOf("Windows") < 0) {
                        System.setProperty("apple.laf.useScreenMenuBar", "true");
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        final MediaMatrixDatabaseFrame frame = new MediaMatrixDatabaseFrame();
                        enableOSXFullscreen(frame);
                        frame.setVisible(true);
                        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            /*
                            if ("Nimbus".equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                SwingUtilities.updateComponentTreeUI(frame.getContentPane());
                                break;
                            }*/
                        }
                    } else {
                        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }
                        JFrame.setDefaultLookAndFeelDecorated(true);
                        final MediaMatrixDatabaseFrame frame = new MediaMatrixDatabaseFrame();
                        frame.setVisible(true);
                    }
                } catch (Exception ex) {
                    ErrorUtils.showDialog(ex);
                }
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void enableOSXFullscreen(Window window) {
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (ClassNotFoundException e1) {
        } catch (Exception e) {
        }
        try {
            Class util = Class.forName("com.apple.eawt.Application");
            Method method = util.getMethod("getApplication");
            Object app = method.invoke(null);
            Method setEnabledAboutMenu = util.getMethod("setEnabledAboutMenu", new Class[]{Boolean.class});
            Method setEnabledPreferencesMenu = util.getMethod("setEnabledPreferencesMenu", new Class[]{Boolean.class});
            setEnabledAboutMenu.invoke(app, new Object[]{false});
            setEnabledPreferencesMenu.invoke(app, new Object[]{false});
        } catch (ClassNotFoundException e1) {
        } catch (Exception e) {
        }
    }
}
