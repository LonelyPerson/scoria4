package com.l2scoria.gameserver.geodata;

import com.l2scoria.gameserver.model.L2Territory;
import gnu.trove.TLongByteHashMap;

public interface GeoCollision
{
	public L2Territory getGeoPos();

	public TLongByteHashMap getGeoAround();

	public void setGeoAround(TLongByteHashMap geo);

	public abstract boolean isGeoCloser();
}
