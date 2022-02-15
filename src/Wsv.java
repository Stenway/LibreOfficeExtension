package com.stenway.loextensions.formats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

class WsvChar {
	public static boolean isWhitespace(int c) {
		return c == 0x09 || 
				(c >= 0x0B && c <= 0x0D) ||
				c == 0x0020 ||
				c == 0x0085 ||
				c == 0x00A0 ||
				c == 0x1680 ||
				(c >= 0x2000 && c <= 0x200A) ||
				c == 0x2028 ||
				c == 0x2029 ||
				c == 0x202F ||
				c == 0x205F ||
				c == 0x3000;
	}
	
	public static int[] getWhitespaceCodePoints() {
		return new int[] {
			0x0009,
			0x000B,
			0x000C,
			0x000D,
			0x0020,
			0x0085,
			0x00A0,
			0x1680,
			0x2000,
			0x2001,
			0x2002,
			0x2003,
			0x2004,
			0x2005,
			0x2006,
			0x2007,
			0x2008,
			0x2009,
			0x200A,
			0x2028,
			0x2029,
			0x202F,
			0x205F,
			0x3000
		};
	}
}

class WsvLine {
	public String[] Values;
	
	String[] whitespaces;
	String comment;

	public WsvLine() {
		
	}
	
	public WsvLine(String... values) {
		Values = values;
		
		whitespaces = null;
		comment = null;
	}

	public WsvLine(String[] values, String[] whitespaces, String comment) {
		Values = values;
		
		setWhitespaces(whitespaces);
		setComment(comment);
	}
	
	public boolean hasValues() {
		return Values != null && Values.length > 0;
	}
	
	public void setValues(String... values) {
		Values = values;
	}
	
	public final void setWhitespaces(String... whitespaces) {
		validateWhitespaces(whitespaces);
		this.whitespaces = whitespaces;
	}
	
	public static void validateWhitespaces(String... whitespaces) {
		if (whitespaces != null) {
			for (String whitespace : whitespaces) {
				if (whitespace != null && whitespace.length() > 0 && !WsvString.isWhitespace(whitespace)) {
					throw new IllegalArgumentException(
							"Whitespace value contains non whitespace character or line feed");
				}
			}
		}
	}
	
	public String[] getWhitespaces() {
		if (whitespaces == null) {
			return null;
		}
		return whitespaces.clone();
	}
	
	public final void setComment(String comment) {
		validateComment(comment);
		this.comment = comment;
	}
	
	public static void validateComment(String comment) {
		if (comment != null && comment.indexOf('\n') >= 0) {
			throw new IllegalArgumentException(
					"Line feed in comment is not allowed");
		}
	}
	
	public String getComment() {
		return comment;
	}
	
	void set(String[] values, String[] whitespaces, String comment) {
		Values = values;
		this.whitespaces = whitespaces;
		this.comment = comment;
	}

	@Override
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean preserveWhitespaceAndComment) {
		if (preserveWhitespaceAndComment) {
			return WsvSerializer.serializeLine(this);
		} else {
			return WsvSerializer.serializeLineNonPreserving(this);
		}
	}
	
	public static WsvLine parse(String content) {
		return parse(content, true);
	}
	
	public static WsvLine parse(String content, boolean preserveWhitespaceAndComment) {
		if (preserveWhitespaceAndComment) {
			return WsvParser.parseLine(content);
		} else {
			return WsvParser.parseLineNonPreserving(content);
		}
	}
	
	public static String[] parseAsArray(String content) {
		return WsvParser.parseLineAsArray(content);
	}
}

class WsvDocument {
	public final ArrayList<WsvLine> Lines = new ArrayList<WsvLine>();
	
	private ReliableTxtEncoding encoding;
	
	public WsvDocument() {
		this(ReliableTxtEncoding.UTF_8);
	}
	
	public WsvDocument(ReliableTxtEncoding encoding) {
		setEncoding(encoding);
	}
	
	public final void setEncoding(ReliableTxtEncoding encoding) {
		Objects.requireNonNull(encoding);
		this.encoding = encoding;
	}
	
	public ReliableTxtEncoding getEncoding() {
		return encoding;
	}
	
	public void addLine(String... values) {
		addLine(new WsvLine(values));
	}
	
	public void addLine(String[] values, String[] whitespaces, String comment) {
		addLine(new WsvLine(values, whitespaces, comment));
	}

	public void addLine(WsvLine line) {
		Lines.add(line);
	}
	
