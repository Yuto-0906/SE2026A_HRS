package app.reservation;

import java.util.List;

import app.AppException;
import domain.DaoFactory;
import domain.RepositoryException;
import domain.reservation.Reservation;
import domain.reservation.ReservationDao;
import domain.user.UserDao;

/**
 * 利用者自身の予約一覧を取得する。
 */
public class CustomerReservationListControl {

	private final UserDao userDao;

	private final ReservationDao reservationDao;

	public CustomerReservationListControl() {
		this(DaoFactory.getInstance().getUserDao(),
				DaoFactory.getInstance().getReservationDao());
	}

	public CustomerReservationListControl(UserDao userDao, ReservationDao reservationDao) {
		this.userDao = userDao;
		this.reservationDao = reservationDao;
	}

	public List<Reservation> findByUser(String userId) throws AppException {
		if (userId == null || userId.trim().length() == 0) {
			throw new AppException("ユーザIDを入力してください。");
		}
		try {
			if (userDao.findById(userId) == null) {
				throw new AppException("入力されたユーザIDは登録されていません。");
			}
			return reservationDao.findByUserId(userId);
		}
		catch (RepositoryException e) {
			throw new AppException("予約一覧を取得できませんでした。", e);
		}
	}
}
