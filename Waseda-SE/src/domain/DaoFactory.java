/*
 * Copyright(C) 2007-2013 National Institute of Informatics, All rights reserved.
 */
package domain;

import app.transaction.TransactionManager;
import domain.payment.PaymentDao;
import domain.payment.PaymentSqlDao;
import domain.reservation.ReservationDao;
import domain.reservation.ReservationSqlDao;
import domain.room.AvailableQtyDao;
import domain.room.AvailableQtySqlDao;
import domain.room.RoomDao;
import domain.room.RoomSqlDao;
import domain.user.UserDao;
import domain.user.UserSqlDao;
import infrastructure.jdbc.JdbcTransactionManager;

/**
 * Factory class for generating instance of any Data Object class
 * 
 */
public class DaoFactory {

	private static DaoFactory instance = new DaoFactory();

	private ReservationDao reservationDao = new ReservationSqlDao();

	private RoomDao roomDao = new RoomSqlDao();

	private AvailableQtyDao availableQtyDao = new AvailableQtySqlDao();

	private PaymentDao paymentDao = new PaymentSqlDao();

	private UserDao userDao = new UserSqlDao();

	private TransactionManager transactionManager = new JdbcTransactionManager();

	private DaoFactory() {
	}

	public static DaoFactory getInstance() {
		return instance;
	}

	public ReservationDao getReservationDao() {
		return reservationDao;
	}

	public RoomDao getRoomDao() {
		return roomDao;
	}

	public AvailableQtyDao getAvailableQtyDao() {
		return availableQtyDao;
	}

	public PaymentDao getPaymentDao() {
		return paymentDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}
}
