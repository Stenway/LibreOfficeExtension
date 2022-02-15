package com.stenway.loextensions.formats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

abstract class SmlNode {
	String[] whitespaces;
	String comment;
	
	public final void setWhitespaces(String... whitespaces) {
		WsvLine.validateWhitespaces(whitespaces);
		this.whitespaces = whitespaces;
	}

	public String[] getWhitespaces() {
		if (whitespaces == null) {
			return null;
		}
		return whitespaces.clone();
	}
	
	public final void setComment(String comment) {
		WsvLine.validateComment(comment);
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}
	
	void setWhitespacesAndComment(String[] whitespaces, String comment) {
		this.whitespaces = whitespaces;
		this.comment = comment;
	}
	
	abstract void toWsvLines(WsvDocument document, int level, String defaultIndentation, String endKeyword);
		
	public void minify() {
		whitespaces = null;
		comment = null;
	}
}

class SmlEmptyNode extends SmlNode {
	@Override
	public String toString() {
		return SmlSerializer.serializeEmptyNode(this);
	}
	
	@Override
	void toWsvLines(WsvDocument document, int level, String defaultIndentation, String endKeyword) {
		SmlSerializer.serializeEmptyNode(this, document, level, defaultIndentation);
	}
}

abstract class SmlNamedNode extends SmlNode {
	private String name;
	
	public SmlNamedNode(String name) {
		setName(name);
	}
	
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null");
		}
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasName(String name) {
		if (name == null) return false;
		return this.name.equalsIgnoreCase(name);
	}
}

class SmlAttribute extends SmlNamedNode {
	String[] values;
	
	public SmlAttribute(String name, String... values) {
		super(name);
		setValues(values);
	}
	
	public SmlAttribute(String name, int... values) {
		super(name);
		setValues(values);
	}
	
	public SmlAttribute(String name, float... values) {
		super(name);
		setValues(values);
	}
	
	public SmlAttribute(String name, double... values) {
		super(name);
		setValues(values);
	}
	
	public SmlAttribute(String name, boolean... values) {
		super(name);
		setValues(values);
	}
	
	public SmlAttribute(String name, byte[]... values) {
		super(name);
		setValues(values);
	}
	
	public final void setValues(String... values) {
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("Values must contain at least one value");
		}
		this.values = values;
	}
	
	public final void setValues(int... values) {
		String[] strValues = Arrays.stream(values)
				.mapToObj(String::valueOf)
				.toArray(String[]::new);
		setValues(strValues);
	}
	
	public final void setValues(float... values) {
		String[] strValues = new String[values.length];
		for (int i=0; i<values.length; i++) {
			strValues[i] = String.valueOf(values[i]);
		}
		setValues(strValues);
	}
	
	public final void setValues(double... values) {
		String[] strValues = Arrays.stream(values)
				.mapToObj(String::valueOf)
				.toArray(String[]::new);
		setValues(strValues);
	}
	
	public final void setValues(boolean... values) {
		String[] strValues = new String[values.length];
		for (int i=0; i<values.length; i++) {
			strValues[i] = String.valueOf(values[i]);
		}
		setValues(strValues);
	}
	
	public final void setValues(byte[]... values) {
		String[] strValues = new String[values.length];
		for (int i=0; i<values.length; i++) {
			strValues[i] = Base64.getEncoder().encodeToString(values[i]);
		}
		setValues(strValues);
	}
	
	public String[] getValues() {
		return values;
	}
	
	public String[] getValues(int offset) {
		return Arrays.stream(values)
				.skip(offset)
				.toArray(String[]::new);
	}
	
	public int[] getIntValues() {
		return Arrays.stream(values)
				.mapToInt(Integer::parseInt)
				.toArray();
	}
	
	public int[] getIntValues(int offset) {
		return Arrays.stream(values)
				.skip(offset)
				.mapToInt(Integer::parseInt)
				.toArray();
	}
	
	public float[] getFloatValues() {
		return getFloatValues(0);
	}
	
	public float[] getFloatValues(int offset) {
		double[] values = getDoubleValues(offset);
		float[] result = new float[values.length];
		for (int i=0; i<values.length; i++) {
			result[i] = (float)values[i];
		}
		return result;
	}
	
	public double[] getDoubleValues() {
		return Arrays.stream(values)
				.mapToDouble(Double::parseDouble)
				.toArray();
	}
	
	public double[] getDoubleValues(int offset) {
		return Arrays.stream(values)
				.skip(offset)
				.mapToDouble(Double::parseDouble)
				.toArray();
	}
	
	public boolean[] getBooleanValues() {
		return getBooleanValues(0);
	}
	
	public boolean[] getBooleanValues(int offset) {
		String[] values = getValues(offset);
		boolean[] result = new boolean[values.length];
		for (int i=0; i<values.length; i++) {
			result[i] = Boolean.parseBoolean(values[i]);
		}
		return result;
	}
	
	public byte[][] getBytesValues() {
		return getBytesValues(0);
	}
	
	public byte[][] getBytesValues(int offset) {
		String[] values = getValues(offset);
		byte[][] result = new byte[values.length][];
		for (int i=0; i<values.length; i++) {
			result[i] = Base64.getDecoder().decode(values[i]);
		}
		return result;
	}
	
	public String getString() {
		return values[0];
	}
	
	public String getString(int index) {
		return values[index];
	}
	
	public int getInt() {
		return getInt(0);
	}
	
	public int getInt(int index) {
		return Integer.parseInt(values[index]);
	}
	
	public float getFloat() {
		return getFloat(0);
	}
	
	public float getFloat(int index) {
		return Float.parseFloat(values[index]);
	}
	
	public double getDouble() {
		return getDouble(0);
	}
	
	public double getDouble(int index) {
		return Double.parseDouble(values[index]);
	}
	
	public boolean getBoolean() {
		return getBoolean(0);
	}
	
	public boolean getBoolean(int index) {
		return Boolean.parseBoolean(values[index]);
	}
	
	public byte[] getBytes() {
		return getBytes(0);
	}
	
	public byte[] getBytes(int index) {
		return Base64.getDecoder().decode(values[index]);
	}
	
	public void setValue(String value) {
		setValues(value);
	}
	
	public void setValue(int value) {
		setValues(value);
	}
	
	public void setValue(float value) {
		setValues(value);
	}
	
	public void setValue(double value) {
		setValues(value);
	}
	
	public void setValue(boolean value) {
		setValues(value);
	}
	
	public void setValue(byte[] value) {
		setValues(value);
	}
	
	@Override
	public String toString() {
		return SmlSerializer.serializeAttribute(this);
	}
	
	@Override
	void toWsvLines(WsvDocument document, int level, String defaultIndentation, String endKeyword) {
		SmlSerializer.serializeAttribute(this, document, level, defaultIndentation);
	}
}

