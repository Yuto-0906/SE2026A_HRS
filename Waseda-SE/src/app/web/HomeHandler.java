package app.web;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * トップページ。
 */
public class HomeHandler implements HttpHandler {

	public void handle(HttpExchange exchange) throws IOException {
		if (!"/".equals(exchange.getRequestURI().getPath())
				|| !"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.html(exchange, 404, HtmlRenderer.errorPage("ページが見つかりません。"));
			return;
		}
		String body = "<div class=\"grid\">"
				+ "<section class=\"card\"><h2>ホテルを予約する</h2>"
				+ "<p>利用者情報とチェックイン日を入力して部屋を予約します。</p>"
				+ "<a class=\"button\" href=\"/reservations/new\">予約画面へ</a></section>"
				+ "<section class=\"card\"><h2>自分の予約を確認する</h2>"
				+ "<p>ユーザIDから予約一覧を確認し，予約をキャンセルできます。</p>"
				+ "<a class=\"button\" href=\"/reservations/mine\">予約一覧へ</a></section>"
				+ "<section class=\"card\"><h2>受付係用管理</h2>"
				+ "<p>全予約の確認，チェックイン，チェックアウトを行います。</p>"
				+ "<a class=\"button secondary\" href=\"/staff/reservations\">受付係画面へ</a></section>"
				+ "</div>";
		WebResponses.html(exchange, 200, HtmlRenderer.page("ホテル予約システム", body));
	}
}
