package com.stenway.loextensions.formats;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

enum ReliableTxtEncoding {
	UTF_8 {
		@Override
		public Charset getCharset() {
			return StandardCharsets.UTF_8;
		}
		
		@Override
		public byte getPreambleLength() {
			return 3;
		}
	},
	UTF_16 {
		@Override
		public Charset getCharset() {
			return StandardCharsets.UTF_16BE;
		}
		
		@Override
		public byte getPreambleLength() {
			return 2;
		}
	},
	UTF_16_REVERSE {
		@Override
		public Charset getCharset() {
			return StandardCharsets.UTF_16LE;
		}
		
		@Override
		public byte getPreambleLength() {
			return 2;
		}
	},
	UTF_32 {
		@Override
		public Charset getCharset() {
			return CHARSET_UTF_32;
		}
		
		@Override
		public byte getPreambleLength() {
			return 4;
		}
	};
	
	private static final Charset CHARSET_UTF_32 = Charset.forName("UTF-32BE");
  
	public abstract Charset getCharset();
	public abstract byte getPreambleLength();
}

class ReliableTxtEncoder {
	public static byte[] encode(String text, ReliableTxtEncoding encoding) {
		Charset charset = encoding.getCharset();
		String textWithPreamble = ((char)65279) + text;
		CharsetEncoder encoder = charset.newEncoder();
		CharBuffer charBuffer = CharBuffer.wrap(textWithPreamble);
		try {
			ByteBuffer byteBuffer = encoder.encode(charBuffer);
			int numBytes = byteBuffer.limit();
			byte[] bytes = new byte[numBytes];
			byteBuffer.get(bytes);
			return bytes;
		} catch (Exception e) {
			throw new ReliableTxtException("Text contains invalid characters");
		}
	}
}

class ReliableTxtDecoder {
	private static final String NO_RELIABLETXT_PREAMBLE = "Document does not have a ReliableTXT preamble";
	
	public static ReliableTxtEncoding getEncoding(byte[] bytes) {
		Objects.requireNonNull(bytes);

		if (bytes.length >= 3
				&& bytes[0] == (byte)0xEF 
				&& bytes[1] == (byte)0xBB
				&& bytes[2] == (byte)0xBF) {
			return ReliableTxtEncoding.UTF_8;
		} else if (bytes.length >= 2
				&& bytes[0] == (byte)0xFE 
				&& bytes[1] == (byte)0xFF) {
			return ReliableTxtEncoding.UTF_16;
		} else if (bytes.length >= 2
				&& bytes[0] == (byte)0xFF 
				&& bytes[1] == (byte)0xFE) {
			return ReliableTxtEncoding.UTF_16_REVERSE;
		} else if (bytes.length >= 4
				&& bytes[0] == 0 
				&& bytes[1] == 0
				&& bytes[2] == (byte)0xFE 
				&& bytes[3] == (byte)0xFF) {
			return ReliableTxtEncoding.UTF_32;
		} else {
			throw new ReliableTxtException(NO_RELIABLETXT_PREAMBLE);
		}
	}
	
	public static ReliableTxtEncoding getEncodingFromFile(String filePath) throws IOException {
		byte[] bytes = new byte[4];
		try (InputStream inputStream = new FileInputStream(filePath)) {
			if (inputStream.read(bytes, 0, 2) == 2) {
				if (bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB) {
					if (inputStream.read(bytes, 2, 1) == 1 
							&& bytes[2] == (byte)0xBF) {
						return ReliableTxtEncoding.UTF_8;
					}
				} else if (bytes[0] == (byte)0xFE && bytes[1] == (byte)0xFF) {
					return ReliableTxtEncoding.UTF_16;
				} else if (bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE) {
					return ReliableTxtEncoding.UTF_16_REVERSE;
				} else if (bytes[0] == 0 && bytes[1] == 0) {
					if (inputStream.read(bytes, 2, 2) == 2 
							&& bytes[2] == (byte)0xFE && bytes[3] == (byte)0xFF) {
						return ReliableTxtEncoding.UTF_32;
					}
				}
			}
		}
		throw new ReliableTxtException(NO_RELIABLETXT_PREAMBLE);
	}
	