class SmlElement extends SmlNamedNode {
	public final ArrayList<SmlNode> Nodes = new ArrayList<SmlNode>();
	
	String[] endWhitespaces;
	String endComment;
	
	public SmlElement(String name) {
		super(name);
	}

	public final void setEndWhitespaces(String... whitespaces) {
		WsvLine.validateWhitespaces(whitespaces);
		this.endWhitespaces = whitespaces;
	}

	public String[] getEndWhitespaces() {
		if (endWhitespaces == null) {
			return null;
		}
		return endWhitespaces.clone();
	}
	
	public final void setEndComment(String comment) {
		WsvLine.validateComment(comment);
		this.endComment = comment;
	}

	public String getEndComment() {
		return endComment;
	}
	
	void setEndWhitespacesAndComment(String[] whitespaces, String comment) {
		this.endWhitespaces = whitespaces;
		this.endComment = comment;
	}
	
	public SmlNode add(SmlNode node) {
		Nodes.add(node);
		return node;
	}
	
	public SmlAttribute addAttribute(String name, String... values) {
		SmlAttribute attribute = new SmlAttribute(name, values);
		add(attribute);
		return attribute;
	}
	
	public SmlAttribute addAttribute(String name, int... values) {
		SmlAttribute attribute = new SmlAttribute(name, values);
		add(attribute);
		return attribute;
	}
	
	public SmlAttribute addAttribute(String name, float... values) {
		SmlAttribute attribute = new SmlAttribute(name, values);
		add(attribute);
		return attribute;
	}
	
	public SmlAttribute addAttribute(String name, double... values) {
		SmlAttribute attribute = new SmlAttribute(name, values);
		add(attribute);
		return attribute;
	}
	
	public SmlAttribute addAttribute(String name, boolean... values) {
		SmlAttribute attribute = new SmlAttribute(name, values);
		add(attribute);
		return attribute;
	}
	
	public SmlAttribute addAttribute(String name, byte[]... values) {
		SmlAttribute attribute = new SmlAttribute(name, values);
		add(attribute);
		return attribute;
	}
	
	public SmlElement addElement(String name) {
		SmlElement element = new SmlElement(name);
		add(element);
		return element;
	}
	
	public SmlEmptyNode addEmptyNode() {
		SmlEmptyNode emptyNode = new SmlEmptyNode();
		add(emptyNode);
		return emptyNode;
	}
	
	public SmlAttribute[] attributes() {
		return Nodes.stream()
				.filter(node -> node instanceof SmlAttribute)
				.map(node -> (SmlAttribute)node)
				.toArray(SmlAttribute[]::new);
	}
	
	public SmlAttribute[] attributes(String name) {
		return Nodes.stream()
				.filter(node -> node instanceof SmlAttribute)
				.map(node -> (SmlAttribute)node)
				.filter(attribute -> attribute.hasName(name))
				.toArray(SmlAttribute[]::new);
	}
	
