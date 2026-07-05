package app.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.AppException;
import app.checkin.CheckInRoomControl;
import domain.reservation.Reservation;

/**
 * チェックイン操作。
 */
public class CheckInHandler implements HttpHandler {

	private final CheckInRoomControl control;

	public CheckInHandler(CheckInRoomControl control) {
		this.control = control;
	}

	public void handle(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.methodNotAllowed(exchange);
			return;
		}
		Map<String, String> parameters = RequestParameters.parse(exchange);
		try {
			Reservation reservation = control.checkIn(
					parameters.get("reservationNumber"), LocalDate.now());
			WebResponses.html(exchange, 200,
					HtmlRenderer.messagePage("チェックイン完了",
							"部屋番号は" + reservation.getRoom().getRoomNumber() + "です。",
							"/staff/reservations", "予約管理へ戻る"));
		}
		catch (AppException e) {
			WebResponses.html(exchange, 400, HtmlRenderer.errorPage(e.getMessage()));
		}
	}
}
