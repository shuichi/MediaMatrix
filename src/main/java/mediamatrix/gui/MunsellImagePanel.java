/* autonoesis -- Automatic Movie Processor
 * Copyright (C) 2008 Shuichi Kurabayashi <Shuichi.Kurabayashi@acm.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package mediamatrix.gui;

import mediamatrix.munsell.ColorHistogram;
import mediamatrix.munsell.ColorHistogramScore;
import mediamatrix.munsell.ColorImpressionDataStore;
import mediamatrix.munsell.ColorImpressionKnowledge;
import mediamatrix.munsell.Correlation;
import mediamatrix.mvc.DoubleTableCellRenderer;
import mediamatrix.mvc.ImpressionWordTableCellRenderer;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;

public class MunsellImagePanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private DefaultComboBoxModel<String> csModel;
    private ColorImpressionKnowledge ci;
    private BufferedImage image;

    public MunsellImagePanel() {
        initComponents();
        csModel = new DefaultComboBoxModel<String>();
        try {
            final String[] ciList = ColorImpressionDataStore.getColorImpressionKnowledgeList();
            for (String ciName : ciList) {
                csModel.addElement(ciName);
            }
        } catch (Exception ex) {
            ErrorUtils.showDialog(ex, this);
        }
        csComboBox.setModel(csModel);
        colorList.setCellRenderer(new ColorHistogramScoreListCellRenderer());
    }

    public MunsellImagePanel(BufferedImage image, ColorImpressionKnowledge ci) {
        this();
        this.image = image;
        this.ci = ci;
        updateImage();
    }

    private void updateImage() {
        scoreTable.setDefaultRenderer(String.class, new ImpressionWordTableCellRenderer(14f, ci));
        scoreTable.setDefaultRenderer(Double.class, new DoubleTableCellRenderer(13f, 3));
        scoreTable.setRowHeight(55);
        final ColorHistogram histogram = ci.generateHistogram(image);
        final Correlation[] correlations = ci.generateMetadata(histogram);
        final BufferedImage clusteredImage = ci.createClusterdImage(image);
        imageLabel.setIcon(new ImageIcon(image));
        clusteredImageLabel.setIcon(new ImageIcon(clusteredImage));
        final ColorHistogramScore[] scores = histogram.orderedScore();
        final DefaultListModel<ColorHistogramScore> listModel = new DefaultListModel<ColorHistogramScore>();
        for (ColorHistogramScore score : scores) {
            listModel.addElement(score);
        }
        colorList.setModel(listModel);
        scoreTable.setModel(new AbstractTableModel() {

            private static final long serialVersionUID = 1L;

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public int getRowCount() {
                return correlations.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return correlations[rowIndex].getWord();
                    case 1:
                        return correlations[rowIndex].getValue();
                    default:
                        return null;
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        return Double.class;
                    default:
                        return null;
                }
            }

            @Override
            public String getColumnName(int column) {
                switch (column) {
                    case 0:
                        return "Color Scheme";
                    case 1:
                        return "Score";
                    default:
                        return null;
                }
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        imageLabel = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        clusteredImageLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        colorList = new javax.swing.JList<ColorHistogramScore>();
        jScrollPane3 = new javax.swing.JScrollPane();
        scoreTable = new javax.swing.JTable();
        csComboBox = new javax.swing.JComboBox<String>();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(imageLabel);

        aTabbedPane.addTab("Image", jScrollPane1);

        jScrollPane4.setViewportView(clusteredImageLabel);

        aTabbedPane.addTab("Clustered Image", jScrollPane4);

        jScrollPane2.setViewportView(colorList);

        aTabbedPane.addTab("Histogram", jScrollPane2);

        jScrollPane3.setViewportView(scoreTable);

        aTabbedPane.addTab("Metadata", jScrollPane3);

        add(aTabbedPane, java.awt.BorderLayout.CENTER);

        csComboBox.setMaximumSize(new java.awt.Dimension(200, 32767));
        csComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csComboBoxActionPerformed(evt);
            }
        });
        add(csComboBox, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void csComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csComboBoxActionPerformed
        try {
            ci = ColorImpressionDataStore.getColorImpressionKnowledge(csModel.getSelectedItem().toString());
        } catch (Exception ex) {
            ErrorUtils.showDialog(ex, this);
        }
        updateImage();
    }//GEN-LAST:event_csComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane aTabbedPane;
    private javax.swing.JLabel clusteredImageLabel;
    private javax.swing.JList<ColorHistogramScore> colorList;
    private javax.swing.JComboBox<String> csComboBox;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable scoreTable;
    // End of variables declaration//GEN-END:variables
}

class ColorHistogramScoreListCellRenderer extends JLabel implements ListCellRenderer<ColorHistogramScore> {

    private static final long serialVersionUID = 1L;

    public ColorHistogramScoreListCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ColorHistogramScore> list, ColorHistogramScore value, int index, boolean isSelected, boolean cellHasFocus) {
        setBackground(value.getColor());
        setText(value.toString());
        return this;
    }
}
