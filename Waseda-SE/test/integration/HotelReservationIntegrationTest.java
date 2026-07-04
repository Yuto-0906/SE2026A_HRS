package integration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import app.AppException;
import app.checkin.CheckInRoomControl;
import app.checkout.CheckOutRoomControl;
import app.reservation.CancelReservationControl;
import app.reservation.CustomerReservationListControl;
import app.reservation.ReserveRoomControl;
import app.reservation.StaffReservationListControl;
import domain.reservation.Reservation;
import domain.reservation.ReservationStatus;

/**
 * HSQLDB上で主要ユースケースを通して検証する。
 */
public class HotelReservationIntegrationTest {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("setup.sqlのパスを指定してください。");
		}
		String url = "jdbc:hsqldb:mem:hrs-test";
		System.setProperty("hrs.db.url", url);
		initializeDatabase(url, Path.of(args[0]));

		ReserveRoomControl reserveControl = new ReserveRoomControl();
		CustomerReservationListControl customerListControl =
				new CustomerReservationListControl();
		StaffReservationListControl staffListControl =
				new StaffReservationListControl();
		CancelReservationControl cancelControl = new CancelReservationControl();
		CheckInRoomControl checkInControl = new CheckInRoomControl();
		CheckOutRoomControl checkOutControl = new CheckOutRoomControl();

		LocalDate today = LocalDate.now();
		List<Reservation> reservations = new ArrayList<Reservation>();
		for (int i = 1; i <= 5; i++) {
			reservations.add(reserveControl.reserve(
					"user-" + i,
					"利用者" + i,
					"090-0000-000" + i,
					today));
		}

		assertEquals(5, staffListControl.findAll().size());
		assertEquals(1, customerListControl.findByUser("user-1").size());
		expectAppException(() -> reserveControl.reserve(
				"user-6", "利用者6", "090-0000-0006", today));

		Reservation canceled = cancelControl.cancel(
				"user-1", reservations.get(0).getReservationNumber());
		assertEquals(ReservationStatus.CANCELED, canceled.getReservationStatus());
		expectAppException(() -> cancelControl.cancel(
				"user-3", reservations.get(1).getReservationNumber()));

		Reservation replacement = reserveControl.reserve(
				"user-6", "利用者6", "090-0000-0006", today);
		assertEquals(reservations.get(0).getRoom().getRoomNumber(),
				replacement.getRoom().getRoomNumber());

		Reservation checkedIn = checkInControl.checkIn(
				reservations.get(1).getReservationNumber(), today);
		assertEquals(ReservationStatus.CHECKED_IN, checkedIn.getReservationStatus());
		Reservation checkedOut = checkOutControl.checkOut(
				reservations.get(1).getReservationNumber(), today.plusDays(1));
		assertEquals(ReservationStatus.CHECKED_OUT, checkedOut.getReservationStatus());
		assertEquals(6000, checkOutControl.getCharge());
		System.out.println("HotelReservationIntegrationTest: OK");
	}

	private static void initializeDatabase(String url, Path setupSql) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		String sql = Files.readString(setupSql, StandardCharsets.UTF_8)
				.replaceAll("(?s)/\\*.*?\\*/", "");
		try (Connection connection = DriverManager.getConnection(url, "sa", "");
				Statement statement = connection.createStatement()) {
			for (String command : sql.split(";")) {
				if (command.trim().length() > 0) {
					statement.execute(command.trim());
				}
			}
		}
	}

	private static void assertEquals(Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError("expected=" + expected + ", actual=" + actual);
		}
	}

	private static void expectAppException(ThrowingAction action) throws Exception {
		try {
			action.run();
			throw new AssertionError("AppExceptionが必要です。");
		}
		catch (AppException expected) {
			// 期待した例外。
		}
	}

	private interface ThrowingAction {
		void run() throws Exception;
	}
}
