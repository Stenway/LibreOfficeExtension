package com.stenway.loextensions.formats;

import java.util.ArrayList;
import java.util.Objects;

class TblDocument {
	private ReliableTxtEncoding encoding = ReliableTxtEncoding.UTF_8;
	private final String[] columnNames;
	private final ArrayList<String[]> rows = new ArrayList<>();
	public final TblMetaData meta = new TblMetaData();
	
	public TblDocument(String... columnNames) {
		Objects.requireNonNull(columnNames);
		if (columnNames.length < 2) { throw new IllegalArgumentException("Table must have at least two columns"); }
		for (String columnName : columnNames) {
			if (columnName == null) { throw new IllegalArgumentException("Column name cannot be null"); }
		}
		this.columnNames = columnNames;
	}
	
	public String[] getColumnNames() {
		return columnNames.clone();
	}
	
	public final void setEncoding(ReliableTxtEncoding encoding) {
		Objects.requireNonNull(encoding);
		this.encoding = encoding;
	}
	
	public ReliableTxtEncoding getEncoding() {
		return encoding;
	}
	
	public void addRow(String... values) {
		if (values.length < 2) { throw new IllegalArgumentException("Row must have at least two values"); }
		if (values[0] == null) { throw new IllegalArgumentException("First row value cannot be null"); }
		if (values.length > columnNames.length) { throw new IllegalArgumentException("Row has more values than there are columns"); }
		rows.add(values);
	}
	
	public String[][] getRows() {
		return rows.toArray(new String[0][]);
	}
	
	public static TblDocument parse(String content) {
		SmlDocument smlDocument = SmlDocument.parse(content, false);
		return TblParser.parseElement(smlDocument.getRoot());
	}
}

class TblMetaData {
	public String title;
}

class TblParser {
	private static String[] combine(String name, String[] values) {
		String[] result = new String[values.length + 1];
		result[0] = name;
		System.arraycopy(values, 0, result, 1, values.length);
		return result;
	}
	
	private static void parseMeta(TblMetaData metaData, SmlElement metaElement) {
		metaData.title = getSingleStringOrNull(metaElement, "Title");
	}
	
	private static String getSingleStringOrNull(SmlElement element, String name) {
		if (element.hasAttribute(name)) {
			if (element.attributes(name).length > 1) { throw new IllegalArgumentException("Only one \""+name+"\" attribute allowed"); }
			SmlAttribute attribute = element.attribute(name);
			if (attribute.getValues().length > 1) { throw new IllegalArgumentException("Only one value in meta attribute\""+name+"\" allowed"); }
			return attribute.getString();
		} else {
			return null;
		}
	}
	
	public static TblDocument parseElement(SmlElement element) {
		if (!element.hasName("Table")) { throw new IllegalArgumentException("Not a valid table document"); }
		
		SmlAttribute[] attributes = element.attributes();
		if (attributes.length == 0) { throw new IllegalArgumentException("No column names"); }
		SmlAttribute columnNamesAttribute = attributes[0];
		for (String value : columnNamesAttribute.getValues()) {
			if (value == null) { throw new IllegalArgumentException("Column name cannot be null"); }
		}
		String[] columnNames = combine(columnNamesAttribute.getName(), columnNamesAttribute.getValues());
		TblDocument document = new TblDocument(columnNames);
		
		if (element.hasElement("Meta")) {
			if (element.elements().length > 1) { throw new IllegalArgumentException("Only one meta element is allowed"); }
			//if (!element.namedNodes()[0].isElement()) { throw new Error("Meta element must be first node")}
			parseMeta(document.meta, element.element("Meta"));
		} else {
			if (element.elements().length > 0) { throw new IllegalArgumentException("Only meta element is allowed"); }
		}

		for (int i=1; i<attributes.length; i++) {
			SmlAttribute rowAttribute = attributes[i];
			String[] rowValues = combine(rowAttribute.getName(), rowAttribute.getValues());
			document.addRow(rowValues);
		}
		return document;
	}
}

class TblsDocument {
	private ReliableTxtEncoding encoding = ReliableTxtEncoding.UTF_8;
	
	public final ArrayList<TblDocument> tables = new ArrayList<>();
	
	public final void setEncoding(ReliableTxtEncoding encoding) {
		Objects.requireNonNull(encoding);
		this.encoding = encoding;
	}
	
	public ReliableTxtEncoding getEncoding() {
		return encoding;
	}
	
	public static TblsDocument parse(String content) {
		TblsDocument document = new TblsDocument();
		SmlDocument smlDocument = SmlDocument.parse(content, false);
		SmlElement rootElement = smlDocument.getRoot();
		if (!rootElement.hasName("Tables")) { throw new IllegalArgumentException("Not a valid tables document"); }
		for (SmlElement tableElement : rootElement.elements("Table")) {
			TblDocument tableDocument = TblParser.parseElement(tableElement);
			document.tables.add(tableDocument);
		}
		return document;
	}
}