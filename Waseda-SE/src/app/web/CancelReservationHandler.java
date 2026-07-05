package app.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.AppException;
import app.reservation.CancelReservationControl;

/**
 * 予約キャンセル操作。
 */
public class CancelReservationHandler implements HttpHandler {

	private final CancelReservationControl control;

	public CancelReservationHandler(CancelReservationControl control) {
		this.control = control;
	}

	public void handle(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.methodNotAllowed(exchange);
			return;
		}
		Map<String, String> parameters = RequestParameters.parse(exchange);
		try {
			control.cancel(parameters.get("userId"), parameters.get("reservationNumber"));
			String link = "/reservations/mine?userId=" + URLEncoder.encode(
					parameters.get("userId"), StandardCharsets.UTF_8);
			WebResponses.html(exchange, 200,
					HtmlRenderer.messagePage("キャンセル完了",
							"予約をキャンセルしました。", link, "予約一覧へ戻る"));
		}
		catch (AppException e) {
			WebResponses.html(exchange, 400, HtmlRenderer.errorPage(e.getMessage()));
		}
	}
}