	public WsvLine getLine(int index) {
		return Lines.get(index);
	}
	
	public String[][] toArray() {
		String[][] array = new String[Lines.size()][];
		for (int i=0; i<Lines.size(); i++) {
			array[i] = Lines.get(i).Values;
		}
		return array;
	}

	@Override
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean preserveWhitespaceAndComments) {
		if (preserveWhitespaceAndComments) {
			return WsvSerializer.serializeDocument(this);
		} else {
			return WsvSerializer.serializeDocumentNonPreserving(this);
		}
	}

	public void save(String filePath) throws IOException {
		String content = toString();
		ReliableTxtDocument.save(content, encoding, filePath);
	}

	public static WsvDocument load(String filePath) throws IOException {
		return load(filePath, true);
	}
	
	public static WsvDocument load(String filePath, boolean preserveWhitespaceAndComments) throws IOException {
		ReliableTxtDocument txt = ReliableTxtDocument.load(filePath);
		WsvDocument document = parse(txt.getText(), preserveWhitespaceAndComments);
		document.encoding = txt.getEncoding();
		return document;
	}

	public static WsvDocument parse(String content) {
		return parse(content, true);
	}
	
	public static WsvDocument parse(String content, boolean preserveWhitespaceAndComments) {
		if (preserveWhitespaceAndComments) {
			return WsvParser.parseDocument(content);
		} else {
			return WsvParser.parseDocumentNonPreserving(content);
		}
	}
	
	public static String[][] parseAsJaggedArray(String content) {
		return WsvParser.parseDocumentAsJaggedArray(content);
	}
}