	public SmlAttribute attribute(String name) {
		Optional<SmlAttribute> result = Nodes.stream()
				.filter(node -> node instanceof SmlAttribute)
				.map(node -> (SmlAttribute)node)
				.filter(attribute -> attribute.hasName(name))
				.findFirst();
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new IllegalArgumentException("Element \""+getName()+"\" does not contain a \""+name+"\" attribute");
		}
	}
	
	public boolean hasAttribute(String name) {
		return Nodes.stream()
				.filter(node -> node instanceof SmlAttribute)
				.map(node -> (SmlAttribute)node)
				.filter(attribute -> attribute.hasName(name))
				.findFirst()
				.isPresent();
	}
	
	public SmlElement[] elements() {
		return Nodes.stream()
				.filter(node -> node instanceof SmlElement)
				.map(node -> (SmlElement)node)
				.toArray(SmlElement[]::new);
	}
	
	public SmlElement[] elements(String name) {
		return Nodes.stream()
				.filter(node -> node instanceof SmlElement)
				.map(node -> (SmlElement)node)
				.filter(element -> element.hasName(name))
				.toArray(SmlElement[]::new);
	}
	
	public SmlElement element(String name) {
		Optional<SmlElement> result = Nodes.stream()
				.filter(node -> node instanceof SmlElement)
				.map(node -> (SmlElement)node)
				.filter(element -> element.hasName(name))
				.findFirst();
		if (result.isPresent()) {
			return result.get();
		} else {
			throw new IllegalArgumentException("Element \""+getName()+"\" does not contain a \""+name+"\" element");
		}
	}
	
	public boolean hasElement(String name) {
		return Nodes.stream()
				.filter(node -> node instanceof SmlElement)
				.map(node -> (SmlElement)node)
				.filter(element -> element.hasName(name))
				.findFirst()
				.isPresent();
	}
	
	public SmlNamedNode[] nodes(String name) {
		return Nodes.stream()
				.filter(node -> node instanceof SmlNamedNode)
				.map(node -> (SmlNamedNode)node)
				.filter(attribute -> attribute.hasName(name))
				.toArray(SmlNamedNode[]::new);
	}
	
	public String getString(String attributeName) {
		return attribute(attributeName).getString();
	}
	
	public int getInt(String attributeName) {
		return attribute(attributeName).getInt();
	}
	
	public float getFloat(String attributeName) {
		return attribute(attributeName).getFloat();
	}
	
	public double getDouble(String attributeName) {
		return attribute(attributeName).getDouble();
	}
	
	public boolean getBoolean(String attributeName) {
		return attribute(attributeName).getBoolean();
	}
	
	public byte[] getBytes(String attributeName) {
		return attribute(attributeName).getBytes();
	}
	
	public String getString(String attributeName, String defaultValue) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getString();
		} else {
			return defaultValue;
		}
	}
	
	public int getInt(String attributeName, int defaultValue) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getInt();
		} else {
			return defaultValue;
		}
	}
	
	public float getFloat(String attributeName, float defaultValue) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getFloat();
		} else {
			return defaultValue;
		}
	}
	
	public double getDouble(String attributeName, double defaultValue) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getDouble();
		} else {
			return defaultValue;
		}
	}
	
	public boolean getBoolean(String attributeName, boolean defaultValue) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getBoolean();
		} else {
			return defaultValue;
		}
	}
	
	public byte[] getBytes(String attributeName, byte[] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getBytes();
		} else {
			return defaultValues;
		}
	}
	
	public String getStringOrNull(String attributeName) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getString();
		} else {
			return null;
		}
	}
	
	public Integer getIntOrNull(String attributeName) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getInt();
		} else {
			return null;
		}
	}
	
	public Float getFloatOrNull(String attributeName) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getFloat();
		} else {
			return null;
		}
	}
	
	public Double getDoubleOrNull(String attributeName) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getDouble();
		} else {
			return null;
		}
	}
	
	public Boolean getBooleanOrNull(String attributeName) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getBoolean();
		} else {
			return null;
		}
	}
	
	public byte[] getBytesOrNull(String attributeName) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getBytes();
		} else {
			return null;
		}
	}
	
	public String[] getValues(String attributeName) {
		return attribute(attributeName).getValues();
	}
	
	public int[] getIntValues(String attributeName) {
		return attribute(attributeName).getIntValues();
	}
	
	public float[] getFloatValues(String attributeName) {
		return attribute(attributeName).getFloatValues();
	}
	
	public double[] getDoubleValues(String attributeName) {
		return attribute(attributeName).getDoubleValues();
	}
	
	public boolean[] getBooleanValues(String attributeName) {
		return attribute(attributeName).getBooleanValues();
	}
	
	public byte[][] getBytesValues(String attributeName) {
		return attribute(attributeName).getBytesValues();
	}
		
	public String[] getValues(String attributeName, String[] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getValues();
		} else {
			return defaultValues;
		}
	}
	
	public int[] getIntValues(String attributeName, int[] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getIntValues();
		} else {
			return defaultValues;
		}
	}
	
	public float[] getFloatValues(String attributeName, float[] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getFloatValues();
		} else {
			return defaultValues;
		}
	}
	
	public double[] getDoubleValues(String attributeName, double[] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getDoubleValues();
		} else {
			return defaultValues;
		}
	}
	
	public boolean[] getBooleanValues(String attributeName, boolean[] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getBooleanValues();
		} else {
			return defaultValues;
		}
	}
	
	public byte[][] getBytesValues(String attributeName, byte[][] defaultValues) {
		if (hasAttribute(attributeName)) {
			return attribute(attributeName).getBytesValues();
		} else {
			return defaultValues;
		}
	}
	
	@Override
	public String toString() {
		return SmlSerializer.serializeElement(this);
	}
	
	@Override
	void toWsvLines(WsvDocument document, int level, String defaultIndentation, String endKeyword) {
		SmlSerializer.serializeElement(this, document, level, defaultIndentation, endKeyword);
	}
	
	@Override
	public void minify() {
		Object[] toRemoveList = Nodes.stream().filter(node -> node instanceof SmlEmptyNode).toArray();
		for (Object toRemove : toRemoveList) {
			Nodes.remove(toRemove);
		}
		whitespaces = null;
		comment = null;
		endWhitespaces = null;
		endComment = null;
		for (SmlNode node : Nodes) {
			node.minify();
		}
	}
}

class SmlException extends RuntimeException {
	public SmlException(String message) {
		super(message);
	}
}

class SmlDocument {
	SmlElement root;
	ReliableTxtEncoding encoding;
	String endKeyword = "End";
	String defaultIndentation;
	
	public final ArrayList<SmlEmptyNode> EmptyNodesBefore = new ArrayList<>();
	public final ArrayList<SmlEmptyNode> EmptyNodesAfter = new ArrayList<>();
	
	SmlDocument() {
		this(new SmlElement("Root"));
	}
	
	SmlDocument(ReliableTxtEncoding encoding) {
		this(new SmlElement("Root"), encoding);
	}
	
