package domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import domain.DomainException;
import domain.room.Room;
import domain.user.HotelUser;

/**
 * Reservationの状態遷移を外部ライブラリなしで検証する。
 */
public class ReservationStateTest {

	public static void main(String[] args) throws Exception {
		LocalDate today = LocalDate.now();
		Reservation reservation = new Reservation(
				"R-TEST",
				LocalDateTime.now(),
				today,
				new HotelUser("user-1", "利用者", "090-0000-0000"),
				new Room("1001"));

		assertEquals(ReservationStatus.RESERVED, reservation.getReservationStatus());
		expectDomainException(() -> reservation.checkOut(today.plusDays(1)));
		reservation.checkIn(today);
		assertEquals(ReservationStatus.CHECKED_IN, reservation.getReservationStatus());
		expectDomainException(() -> reservation.cancel("user-1"));
		reservation.checkOut(today.plusDays(1));
		assertEquals(ReservationStatus.CHECKED_OUT, reservation.getReservationStatus());

		Reservation cancelTarget = new Reservation(
				"R-CANCEL",
				LocalDateTime.now(),
				today.plusDays(1),
				new HotelUser("user-2", "利用者2", "090-1111-1111"),
				new Room("1002"));
		expectDomainException(() -> cancelTarget.cancel("other-user"));
		cancelTarget.cancel("user-2");
		assertEquals(ReservationStatus.CANCELED, cancelTarget.getReservationStatus());
		System.out.println("ReservationStateTest: OK");
	}

	private static void assertEquals(Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError("expected=" + expected + ", actual=" + actual);
		}
	}

	private static void expectDomainException(ThrowingAction action) throws Exception {
		try {
			action.run();
			throw new AssertionError("DomainExceptionが必要です。");
		}
		catch (DomainException expected) {
			// 期待した例外。
		}
	}

	private interface ThrowingAction {
		void run() throws Exception;
	}
}
