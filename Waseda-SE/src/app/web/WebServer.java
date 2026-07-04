package app.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import app.checkin.CheckInRoomControl;
import app.checkout.CheckOutRoomControl;
import app.reservation.CancelReservationControl;
import app.reservation.CustomerReservationListControl;
import app.reservation.ReserveRoomControl;
import app.reservation.StaffReservationListControl;

/**
 * ホテル予約システムのWebサーバ。
 */
public class WebServer {

	private final HttpServer server;

	private final ExecutorService executor;

	public WebServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
		executor = Executors.newFixedThreadPool(8);
		server.setExecutor(executor);
		server.createContext("/", new HomeHandler());
		server.createContext("/reservations/new",
				new ReservationHandler(new ReserveRoomControl()));
		server.createContext("/reservations/mine",
				new CustomerReservationListHandler(new CustomerReservationListControl()));
		server.createContext("/reservations/cancel",
				new CancelReservationHandler(new CancelReservationControl()));
		server.createContext("/staff/reservations",
				new StaffReservationListHandler(new StaffReservationListControl()));
		server.createContext("/staff/checkin",
				new CheckInHandler(new CheckInRoomControl()));
		server.createContext("/staff/checkout",
				new CheckOutHandler(new CheckOutRoomControl()));
	}

	public void start() {
		server.start();
	}

	public void stop() {
		server.stop(1);
		executor.shutdown();
	}

	public int getPort() {
		return server.getAddress().getPort();
	}

	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(System.getProperty("hrs.port", "8080"));
		WebServer webServer = new WebServer(port);
		Runtime.getRuntime().addShutdownHook(new Thread(webServer::stop));
		webServer.start();
		System.out.println("Hotel Reservation System: http://127.0.0.1:" + port);
	}
}
