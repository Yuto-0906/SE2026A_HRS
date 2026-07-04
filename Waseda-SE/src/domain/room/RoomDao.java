/*
 * Copyright(C) 2007-2013 National Institute of Informatics, All rights reserved.
 */
package domain.room;

import java.time.LocalDate;
import java.util.List;

import domain.RepositoryException;

/**
 * Interface for accessing to Room Data Object<br>
 * 
 */
public interface RoomDao {

	public List getRooms() throws RoomException;

	public List getEmptyRooms() throws RoomException;

	public Room getRoom(String roomNumber) throws RoomException;

	public void updateRoom(Room room) throws RoomException;

	List<Room> findAll() throws RepositoryException;

	Room findAvailable(LocalDate checkInDate) throws RepositoryException;

	Room findByNumber(String roomNumber) throws RepositoryException;
}
