package ui;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.input.*;

public class TableContextMenu extends ContextMenu {

    public <S> TableContextMenu(TableView<S> table) {

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().setCellSelectionEnabled(true);

        //Item "Copy selected cells data"
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(a -> {
            StringBuilder selectedData = new StringBuilder();
            Integer prevRowIndex = null;
            Integer prevColIndex = null;
            int startColIndex = Integer.MAX_VALUE;
            for (TablePosition pos : table.getSelectionModel().getSelectedCells())
                if (pos.getColumn() < startColIndex) startColIndex = pos.getColumn();

            for (TablePosition pos : table.getSelectionModel().getSelectedCells()) {
                int rowIndex = pos.getRow();
                int colIndex = pos.getColumn();
                Object cellData = table.getColumns().get(pos.getColumn()).getCellData(rowIndex);
                String cellString = cellData != null ? cellData.toString() : "";
                if (prevRowIndex == null) selectedData.append(cellString);
                else if (prevRowIndex == rowIndex) {
                    for (int i = prevColIndex; i < colIndex; i++) selectedData.append("\t");
                    selectedData.append(cellString);
                }
                else {
                    selectedData.append("\n");
                    for (int i = startColIndex; i < colIndex; i++) selectedData.append("\t");
                    selectedData.append(cellString);
                }
                prevColIndex = colIndex;
                prevRowIndex = rowIndex;
            }
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(selectedData.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);

        });
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        this.getItems().addAll(copyItem);

        table.setRowFactory(c -> {
            TableRow<S> row = new TableRow<>();
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(this));
            return row;
        });
    }

    public MenuItem getCopyItem() {
        return this.getItems().get(0);
    }


}
