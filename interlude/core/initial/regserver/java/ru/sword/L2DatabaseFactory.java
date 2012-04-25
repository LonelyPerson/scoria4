/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package ru.sword;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * @author l2j-server
 * @author ProGramMoS
 * @version 0.2 BETA
 */

public class L2DatabaseFactory
{
    public static enum ProviderType
    {
        MySql,
        MsSql
    }

    // =========================================================
    // Data Field
    private static L2DatabaseFactory _instance;
    @SuppressWarnings("unused")
	private ProviderType _providerType;
	private ComboPooledDataSource _source;
	
    // =========================================================
    // Constructor
	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 10)
            {
                Config.DATABASE_MAX_CONNECTIONS = 10;
                System.out.println("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
            }
			_source = new ComboPooledDataSource();
			_source.setAutoCommitOnClose(true);

			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);


			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500);  // 500 miliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0);      // 0 = wait indefinitely for new connection
			// if pool is exhausted
			_source.setAcquireIncrement(5);     // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.

			// this "connection_test_table" is automatically created if not already there
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);

			// testing OnCheckin used with IdleConnectionTestPeriod is faster than  testing on checkout

			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
			_source.setMaxIdleTime(0); // 0 = idle connections never expire
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour

			// enables statement caching,  there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(100);

			_source.setBreakAfterAcquireFailure(false);  // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass(Config.DATABASE_DRIVER);
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);

			/* Test the connection */
			_source.getConnection().close();

			if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
                _providerType = ProviderType.MsSql;
            else
                _providerType = ProviderType.MySql;
		}
		catch (SQLException x)
		{
			System.out.println("Database Connection FAILED");
			// rethrow the exception
			throw x;
		}
		catch (Exception e)
		{
			System.out.println("Database Connection FAILED");
			throw new SQLException("could not init DB connection:"+e);
		}
	}

    // =========================================================
    // Property - Public
	public static L2DatabaseFactory getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new L2DatabaseFactory();
		}
		return _instance;
	}
	
	public Connection getConnection() //throws SQLException
	{
		Connection con = null;
 
		while(con == null)
		{
			try
			{
				con = _source.getConnection();
			} catch (SQLException e)
			{
				System.out.println("L2DatabaseFactory: getConnection() failed, trying again "+e);
			}
		}
		return con;
	}
}