class WsvString {
	public static boolean isWhitespace(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		for (int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			if (!WsvChar.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}
}

class WsvParserException extends RuntimeException {
	public final int Index;
	public final int LineIndex;
	public final int LinePosition;
	
	WsvParserException(int index, int lineIndex, int linePosition, String message) {
		super(String.format("%s (%d, %d)", message, lineIndex + 1, linePosition + 1));
		Index = index;
		LineIndex = lineIndex;
		LinePosition = linePosition;
	}
}

class WsvParser {
	private static final String MULTIPLE_WSV_LINES_NOT_ALLOWED = "Multiple WSV lines not allowed";
	private static final String UNEXPECTED_PARSER_ERROR = "Unexpected parser error";
	
	private static WsvLine parseLine(WsvCharIterator iterator, 
			ArrayList<String> values, ArrayList<String> whitespaces) {
		values.clear();
		whitespaces.clear();
		
		String whitespace = iterator.readWhitespaceOrNull();
		whitespaces.add(whitespace);

		while (!iterator.isChar('\n') && !iterator.isEndOfText()) {
			String value;
			if(iterator.isChar('#')) {
				break;
			} else if(iterator.tryReadChar('"')) {
				value = iterator.readString();
			} else {
				value = iterator.readValue();
				if (value.equals("-")) {
					value = null;
				}
			}
			values.add(value);

			whitespace = iterator.readWhitespaceOrNull();
			if (whitespace == null) {
				break;
			}
			whitespaces.add(whitespace);
		}
		
		String comment = null;
		if(iterator.tryReadChar('#')) {
			comment = iterator.readCommentText();
			if (whitespace == null) {
				whitespaces.add(null);
			}
		}

		String[] valueArray = new String[values.size()];
		String[] whitespaceArray = new String[whitespaces.size()];
		values.toArray(valueArray);
		whitespaces.toArray(whitespaceArray);

		WsvLine newLine = new WsvLine();
		newLine.set(valueArray, whitespaceArray, comment);
		return newLine;
	}
	
	public static WsvLine parseLine(String content) {
		WsvCharIterator iterator = new WsvCharIterator(content);
		ArrayList<String> values = new ArrayList<>();
		ArrayList<String> whitespaces = new ArrayList<>();
		
		WsvLine newLine = parseLine(iterator, values, whitespaces);
		if (iterator.isChar('\n')) {
			throw iterator.getException(MULTIPLE_WSV_LINES_NOT_ALLOWED);
		} else if (!iterator.isEndOfText()) {
			throw iterator.getException(UNEXPECTED_PARSER_ERROR);
		}
		
		return newLine;
	}
	
	public static WsvDocument parseDocument(String content) {
		WsvDocument document = new WsvDocument();
		
		WsvCharIterator iterator = new WsvCharIterator(content);
		ArrayList<String> values = new ArrayList<>();
		ArrayList<String> whitespaces = new ArrayList<>();
		
		while (true) {
			WsvLine newLine = parseLine(iterator, values, whitespaces);
			document.addLine(newLine);
			
			if (iterator.isEndOfText()) {
				break;
			} else if(!iterator.tryReadChar('\n')) {
				throw iterator.getException(UNEXPECTED_PARSER_ERROR);
			}
		}
		
		if (!iterator.isEndOfText()) {
			throw iterator.getException(UNEXPECTED_PARSER_ERROR);
		}

		return document;
	}
	
	public static WsvLine parseLineNonPreserving(String content) {
		String[] values = parseLineAsArray(content);
		return new WsvLine(values);
	}
	
	public static WsvDocument parseDocumentNonPreserving(String content) {
		WsvDocument document = new WsvDocument();
		
		WsvCharIterator iterator = new WsvCharIterator(content);
		ArrayList<String> values = new ArrayList<>();
		
		while (true) {
			String[] lineValues = parseLineAsArray(iterator, values);
			WsvLine newLine = new WsvLine(lineValues);
			document.addLine(newLine);
			
			if (iterator.isEndOfText()) {
				break;
			} else if(!iterator.tryReadChar('\n')) {
				throw iterator.getException(UNEXPECTED_PARSER_ERROR);
			}
		}
		
		if (!iterator.isEndOfText()) {
			throw iterator.getException(UNEXPECTED_PARSER_ERROR);
		}

		return document;
	}
	
	public static String[][] parseDocumentAsJaggedArray(String content) {
		WsvCharIterator iterator = new WsvCharIterator(content);
		ArrayList<String> values = new ArrayList<>();
		ArrayList<String[]> lines = new ArrayList<>();
		
		while (true) {
			String[] newLine = parseLineAsArray(iterator, values);
			lines.add(newLine);
			
			if (iterator.isEndOfText()) {
				break;
			} else if(!iterator.tryReadChar('\n')) {
				throw iterator.getException(UNEXPECTED_PARSER_ERROR);
			}
		}
		
		if (!iterator.isEndOfText()) {
			throw iterator.getException(UNEXPECTED_PARSER_ERROR);
		}
		
		String[][] linesArray = new String[lines.size()][];
		lines.toArray(linesArray);
		return linesArray;
	}
	
	public static String[] parseLineAsArray(String content) {
		WsvCharIterator iterator = new WsvCharIterator(content);
		ArrayList<String> values = new ArrayList<>();
		String[] result = parseLineAsArray(iterator, values);
		if (iterator.isChar('\n')) {
			throw iterator.getException(MULTIPLE_WSV_LINES_NOT_ALLOWED);
		} else if (!iterator.isEndOfText()) {
			throw iterator.getException(UNEXPECTED_PARSER_ERROR);
		}
		return result;
	}
	
	private static String[] parseLineAsArray(WsvCharIterator iterator, ArrayList<String> values) {
		values.clear();
		iterator.skipWhitespace();

		while (!iterator.isChar('\n') && !iterator.isEndOfText()) {
			String value;
			if(iterator.isChar('#')) {
				break;
			} else if(iterator.tryReadChar('"')) {
				value = iterator.readString();
			} else {
				value = iterator.readValue();
				if (value.equals("-")) {
					value = null;
				}
			}
			values.add(value);

			if (!iterator.skipWhitespace()) {
				break;
			}
		}
		
		String comment = null;
		if(iterator.tryReadChar('#')) {
			iterator.skipCommentText();
		}

		String[] valueArray = new String[values.size()];
		values.toArray(valueArray);
		return valueArray;
	}
}

class WsvSerializer {
	private static boolean containsSpecialChar(String value) {
		for (int i=0; i<value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\n' || WsvChar.isWhitespace(c) || c == '"'
					 || c == '#') {
				return true;
			}
		}
		return false;
	}

	public static void serializeValue(StringBuilder sb, String value) {
		if (value==null) {
			sb.append('-');
		} else if (value.length() == 0) {
			sb.append("\"\"");
		} else if (value.equals("-")) {
			sb.append("\"-\"");
		} else if (containsSpecialChar(value)) {
			sb.append('"');
			for (int i=0; i<value.length(); i++) {
				char c = value.charAt(i);
				if (c == '\n') {
					sb.append("\"/\"");
				} else if(c == '"') {
					sb.append("\"\"");
				} else {
					sb.append(c);
				}
			}
			sb.append('"');
		} else {
			sb.append(value);
		}
	}
	
	private static void serializeWhitespace(StringBuilder sb, String whitespace,
			boolean isRequired) {
		if (whitespace != null && whitespace.length() > 0) {
			sb.append(whitespace);
		} else if (isRequired) {
			sb.append(" ");
		} 
	}

	private static void serializeValuesWithWhitespace(StringBuilder sb,
			WsvLine line) {
		if (line.Values == null) {
			String whitespace = line.whitespaces[0];
			serializeWhitespace(sb, whitespace, false);
			return;
		}
		
		for (int i=0; i<line.Values.length; i++) {
			String whitespace = null;
			if (i < line.whitespaces.length) {
				whitespace = line.whitespaces[i];
			}
			if (i == 0) {
				serializeWhitespace(sb, whitespace, false);
			} else {
				serializeWhitespace(sb, whitespace, true);
			}

			serializeValue(sb, line.Values[i]);
		}
		
		if (line.whitespaces.length >= line.Values.length + 1) {
			String whitespace = line.whitespaces[line.Values.length];
			serializeWhitespace(sb, whitespace, false);
		} else if (line.comment != null && line.Values.length > 0) {
			sb.append(' ');
		}
	}
	
	private static void serializeValuesWithoutWhitespace(StringBuilder sb, 
			WsvLine line) {
		if (line.Values == null) {
			return;
		}
		
		boolean isFollowingValue = false;
		for (String value : line.Values) {
			if (isFollowingValue) {
				sb.append(' ');
			} else {
				isFollowingValue = true;
			}
			serializeValue(sb, value);
		}

		if (line.comment != null && line.Values.length > 0) {
			sb.append(' ');
		}
	}
	
	public static void serializeLine(StringBuilder sb, WsvLine line) {
		if (line.whitespaces != null && line.whitespaces.length > 0) {
			serializeValuesWithWhitespace(sb, line);
		} else {
			serializeValuesWithoutWhitespace(sb, line);
		}
		
		if (line.comment != null) {
			sb.append('#');
			sb.append(line.comment);
		}
	}
	
	public static String serializeLine(WsvLine line) {
		StringBuilder sb = new StringBuilder();
		serializeLine(sb, line);
		return sb.toString();
	}
	
	public static String serializeDocument(WsvDocument document) {
		StringBuilder sb = new StringBuilder();
		boolean isFirstLine = true;
		for (WsvLine line : document.Lines) {
			if (!isFirstLine) {
				sb.append('\n');
			} else {
				isFirstLine = false;
			}
			serializeLine(sb, line);
		}
		return sb.toString();
	}
	
	public static String serializeLineNonPreserving(WsvLine line) {
		StringBuilder sb = new StringBuilder();
		serializeLine(sb, line.Values);
		return sb.toString();
	}
	
	public static String serializeDocumentNonPreserving(WsvDocument document) {
		StringBuilder sb = new StringBuilder();
		boolean isFirstLine = true;
		for (WsvLine line : document.Lines) {
			if (!isFirstLine) {
				sb.append('\n');
			} else {
				isFirstLine = false;
			}
			serializeLine(sb, line.Values);
		}
		return sb.toString();
	}
	
	public static void serializeLine(StringBuilder sb, String[] line) {
		boolean isFirstValue = true;
		for (String value : line) {
			if (!isFirstValue) {
				sb.append(' ');
			} else {
				isFirstValue = false;
			}
			serializeValue(sb, value);
		}
	}
	
	public static String serializeLine(String... line) {
		StringBuilder sb = new StringBuilder();
		serializeLine(sb, line);
		return sb.toString();
	}
	
	public static String serializeDocument(String[][] lines) {
		StringBuilder sb = new StringBuilder();
		boolean isFirstLine = true;
		for (String[] line : lines) {
			if (!isFirstLine) {
				sb.append('\n');
			} else {
				isFirstLine = false;
			}
			serializeLine(sb, line);
		}
		return sb.toString();
	}
}

class WsvStreamReader implements AutoCloseable {
	public final ReliableTxtEncoding Encoding;
	ReliableTxtStreamReader reader;
	
