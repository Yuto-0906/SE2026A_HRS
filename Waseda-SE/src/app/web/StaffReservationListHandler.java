package app.web;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.AppException;
import app.reservation.StaffReservationListControl;
import domain.reservation.Reservation;

/**
 * 受付係向け予約一覧画面。
 */
public class StaffReservationListHandler implements HttpHandler {

	private final StaffReservationListControl control;

	public StaffReservationListHandler(StaffReservationListControl control) {
		this.control = control;
	}

	public void handle(HttpExchange exchange) throws IOException {
		if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.methodNotAllowed(exchange);
			return;
		}
		try {
			List<Reservation> reservations = control.findAll();
			String body = "<div class=\"message\">予約済みはチェックイン，チェックイン済みは"
					+ "6000円の支払確認後にチェックアウトできます。</div>"
					+ HtmlRenderer.reservationTable(reservations, true, null);
			WebResponses.html(exchange, 200,
					HtmlRenderer.page("受付係用予約管理", body));
		}
		catch (AppException e) {
			WebResponses.html(exchange, 500, HtmlRenderer.errorPage(e.getMessage()));
		}
	}
}
