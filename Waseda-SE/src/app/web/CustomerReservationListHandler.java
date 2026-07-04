package app.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.AppException;
import app.reservation.CustomerReservationListControl;
import domain.reservation.Reservation;
import util.HtmlEscaper;

/**
 * 利用者自身の予約一覧画面。
 */
public class CustomerReservationListHandler implements HttpHandler {

	private final CustomerReservationListControl control;

	public CustomerReservationListHandler(CustomerReservationListControl control) {
		this.control = control;
	}

	public void handle(HttpExchange exchange) throws IOException {
		if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.methodNotAllowed(exchange);
			return;
		}
		Map<String, String> parameters = RequestParameters.parse(exchange);
		String userId = parameters.get("userId");
		List<Reservation> reservations = Collections.emptyList();
		String error = null;
		if (userId != null && userId.trim().length() > 0) {
			try {
				reservations = control.findByUser(userId);
			}
			catch (AppException e) {
				error = e.getMessage();
			}
		}
		StringBuilder body = new StringBuilder("<section class=\"card\"><form method=\"get\" ")
				.append("action=\"/reservations/mine\"><label for=\"userId\">ユーザID</label>")
				.append("<input id=\"userId\" name=\"userId\" value=\"")
				.append(HtmlEscaper.escape(userId)).append("\" required>")
				.append("<p><button type=\"submit\">予約を検索する</button></p></form></section>");
		if (error != null) {
			body.append("<div class=\"message error\">").append(HtmlEscaper.escape(error)).append("</div>");
		}
		else if (userId != null && userId.trim().length() > 0) {
			body.append(HtmlRenderer.reservationTable(reservations, false, userId));
		}
		WebResponses.html(exchange, 200,
				HtmlRenderer.page("自分の予約一覧", body.toString()));
	}
}
