package integration;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import app.web.WebServer;

/**
 * Webサーバの主要画面と予約フォームをHTTP経由で検証する。
 */
public class WebHttpSmokeTest {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("setup.sqlのパスを指定してください。");
		}
		String databaseUrl = "jdbc:hsqldb:mem:hrs-web-test";
		System.setProperty("hrs.db.url", databaseUrl);
		initializeDatabase(databaseUrl, Path.of(args[0]));

		WebServer server = new WebServer(0);
		server.start();
		try {
			String baseUrl = "http://127.0.0.1:" + server.getPort();
			HttpClient client = HttpClient.newHttpClient();
			assertContains(get(client, baseUrl + "/"), "ホテル予約システム");
			assertContains(get(client, baseUrl + "/reservations/new"), "部屋を予約する");

			String form = "userId=" + encode("http-user")
					+ "&name=" + encode("HTTP テスト")
					+ "&phone=" + encode("090-9999-9999")
					+ "&checkInDate=" + encode(LocalDate.now().toString());
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "/reservations/new"))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(form))
					.build();
			HttpResponse<String> response =
					client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			assertEquals(200, response.statusCode());
			assertContains(response.body(), "予約完了");

			String customerReservations =
					get(client, baseUrl + "/reservations/mine?userId=http-user");
			assertContains(customerReservations, "<tbody><tr>");
			assertContains(customerReservations, "1001");
			String staffReservations = get(client, baseUrl + "/staff/reservations");
			assertContains(staffReservations, "http-user");
			assertContains(staffReservations, "action=\"/staff/checkin\"");
			System.out.println("WebHttpSmokeTest: OK");
		}
		finally {
			server.stop();
		}
	}

	private static String get(HttpClient client, String url) throws Exception {
		HttpResponse<String> response = client.send(
				HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		assertEquals(200, response.statusCode());
		return response.body();
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static void initializeDatabase(String url, Path setupSql) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		String sql = Files.readString(setupSql, StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "");
		try (Connection connection = DriverManager.getConnection(url, "sa", "");
				Statement statement = connection.createStatement()) {
			for (String command : sql.split(";")) {
				if (command.trim().length() > 0) {
					statement.execute(command.trim());
				}
			}
		}
	}

	private static void assertEquals(Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError("expected=" + expected + ", actual=" + actual);
		}
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError("expected text not found: " + expected);
		}
	}
}
