package com.stenway.loextensions.formats;

public class WsvImporter {
	private final SpreadsheetDocument spreadsheetDocument;
	private final FilterStream stream;
	
	public WsvImporter(SpreadsheetDocument spreadsheetDocument, FilterStream stream) {
		this.spreadsheetDocument = spreadsheetDocument;
		this.stream = stream;
	}
	
	public void importWsv() {
		byte[] bytes = stream.readAllBytes();
		ReliableTxtDocument reliableTxtDocument = new ReliableTxtDocument(bytes);
		WsvDocument wsvDocument = WsvDocument.parse(reliableTxtDocument.getText());
		
		Spreadsheet spreadsheet = spreadsheetDocument.getSpreadsheet(0);
		setWsvDocument(wsvDocument, spreadsheet, 0, 0);
	}
	
	private static void setWsvDocument(WsvDocument wsvDocument, Spreadsheet spreadsheet, int offsetX, int offsetY) {
		int rowIndex = offsetY;
		int maxColumnIndex = 0;
		for (WsvLine wsvLine : wsvDocument.Lines) {
			int columnIndex = offsetX;
			for (String wsvValue : wsvLine.Values) {
				if (wsvValue != null) {
					spreadsheet.setCellText(columnIndex, rowIndex, wsvValue);
				}
				columnIndex++;
			}
			if (columnIndex > maxColumnIndex) {
				maxColumnIndex = columnIndex;
			}
			spreadsheet.getRowCells(rowIndex).setOptimalHeight(true);
			rowIndex++;
		}
		for (int i=0; i<maxColumnIndex; i++) {
			spreadsheet.getColumnCells(i).setOptimalWidth(true);
		}
	}
}