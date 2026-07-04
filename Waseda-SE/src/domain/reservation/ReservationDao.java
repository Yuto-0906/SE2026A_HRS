/*
 * Copyright(C) 2007-2013 National Institute of Informatics, All rights reserved.
 */
package domain.reservation;

import java.time.LocalDate;
import java.util.List;

import domain.RepositoryException;

/**
 * Interface for accessing to Reservation Data Object<br>
 * 
 */
public interface ReservationDao {

	public abstract Reservation getReservation(String reservationNumber)
			throws ReservationException;

	public abstract void updateReservation(Reservation reservation) throws ReservationException;

	public abstract void createReservation(Reservation reservation) throws ReservationException;

	Reservation findByNumber(String reservationNumber) throws RepositoryException;

	List<Reservation> findByUserId(String userId) throws RepositoryException;

	List<Reservation> findAll() throws RepositoryException;

	boolean existsActive(String roomNumber, LocalDate checkInDate) throws RepositoryException;

	void insert(Reservation reservation) throws RepositoryException;

	void update(Reservation reservation) throws RepositoryException;

	void releaseRoom(Reservation reservation) throws RepositoryException;
}