	public SmlDocument(String rootName) {
		this(new SmlElement(rootName));
	}
	
	public SmlDocument(String rootName, ReliableTxtEncoding encoding) {
		this(new SmlElement(rootName), encoding);
	}
	
	public SmlDocument(SmlElement root) {
		this(root, ReliableTxtEncoding.UTF_8);
	}
	
	public SmlDocument(SmlElement root, ReliableTxtEncoding encoding) {
		setRoot(root);
		setEncoding(encoding);
	}
	
	public final void setRoot(SmlElement root) {
		Objects.requireNonNull(root);
		this.root = root;
	}
	
	public SmlElement getRoot() {
		return root;
	}
	
	public final void setEncoding(ReliableTxtEncoding encoding) {
		Objects.requireNonNull(encoding);
		this.encoding = encoding;
	}
	
	public ReliableTxtEncoding getEncoding() {
		return encoding;
	}
	
	public void setDefaultIndentation(String defaultIndentation) {
		if (defaultIndentation != null && defaultIndentation.length() > 0 &&
				!WsvString.isWhitespace(defaultIndentation)) {
			throw new IllegalArgumentException(
					"Indentation value contains non whitespace character or line feed");
		}
		this.defaultIndentation = defaultIndentation;
	}
	
	public String getDefaultIndentation() {
		return defaultIndentation;
	}
	
	public void setEndKeyword(String endKeyword) {
		this.endKeyword = endKeyword;
	}
	
	public String getEndKeyword() {
		return endKeyword;
	}
	
	public void minify() {
		EmptyNodesBefore.clear();
		EmptyNodesAfter.clear();
		setDefaultIndentation("");
		setEndKeyword(null);
		root.minify();
	}
	
	@Override
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean preserveWhitespaceAndComments) {
		if (preserveWhitespaceAndComments) {
			return SmlSerializer.serializeDocument(this);
		} else {
			return SmlSerializer.serializeDocumentNonPreserving(this, false);
		}
	}
	
	public String toStringMinified() {
		return SmlSerializer.serializeDocumentNonPreserving(this, true);
	}
	
	public void save(String filePath) throws IOException {
		String content = toString();
		ReliableTxtDocument.save(content, encoding, filePath);
	}
	
	public static SmlDocument load(String filePath) throws IOException {
		ReliableTxtDocument txt = ReliableTxtDocument.load(filePath);
		SmlDocument document = parse(txt.getText());
		document.encoding = txt.getEncoding();
		return document;
	}

	public static SmlDocument parse(String content) {
		return parse(content, true);
	}
	
	public static SmlDocument parse(String content, boolean preserveWhitespaceAndComments) {
		try {
			if (preserveWhitespaceAndComments) {
				return SmlParser.parseDocument(content);
			} else {
				return SmlParser.parseDocumentNonPreserving(content);
			}
		} catch (IOException exception) {
			throw new RuntimeException();
		}
	}
}

class SmlSerializer {
	public static String serializeDocument(SmlDocument document) {
		WsvDocument wsvDocument = new WsvDocument();
		
		serialzeEmptyNodes(document.EmptyNodesBefore, wsvDocument);
		document.getRoot().toWsvLines(wsvDocument, 0, document.defaultIndentation, document.endKeyword);
		serialzeEmptyNodes(document.EmptyNodesAfter, wsvDocument);
		
		return wsvDocument.toString();
	}
	
	public static String serializeElement(SmlElement element) {
		WsvDocument wsvDocument = new WsvDocument();
		element.toWsvLines(wsvDocument, 0, null, "End");
		return wsvDocument.toString();
	}

	public static String serializeAttribute(SmlAttribute attribute) {
		WsvDocument wsvDocument = new WsvDocument();
		attribute.toWsvLines(wsvDocument, 0, null, null);
		return wsvDocument.toString();
	}
	
	public static String serializeEmptyNode(SmlEmptyNode emptyNode) {
		WsvDocument wsvDocument = new WsvDocument();
		emptyNode.toWsvLines(wsvDocument, 0, null, null);
		return wsvDocument.toString();
	}
	
	private static void serialzeEmptyNodes(ArrayList<SmlEmptyNode> emptyNodes, WsvDocument wsvDocument) {
		for (SmlEmptyNode emptyNode : emptyNodes) {
			emptyNode.toWsvLines(wsvDocument, 0, null, null);
		}
	}

	public static void serializeElement(SmlElement element, WsvDocument wsvDocument,
			int level, String defaultIndentation, String endKeyword) {
		if (endKeyword != null && element.hasName(endKeyword)) {
			throw new SmlException("Element name matches the end keyword '"+endKeyword+"'");
		}
		int childLevel = level + 1;
		
		String[] whitespaces = getWhitespaces(element.whitespaces, level, defaultIndentation);
		wsvDocument.addLine(new String[]{element.getName()}, whitespaces, element.comment);
		
		for (SmlNode child : element.Nodes) {
			child.toWsvLines(wsvDocument, childLevel, defaultIndentation, endKeyword);
		}
		
		String[] endWhitespaces = getWhitespaces(element.endWhitespaces, level, defaultIndentation);
		wsvDocument.addLine(new String[]{endKeyword}, endWhitespaces, element.endComment);
	}
	
	private static String[] getWhitespaces(String[] whitespaces, int level, 
			String defaultIndentation) {
		if (whitespaces != null && whitespaces.length > 0) {
			return whitespaces;
		}
		if (defaultIndentation == null) {
			char[] indentChars = new char[level];
			Arrays.fill(indentChars, '\t');
			return new String[] { new String(indentChars) };
		} else {
			String indentStr = getIndentationString(defaultIndentation, level);
			return new String[] {indentStr};
		}
	}
	
