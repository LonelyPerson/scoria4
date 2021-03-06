/* This program is free software; you can redistribute it and/or modify
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
package com.l2scoria.gameserver.model.zone.type;

import com.l2scoria.gameserver.model.zone.L2ZoneDefault;

/**
 * A fishing zone
 * 
 * @author durgus
 */
public class L2FishingZone extends L2ZoneDefault
{
	public L2FishingZone(int id)
	{
		super(id);
	}

	/* getWaterZ() this added function returns the Z value for the water surface.
	 * In effect this simply returns the upper Z value of the zone. This required
	 * some modification of L2ZoneForm, and zone form extentions.
	 */
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
