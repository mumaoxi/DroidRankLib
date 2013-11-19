package com.umeng.ad.app.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class IOUtils {

	public static String toString(InputStream input) {
		try {
			StringWriter sw = new StringWriter();
			copy(input, sw);
			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String toString(InputStream input, String encoding)
			throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw, encoding);
		return sw.toString();
	}

	static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[4096];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	static void copy(InputStream input, Writer output) throws IOException {
		InputStreamReader in = new InputStreamReader(input);
		copy(in, output);
	}

	static void copy(InputStream input, Writer output, String encoding)
			throws IOException {
		if (encoding == null) {
			copy(input, output);
		} else {
			InputStreamReader in = new InputStreamReader(input, encoding);
			copy(in, output);
		}
	}

	static int copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[4096];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	static void copy(Reader input, OutputStream output) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(output);
		copy(input, out);

		out.flush();
	}

	static void copy(Reader input, OutputStream output, String encoding)
			throws IOException {
		if (encoding == null) {
			copy(input, output);
		} else {
			OutputStreamWriter out = new OutputStreamWriter(output, encoding);
			copy(input, out);

			out.flush();
		}
	}

}
