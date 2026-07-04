package app.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.AppException;
import app.checkout.CheckOutRoomControl;

/**
 * チェックアウト操作。
 */
public class CheckOutHandler implements HttpHandler {

	private final CheckOutRoomControl control;

	public CheckOutHandler(CheckOutRoomControl control) {
		this.control = control;
	}

	public void handle(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			WebResponses.methodNotAllowed(exchange);
			return;
		}
		Map<String, String> parameters = RequestParameters.parse(exchange);
		if (!"true".equals(parameters.get("paymentComplete"))) {
			WebResponses.html(exchange, 400,
					HtmlRenderer.errorPage("宿泊料金の支払完了を確認してください。"));
			return;
		}
		try {
			control.checkOut(parameters.get("reservationNumber"), LocalDate.now());
			WebResponses.html(exchange, 200,
					HtmlRenderer.messagePage("チェックアウト完了",
							"宿泊料金" + control.getCharge() + "円の支払を確認し，チェックアウトしました。",
							"/staff/reservations", "予約管理へ戻る"));
		}
		catch (AppException e) {
			WebResponses.html(exchange, 400, HtmlRenderer.errorPage(e.getMessage()));
		}
	}
}
