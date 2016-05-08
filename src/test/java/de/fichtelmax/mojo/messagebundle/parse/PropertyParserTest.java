package de.fichtelmax.mojo.messagebundle.parse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class PropertyParserTest {

	@Test
	public void simpleProperty() throws Exception {
		String content = "foo=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("foo"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void trimmedKey() throws Exception {
		String content = " \f\tfoo \f\t=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("foo"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void ignoreComments() throws Exception {
		String content = "#comment\nfoo=bar\r!2nd comment";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("foo"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void escapedBackslash() throws Exception {
		String content = "foo\\\\=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("foo\\"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void escapedNewline() throws Exception {
		String content = "line1\\\r\nline2\\\rline3\\\nline4=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("line1line2line3line4"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void escapedCarriageReturnLinefeed() throws Exception {
		String content = "line1\\\r\nline2=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("line1line2"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void skipWhitespaceAfterEscapedNewline() throws Exception {
		String content = "foo\\\n \t\fbar=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("foobar"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void decodeUnicode() throws Exception {
		String content = "foo\\u20AC\\u20ac=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getPropertyName(), is("foo€€"));
		assertThat(info.getValue(), is("bar"));
	}

	@Test
	public void parseDescriptionFromComment() throws Exception {
		String content = "# some description\nfoo=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getDescription(), is("some description"));
		assertThat(info.getPropertyName(), is("foo"));
		assertThat(info.getValue(), is("bar"));
	}
	
	@Test
	public void parseMultilineDescriptionFromComment() throws Exception {
		String content = "# line 1\n! line 2\nfoo=bar";

		InputStream data = IOUtils.toInputStream(content, StandardCharsets.US_ASCII);

		Collection<MessagePropertyInfo> infos = new PropertyParser().parse(data);

		assertThat(infos, hasSize(1));

		MessagePropertyInfo info = infos.iterator().next();

		assertThat(info.getDescription(), is("line 1\nline 2"));
		assertThat(info.getPropertyName(), is("foo"));
		assertThat(info.getValue(), is("bar"));
	}
}
