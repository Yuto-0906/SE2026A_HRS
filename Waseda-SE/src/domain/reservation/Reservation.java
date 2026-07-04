/*
 * Copyright(C) 2007-2013 National Institute of Informatics, All rights reserved.
 */
package domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import domain.DomainException;
import domain.room.Room;
import domain.user.HotelUser;

/**
 * Reservation entity<br>
 * 
 */
public class Reservation {

	public static final String RESERVATION_STATUS_CREATE = "create";

	public static final String RESERVATION_STATUS_CONSUME = "consume";

	private String reservationNumber;

	private Date stayingDate;

	private String status;

	private LocalDateTime reservedAt;

	private LocalDate checkInDate;

	private ReservationStatus reservationStatus;

	private HotelUser user;

	private Room room;

	public Reservation() {
	}

	public Reservation(String reservationNumber, LocalDateTime reservedAt, LocalDate checkInDate,
			HotelUser user, Room room) {
		this.reservationNumber = reservationNumber;
		this.reservedAt = reservedAt;
		this.checkInDate = checkInDate;
		this.user = user;
		this.room = room;
		setReservationStatus(ReservationStatus.RESERVED);
	}

	public String getReservationNumber() {
		return reservationNumber;
	}

	public void setReservationNumber(String reservationNumber) {
		this.reservationNumber = reservationNumber;
	}

	public Date getStayingDate() {
		return stayingDate;
	}

	public void setStayingDate(Date stayingDate) {
		this.stayingDate = stayingDate;
		if (stayingDate != null) {
			this.checkInDate = stayingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		if (status == null) {
			this.reservationStatus = null;
		}
		else if (RESERVATION_STATUS_CREATE.equals(status)) {
			this.reservationStatus = ReservationStatus.RESERVED;
		}
		else if (RESERVATION_STATUS_CONSUME.equals(status)) {
			this.reservationStatus = ReservationStatus.CHECKED_IN;
		}
		else {
			this.reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
		}
	}

	public LocalDateTime getReservedAt() {
		return reservedAt;
	}

	public void setReservedAt(LocalDateTime reservedAt) {
		this.reservedAt = reservedAt;
	}

	public LocalDate getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
		if (checkInDate != null) {
			this.stayingDate = Date.from(checkInDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
	}

	public ReservationStatus getReservationStatus() {
		return reservationStatus;
	}

	public void setReservationStatus(ReservationStatus reservationStatus) {
		this.reservationStatus = reservationStatus;
		this.status = reservationStatus == null ? null : reservationStatus.name();
	}

	public HotelUser getUser() {
		return user;
	}

	public void setUser(HotelUser user) {
		this.user = user;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public void checkIn(LocalDate today) throws DomainException {
		if (reservationStatus != ReservationStatus.RESERVED) {
			throw new DomainException("予約済みの予約だけがチェックインできます。");
		}
		if (!checkInDate.equals(today)) {
			throw new DomainException("チェックイン日は" + checkInDate + "です。");
		}
		setReservationStatus(ReservationStatus.CHECKED_IN);
	}

	public void checkOut(LocalDate today) throws DomainException {
		if (reservationStatus != ReservationStatus.CHECKED_IN) {
			throw new DomainException("チェックイン済みの予約だけがチェックアウトできます。");
		}
		if (!checkInDate.plusDays(1).equals(today)) {
			throw new DomainException("チェックアウト日は" + checkInDate.plusDays(1) + "です。");
		}
		setReservationStatus(ReservationStatus.CHECKED_OUT);
	}

	public void cancel(String requestingUserId) throws DomainException {
		if (user == null || !user.getUserId().equals(requestingUserId)) {
			throw new DomainException("この予約をキャンセルする権限がありません。");
		}
		if (reservationStatus != ReservationStatus.RESERVED) {
			throw new DomainException("予約済みの予約だけがキャンセルできます。");
		}
		setReservationStatus(ReservationStatus.CANCELED);
	}
}
