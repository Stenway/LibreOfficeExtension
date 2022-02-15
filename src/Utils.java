package com.stenway.loextensions.formats;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.MessageBoxType;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XToolkit;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.datatransfer.DataFlavor;
import com.sun.star.datatransfer.UnsupportedFlavorException;
import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.clipboard.XClipboard;
import com.sun.star.datatransfer.clipboard.XClipboardOwner;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XSheetCellCursor;
import com.sun.star.sheet.XSheetCellRange;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheetView;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.sheet.XUsedAreaCursor;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.CellVertJustify;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.table.XTableColumns;
import com.sun.star.table.XTableRows;
import com.sun.star.text.XTextRange;
import com.sun.star.ucb.XSimpleFileAccess;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.ui.dialogs.TemplateDescription;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.ui.dialogs.XFilterManager;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.io.ByteArrayOutputStream;

class Utils {
	private static XComponentContext componentContext;
	private static XMultiComponentFactory multiComponentFactory;
	private static XDesktop desktop;
	
	public static void init(XComponentContext componentContext) {
		Utils.componentContext = componentContext;
		multiComponentFactory = componentContext.getServiceManager();
		desktop = createInstance(XDesktop.class, "com.sun.star.frame.Desktop");
	}
	
	public static SpreadsheetDocument createSpreadsheetDocument() {
		try {
			XComponentLoader componentLoader = getInterface(XComponentLoader.class, desktop);
			PropertyValue[] emptyArgs = new PropertyValue[0];
			XComponent component = componentLoader.loadComponentFromURL("private:factory/scalc", "_blank", 0, emptyArgs);
			XSpreadsheetDocument spreadsheetDocument = getInterface(XSpreadsheetDocument.class, component);
			return new SpreadsheetDocument(spreadsheetDocument, true);
		} catch (Exception e) {
			throw new RuntimeException("Could not create new spreadsheet document", e);
		}
	}
		
	public static SpreadsheetDocument getSpreadsheetDocument() {
		XComponent component = desktop.getCurrentComponent();
		XSpreadsheetDocument spreadsheetDocument = getInterface(XSpreadsheetDocument.class, component);
		return new SpreadsheetDocument(spreadsheetDocument, true);
	}
		
	public static <T> T createInstance(Class<T> cls, String name) {
		try {
			Object obj = multiComponentFactory.createInstanceWithContext(name, componentContext); 
			return UnoRuntime.queryInterface(cls, obj);
		} catch (Exception e) {
			throw new RuntimeException("Could not create instance of '"+name+"'", e);
		}
	}
	
	public static <T> T getInterface(Class<T> cls, Object obj) {
		if (obj == null) { throw new RuntimeException("Could not get interface for '"+cls+"' because object is null");}
		T result = UnoRuntime.queryInterface(cls, obj);
		if (result == null) { throw new RuntimeException("Could not get interface for '"+cls+"'"); }
		return result;
	}
	
	public static void showErrorMessage(String text) {
		showMessageBox(MessageBoxType.ERRORBOX, "Error", text);
	}
	
	public static void showErrorMessage(String text, Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		text += "\n\n"+sw.toString();
		showErrorMessage(text);
	}
	
	public static void showDebugMessage(String text) {
		showMessageBox(MessageBoxType.WARNINGBOX, "Debug", text);
	}
	
	private static void showMessageBox(MessageBoxType type, String title, String text) {
		XToolkit toolkit = createInstance(XToolkit.class, "com.sun.star.awt.Toolkit");
		XMessageBoxFactory messageBoxFactory = getInterface(XMessageBoxFactory.class, toolkit);
		XMessageBox messageBox = messageBoxFactory.createMessageBox(null, type, MessageBoxButtons.BUTTONS_OK, title, text);
		messageBox.execute();
	}
	
	public static void setProperty(Object obj, String propertyName, Object value) {
		XPropertySet propertySet = getInterface(XPropertySet.class, obj);
		setProperty(propertySet, propertyName, value);
	}
	
	public static void setProperty(XPropertySet propertySet, String propertyName, Object value) {
		try {
			propertySet.setPropertyValue(propertyName, value);
		} catch(Exception e) {
			throw new RuntimeException("Could not set property '"+propertyName+"'", e);
		}
	}
	
