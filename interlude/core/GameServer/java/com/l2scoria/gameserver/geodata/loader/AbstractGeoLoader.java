package com.l2scoria.gameserver.geodata.loader;

import com.l2scoria.gameserver.model.L2World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class AbstractGeoLoader implements GeoLoader
{

	private static final Logger log = Logger.getLogger(AbstractGeoLoader.class.getName());

	private static final Pattern SCANNER_DELIMITER = Pattern.compile("([_|\\.]){1}");

	public boolean isAcceptable(File file)
	{


		if (!file.exists())
		{
			log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file doesn't exists.");
			return false;
		}

		if (file.isDirectory())
		{
			log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is directory.");
			return false;
		}

		if (file.isHidden())
		{
			log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is hidden.");
			return false;
		}

		if (file.length() > Integer.MAX_VALUE)
		{
			log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is to big.");
			return false;
		}

		if (!getPattern().matcher(file.getName()).matches())
		{
			//log.info(getClass().getSimpleName() + ": can't load file: " + file.getName() + "!!! Reason: pattern missmatch");
			return false;
		}

		GeoFileInfo geoFileInfo = createGeoFileInfo(file);
		int x = geoFileInfo.getX() - 15;
		int y = geoFileInfo.getY() - 10;

		if (x < 0 || y < 0 || x > (L2World.MAP_MAX_X >> 15) + Math.abs(L2World.MAP_MIN_X >> 15) || y > (L2World.MAP_MAX_Y >> 15) + Math.abs(L2World.MAP_MIN_Y >> 15))
		{
			log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is out of map.");
			return false;
		}

		return true;
	}

	public GeoFileInfo readFile(File file)
	{

		log.info(getClass().getSimpleName() + ": loading geodata file: " + file.getName());

		FileInputStream fis = null;
		byte[] data = null;
		try
		{
			fis = new FileInputStream(file);
			data = new byte[fis.available()];
			int readed = fis.read(data);
			if (readed != data.length)
			{
				log.warning("Not fully readed file?");
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		} finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		GeoFileInfo geoFileInfo = createGeoFileInfo(file);
		geoFileInfo.setData(parse(convert(data)));
		return geoFileInfo;
	}

	protected GeoFileInfo createGeoFileInfo(File file)
	{
		Scanner scanner = new Scanner(file.getName());
		scanner.useDelimiter(SCANNER_DELIMITER);
		int ix = scanner.nextInt();
		int iy = scanner.nextInt();
		scanner.close();

		GeoFileInfo geoFileInfo = new GeoFileInfo();
		geoFileInfo.setX(ix);
		geoFileInfo.setY(iy);
		return geoFileInfo;
	}

	protected abstract byte[][] parse(byte[] data);

	public abstract Pattern getPattern();

	public abstract byte[] convert(byte[] data);
}