	private static String getIndentationString(String defaultIndentation, int level) {
		//String indentStr = defaultIndentation.repeat(level);
		return String.join("", Collections.nCopies(level, defaultIndentation));
	}
	
	public static void serializeAttribute(SmlAttribute attribute, WsvDocument wsvDocument,
			int level, String defaultIndentation) {
		String[] whitespaces = getWhitespaces(attribute.whitespaces, level, defaultIndentation);
		String[] combined = combine(attribute.getName(), attribute.values);
		wsvDocument.addLine(combined, whitespaces, attribute.comment);
	}
	
	private static String[] combine(String name, String[] values) {
		String[] result = new String[values.length + 1];
		result[0] = name;
		System.arraycopy(values, 0, result, 1, values.length);
		return result;
	}
	
	public static void serializeEmptyNode(SmlEmptyNode emptyNode, WsvDocument wsvDocument, 
			int level, String defaultIndentation) {
		String[] whitespaces = getWhitespaces(emptyNode.whitespaces, level, defaultIndentation);
		wsvDocument.addLine(null, whitespaces, emptyNode.comment);
	}
	
	public static String serializeDocumentNonPreserving(SmlDocument document) {
		return serializeDocumentNonPreserving(document, false);
	}
	
	public static String serializeDocumentNonPreserving(SmlDocument document, boolean minified) {
		StringBuilder sb = new StringBuilder();
		String defaultIndentation = document.getDefaultIndentation();
		if (defaultIndentation == null) {
			defaultIndentation = "\t";
		}
		String endKeyword = document.getEndKeyword();
		if (minified) {
			defaultIndentation = "";
			endKeyword = null;
		}
		serializeElementNonPreserving(sb, document.getRoot(), 0, defaultIndentation, endKeyword);
		sb.setLength(sb.length()-1);
		return sb.toString();
	}

	private static void serializeElementNonPreserving(StringBuilder sb, SmlElement element,
			int level, String defaultIndentation, String endKeyword) {
		serializeIndentation(sb, level, defaultIndentation);
		WsvSerializer.serializeValue(sb, element.getName());
		sb.append('\n'); 

		int childLevel = level + 1;
		for (SmlNode child : element.Nodes) {
			if (child instanceof SmlElement) {
				serializeElementNonPreserving(sb, (SmlElement)child, childLevel, defaultIndentation, endKeyword);
			} else if (child instanceof SmlAttribute) {
				serializeAttributeNonPreserving(sb, (SmlAttribute)child, childLevel, defaultIndentation);
			}
		}
		
		serializeIndentation(sb, level, defaultIndentation);
		WsvSerializer.serializeValue(sb, endKeyword);
		sb.append('\n'); 
	}
	
	private static void serializeAttributeNonPreserving(StringBuilder sb, SmlAttribute attribute,
			int level, String defaultIndentation) {
		serializeIndentation(sb, level, defaultIndentation);
		WsvSerializer.serializeValue(sb, attribute.getName());
		sb.append(' '); 
		WsvSerializer.serializeLine(sb, attribute.getValues());
		sb.append('\n'); 
	}
	
	private static void serializeIndentation(StringBuilder sb, int level, String defaultIndentation) {
		String indentStr = getIndentationString(defaultIndentation, level);
		sb.append(indentStr);
	}
}

class SmlParserException extends RuntimeException {
	public final int LineIndex;
	
	public SmlParserException(int lineIndex, String message) {
		super(String.format("%s (%d)", message, lineIndex + 1));
		LineIndex = lineIndex;
	}
}

class SmlParser {
	private static final String ONLY_ONE_ROOT_ELEMENT_ALLOWED				= "Only one root element allowed";
	private static final String ROOT_ELEMENT_EXPECTED						= "Root element expected";
	private static final String INVALID_ROOT_ELEMENT_START					= "Invalid root element start";
	private static final String NULL_VALUE_AS_ELEMENT_NAME_IS_NOT_ALLOWED	= "Null value as element name is not allowed";
	private static final String NULL_VALUE_AS_ATTRIBUTE_NAME_IS_NOT_ALLOWED	= "Null value as attribute name is not allowed";
	private static final String END_KEYWORD_COULD_NOT_BE_DETECTED			= "End keyword could not be detected";
	
	public static SmlDocument parseDocument(String content) throws IOException {
		WsvDocument wsvDocument = WsvDocument.parse(content);
		String endKeyword = determineEndKeyword(wsvDocument);
		WsvLineIterator iterator = new WsvDocumentLineIterator(wsvDocument, endKeyword);
		
		SmlDocument document = new SmlDocument();
		document.setEndKeyword(endKeyword);
		
		SmlElement rootElement = readRootElement(iterator, document.EmptyNodesBefore);
		readElementContent(iterator, rootElement);
		document.setRoot(rootElement);
		
		readEmptyNodes(document.EmptyNodesAfter, iterator);
		if (iterator.hasLine()) {
			throw getException(iterator, ONLY_ONE_ROOT_ELEMENT_ALLOWED);
		}
		return document;
	}
	
	private static boolean equalIgnoreCase(String name1, String name2) {
		if (name1 == null) {
			return name1 == name2;
		}
		return name1.equalsIgnoreCase(name2);
	}
	
