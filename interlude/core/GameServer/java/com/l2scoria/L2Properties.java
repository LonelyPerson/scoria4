package com.l2scoria;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

@SuppressWarnings("serial")
public final class L2Properties extends Properties
{
	private static final Logger _log = Logger.getLogger(L2Properties.class);
	private boolean _warn = false;

	public L2Properties()
	{}

	public L2Properties setLog(boolean warn)
	{
		_warn = warn;

		return this;
	}

	public L2Properties(String name) throws IOException
	{
		load(new FileInputStream(name));
	}

	public L2Properties(File file) throws IOException
	{
		load(new FileInputStream(file));
	}

	public L2Properties(InputStream inStream) throws IOException
	{
		load(inStream);
	}

	public L2Properties(Reader reader) throws IOException
	{
		load(reader);
	}

	public void load(String name) throws IOException
	{
		load(new FileInputStream(name));
	}

	public void load(File file) throws IOException
	{
		load(new FileInputStream(file));
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException
	{
		try
		{
			super.load(inStream);
		}
		finally
		{
			inStream.close();
		}
	}

	@Override
	public synchronized void load(Reader reader) throws IOException
	{
		try
		{
			super.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	@Override
	public String getProperty(String key)
	{
		String property = super.getProperty(key);

		if(property == null)
		{
			if(_warn)
			{
				_log.error("L2Properties: Missing property for key - " + key);
			}

			return null;
		}

		return property.trim();
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		String property = super.getProperty(key, defaultValue);

		if(property == null)
		{
			if(_warn)
			{
				_log.error("L2Properties: Missing defaultValue for key - " + key);
			}

			return null;
		}

		return property.trim();
	}
}