	public WsvStreamReader(String filePath) throws IOException {
		reader = new ReliableTxtStreamReader(filePath);
		Encoding = reader.Encoding;
	}
	
	public WsvLine readLine() throws IOException {
		String str = reader.readLine();
		if (str == null) {
			return null;
		}
		return WsvLine.parse(str);
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}
}

class WsvStreamWriter implements AutoCloseable {
	ReliableTxtStreamWriter writer;
	StringBuilder sb;
	
	public final ReliableTxtEncoding Encoding;
	public final boolean AppendMode;
	
	public WsvStreamWriter(String filePath) throws IOException {
		this(filePath, null, false);
	}
	
	public WsvStreamWriter(String filePath, boolean append) throws IOException {
		this(filePath, null, append);
	}
	
	public WsvStreamWriter(String filePath, ReliableTxtEncoding encoding) throws IOException {
		this(filePath, encoding, false);
	}
	
	public WsvStreamWriter(String filePath, ReliableTxtEncoding encoding,
			boolean append) throws IOException {
		writer = new ReliableTxtStreamWriter(filePath, encoding, append);
		sb = new StringBuilder();
		
		Encoding = writer.Encoding;
		AppendMode = writer.AppendMode;
	}
	
	public void writeLine(String... values) throws IOException {
		writeLine(new WsvLine(values));
	}
	
