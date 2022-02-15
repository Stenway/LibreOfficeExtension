package com.stenway.loextensions.formats;

public class TblsImporter {
	private final SpreadsheetDocument spreadsheetDocument;
	private final FilterStream stream;
	
	public TblsImporter(SpreadsheetDocument spreadsheetDocument, FilterStream stream) {
		this.spreadsheetDocument = spreadsheetDocument;
		this.stream = stream;
	}
	
	public void importTbls() {
		byte[] bytes = stream.readAllBytes();
		ReliableTxtDocument reliableTxtDocument = new ReliableTxtDocument(bytes);
		TblsDocument tblsDocument = TblsDocument.parse(reliableTxtDocument.getText());
		
		boolean isFirst = true;
		String defaultSheetName = spreadsheetDocument.getSpreadsheetNames()[0];
		int counter = 1;
		for (TblDocument table : tblsDocument.tables) {
			String tableName = table.meta.title;
			if (tableName == null) { tableName = "Table "+counter; }
			Spreadsheet spreadsheet = spreadsheetDocument.addSpreadsheet(tableName);
			TblImporter.setTblDocument(table, spreadsheet, 0, 0);
			counter++;
		}
		spreadsheetDocument.removeSpreadsheet(defaultSheetName);
	}
}