	public static SmlElement readRootElement(WsvLineIterator iterator, 
			ArrayList<SmlEmptyNode> emptyNodesBefore) throws IOException {
		readEmptyNodes(emptyNodesBefore, iterator);
		
		if (!iterator.hasLine()) {
			throw getException(iterator, ROOT_ELEMENT_EXPECTED);
		}
		WsvLine rootStartLine = iterator.getLine();
		if (!rootStartLine.hasValues() || rootStartLine.Values.length != 1 
				|| equalIgnoreCase(iterator.getEndKeyword(), rootStartLine.Values[0])) {
			throw getLastLineException(iterator, INVALID_ROOT_ELEMENT_START);
		}
		String rootElementName = rootStartLine.Values[0];
		if (rootElementName == null) {
			throw getLastLineException(iterator, NULL_VALUE_AS_ELEMENT_NAME_IS_NOT_ALLOWED);
		}
		SmlElement rootElement = new SmlElement(rootElementName);
		rootElement.setWhitespacesAndComment(WsvBasedFormat.getWhitespaces(rootStartLine), rootStartLine.getComment());
		return rootElement;
	}
	
	public static SmlNode readNode(WsvLineIterator iterator, SmlElement parentElement) throws IOException {
		SmlNode node;
		WsvLine line = iterator.getLine();
		if (line.hasValues()) {
			String name = line.Values[0];
			if (line.Values.length == 1) {
				if (equalIgnoreCase(iterator.getEndKeyword(),name)) {
					parentElement.setEndWhitespacesAndComment(WsvBasedFormat.getWhitespaces(line), line.getComment());
					return null;
				}
				if (name == null) {
					throw getLastLineException(iterator, NULL_VALUE_AS_ELEMENT_NAME_IS_NOT_ALLOWED);
				}
				SmlElement childElement = new SmlElement(name);
				childElement.setWhitespacesAndComment(WsvBasedFormat.getWhitespaces(line), line.getComment());

				readElementContent(iterator, childElement);

				node = childElement;
			} else {
				if (name == null) {
					throw getLastLineException(iterator, NULL_VALUE_AS_ATTRIBUTE_NAME_IS_NOT_ALLOWED);
				}
				String[] values = Arrays.copyOfRange(line.Values, 1, line.Values.length);
				SmlAttribute childAttribute = new SmlAttribute(name, values);
				childAttribute.setWhitespacesAndComment(WsvBasedFormat.getWhitespaces(line), line.getComment());

				node = childAttribute;
			}
		} else {
			SmlEmptyNode emptyNode = new SmlEmptyNode();
			emptyNode.setWhitespacesAndComment(WsvBasedFormat.getWhitespaces(line), line.getComment());

			node = emptyNode;
		}
		return node;
	}
	
	private static void readElementContent(WsvLineIterator iterator, SmlElement element) throws IOException {
		while (true) {
			if (!iterator.hasLine()) {
				throw getLastLineException(iterator, "Element \""+element.getName()+"\" not closed");
			}
			SmlNode node = readNode(iterator, element);
			if (node == null) {
				break;
			}
			element.add(node);
		}
	}
	
	private static void readEmptyNodes(ArrayList<SmlEmptyNode> nodes, WsvLineIterator iterator) throws IOException {
		while (iterator.isEmptyLine()) {
			SmlEmptyNode emptyNode = readEmptyNode(iterator);
			nodes.add(emptyNode);
		}
	}
	
	private static SmlEmptyNode readEmptyNode(WsvLineIterator iterator) throws IOException {
		WsvLine line = iterator.getLine();
		SmlEmptyNode emptyNode = new SmlEmptyNode();
		emptyNode.setWhitespacesAndComment(WsvBasedFormat.getWhitespaces(line), line.getComment());
		return emptyNode;
	}
	
	private static String determineEndKeyword(WsvDocument wsvDocument) {
		for (int i=wsvDocument.Lines.size()-1; i>=0; i--) {
			String[] values = wsvDocument.Lines.get(i).Values;
			if (values != null) {
				if (values.length == 1) {
					return values[0];
				} else if (values.length > 1) {
					break;
				}
			}
		}
		throw new SmlParserException(wsvDocument.Lines.size()-1, END_KEYWORD_COULD_NOT_BE_DETECTED);
	}
	
	private static SmlParserException getException(WsvLineIterator iterator, String message) {
		return new SmlParserException(iterator.getLineIndex(), message);
	}

	private static SmlParserException getLastLineException(WsvLineIterator iterator, String message) {
		return new SmlParserException(iterator.getLineIndex()-1, message);
	}
	
	public static SmlDocument parseDocumentNonPreserving(String content) throws IOException {
		String[][] wsvLines = WsvDocument.parseAsJaggedArray(content);
		return parseDocument(wsvLines);
	}
		
	public static SmlDocument parseDocument(String[][] wsvLines) throws IOException {
		String endKeyword = determineEndKeyword(wsvLines);
		WsvLineIterator iterator = new WsvJaggedArrayLineIterator(wsvLines, endKeyword);
		
		SmlDocument document = new SmlDocument();
		document.setEndKeyword(endKeyword);
		
		SmlElement rootElement = parseDocumentNonPreserving(iterator);
		document.setRoot(rootElement);
		
		return document;
	}
	
	private static SmlElement parseDocumentNonPreserving(WsvLineIterator iterator) throws IOException {
		skipEmptyLines(iterator);
		if (!iterator.hasLine()) {
			throw getException(iterator, ROOT_ELEMENT_EXPECTED);
		}
		
		SmlNode node = readNodeNonPreserving(iterator);
		if (!(node instanceof SmlElement)) {
			throw getLastLineException(iterator, INVALID_ROOT_ELEMENT_START);
		}
		
		skipEmptyLines(iterator);
		if (iterator.hasLine()) {
			throw getException(iterator, ONLY_ONE_ROOT_ELEMENT_ALLOWED);
		}

		return (SmlElement)node;
	}
	
