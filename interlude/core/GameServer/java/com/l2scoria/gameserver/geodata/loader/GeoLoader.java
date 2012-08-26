package com.l2scoria.gameserver.geodata.loader;

import java.io.File;

public interface GeoLoader
{

	public boolean isAcceptable(File file);

	public GeoFileInfo readFile(File file);
}