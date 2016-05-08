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

	private class State {
		StringBuilder line = new StringBuilder();
		boolean precedingBackslash = false;
		boolean newLine = true;
		boolean isCommentLine = false;
		boolean skipLinefeed = false;
		boolean skipWhitespace = true;
		int index = 0;
		int limit = 0;
		int unicode = 0;
		int unicodeIndex = -1;
		StringBuilder description = new StringBuilder();
		char c;
	}

	public Collection<MessagePropertyInfo> parse(InputStream data) throws IOException {
		Collection<MessagePropertyInfo> infos = new ArrayList<>();

		byte[] buffer = new byte[8192];
		MessagePropertyInfo info = new MessagePropertyInfo();

		State state = new State();
		while (true) {
			state.limit = data.read(buffer);
			if (state.limit < 0) {
				break;
			}

			while (state.index < state.limit) {
				state.c = (char) (0xff & buffer[state.index++]);
				if (handleSkipLinefeed(state)) {
					continue;
				}
				if (handleSkipWhitespace(state)) {
					continue;
				}
				if (handleNewLine(state)) {
					continue;
				}
				if (!ArrayUtils.contains(LINE_BREAK, state.c)) {
					if (buildUpUnicode(state)) {
						continue;
					}
					unescapeEscapedValue(state);
					handleBackslashState(state);

					if (!state.precedingBackslash) {
						state.line.append(state.c);
					}
				} else {
					if (state.line.length() > 0) {
						String content = state.line.toString();
						if (state.isCommentLine) {
							appendComment(state, content);
						} else {
							if (state.precedingBackslash) {
								resetStateForEscapedBaskslash(state);
								continue;
							}
							addKeyValueData(info, content, state.description);
							infos.add(info);
							info = new MessagePropertyInfo();
						}
						state.line.setLength(0);
					}
					resetStateForNextLine(state);
				}
			}
		}
		if (!state.isCommentLine && state.line.length() > 0) {
			addKeyValueData(info, state.line.toString(), state.description);
			infos.add(info);
		}

		return infos;
	}

	private void resetStateForNextLine(State state) {
		state.isCommentLine = false;
		state.newLine = true;
		state.skipWhitespace = true;
	}

	private void appendComment(State state, String content) {
		if (state.description.length() > 0) {
			state.description.append('\n');
		}
		state.description.append(content);
	}

	private boolean handleSkipLinefeed(State state) {
		if (state.skipLinefeed) {
			state.skipLinefeed = false;
			if (state.c == '\n') {
				return true;
			}
		}

		return false;
	}

	private boolean handleSkipWhitespace(State state) {
		if (state.skipWhitespace) {
			if (ArrayUtils.contains(WHITESPACE, state.c)) {
				return true;
			}
			state.skipWhitespace = false;
		}

		return false;
	}

	private boolean handleNewLine(State state) {
		if (state.newLine) {
			state.newLine = false;
			if (ArrayUtils.contains(COMMENT_CHARS, state.c)) {
				state.isCommentLine = true;
				state.skipWhitespace = true;
				return true;
			}
		}

		return false;
	}

	private void handleBackslashState(State state) {
		if (state.c == '\\') {
			state.precedingBackslash = !state.precedingBackslash;
		} else {
			state.precedingBackslash = false;
		}
	}

	private boolean buildUpUnicode(State state) {
		if (state.unicodeIndex >= 0) {
			state.unicode |= toUnicodeHalfByte(state.c) << 4 * (4 - ++state.unicodeIndex);
			if (state.unicodeIndex == 4) {
				state.c = (char) state.unicode;
				state.unicodeIndex = -1;
				state.unicode = 0;
			} else {
				return true;
			}
		}

		if (state.precedingBackslash && state.c == 'u') {
			state.unicodeIndex = 0;
			return true;
		}

		return false;
	}

	private void unescapeEscapedValue(State state) {
		if (state.precedingBackslash) {
			switch (state.c) {
			case 't':
				state.c = '\t';
				break;
			case 'r':
				state.c = '\r';
				break;
			case 'n':
				state.c = '\n';
				break;
			case 'f':
				state.c = '\f';
				break;
			}
		}
	}
	
	private void resetStateForEscapedBaskslash(State state) {
			state.skipWhitespace = true;
			state.precedingBackslash = false;
			state.skipLinefeed = true;
	}

	private void addKeyValueData(MessagePropertyInfo info, String content, StringBuilder description) {
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

		if (description.length() > 0) {
			info.setDescription(description.toString());
			description.setLength(0);
		}
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
