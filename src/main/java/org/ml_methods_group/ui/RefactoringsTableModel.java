/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.ui;

import com.intellij.ui.BooleanTableCellRenderer;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RefactoringsTableModel extends AbstractTableModel {
    private static final String UNIT_COLUMN_TITLE_KEY = "unit.column.title";
    private static final String MOVE_TO_COLUMN_TITLE_KEY = "move.to.column.title";
    public static final int SELECTION_COLUMN_INDEX = 0;
    public static final int UNIT_COLUMN_INDEX = 1;
    public static final int MOVE_TO_COLUMN_INDEX = 2;
    private static final int COLUMNS_COUNT = 3;

    private final List<String> units = new ArrayList<>();
    private final List<String> movements = new ArrayList<>();
    private final boolean[] isSelected;
    private final boolean[] isEnabled;

    RefactoringsTableModel(Map<String, String> refactorings) {
        for (Entry<String, String> refactoring : refactorings.entrySet()) {
            if (refactoring.getKey() == null || refactoring.getValue() == null) {
                continue;
            }
            units.add(refactoring.getKey());
            movements.add(refactoring.getValue());
        }
        isSelected = new boolean[units.size()];
        isEnabled = new boolean[units.size()];
        Arrays.fill(isEnabled, true);
    }

    public void selectAll() {
        Arrays.fill(isSelected, true);
        fireTableDataChanged();
    }

    public Map<String, String> getSelected() {
        Map<String, String> result = IntStream.range(0, isSelected.length)
                .filter(i -> isSelected[i])
                .peek(i -> isEnabled[i] = false)
                .boxed()
                .collect(Collectors.toMap(units::get, movements::get));
        fireTableDataChanged();
        return result;
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case SELECTION_COLUMN_INDEX:
                return "";
            case UNIT_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(UNIT_COLUMN_TITLE_KEY);
            case MOVE_TO_COLUMN_INDEX:
                return ArchitectureReloadedBundle.message(MOVE_TO_COLUMN_TITLE_KEY);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == SELECTION_COLUMN_INDEX && isEnabled[rowIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == SELECTION_COLUMN_INDEX ? Boolean.class : String.class;
    }

    @Override
    public int getRowCount() {
        return units.size();
    }


    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        isSelected[rowIndex] = (Boolean) value;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    @Nullable
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case SELECTION_COLUMN_INDEX:
                return isSelected[rowIndex];
            case UNIT_COLUMN_INDEX:
                return units.get(rowIndex);
            case MOVE_TO_COLUMN_INDEX:
                return movements.get(rowIndex);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + columnIndex);
    }

    public String getUnitAt(int row, int column) {
        switch (column) {
            case UNIT_COLUMN_INDEX:
                return units.get(row);
            case MOVE_TO_COLUMN_INDEX:
                return movements.get(row);
        }
        throw new IndexOutOfBoundsException("Unexpected column index: " + column);
    }
}
