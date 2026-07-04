package app.checkout;

import java.time.LocalDate;

import app.AppException;
import app.transaction.TransactionManager;
import app.transaction.TransactionWork;
import domain.DaoFactory;
import domain.reservation.Reservation;
import domain.reservation.ReservationDao;

/**
 * チェックアウトユースケースを制御する。
 */
public class CheckOutRoomControl {

	public static final int CHARGE = 6000;

	private final ReservationDao reservationDao;

	private final TransactionManager transactionManager;

	public CheckOutRoomControl() {
		this(DaoFactory.getInstance().getReservationDao(),
				DaoFactory.getInstance().getTransactionManager());
	}

	public CheckOutRoomControl(ReservationDao reservationDao,
			TransactionManager transactionManager) {
		this.reservationDao = reservationDao;
		this.transactionManager = transactionManager;
	}

	public Reservation checkOut(final String reservationNumber, final LocalDate today)
			throws AppException {
		if (reservationNumber == null || reservationNumber.trim().length() == 0) {
			throw new AppException("予約番号を入力してください。");
		}
		return transactionManager.execute(new TransactionWork<Reservation>() {
			public Reservation run() throws Exception {
				Reservation reservation = reservationDao.findByNumber(reservationNumber);
				if (reservation == null) {
					throw new AppException("指定された予約は存在しません。");
				}
				reservation.checkOut(today);
				reservationDao.update(reservation);
				return reservation;
			}
		});
	}

	public int getCharge() {
		return CHARGE;
	}

	/**
	 * 半完成コードのCUIとのコンパイル互換性を保つ。
	 */
	public void checkOut(String reservationNumber) throws AppException {
		checkOut(reservationNumber, LocalDate.now());
	}
}
