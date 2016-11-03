package com.company;

import javax.swing.table.AbstractTableModel;

/**
 * our table model to allow us to update our JTable's values
 * Created by Ryan Korteway on 11/2/16.
 */
public class TableModel extends AbstractTableModel{

    String[] ourHeaders = { "Speed", "Hostname", "File Name"};

    Object[][] ourData = { {"Welcome", "to the", "program"}};

    @Override
    public int getRowCount() {
        return ourData.length;
    }

    @Override
    public int getColumnCount() {
        return ourHeaders.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ourData[rowIndex][columnIndex];
    }

    @Override
    public void setValueAt(Object value, int row, int col){
        ourData[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void setOurData(Object[][] data){
        ourData = data;
        fireTableDataChanged();
    }
}