	private static void skipEmptyLines(WsvLineIterator iterator) throws IOException {
		while (iterator.isEmptyLine()) {
			iterator.getLineAsArray();
		}
	}
	
	private static SmlNode readNodeNonPreserving(WsvLineIterator iterator) throws IOException {
		String[] line = iterator.getLineAsArray();
		
		String name = line[0];
		if (line.length == 1) {
			if (equalIgnoreCase(iterator.getEndKeyword(),name)) {
				return null;
			}
			if (name == null) {
				throw getLastLineException(iterator, NULL_VALUE_AS_ELEMENT_NAME_IS_NOT_ALLOWED);
			}
			SmlElement element = new SmlElement(name);
			readElementContentNonPreserving(iterator, element);
			return element;
		} else {
			if (name == null) {
				throw getLastLineException(iterator, NULL_VALUE_AS_ATTRIBUTE_NAME_IS_NOT_ALLOWED);
			}
			String[] values = Arrays.copyOfRange(line, 1, line.length);
			SmlAttribute attribute = new SmlAttribute(name, values);
			return attribute;
		}
	}
	
	private static void readElementContentNonPreserving(WsvLineIterator iterator, SmlElement element) throws IOException {
		while (true) {
			skipEmptyLines(iterator);
			if (!iterator.hasLine()) {
				throw getLastLineException(iterator, "Element \""+element.getName()+"\" not closed");
			}
			SmlNode node = readNodeNonPreserving(iterator);
			if (node == null) {
				break;
			}
			element.add(node);
		}
	}
	
	private static String determineEndKeyword(String[][] lines) {
		int i;
		for (i=lines.length-1; i>=0; i--) {
			String[] values = lines[i];
			if (values.length == 1) {
				return values[0];
			} else if (values.length > 1) {
				break;
			}
		}
		throw new SmlParserException(lines.length-1, END_KEYWORD_COULD_NOT_BE_DETECTED);
	}
}

interface WsvLineIterator {	
	boolean hasLine();
	boolean isEmptyLine();
	WsvLine getLine() throws IOException;
	String[] getLineAsArray() throws IOException;
	String getEndKeyword();
	int getLineIndex();
}

class WsvDocumentLineIterator implements WsvLineIterator {
	WsvDocument wsvDocument;
	String endKeyword;

	int index;

	public WsvDocumentLineIterator(WsvDocument wsvDocument, String endKeyword) {
		this.wsvDocument = wsvDocument;
		this.endKeyword = endKeyword;
	}

	@Override
	public String getEndKeyword() {
		return endKeyword;
	}

	@Override
	public boolean hasLine() {
		return index < wsvDocument.Lines.size();
	}

	@Override
	public boolean isEmptyLine() {
		return hasLine() && !wsvDocument.Lines.get(index).hasValues();
	}

	@Override
	public WsvLine getLine() {
		WsvLine line = wsvDocument.Lines.get(index);
		index++;
		return line;
	}

	@Override
	public String[] getLineAsArray() throws IOException {
		return getLine().Values;
	}

	@Override
	public String toString() {
		String result = "(" + index + "): ";
		if (hasLine()) {
			result += wsvDocument.Lines.get(index).toString();
		}
		return result;
	}

	@Override
	public int getLineIndex() {
		return index;
	}
}

class WsvJaggedArrayLineIterator implements WsvLineIterator {
	private final String[][] lines;
	String endKeyword;

	int index;

	public WsvJaggedArrayLineIterator(String[][] lines, String endKeyword) {
		this.lines = lines;
		this.endKeyword = endKeyword;
	}

	@Override
	public String getEndKeyword() {
		return endKeyword;
	}

	@Override
	public boolean hasLine() {
		return index < lines.length;
	}

	@Override
	public boolean isEmptyLine() {
		return hasLine() && (lines[index] == null || lines[index].length == 0);
	}

	@Override
	public WsvLine getLine() throws IOException {
		return new WsvLine(getLineAsArray());
	}

	@Override
	public String[] getLineAsArray() throws IOException {
		String[] line = lines[index];
		index++;
		return line;
	}

	@Override
	public String toString() {
		String result = "(" + index + "): ";
		if (hasLine()) {
			String[] line = lines[index];
			if (line != null) {
				result += WsvSerializer.serializeLine(line);
			}
		}
		return result;
	}

	@Override
	public int getLineIndex() {
		return index;
	}
}

class WsvStreamLineIterator implements WsvLineIterator {
	WsvStreamReader reader;
	String endKeyword;
	WsvLine currentLine;

	int index;

	public WsvStreamLineIterator(WsvStreamReader reader, String endKeyword) throws IOException {
		Objects.requireNonNull(endKeyword);
		this.reader = reader;
		this.endKeyword = endKeyword;

		currentLine = reader.readLine();
	}

	@Override
	public boolean hasLine() {
		return currentLine != null;
	}

	@Override
	public boolean isEmptyLine() {
		return hasLine() && !currentLine.hasValues();
	}

	@Override
	public WsvLine getLine() throws IOException {
		WsvLine result = currentLine;
		currentLine = reader.readLine();
		index++;
		return result;
	}

	@Override
	public String[] getLineAsArray() throws IOException {
		return getLine().Values;
	}

	@Override
	public String getEndKeyword() {
		return endKeyword;
	}

	@Override
	public String toString() {
		String result = "(" + index + "): ";
		if (hasLine()) {
			result += currentLine.toString();
		}
		return result;
	}

