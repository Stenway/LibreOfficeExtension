package com.stenway.loextensions.formats;

public class TblImporter {
	private final SpreadsheetDocument spreadsheetDocument;
	private final FilterStream stream;
	
	public TblImporter(SpreadsheetDocument spreadsheetDocument, FilterStream stream) {
		this.spreadsheetDocument = spreadsheetDocument;
		this.stream = stream;
	}
	
	public void importTbl() {
		byte[] bytes = stream.readAllBytes();
		ReliableTxtDocument reliableTxtDocument = new ReliableTxtDocument(bytes);
		TblDocument tblDocument = TblDocument.parse(reliableTxtDocument.getText());
		
		Spreadsheet spreadsheet = spreadsheetDocument.getSpreadsheet(0);
		setTblDocument(tblDocument, spreadsheet, 0, 0);
	}
	
	static void setTblDocument(TblDocument tblDocument, Spreadsheet spreadsheet, int offsetX, int offsetY) {
		int rowIndex = offsetY;
		String[] columnNames = tblDocument.getColumnNames();
		for (int i=0; i<columnNames.length; i++) {
			spreadsheet.setCellText(offsetX+i, rowIndex, columnNames[i]);
		}
		spreadsheet.getRowCells(rowIndex).setOptimalHeight(true);
		rowIndex++;
		
		String[][] rows = tblDocument.getRows();
		for (String[] row : rows) {
			for (int i=0; i<row.length; i++) {
				String currentValue = row[i];
				if (currentValue != null) {
					spreadsheet.setCellText(offsetX+i, rowIndex, currentValue);
				}
			}
			spreadsheet.getRowCells(rowIndex).setOptimalHeight(true);
			rowIndex++;
		}
		for (int i=0; i<columnNames.length; i++) {
			spreadsheet.getColumnCells(offsetX+i).setOptimalWidth(true);
		}
	}
}