	public static void copyTextToClipboard(String text) {
		XClipboard clipboard = createInstance(XClipboard.class, "com.sun.star.datatransfer.clipboard.SystemClipboard");
		ClipboardOwner clipboardOwner = new ClipboardOwner();
		TextTransferable transferable = new TextTransferable(text);
		clipboard.setContents(transferable, clipboardOwner);
	}
	
	public static String getClipboardText() {
		try {
			XClipboard clipboard = createInstance(XClipboard.class, "com.sun.star.datatransfer.clipboard.SystemClipboard");
			XTransferable transferable = clipboard.getContents();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor.MimeType.equalsIgnoreCase("text/plain;charset=utf-16")) {
					Object data = transferable.getTransferData(flavor);      
					String text = AnyConverter.toString(data);
					return text;
				}
			}
		} catch (Exception e) {
			
		}
		return null;
	}
}

class SpreadsheetDocument {
	private XSpreadsheetDocument document;
	private XSpreadsheets spreadsheets;
	private XModel model;
	private XController controller;
	private XSpreadsheetView spreadsheetView;
	
	public SpreadsheetDocument(XSpreadsheetDocument document, boolean withController) {
		this.document = document;
		spreadsheets = document.getSheets();
		model = Utils.getInterface(XModel.class, document);
		if (withController) {
			controller = model.getCurrentController();
			spreadsheetView = Utils.getInterface(XSpreadsheetView.class, controller);
		}
	}
	
	public Spreadsheet getSpreadsheet(int index) {
		try {
			XIndexAccess indexAccess = Utils.getInterface(XIndexAccess.class, spreadsheets);
			Object obj = indexAccess.getByIndex(index);
			XSpreadsheet spreadsheet = Utils.getInterface(XSpreadsheet.class, obj);
			return new Spreadsheet(spreadsheet);
		} catch (Exception e) {
			throw new RuntimeException("Could not get sheet at index "+index);
		}
	}
	
	public Spreadsheet getActiveSpreadsheet() {
		Objects.requireNonNull(spreadsheetView);
		XSpreadsheet spreadsheet = spreadsheetView.getActiveSheet();
		return new Spreadsheet(spreadsheet);
	}
	
	public Rectangle getSelectionDimensions() {
		XCellRangeAddressable cellRangeAddressable = Utils.getInterface(XCellRangeAddressable.class, model.getCurrentSelection());
		if (cellRangeAddressable == null) {
			return null;
		}
		CellRangeAddress cellRangeAddress = cellRangeAddressable.getRangeAddress();
		return new Rectangle(cellRangeAddress);
	}

	public String[] getSpreadsheetNames() {
		return spreadsheets.getElementNames();
	}
	
	public Spreadsheet getSpreadsheet(String name) {
		try {
			XSpreadsheet spreadsheet = Utils.getInterface(XSpreadsheet.class, spreadsheets.getByName(name));
			return new Spreadsheet(spreadsheet);
		} catch (Exception e) {
			throw new RuntimeException("Could not get sheet '"+name+"'", e);
		}
	}
	
	public boolean hasSpreadsheet(String name) {
		return spreadsheets.hasByName(name);
	}
	
	public Spreadsheet addSpreadsheet(String name) {
		short newIndex = (short)spreadsheets.getElementNames().length;
		try {
			spreadsheets.insertNewByName(name, newIndex);
		} catch (Exception e) {
			throw new RuntimeException("Could not add spreadsheet with name '"+name+"'", e);
		}
		return getSpreadsheet(name);
	}

	public void removeSpreadsheet(String name) {
		try {
			spreadsheets.removeByName(name);
		} catch (Exception e) {
			throw new RuntimeException("Could not remove spreadsheet with name '"+name+"'", e);
		}
	}
}

class Spreadsheet {
	private XSpreadsheet spreadsheet;
	
	public Spreadsheet(XSpreadsheet spreadsheet) {
		Objects.requireNonNull(spreadsheet);
		this.spreadsheet = spreadsheet;
	}
	
	public String getCellText(int x, int y) {
		try {
			XCell cell = spreadsheet.getCellByPosition(x, y);
			XTextRange textRange = Utils.getInterface(XTextRange.class, cell);
			return textRange.getString();
		} catch (Exception e) {
			throw new RuntimeException("Could not get cell text", e);
		}
	}
	
	public void setCellText(int x, int y, String str) {
		try {
			XCell cell = spreadsheet.getCellByPosition(x, y);
			XTextRange textRange = Utils.getInterface(XTextRange.class, cell);
			textRange.setString(str);
		} catch (Exception e) {
			throw new RuntimeException("Could not set cell text", e);
		}
	}
	