	public static Object[] decode(byte[] bytes) {
		ReliableTxtEncoding detectedEncoding = getEncoding(bytes);
		Charset charset = detectedEncoding.getCharset();
		byte preambleLength = detectedEncoding.getPreambleLength();
		
		
		CharsetDecoder decoder = charset.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, preambleLength,
				bytes.length-preambleLength);
		String decodedText = null;
		try {
			CharBuffer charBuffer = decoder.decode(byteBuffer);
			decodedText = charBuffer.toString();
		} catch (Exception e) {
			throw new ReliableTxtException("The "+detectedEncoding.name()+" encoded text contains invalid data.");
		}
		
		return new Object[] {detectedEncoding, decodedText};
	}
}

class ReliableTxtDocument {
	protected String text;
	protected ReliableTxtEncoding encoding;
	
	public ReliableTxtDocument() {
		this("");
	}

	public ReliableTxtDocument(String text) {
		this(text, ReliableTxtEncoding.UTF_8);
	}
	
	public ReliableTxtDocument(int... codePoints) {
		this(codePoints, ReliableTxtEncoding.UTF_8);
	}
	
	public ReliableTxtDocument(CharSequence... lines) {
		this(lines, ReliableTxtEncoding.UTF_8);
	}
	
	public ReliableTxtDocument(Iterable<? extends CharSequence> lines) {
		this(lines, ReliableTxtEncoding.UTF_8);
	}

	public ReliableTxtDocument(String text, ReliableTxtEncoding encoding) {
		setText(text);
		setEncoding(encoding);
	}
	
	public ReliableTxtDocument(int[] codePoints, ReliableTxtEncoding encoding) {
		setText(codePoints);
		setEncoding(encoding);
	}
	
	public ReliableTxtDocument(CharSequence[] lines, ReliableTxtEncoding encoding) {
		setLines(lines);
		setEncoding(encoding);
	}
	
	public ReliableTxtDocument(Iterable<? extends CharSequence> lines, ReliableTxtEncoding encoding) {
		setLines(lines);
		setEncoding(encoding);
	}

	public ReliableTxtDocument(byte[] bytes) {
		Object[] decoderResult = ReliableTxtDecoder.decode(bytes);
		
		setEncoding((ReliableTxtEncoding)decoderResult[0]);
		setText((String)decoderResult[1]);
	}
	
	public final void setText(String text) {
		Objects.requireNonNull(text);
		this.text = text;
	}
	
	public void setText(int[] codePoints) {
		Objects.requireNonNull(codePoints);
		this.text = new String(codePoints,0,codePoints.length);
	}
	
	public String getText() {
		return text;
	}
	
	public int[] getCodePoints() {
		return text.codePoints().toArray();
	}
	
	public final void setEncoding(ReliableTxtEncoding encoding) {
		Objects.requireNonNull(encoding);
		this.encoding = encoding;
	}
	
	public ReliableTxtEncoding getEncoding() {
		return encoding;
	}
	
	public final void setLines(CharSequence... lines) {
		text = ReliableTxtLines.join(lines);
	}
	
	public final void setLines(Iterable<? extends CharSequence> lines) {
		text = ReliableTxtLines.join(lines);
	}

	public String[] getLines() {
		return ReliableTxtLines.split(text);
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public byte[] getBytes() {
		return ReliableTxtEncoder.encode(text, encoding);
	}
	
	public void save(String filePath) throws IOException {
		Objects.requireNonNull(filePath);
		
		byte[] bytes = getBytes();
		Files.write(Paths.get(filePath),bytes);
	}
	
	public static ReliableTxtDocument load(String filePath) throws IOException {
		Objects.requireNonNull(filePath);
		
		byte[] bytes = Files.readAllBytes(Paths.get(filePath));
		return new ReliableTxtDocument(bytes);
	}

	public static void save(String text, String filePath) throws IOException {
		new ReliableTxtDocument(text).save(filePath);
	}
	
	public static void save(String text, ReliableTxtEncoding encoding,
			String filePath) throws IOException {
		new ReliableTxtDocument(text,encoding).save(filePath);
	}
	
	public static void save(int[] codepoints, String filePath) throws IOException {
		new ReliableTxtDocument(codepoints).save(filePath);
	}
	
	public static void save(int[] codepoints, ReliableTxtEncoding encoding,
			String filePath) throws IOException {
		new ReliableTxtDocument(codepoints,encoding).save(filePath);
	}
}

class ReliableTxtException extends RuntimeException {
	public ReliableTxtException(String message) {
		super(message);
	}
}

class ReliableTxtLines {
	public static String join(CharSequence... lines) {
		return String.join("\n", lines);
	}
	
