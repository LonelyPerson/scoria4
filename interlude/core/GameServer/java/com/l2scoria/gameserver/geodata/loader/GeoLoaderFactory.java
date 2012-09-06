package com.l2scoria.gameserver.geodata.loader;

import java.io.File;

/**
 * Singleton, contains information about aviable geoloaders
 */
public class GeoLoaderFactory
{

	private static GeoLoaderFactory instance;

	private final GeoLoader[] geoLoaders;

	public static GeoLoaderFactory getInstance()
	{
		if (instance == null)
		{
			instance = new GeoLoaderFactory();
		}

		return instance;
	}

	private GeoLoaderFactory()
	{
		geoLoaders = new GeoLoader[]{new L2JGeoLoader(), new OffGeoLoader()};
	}


	public GeoLoader getGeoLoader(File file)
	{
		if (file == null)
		{
			return null;
		}

		for (GeoLoader geoLoader : geoLoaders)
		{
			if (geoLoader.isAcceptable(file))
			{
				return geoLoader;
			}
		}

		return null;
	}
}
