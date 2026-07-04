package app.checkin;

import java.time.LocalDate;

import app.AppException;
import app.transaction.TransactionManager;
import app.transaction.TransactionWork;
import domain.DaoFactory;
import domain.reservation.Reservation;
import domain.reservation.ReservationDao;

/**
 * チェックインユースケースを制御する。
 */
public class CheckInRoomControl {

	private final ReservationDao reservationDao;

	private final TransactionManager transactionManager;

	public CheckInRoomControl() {
		this(DaoFactory.getInstance().getReservationDao(),
				DaoFactory.getInstance().getTransactionManager());
	}

	public CheckInRoomControl(ReservationDao reservationDao,
			TransactionManager transactionManager) {
		this.reservationDao = reservationDao;
		this.transactionManager = transactionManager;
	}

	public Reservation checkIn(final String reservationNumber, final LocalDate today)
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
				reservation.checkIn(today);
				reservationDao.update(reservation);
				return reservation;
			}
		});
	}

	/**
	 * 半完成コードのCUIとのコンパイル互換性を保つ。
	 */
	public String checkIn(String reservationNumber) throws AppException {
		return checkIn(reservationNumber, LocalDate.now()).getRoom().getRoomNumber();
	}
}