	public static String join(Iterable<? extends CharSequence> lines) {
		return String.join("\n", lines);
	}
	
	public static String[] split(String text) {
		return text.split("\\n");
	}
}

class ReliableTxtCharIterator {
	protected final StringBuilder sb = new StringBuilder();
	protected final int[] chars;
	protected int index;
	
	public ReliableTxtCharIterator(String text) {
		Objects.requireNonNull(text);
		chars = text.codePoints().toArray();
	}
	
	public String getText() {
		return new String(chars, 0, chars.length);
	}

	public int[] getLineInfo() {
		int lineIndex = 0;
		int linePosition = 0;
		for (int i=0; i<index; i++) {
			if (chars[i] == '\n') {
				lineIndex++;
				linePosition = 0;
			} else {
				linePosition++;
			}
		}
		return new int[] {lineIndex, linePosition};
	}
	
	public boolean isEndOfText() {
		return index >= chars.length;
	}

	public boolean isChar(int c) {
		if (isEndOfText()) return false;
		return chars[index] == c;
	}
	
	public boolean tryReadChar(int c) {
		if (!isChar(c)) return false;
		index++;
		return true;
	}
}

class ReliableTxtStreamReader implements AutoCloseable {
	public final ReliableTxtEncoding Encoding;
	BufferedReader reader;
	StringBuilder sb;
	boolean endReached;
	
	public ReliableTxtStreamReader(String filePath) throws IOException {
		Encoding = ReliableTxtDecoder.getEncodingFromFile(filePath);
		Charset charset = Encoding.getCharset();
		Path path = Paths.get(filePath);
		reader = Files.newBufferedReader(path, charset);
		
		if (Encoding != ReliableTxtEncoding.UTF_32) {
			int preamble = reader.read();
		}
		sb = new StringBuilder();
	}
	
	public String readLine() throws IOException {
		if (endReached) {
			return null;
		}
		int c;
		sb.setLength(0);
		while (true) {
			c = reader.read();
			if (c == '\n') {
				break;
			} else if(c < 0) {
				endReached = true;
				break;
			}
			sb.append((char)c);
		}
		return sb.toString();
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}
}

class ReliableTxtStreamWriter implements AutoCloseable {
	public final ReliableTxtEncoding Encoding;
	BufferedWriter writer;
	boolean isFirstLine;
	
	public final boolean AppendMode;
	
	public ReliableTxtStreamWriter(String filePath) throws IOException {
		this(filePath, null, false);
	}
	
	public ReliableTxtStreamWriter(String filePath, boolean append) throws IOException {
		this(filePath, null, append);
	}
	
	public ReliableTxtStreamWriter(String filePath, ReliableTxtEncoding encoding) throws IOException {
		this(filePath, encoding, false);
	}
			
	public ReliableTxtStreamWriter(String filePath, ReliableTxtEncoding encoding,
			boolean append) throws IOException {
		if (encoding == null) {
			encoding = ReliableTxtEncoding.UTF_8;
		}

		Path path = Paths.get(filePath);
		
		isFirstLine = true;
		OpenOption[] options = new OpenOption[0];
		if (append && Files.exists(path) && Files.size(path) > 0) {
			encoding = ReliableTxtDecoder.getEncodingFromFile(filePath);
			isFirstLine = false;
			
			options = new OpenOption[] {StandardOpenOption.APPEND};
		}
		AppendMode = !isFirstLine;
		
		Encoding = encoding;
		
		Charset charset = Encoding.getCharset();

		writer = Files.newBufferedWriter(path, charset, options);
		if (isFirstLine) {
			writer.write((int)0xFEFF);
		}
	}
	
	public void writeLine(String line) throws IOException {
		if (!isFirstLine) {
			writer.append('\n');
		} else {
			isFirstLine = false;
		}
		writer.write(line);
	}
	
	public void writeLines(String... lines) throws IOException {
		for (String line:lines) {
			writeLine(line);
		}
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}
}
