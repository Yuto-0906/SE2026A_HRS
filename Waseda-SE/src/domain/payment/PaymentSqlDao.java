package domain.payment;

import java.util.Date;

/**
 * 旧CUIとのコンパイル互換性を保つクラス。
 *
 * 新しいWeb版では支払情報を永続化せず，予約状態でチェックアウトを管理する。
 */
public class PaymentSqlDao implements PaymentDao {

	public Payment getPayment(Date stayingDate, String roomNumber) throws PaymentException {
		throw unsupported();
	}

	public void updatePayment(Payment payment) throws PaymentException {
		throw unsupported();
	}

	public void createPayment(Payment payment) throws PaymentException {
		throw unsupported();
	}

	private PaymentException unsupported() {
		PaymentException exception =
				new PaymentException(PaymentException.CODE_DB_EXEC_QUERY_ERROR);
		exception.getDetailMessages().add("Payment persistence is not used by the Web version.");
		return exception;
	}
}
