package app.reservation;

import java.util.List;

import app.AppException;
import domain.DaoFactory;
import domain.RepositoryException;
import domain.reservation.Reservation;
import domain.reservation.ReservationDao;

/**
 * 受付係向けに全予約を取得する。
 */
public class StaffReservationListControl {

	private final ReservationDao reservationDao;

	public StaffReservationListControl() {
		this(DaoFactory.getInstance().getReservationDao());
	}

	public StaffReservationListControl(ReservationDao reservationDao) {
		this.reservationDao = reservationDao;
	}

	public List<Reservation> findAll() throws AppException {
		try {
			return reservationDao.findAll();
		}
		catch (RepositoryException e) {
			throw new AppException("予約一覧を取得できませんでした。", e);
		}
	}
}
