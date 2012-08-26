package com.l2scoria.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created: 26.06.2007 0:32:02
 *
 * @author Alexey Lahtadir <a href="mailto:alexey_lahtadir@mail.ru">mailto: alexey_lahtadir@mail.ru</a>
 */
public class DatabaseUtils
{

	/**
	 * Закрыть коннект
	 *
	 * @param conn - коннект к базе данных
	 */
	public static void closeConnection(Connection conn)
	{
		if(conn != null)
		{
			try
			{
				conn.close();
			}
			catch (SQLException sqle){}
		}
	}

	/**
	 * Закрыть Statement
	 *
	 * @param stmt - Statement
	 */
	public static void closeStatement(PreparedStatement stmt)
	{
		if(stmt != null)
		{
			try
			{
				stmt.close();
			}
			catch (SQLException sqle){}
		}
	}

	/**
	 * Закрыть ResultSet
	 *
	 * @param rs - ResultSet
	 */
	public static void closeResultSet(ResultSet rs)
	{
		if(rs != null)
			try
			{
				rs.close();
			}
			catch(SQLException e)
			{}
	}

	/**
	 * Закрыть коннект, Statement и ResultSet
	 *
	 * @param conn - Connection
	 * @param stmt - Statement
	 * @param rs   - ResultSet
	 */
	public static void closeDatabaseCSR(Connection conn, PreparedStatement stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
		closeConnection(conn);
	}

	/**
	 * закрыть коннект, Statement
	 *
	 * @param conn - Connection
	 * @param stmt - Statement
	 */
	public static void closeDatabaseCS(Connection conn, PreparedStatement stmt)
	{
		closeStatement(stmt);
		closeConnection(conn);
	}

	/**
	 * закрыть Statement и ResultSet
	 *
	 * @param stmt - Statement
	 * @param rs   - ResultSet
	 */
	public static void closeDatabaseSR(PreparedStatement stmt, ResultSet rs)
	{
		closeResultSet(rs);
		closeStatement(stmt);
	}
}