	public CellRange getColumnCells(int columnIndex) {
		XColumnRowRange columnRowRange = Utils.getInterface(XColumnRowRange.class, spreadsheet);
		XTableColumns tableColumns = columnRowRange.getColumns();
		XIndexAccess indexAccess = Utils.getInterface(XIndexAccess.class, tableColumns);
		try {
			Object obj = indexAccess.getByIndex(columnIndex);
			XCellRange cellRange = Utils.getInterface(XCellRange.class, obj);
			return new CellRange(cellRange);
		} catch (Exception e) {
			throw new RuntimeException("Could not access column "+columnIndex+"'", e); 
		}
	}
	
	public CellRange getRowCells(int rowIndex) {
		XColumnRowRange columnRowRange = Utils.getInterface(XColumnRowRange.class, spreadsheet);
		XTableRows tableRows = columnRowRange.getRows();
		XIndexAccess indexAccess = Utils.getInterface(XIndexAccess.class, tableRows);
		try {
			Object obj = indexAccess.getByIndex(rowIndex);
			XCellRange cellRange = Utils.getInterface(XCellRange.class, obj);
			return new CellRange(cellRange);
		} catch (Exception e) {
			throw new RuntimeException("Could not access row "+rowIndex+"'", e); 
		}
	}
	
	public CellRange getUsedArea() {
		XSheetCellCursor sheetCellCursor = spreadsheet.createCursor();
		XUsedAreaCursor usedAreaCursor = Utils.getInterface(XUsedAreaCursor.class, sheetCellCursor); 
		usedAreaCursor.gotoStartOfUsedArea(false);
		usedAreaCursor.gotoEndOfUsedArea(true);
		XCellRange usedRange = Utils.getInterface(XCellRange.class, usedAreaCursor); 
		return new CellRange(usedRange); 
	}
}

class CellRange {
	private XCellRange cellRange;
	
	public CellRange(XCellRange cellRange) {
		this.cellRange = cellRange;
	}
	
	public void setOptimalHeight(boolean enabled) {
		Utils.setProperty(cellRange, "OptimalHeight", enabled);
		
		XSheetCellRange sheetCellRange = Utils.getInterface(XSheetCellRange.class, cellRange);
		XPropertySet propertySet = Utils.getInterface(XPropertySet.class, sheetCellRange);
		Utils.setProperty(propertySet, "VertJustify", CellVertJustify.CENTER);
	}
	
	public void setOptimalWidth(boolean enabled) {
		Utils.setProperty(cellRange, "OptimalWidth", enabled);
	}
	
	public Rectangle getDimensions() {
		XCellRangeAddressable cellRangeAddressable = Utils.getInterface(XCellRangeAddressable.class, cellRange);
		CellRangeAddress cellRangeAddress = cellRangeAddressable.getRangeAddress();
		return new Rectangle(cellRangeAddress);
	}
}

class Rectangle {
	public int StartX;
	public int StartY;
	public int EndX;
	public int EndY;
	
	public Rectangle(int startX, int startY, int endX, int endY) {
		StartX = startX;
		StartY = startY;
		EndX = endX;
		EndY = endY;
	}
	
	public Rectangle(CellRangeAddress cellRangeAddress) {
		this(cellRangeAddress.StartColumn,
				cellRangeAddress.StartRow,
				cellRangeAddress.EndColumn,
				cellRangeAddress.EndRow);
	}
	
	public boolean isSingleCell() {
		return StartX == EndX && StartY == EndY;
	}
}

class TextTransferable implements XTransferable {
	private final String text;  
	private final String mimeType = "text/plain;charset=utf-16"; 
	
