package app.reservation;

import app.AppException;
import app.transaction.TransactionManager;
import app.transaction.TransactionWork;
import domain.DaoFactory;
import domain.reservation.Reservation;
import domain.reservation.ReservationDao;

/**
 * 予約キャンセルユースケースを制御する。
 */
public class CancelReservationControl {

	private final ReservationDao reservationDao;

	private final TransactionManager transactionManager;

	public CancelReservationControl() {
		this(DaoFactory.getInstance().getReservationDao(),
				DaoFactory.getInstance().getTransactionManager());
	}

	public CancelReservationControl(ReservationDao reservationDao,
			TransactionManager transactionManager) {
		this.reservationDao = reservationDao;
		this.transactionManager = transactionManager;
	}

	public Reservation cancel(final String userId, final String reservationNumber)
			throws AppException {
		if (isBlank(userId) || isBlank(reservationNumber)) {
			throw new AppException("ユーザIDと予約番号を入力してください。");
		}
		return transactionManager.execute(new TransactionWork<Reservation>() {
			public Reservation run() throws Exception {
				Reservation reservation = reservationDao.findByNumber(reservationNumber);
				if (reservation == null) {
					throw new AppException("指定された予約は存在しません。");
				}
				reservation.cancel(userId);
				reservationDao.update(reservation);
				reservationDao.releaseRoom(reservation);
				return reservation;
			}
		});
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().length() == 0;
	}
}
