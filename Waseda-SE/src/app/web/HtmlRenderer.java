package app.web;

import java.time.format.DateTimeFormatter;
import java.util.List;

import app.checkout.CheckOutRoomControl;
import domain.reservation.Reservation;
import domain.reservation.ReservationStatus;
import util.HtmlEscaper;

/**
 * Web画面のHTMLを生成する。
 */
public final class HtmlRenderer {

	private static final DateTimeFormatter DATE_TIME =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private HtmlRenderer() {
	}

	public static String page(String title, String body) {
		return "<!doctype html><html lang=\"ja\"><head><meta charset=\"UTF-8\">"
				+ "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
				+ "<title>" + HtmlEscaper.escape(title) + " - HRS</title>"
				+ "<style>" + styles() + "</style></head><body>"
				+ "<header><a class=\"brand\" href=\"/\">Hotel Reservation System</a>"
				+ "<nav><a href=\"/reservations/new\">予約</a>"
				+ "<a href=\"/reservations/mine\">自分の予約</a>"
				+ "<a href=\"/staff/reservations\">受付係画面</a></nav></header>"
				+ "<main><h1>" + HtmlEscaper.escape(title) + "</h1>" + body + "</main>"
				+ "<footer>Software Engineering A - Hotel Reservation System</footer>"
				+ "</body></html>";
	}

	public static String errorPage(String message) {
		return page("エラー", "<div class=\"message error\">"
				+ HtmlEscaper.escape(message) + "</div><p><a href=\"javascript:history.back()\">戻る</a></p>");
	}

	public static String messagePage(String title, String message, String link, String linkText) {
		return page(title, "<div class=\"message success\">" + HtmlEscaper.escape(message)
				+ "</div><p><a class=\"button secondary\" href=\"" + HtmlEscaper.escape(link)
				+ "\">" + HtmlEscaper.escape(linkText) + "</a></p>");
	}

	public static String reservationTable(List<Reservation> reservations, boolean staff,
			String userId) {
		if (reservations.isEmpty()) {
			return "<div class=\"empty\">予約がありません。</div>";
		}
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"table-wrap\"><table><thead><tr>");
		if (staff) {
			html.append("<th>ユーザ</th><th>氏名</th>");
		}
		html.append("<th>予約番号</th><th>チェックイン日</th><th>部屋</th>")
				.append("<th>予約日時</th><th>状態</th><th>操作</th></tr></thead><tbody>");
		for (Reservation reservation : reservations) {
			html.append("<tr>");
			if (staff) {
				html.append("<td>").append(escape(reservation.getUser().getUserId())).append("</td>")
						.append("<td>").append(escape(reservation.getUser().getName())).append("</td>");
			}
			html.append("<td><strong>").append(escape(reservation.getReservationNumber()))
					.append("</strong></td>")
					.append("<td>").append(reservation.getCheckInDate()).append("</td>")
					.append("<td>").append(escape(reservation.getRoom().getRoomNumber())).append("</td>")
					.append("<td>").append(reservation.getReservedAt().format(DATE_TIME)).append("</td>")
					.append("<td><span class=\"status ").append(reservation.getReservationStatus().name().toLowerCase())
					.append("\">").append(escape(reservation.getReservationStatus().getDisplayName()))
					.append("</span></td><td>");
			appendAction(html, reservation, staff, userId);
			html.append("</td></tr>");
		}
		return html.append("</tbody></table></div>").toString();
	}

	private static void appendAction(StringBuilder html, Reservation reservation, boolean staff,
			String userId) {
		if (!staff && reservation.getReservationStatus() == ReservationStatus.RESERVED) {
			html.append("<form method=\"post\" action=\"/reservations/cancel\" ")
					.append("onsubmit=\"return confirm('この予約をキャンセルしますか？')\">")
					.append(hidden("userId", userId))
					.append(hidden("reservationNumber", reservation.getReservationNumber()))
					.append("<button class=\"danger\" type=\"submit\">キャンセル</button></form>");
			return;
		}
		if (staff && reservation.getReservationStatus() == ReservationStatus.RESERVED) {
			html.append("<form method=\"post\" action=\"/staff/checkin\">")
					.append(hidden("reservationNumber", reservation.getReservationNumber()))
					.append("<button type=\"submit\">チェックイン</button></form>");
			return;
		}
		if (staff && reservation.getReservationStatus() == ReservationStatus.CHECKED_IN) {
			html.append("<form method=\"post\" action=\"/staff/checkout\" ")
					.append("onsubmit=\"return confirm('").append(CheckOutRoomControl.CHARGE)
					.append("円の支払を確認しましたか？')\">")
					.append(hidden("reservationNumber", reservation.getReservationNumber()))
					.append(hidden("paymentComplete", "true"))
					.append("<button type=\"submit\">").append(CheckOutRoomControl.CHARGE)
					.append("円支払確認・チェックアウト</button></form>");
			return;
		}
		html.append("-");
	}

	public static String hidden(String name, String value) {
		return "<input type=\"hidden\" name=\"" + escape(name) + "\" value=\""
				+ escape(value) + "\">";
	}

	private static String escape(String value) {
		return HtmlEscaper.escape(value);
	}

	private static String styles() {
		return "*{box-sizing:border-box}body{margin:0;background:#f5f7fb;color:#1c2430;"
				+ "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','Noto Sans JP',sans-serif}"
				+ "header{display:flex;justify-content:space-between;align-items:center;padding:16px 5%;"
				+ "background:#16324f;color:white;gap:24px;flex-wrap:wrap}.brand{font-weight:700;font-size:20px}"
				+ "header a{color:white;text-decoration:none}nav{display:flex;gap:18px}"
				+ "main{width:min(1180px,92%);margin:32px auto;min-height:70vh}h1{color:#16324f}"
				+ ".card{background:white;border-radius:12px;padding:24px;box-shadow:0 5px 18px #16324f18;"
				+ "margin:20px 0}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:18px}"
				+ "label{display:block;font-weight:600;margin:14px 0 6px}input{width:100%;padding:10px 12px;"
				+ "border:1px solid #b9c4d0;border-radius:6px;font-size:16px}button,.button{display:inline-block;"
				+ "border:0;border-radius:6px;background:#1565c0;color:white;padding:10px 16px;font-weight:700;"
				+ "cursor:pointer;text-decoration:none}.secondary{background:#52687d}.danger{background:#b42318}"
				+ ".message{padding:14px 18px;border-radius:8px;margin:18px 0}.success{background:#def7e5;"
				+ "color:#176b32}.error{background:#fee4e2;color:#912018}.empty{background:white;padding:24px;"
				+ "border-radius:10px}.table-wrap{overflow-x:auto;background:white;border-radius:10px;"
				+ "box-shadow:0 4px 16px #16324f12}table{width:100%;border-collapse:collapse}th,td{padding:12px;"
				+ "border-bottom:1px solid #e5eaf0;text-align:left;white-space:nowrap}th{background:#eaf0f6}"
				+ ".status{padding:5px 9px;border-radius:99px;font-size:13px}.reserved{background:#fff1c2}"
				+ ".checked_in{background:#dceeff}.checked_out{background:#def7e5}.canceled{background:#eee}"
				+ "footer{text-align:center;padding:24px;color:#607080}@media(max-width:640px){nav{width:100%;"
				+ "overflow-x:auto}main{width:94%}}";
	}
}