	public TextTransferable(String text) {	
		this.text = text;  
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {	
		return flavor.MimeType.equalsIgnoreCase(mimeType);
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {	
		if (!flavor.MimeType.equalsIgnoreCase(mimeType))
				throw new UnsupportedFlavorException();
		return text;
	}
	
	public DataFlavor[] getTransferDataFlavors() {	
		DataFlavor flavor = new DataFlavor(mimeType, "Unicode Text", new Type(String.class));
		return new DataFlavor[] {flavor};
	}
}

class ClipboardOwner implements XClipboardOwner { 
	private boolean isOwner = true;
	
	public void lostOwnership(XClipboard clipboard, XTransferable transferable) {
		isOwner = false;
	}
	
	public boolean isClipboardOwner() {
		return isOwner;
	}
}

class FileDialog {
	public String Title = "Open";
	
	private String selectedFilePath = null;
	private ArrayList<String[]> filters = new ArrayList<>();
	
	private boolean isSaveDialog;
	private String initialDirectory;
	
	public FileDialog(String title) {
		Title = title;
	}
	
	public void addFilter(String title, String filter) {
		String[] filterObj = new String[] {title, filter};
		filters.add(filterObj);
	}
	
	public void setSaveDialog() {
		isSaveDialog = true;
	}
	
	public void setInitialDirectory(String initialDirectory) {
		this.initialDirectory = initialDirectory;
	}
	
	public boolean show() {
		XFilePicker filePicker = Utils.createInstance(XFilePicker.class, "com.sun.star.ui.dialogs.FilePicker");
		filePicker.setTitle(Title);
				
		try {
			if (isSaveDialog) {
				XInitialization initialization = Utils.getInterface(XInitialization.class, filePicker);
				Short[] templateDescription = new Short[] {TemplateDescription.FILESAVE_AUTOEXTENSION};
				initialization.initialize(templateDescription);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
		XFilterManager filterManager = Utils.getInterface(XFilterManager.class, filePicker);
		for (String[] filter : filters) {
			String filterTitle = filter[0];
			String filterValue = filter[1];
			filterManager.appendFilter(filterTitle, filterValue);
		}
		
		if (initialDirectory != null) {
			String initialDirectoryUrl = Paths.get(initialDirectory).toUri().toString();
			filePicker.setDisplayDirectory(initialDirectoryUrl);
		}
		
		short result = filePicker.execute();
		if (result != ExecutableDialogResults.OK) {
			return false;
		}
		String[] fileUrls = filePicker.getFiles();
		String firstFileUrl = fileUrls[0];
		try {
			String filePathStr = Paths.get(new URL(firstFileUrl).toURI()).toString();
			selectedFilePath = filePathStr;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return true;
	}
	
	public String getSelectedFilePath() {
		return selectedFilePath;
	}
}

class FilterStream {
	public XInputStream inputStream;
	public XOutputStream outputStream;
	public String fileName;
	public boolean isOwner;

	public FilterStream(boolean isImport, PropertyValue[] propertyValues) {
		try {
			for (PropertyValue propertyValue : propertyValues) {
				if (propertyValue.Name.equals("FileName")) {
					fileName = AnyConverter.toString(propertyValue.Value);
				} else if (propertyValue.Name.equals("InputStream")) {
					inputStream = (XInputStream)AnyConverter.toObject(new Type(XInputStream.class), propertyValue.Value);
				} else if (propertyValue.Name.equals("OutputStream")) {
					outputStream = (XOutputStream)AnyConverter.toObject(new Type(XOutputStream.class), propertyValue.Value);
				}
			}
			
			if (inputStream == null && outputStream == null && fileName != null) {
				XSimpleFileAccess simpleFileAccess = Utils.createInstance(XSimpleFileAccess.class, "com.sun.star.ucb.SimpleFileAccess");
				if (simpleFileAccess != null) {
					if (isImport) { 
						inputStream = simpleFileAccess.openFileRead(fileName);
						isOwner = true;
					} else {
						outputStream = simpleFileAccess.openFileWrite(fileName);
						isOwner = true;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Filter stream error", e);
		}
		if (!(inputStream != null || outputStream != null)) { throw new RuntimeException("FilterStream initialization failed"); }
	}
	
	public byte[] readAllBytes() {
		byte[] byteArray;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			int maxChunkSize = 4096;
			byte[][] chunkBuffer = new byte[1][];
			while (true) {
				int numReadBytes = inputStream.readBytes(chunkBuffer, maxChunkSize);
				if (numReadBytes == 0) { break; }
				buffer.write(chunkBuffer[0], 0, numReadBytes);
				if (numReadBytes < maxChunkSize) { break; }
			}
			
			byteArray = buffer.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Reading stream failed", e);
		}
		return byteArray;
	}
	
	public void close() {
		try {
			if (isOwner && inputStream != null) { inputStream.closeInput(); }
			else if (isOwner && outputStream != null) { outputStream.closeOutput(); }
		} catch (Exception e) {
			throw new RuntimeException("Close stream failed", e);
		}
		isOwner = false;
		
	}
}