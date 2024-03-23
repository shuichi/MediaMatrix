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
package mediamatrix.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.TreeMap;

public class MediaMatrix implements Serializable {

    private static final long serialVersionUID = 7505454054798245286L;
    private TreeMap<Double, Integer> rowIndex;
    private TreeMap<String, Integer> columnIndex;
    private double[] row;
    private String[] column;
    private float[][] values;
    private String id;

    public MediaMatrix() {
        this.row = new double[0];
        this.column = new String[0];
    }

    public MediaMatrix(double[] row, String[] column) {
        this.row = row;
        this.column = column;
        this.columnIndex = new TreeMap<>();
        this.rowIndex = new TreeMap<>();
        for (int i = 0; i < column.length; i++) {
            columnIndex.put(column[i], i);
        }
        for (int i = 0; i < row.length; i++) {
            rowIndex.put(row[i], i);
        }
        values = new float[row.length][column.length];
        for (int i = 0; i < row.length; i++) {
            for (int j = 0; j < column.length; j++) {
                values[i][j] = 0.0f;
            }
        }
    }

    public void addColumn(String name, float[] rowVector) {
        float[][] newValues = new float[row.length][column.length + 1];
        for (int i = 0; i < row.length; i++) {
            for (int j = 0; j < column.length; j++) {
                newValues[i][j] = values[i][j];
            }
        }
        for (int i = 0; i < row.length; i++) {
            newValues[i][column.length] = rowVector[i];
        }
        this.values = newValues;
        final String[] newColumn = new String[column.length + 1];
        System.arraycopy(column, 0, newColumn, 0, column.length);
        newColumn[newColumn.length - 1] = name;
        this.column = newColumn;
        this.columnIndex = new TreeMap<>();
        this.rowIndex = new TreeMap<>();
        for (int i = 0; i < column.length; i++) {
            columnIndex.put(column[i], i);
        }
        for (int i = 0; i < row.length; i++) {
            rowIndex.put(row[i], i);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MediaMatrix(" + row.length + " x " + column.length + ")";
    }

    public void export(String header, Writer out) throws IOException {
        out.append(header + ",");
        for (int j = 0; j < getWidth(); j++) {
            out.append(getColumn(j));
            if (j + 1 < getWidth()) {
                out.append(",");
            }
        }
        out.append("\n");
        for (int i = 0; i < getHeight(); i++) {
            out.append(getRow(i) + ", ");
            for (int j = 0; j < getWidth(); j++) {
                out.append(Double.toString(get(i, j)));
                if (j + 1 < getWidth()) {
                    out.append(",");
                }
            }
            out.append("\n");
        }
        out.flush();
        out.close();
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(row.length);
        for (int i = 0; i < row.length; i++) {
            out.writeDouble(row[i]);
        }
        out.writeInt(column.length);
        for (int i = 0; i < column.length; i++) {
            out.writeUTF(column[i]);
        }
        for (int i = 0; i < row.length; i++) {
            for (int j = 0; j < column.length; j++) {
                out.writeDouble(values[i][j]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void read(DataInputStream in) throws IOException {
        row = new double[in.readInt()];
        for (int i = 0; i < row.length; i++) {
            row[i] = in.readDouble();
        }
        column = new String[in.readInt()];
        for (int i = 0; i < column.length; i++) {
            column[i] = in.readUTF();
        }
        columnIndex = new TreeMap<>();
        rowIndex = new TreeMap<>();
        for (int i = 0; i < column.length; i++) {
            columnIndex.put(column[i], i);
        }
        for (int i = 0; i < row.length; i++) {
            rowIndex.put(row[i], i);
        }
        values = new float[row.length][column.length];
        for (int i = 0; i < row.length; i++) {
            for (int j = 0; j < column.length; j++) {
                values[i][j] = (float) in.readDouble();
            }
        }
    }

    public boolean containRow(double v) {
        return rowIndex.containsKey(v);
    }

    public boolean containColumn(String c) {
        return columnIndex.containsKey(c);
    }

    public int getRowIndex(double v) {
        return rowIndex.get(v);
    }

    public double getRow(int index) {
        return row[index];
    }

    public String getColumn(int index) {
        return column[index];
    }

    public String[] getColumns() {
        return column;
    }

    public double[] getRows() {
        return row;
    }

    public double get(Double row, String column) {
        return values[rowIndex.get(row)][columnIndex.get(column)];
    }

    public double get(int row, int column) {
        return values[row][column];
    }

    public float[] getRowVector(int row) {
        return values[row];
    }

    public void set(Double row, String column, double value) {
        if (!rowIndex.containsKey(row)) {
            throw new IllegalArgumentException(row + " is undefined");
        }
        if (!columnIndex.containsKey(column)) {
            throw new IllegalArgumentException(column + " is undefined");
        }
        values[rowIndex.get(row)][columnIndex.get(column)] = (float) value;
    }

    public int getHeight() {
        return rowIndex.size();
    }

    public int getWidth() {
        return columnIndex.size();
    }
}
