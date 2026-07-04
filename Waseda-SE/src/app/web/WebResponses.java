package app.web;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/**
 * HTTP応答の共通処理。
 */
public final class WebResponses {

	private WebResponses() {
	}

	public static void html(HttpExchange exchange, int status, String html) throws IOException {
		byte[] body = html.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
		exchange.getResponseHeaders().set("Content-Security-Policy",
				"default-src 'self'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; form-action 'self'");
		exchange.sendResponseHeaders(status, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}

	public static void redirect(HttpExchange exchange, String location) throws IOException {
		exchange.getResponseHeaders().set("Location", URI.create(location).toASCIIString());
		exchange.sendResponseHeaders(303, -1);
		exchange.close();
	}

	public static void methodNotAllowed(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().set("Allow", "GET, POST");
		html(exchange, 405, HtmlRenderer.errorPage("許可されていない操作です。"));
	}
}