	@Override
	public int getLineIndex() {
		return index;
	}
}

class SmlFileAppend {
	static class ReverseLineIterator implements AutoCloseable {
		RandomAccessFile file;
		long index;
		Charset charset;
		ReliableTxtEncoding encoding;
				
		public ReverseLineIterator(String filePath, ReliableTxtEncoding encoding) throws FileNotFoundException, IOException {
			file = new RandomAccessFile(filePath, "rw");
			index = file.length();
			charset = encoding.getCharset();
			this.encoding = encoding;
		}
		
		private void readLeadingZeros() throws IOException {
			int expectedCount = 0;
			if (encoding == ReliableTxtEncoding.UTF_16) {
				expectedCount = 1;
			} else if (encoding == ReliableTxtEncoding.UTF_32) {
				expectedCount = 3;
			}
			for (int i=0; i<expectedCount; i++) {
				index--;
				file.seek(index);
				byte b = file.readByte();
				if (b != 0) {
					throw new SmlParserException(-1, "New line character detection failed");
				}
			}
		}
		
		public String getLine() throws IOException {
			int numBytes = 0;
			while (index > 0) {
				index--;
				file.seek(index);
				byte b = file.readByte();
				if (b == '\n') {
					long lineStart = index+1;
					readLeadingZeros();
					if (encoding == ReliableTxtEncoding.UTF_16_REVERSE) {
						lineStart++;
						numBytes--;
					}
					return getLine(lineStart, numBytes);
				}
				numBytes++;
			}
			throw new SmlParserException(-1, "End line expected");
		}
		
		private String getLine(long lineStart, int numBytes) throws IOException {
			if (numBytes == 0) {
				return "";
			}
			file.seek(lineStart);
			byte[] bytes = new byte[numBytes];
			file.read(bytes);
			return new String(bytes, charset);
		}
		
		public void truncate() throws IOException {
			file.setLength(index);
		}

		@Override
		public void close() throws Exception {
			file.close();
		}
	}
	
	public static String removeEnd(String filePath, ReliableTxtEncoding encoding) throws FileNotFoundException, IOException, Exception {
		
		String endKeyword;
		try (ReverseLineIterator iterator = new ReverseLineIterator(filePath, encoding)) {
			while (true) {
				String lineStr = iterator.getLine();
				WsvLine line = WsvLine.parse(lineStr);
				if (line.hasValues()) {
					if (line.Values.length > 1) {
						throw new SmlParserException(-1, "Invalid end line");
					}
					endKeyword = line.Values[0];
					break;
				}
			}
			iterator.truncate();
		}
		return endKeyword;
	}
}

class SmlStreamReader implements AutoCloseable {
	public final ReliableTxtEncoding Encoding;
	WsvStreamReader reader;
	final String endKeyword;
	public final SmlElement Root;
	WsvStreamLineIterator iterator;
	
	public final ArrayList<SmlEmptyNode> EmptyNodesBefore = new ArrayList<>();
	
	public SmlStreamReader(String filePath) throws IOException {
		this(filePath, null);
	}
	
	public SmlStreamReader(String filePath, String endKeyword) throws IOException {
		reader = new WsvStreamReader(filePath);
		Encoding = reader.Encoding;
		if (endKeyword == null) {
			endKeyword = "End";
		}
		this.endKeyword = endKeyword;
		
		iterator = new WsvStreamLineIterator(reader, endKeyword);
		
		Root = SmlParser.readRootElement(iterator, EmptyNodesBefore);
	}
	
	public SmlNode readNode() throws IOException {
		return SmlParser.readNode(iterator, Root);
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}
}

class SmlStreamWriter implements AutoCloseable {
	WsvStreamWriter writer;
	WsvDocument wsvDocument;
	String endKeyword = "End";
	String defaultIndentation;
	
	public final ReliableTxtEncoding Encoding;
	public final boolean AppendMode;
	
	public SmlStreamWriter(SmlDocument template, String filePath) throws IOException, Exception {
		this(template, filePath, null, false);
	}
	
	public SmlStreamWriter(SmlDocument template, String filePath, boolean append) throws IOException, Exception {
		this(template, filePath, null, append);
	}
	
	public SmlStreamWriter(SmlDocument template, String filePath, ReliableTxtEncoding encoding) throws IOException, Exception {
		this(template, filePath, encoding, false);
	}
	
	public SmlStreamWriter(SmlDocument template, String filePath, ReliableTxtEncoding encoding,
			boolean append) throws IOException, Exception {
		Objects.requireNonNull(template);
		
		if (append) {
			Path path = Paths.get(filePath);
			if (!Files.exists(path) || Files.size(path) == 0) {
				append = false;
			}
		}
		writer = new WsvStreamWriter(filePath, encoding, append);
		Encoding = writer.Encoding;
		AppendMode = writer.AppendMode;
		
		if (append) {
			template.endKeyword = SmlFileAppend.removeEnd(filePath, Encoding);
		}
		
		wsvDocument = new WsvDocument();
		endKeyword = template.endKeyword;
		defaultIndentation = template.defaultIndentation;
		
		if (!append) {
			String rootElementName = template.getRoot().getName();
			writer.writeLine(rootElementName);
		}
	}
	
	public void writeNode(SmlNode node) throws IOException {
		wsvDocument.Lines.clear();
		node.toWsvLines(wsvDocument, 1, defaultIndentation, endKeyword);
		writer.writeLines(wsvDocument);
	}

	@Override
	public void close() throws Exception {
		writer.writeLine(endKeyword);
		writer.close();
	}
}