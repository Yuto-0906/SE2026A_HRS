package app.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

/**
 * URLクエリとフォーム本文を解析する。
 */
public final class RequestParameters {

	private static final int MAX_BODY_SIZE = 16 * 1024;

	private RequestParameters() {
	}

	public static Map<String, String> parse(HttpExchange exchange) throws IOException {
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parseEncoded(exchange.getRequestURI().getRawQuery(), parameters);
		if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			parseEncoded(readBody(exchange.getRequestBody()), parameters);
		}
		return parameters;
	}

	private static String readBody(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int total = 0;
		int length;
		while ((length = input.read(buffer)) >= 0) {
			total += length;
			if (total > MAX_BODY_SIZE) {
				throw new IOException("入力データが大きすぎます。");
			}
			output.write(buffer, 0, length);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private static void parseEncoded(String encoded, Map<String, String> parameters) {
		if (encoded == null || encoded.length() == 0) {
			return;
		}
		String[] pairs = encoded.split("&");
		for (String pair : pairs) {
			String[] parts = pair.split("=", 2);
			String key = decode(parts[0]);
			String value = parts.length == 2 ? decode(parts[1]) : "";
			parameters.put(key, value);
		}
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}
}
