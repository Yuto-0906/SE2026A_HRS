package domain.reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import domain.RepositoryException;
import domain.room.Room;
import domain.user.HotelUser;
import infrastructure.jdbc.JdbcConnectionManager;

/**
 * JDBCによる予約DAO。
 */
public class ReservationSqlDao implements ReservationDao {

	private static final String SELECT_COLUMNS =
			"SELECT R.RESERVATION_NUMBER, R.RESERVED_AT, R.CHECK_IN_DATE, R.STATUS, "
			+ "U.USER_ID, U.NAME, U.PHONE, RM.ROOM_NUMBER "
			+ "FROM RESERVATIONS R "
			+ "JOIN USERS U ON U.USER_ID = R.USER_ID "
			+ "JOIN ROOMS RM ON RM.ROOM_NUMBER = R.ROOM_NUMBER ";

	public Reservation findByNumber(String reservationNumber) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					SELECT_COLUMNS + "WHERE R.RESERVATION_NUMBER = ?")) {
				statement.setString(1, reservationNumber);
				try (ResultSet resultSet = statement.executeQuery()) {
					return resultSet.next() ? map(resultSet) : null;
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("予約の取得に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public List<Reservation> findByUserId(String userId) throws RepositoryException {
		return findMany(SELECT_COLUMNS
				+ "WHERE R.USER_ID = ? ORDER BY R.CHECK_IN_DATE DESC, R.RESERVED_AT DESC", userId);
	}

	public List<Reservation> findAll() throws RepositoryException {
		return findMany(SELECT_COLUMNS
				+ "ORDER BY R.CHECK_IN_DATE DESC, R.RESERVED_AT DESC", null);
	}

	private List<Reservation> findMany(String sql, String userId) throws RepositoryException {
		Connection connection = null;
		List<Reservation> reservations = new ArrayList<Reservation>();
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				if (userId != null) {
					statement.setString(1, userId);
				}
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						reservations.add(map(resultSet));
					}
				}
			}
			return reservations;
		}
		catch (SQLException e) {
			throw new RepositoryException("予約一覧の取得に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public boolean existsActive(String roomNumber, LocalDate checkInDate)
			throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"SELECT COUNT(*) FROM ROOM_ALLOCATIONS "
					+ "WHERE ROOM_NUMBER = ? AND CHECK_IN_DATE = ?")) {
				statement.setString(1, roomNumber);
				statement.setDate(2, Date.valueOf(checkInDate));
				try (ResultSet resultSet = statement.executeQuery()) {
					resultSet.next();
					return resultSet.getInt(1) > 0;
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("部屋の予約状況を確認できませんでした。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public void insert(Reservation reservation) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO RESERVATIONS "
					+ "(RESERVATION_NUMBER, USER_ID, ROOM_NUMBER, RESERVED_AT, CHECK_IN_DATE, STATUS) "
					+ "VALUES (?, ?, ?, ?, ?, ?)")) {
				statement.setString(1, reservation.getReservationNumber());
				statement.setString(2, reservation.getUser().getUserId());
				statement.setString(3, reservation.getRoom().getRoomNumber());
				statement.setTimestamp(4, Timestamp.valueOf(reservation.getReservedAt()));
				statement.setDate(5, Date.valueOf(reservation.getCheckInDate()));
				statement.setString(6, reservation.getReservationStatus().name());
				statement.executeUpdate();
			}
			try (PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO ROOM_ALLOCATIONS "
					+ "(ROOM_NUMBER, CHECK_IN_DATE, RESERVATION_NUMBER) VALUES (?, ?, ?)")) {
				statement.setString(1, reservation.getRoom().getRoomNumber());
				statement.setDate(2, Date.valueOf(reservation.getCheckInDate()));
				statement.setString(3, reservation.getReservationNumber());
				statement.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("予約の登録に失敗しました。空室状況を再確認してください。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public void update(Reservation reservation) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"UPDATE RESERVATIONS SET STATUS = ? WHERE RESERVATION_NUMBER = ?")) {
				statement.setString(1, reservation.getReservationStatus().name());
				statement.setString(2, reservation.getReservationNumber());
				if (statement.executeUpdate() != 1) {
					throw new RepositoryException("更新対象の予約が存在しません。");
				}
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("予約状態の更新に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	public void releaseRoom(Reservation reservation) throws RepositoryException {
		Connection connection = null;
		try {
			connection = JdbcConnectionManager.getInstance().getConnection();
			try (PreparedStatement statement = connection.prepareStatement(
					"DELETE FROM ROOM_ALLOCATIONS WHERE RESERVATION_NUMBER = ?")) {
				statement.setString(1, reservation.getReservationNumber());
				statement.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new RepositoryException("部屋の割当解除に失敗しました。", e);
		}
		finally {
			JdbcConnectionManager.getInstance().closeIfStandalone(connection);
		}
	}

	private Reservation map(ResultSet resultSet) throws SQLException {
		HotelUser user = new HotelUser(
				resultSet.getString("USER_ID"),
				resultSet.getString("NAME"),
				resultSet.getString("PHONE"));
		Room room = new Room(resultSet.getString("ROOM_NUMBER"));
		Reservation reservation = new Reservation();
		reservation.setReservationNumber(resultSet.getString("RESERVATION_NUMBER"));
		reservation.setReservedAt(resultSet.getTimestamp("RESERVED_AT").toLocalDateTime());
		reservation.setCheckInDate(resultSet.getDate("CHECK_IN_DATE").toLocalDate());
		reservation.setReservationStatus(
				ReservationStatus.valueOf(resultSet.getString("STATUS")));
		reservation.setUser(user);
		reservation.setRoom(room);
		return reservation;
	}

	// 半完成コードとのコンパイル互換性を保つ旧インタフェース。
	public Reservation getReservation(String reservationNumber) throws ReservationException {
		try {
			return findByNumber(reservationNumber);
		}
		catch (RepositoryException e) {
			throw new ReservationException(ReservationException.CODE_DB_EXEC_QUERY_ERROR, e);
		}
	}

	public void updateReservation(Reservation reservation) throws ReservationException {
		try {
			update(reservation);
		}
		catch (RepositoryException e) {
			throw new ReservationException(ReservationException.CODE_DB_EXEC_QUERY_ERROR, e);
		}
	}

	public void createReservation(Reservation reservation) throws ReservationException {
		try {
			insert(reservation);
		}
		catch (RepositoryException e) {
			throw new ReservationException(ReservationException.CODE_DB_EXEC_QUERY_ERROR, e);
		}
	}
}
