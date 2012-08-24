package ru.scoria.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import ru.scoria.lang.L2Entity;

public interface DAOImpl<T extends L2Entity> {
	public boolean store(Connection con) throws SQLException;
	public boolean delete(Connection con) throws SQLException;
	public boolean load(Connection con) throws SQLException;
	public boolean load(ResultSet rs);
}
