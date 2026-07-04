package app.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import app.AppException;
import app.transaction.TransactionManager;
import app.transaction.TransactionWork;
import domain.DaoFactory;
import domain.reservation.Reservation;
import domain.reservation.ReservationDao;
import domain.room.Room;
import domain.room.RoomDao;
import domain.user.HotelUser;
import domain.user.UserDao;

/**
 * 部屋予約ユースケースを制御する。
 */
public class ReserveRoomControl {

	private final UserDao userDao;

	private final RoomDao roomDao;

	private final ReservationDao reservationDao;

	private final TransactionManager transactionManager;

	public ReserveRoomControl() {
		this(DaoFactory.getInstance().getUserDao(),
				DaoFactory.getInstance().getRoomDao(),
				DaoFactory.getInstance().getReservationDao(),
				DaoFactory.getInstance().getTransactionManager());
	}

	public ReserveRoomControl(UserDao userDao, RoomDao roomDao,
			ReservationDao reservationDao, TransactionManager transactionManager) {
		this.userDao = userDao;
		this.roomDao = roomDao;
		this.reservationDao = reservationDao;
		this.transactionManager = transactionManager;
	}

	public Reservation reserve(final String userId, final String name, final String phone,
			final LocalDate checkInDate) throws AppException {
		validate(userId, name, phone, checkInDate);
		return transactionManager.execute(new TransactionWork<Reservation>() {
			public Reservation run() throws Exception {
				HotelUser user = userDao.findById(userId);
				if (user == null) {
					user = new HotelUser(userId, name, phone);
				}
				else {
					user.updateProfile(name, phone);
				}
				userDao.save(user);

				Room room = roomDao.findAvailable(checkInDate);
				if (room == null) {
					throw new AppException("指定したチェックイン日は満室です。");
				}

				Reservation reservation = new Reservation(
						generateReservationNumber(),
						LocalDateTime.now(),
						checkInDate,
						user,
						room);
				reservationDao.insert(reservation);
				return reservation;
			}
		});
	}

	private void validate(String userId, String name, String phone, LocalDate checkInDate)
			throws AppException {
		if (isBlank(userId) || isBlank(name) || isBlank(phone) || checkInDate == null) {
			throw new AppException("ユーザID，氏名，電話番号，チェックイン日を入力してください。");
		}
		if (userId.length() > 50 || !userId.matches("[A-Za-z0-9_-]+")) {
			throw new AppException("ユーザIDは50文字以内の半角英数字，ハイフン，アンダースコアで入力してください。");
		}
		if (name.length() > 100) {
			throw new AppException("氏名は100文字以内で入力してください。");
		}
		if (phone.length() > 30 || !phone.matches("[0-9+() -]+")) {
			throw new AppException("電話番号の形式が正しくありません。");
		}
		if (checkInDate.isBefore(LocalDate.now())) {
			throw new AppException("過去の日付は予約できません。");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().length() == 0;
	}

	private String generateReservationNumber() {
		return "R-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	/**
	 * 半完成コードのCUIとのコンパイル互換性を保つ。
	 */
	public String makeReservation(Date stayingDate) throws AppException {
		if (stayingDate == null) {
			throw new AppException("チェックイン日を入力してください。");
		}
		LocalDate date = stayingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return reserve("legacy-user", "Legacy User", "000-0000-0000", date)
				.getReservationNumber();
	}
}
