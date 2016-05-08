package de.fichtelmax.mojo.messagebundle.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class PropertyParser {
	private static final char[] WHITESPACE = { ' ', '\t', '\f' };
	private static final char[] COMMENT_CHARS = { '#', '!' };
	private static final char[] LINE_BREAK = { '\n', '\r' };

	public Collection<MessagePropertyInfo> parse(InputStream data) throws IOException {
		Collection<MessagePropertyInfo> infos = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		boolean precedingBackslash = false;
		boolean newLine = true;
		boolean isCommentLine = false;
		boolean skipLinefeed = false;
		boolean skipWhitespace = true;
		byte[] buffer = new byte[8192];
		int index = 0;
		int limit = 0;
		int unicode = 0;
		int unicodeIndex = -1;
		MessagePropertyInfo info = new MessagePropertyInfo();

		while (true) {
			limit = data.read(buffer);
			if (limit < 0) {
				break;
			}

			while (index < limit) {
				char c = (char) (0xff & buffer[index++]);
				if (skipLinefeed) {
					skipLinefeed = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhitespace) {
					if (ArrayUtils.contains(WHITESPACE, c)) {
						continue;
					}
					skipWhitespace = false;
				}
				if (newLine) {
					newLine = false;
					if (ArrayUtils.contains(COMMENT_CHARS, c)) {
						isCommentLine = true;
						continue;
					}
				}
				if (!ArrayUtils.contains(LINE_BREAK, c)) {
					if (unicodeIndex >= 0) {
						unicode |= toUnicodeHalfByte(c) << 4 * (4 - ++unicodeIndex);
						if (unicodeIndex == 4) {
							c = (char) unicode;
							unicodeIndex = -1;
							unicode = 0;
						} else {
							continue;
						}
					}

					if (precedingBackslash) {
						if (c == 'u') {
							unicodeIndex = 0;
							continue;
						}
						switch (c) {
						case 't':
							c = '\t';
							break;
						case 'r':
							c = '\r';
							break;
						case 'n':
							c = '\n';
							break;
						case 'f':
							c = '\f';
							break;
						}
					}
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
					if (!precedingBackslash) {
						line.append(c);
					}

				} else {
					if (line.length() > 0) {
						String content = line.toString();
						if (isCommentLine) {
							// TODO extract meta-info from comment
						} else {
							if (precedingBackslash) {
								skipWhitespace = true;
								precedingBackslash = false;
								skipLinefeed=true;
								continue;
							}
							addKeyValueData(info, content);
							infos.add(info);
							info = new MessagePropertyInfo();
						}
						line.setLength(0);
					}
					isCommentLine = false;
					newLine = true;
					skipWhitespace = true;
				}
			}
		}
		if (!isCommentLine && line.length() > 0) {
			addKeyValueData(info, line.toString());
			infos.add(info);
		}

		return infos;
	}

	private void addKeyValueData(MessagePropertyInfo info, String content) {
		int equalsIndex = content.indexOf('=');
		int colonIndex = content.indexOf(':');
		String separator;
		if (colonIndex < 0) {
			separator = "=";
		} else if (equalsIndex < 0) {
			separator = ":";
		} else {
			separator = equalsIndex < colonIndex ? "=" : ":";
		}
		String value = StringUtils.substringAfter(content, separator).trim();
		String key = value == null ? content.trim() : StringUtils.substringBefore(content, separator).trim();

		info.setPropertyName(key);
		info.setValue(value);
	}

	private static int toUnicodeHalfByte(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		if (c >= 'a' && c <= 'f') {
			return 10 + c - 'a';
		}
		if (c >= 'A' && c <= 'F') {
			return 10 + c - 'A';
		}
		throw new IllegalArgumentException("Malformed \\uxxxx encoding");
	}
}
