package app.web;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.AppException;
import app.reservation.ReserveRoomControl;
import domain.reservation.Reservation;
import util.HtmlEscaper;

/**
 * 部屋予約画面。
 */
public class ReservationHandler implements HttpHandler {

	private final ReserveRoomControl control;

	public ReservationHandler(ReserveRoomControl control) {
		this.control = control;
	}

	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.html(exchange, 200, form(null, null));
			return;
		}
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.methodNotAllowed(exchange);
			return;
		}
		Map<String, String> parameters = RequestParameters.parse(exchange);
		try {
			LocalDate checkInDate = LocalDate.parse(parameters.get("checkInDate"));
			Reservation reservation = control.reserve(
					parameters.get("userId"),
					parameters.get("name"),
					parameters.get("phone"),
					checkInDate);
			String message = "予約が完了しました。予約番号は"
					+ reservation.getReservationNumber() + "，部屋番号は"
					+ reservation.getRoom().getRoomNumber() + "です。";
			WebResponses.html(exchange, 200,
					HtmlRenderer.messagePage("予約完了", message, "/", "トップへ戻る"));
		}
		catch (DateTimeParseException e) {
			WebResponses.html(exchange, 400, form(parameters, "チェックイン日の形式が正しくありません。"));
		}
		catch (AppException e) {
			WebResponses.html(exchange, 400, form(parameters, e.getMessage()));
		}
	}

	private String form(Map<String, String> values, String error) {
		String userId = value(values, "userId");
		String name = value(values, "name");
		String phone = value(values, "phone");
		String date = value(values, "checkInDate");
		StringBuilder body = new StringBuilder("<section class=\"card\">");
		if (error != null) {
			body.append("<div class=\"message error\">").append(HtmlEscaper.escape(error)).append("</div>");
		}
		body.append("<form method=\"post\" action=\"/reservations/new\">")
				.append(labelInput("ユーザID", "userId", "text", userId, "user-001"))
				.append(labelInput("氏名", "name", "text", name, "早稲田 太郎"))
				.append(labelInput("電話番号", "phone", "tel", phone, "090-1234-5678"))
				.append(labelInput("チェックイン日", "checkInDate", "date", date, ""))
				.append("<p>チェックアウト日は翌日，宿泊料金は1泊6000円です。</p>")
				.append("<button type=\"submit\">予約する</button></form></section>");
		return HtmlRenderer.page("部屋を予約する", body.toString());
	}

	private String labelInput(String label, String name, String type, String value, String placeholder) {
		return "<label for=\"" + name + "\">" + label + "</label><input id=\"" + name
				+ "\" name=\"" + name + "\" type=\"" + type + "\" value=\""
				+ HtmlEscaper.escape(value) + "\" placeholder=\"" + HtmlEscaper.escape(placeholder)
				+ "\" required>";
	}

	private String value(Map<String, String> values, String name) {
		return values == null || values.get(name) == null ? "" : values.get(name);
	}
}