	public void writeLine(String[] values, String[] whitespaces, String comment) throws IOException {
		writeLine(new WsvLine(values, whitespaces, comment));
	}
	
	public void writeLine(WsvLine line) throws IOException {
		sb.setLength(0);
		WsvSerializer.serializeLine(sb, line);
		String str = sb.toString();
		writer.writeLine(str);
	}
	
	public void writeLines(WsvLine... lines) throws IOException {
		for (WsvLine line:lines) {
			writeLine(line);
		}
	}
	
	public void writeLines(WsvDocument document) throws IOException {
		writeLines(document.Lines.toArray(new WsvLine[document.Lines.size()]));
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}
}

class WsvBasedFormat {
	public static String[] getWhitespaces(WsvLine line) {
		return line.whitespaces;
	}
}

class WsvCharIterator extends ReliableTxtCharIterator {
	public WsvCharIterator(String text) {
		super(text);
	}
	
	public boolean isWhitespace() {
		if (isEndOfText()) return false;
		return WsvChar.isWhitespace(chars[index]);
	}
	
	public String readCommentText() {
		int startIndex = index;
		while (true) {
			if (isEndOfText()) break;
			if (chars[index] == '\n') break;
			index++;
		}
		return new String(chars,startIndex,index-startIndex);
	}
	
	public void skipCommentText() {
		while (true) {
			if (isEndOfText()) break;
			if (chars[index] == '\n') break;
			index++;
		}
	}

	public String readWhitespaceOrNull() {
		int startIndex = index;
		while (true) {
			if (isEndOfText()) break;
			int c = chars[index];
			if (c == '\n') break;
			if (!WsvChar.isWhitespace(c)) break;
			index++;
		}
		if (index == startIndex) return null;
		return new String(chars,startIndex,index-startIndex);
	}
	
	public boolean skipWhitespace() {
		int startIndex = index;
		while (true) {
			if (isEndOfText()) break;
			int c = chars[index];
			if (c == '\n') break;
			if (!WsvChar.isWhitespace(c)) break;
			index++;
		}
		return index > startIndex;
	}

	public String readString() {
		sb.setLength(0);
		while (true) {
			if (isEndOfText() || isChar('\n')) {
				throw getException("String not closed");
			}
			int c = chars[index];
			if (c == '"') {
				index++;
				if (tryReadChar('"')) {
					sb.append('"');
				} else if(tryReadChar('/')) {
					if (!tryReadChar('"')) {
						throw getException("Invalid string line break");
					}
					sb.append('\n');
				} else if (isWhitespace() || isChar('\n') || isChar('#') || isEndOfText() ) {
					break;
				} else {
					throw getException("Invalid character after string");
				}
			} else {
				sb.appendCodePoint(c);
				index++;
			}
		}
		return sb.toString();
	}

	public String readValue() {
		int startIndex = index;
		while (true) {
			if (isEndOfText()) {
				break;
			}
			int c = chars[index];
			if (WsvChar.isWhitespace(c) || c == '\n' || c == '#') {
				break;
			}
			if (c == '\"') {
				throw getException("Invalid double quote after value");
			}
			index++;
		}
		if (index == startIndex) {
			throw getException("Invalid value");
		}
		return new String(chars,startIndex,index-startIndex);
	}
	
	public WsvParserException getException(String message) {
		int[] lineInfo = getLineInfo();
		return new WsvParserException(index, lineInfo[0], lineInfo[1], message);
	}
}