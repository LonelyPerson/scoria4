package com.l2scoria.gameserver.geodata.loader;

import java.io.File;
import java.util.regex.Pattern;

public interface GeoLoader
{

	public boolean isAcceptable(File file);

	public GeoFileInfo readFile(File file);

	public abstract Pattern getPattern();
}