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
package com.l2scoria.gameserver.datatables.sql;

import com.l2scoria.Config;
import com.l2scoria.gameserver.idfactory.IdFactory;
import com.l2scoria.gameserver.managers.ClanHallManager;
import com.l2scoria.gameserver.model.L2Territory;
import com.l2scoria.gameserver.model.actor.instance.L2DoorInstance;
import com.l2scoria.gameserver.model.entity.ClanHall;
import com.l2scoria.gameserver.templates.L2CharTemplate;
import com.l2scoria.gameserver.templates.StatsSet;
import com.l2scoria.util.database.DatabaseUtils;
import com.l2scoria.util.database.L2DatabaseFactory;
import javolution.util.FastMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

public class DoorTable
{
	private static Logger _log = Logger.getLogger(DoorTable.class.getName());

	private Map<Integer, L2DoorInstance> _staticItems;

	private static DoorTable _instance;

	public static DoorTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new DoorTable();
		}

		return _instance;
	}

	public DoorTable()
	{
		_staticItems = new FastMap<Integer, L2DoorInstance>();
		parseData();
	}

	public void respawn()
	{
		for (L2DoorInstance door : _staticItems.values())
		{
			if (door != null)
			{
				door.decayMe();
			}
		}
		_instance = new DoorTable();
	}

	public void parseData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM doors");
			rs = statement.executeQuery();
			FillDoors(rs);
		} catch (Exception e)
		{
			_log.warning("Cannot load doors");
			e.printStackTrace();
		} finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void FillDoors(ResultSet DoorData) throws Exception
	{
		StatsSet baseDat = new StatsSet();
		baseDat.set("level", 0);
		baseDat.set("jClass", "door");
		baseDat.set("baseSTR", 0);
		baseDat.set("baseCON", 0);
		baseDat.set("baseDEX", 0);
		baseDat.set("baseINT", 0);
		baseDat.set("baseWIT", 0);
		baseDat.set("baseMEN", 0);
		baseDat.set("baseShldDef", 0);
		baseDat.set("baseShldRate", 0);
		baseDat.set("baseAccCombat", 38);
		baseDat.set("baseEvasRate", 38);
		baseDat.set("baseCritRate", 38);
		baseDat.set("collision_radius", 5);
		baseDat.set("collision_height", 0);
		baseDat.set("sex", "male");
		baseDat.set("type", "");
		baseDat.set("baseAtkRange", 0);
		baseDat.set("baseMpMax", 0);
		baseDat.set("baseCpMax", 0);
		baseDat.set("revardExp", 0);
		baseDat.set("revardSp", 0);
		baseDat.set("basePAtk", 0);
		baseDat.set("baseMAtk", 0);
		baseDat.set("basePAtkSpd", 0);
		baseDat.set("aggroRange", 0);
		baseDat.set("baseMAtkSpd", 0);
		baseDat.set("rhand", 0);
		baseDat.set("lhand", 0);
		baseDat.set("armor", 0);
		baseDat.set("baseWalkSpd", 0);
		baseDat.set("baseRunSpd", 0);
		baseDat.set("baseHpReg", 0);
		baseDat.set("baseCpReg", 0);
		baseDat.set("baseMpReg", 0);
		baseDat.set("siege_weapon", false);
		baseDat.set("geodata", true);

		StatsSet npcDat;
		while (DoorData.next())
		{
			npcDat = baseDat.clone();
			int id = DoorData.getInt("id");
			int zmin = DoorData.getInt("minz");
			int zmax = DoorData.getInt("maxz");
			int posx = DoorData.getInt("posx");
			int posy = DoorData.getInt("posy");
			String doorname = DoorData.getString("name");

			npcDat.set("npcId", id);
			npcDat.set("name", doorname);
			npcDat.set("baseHpMax", DoorData.getInt("hp"));
			npcDat.set("basePDef", DoorData.getInt("pdef"));
			npcDat.set("baseMDef", DoorData.getInt("mdef"));

			L2CharTemplate template = new L2CharTemplate(npcDat);
			L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, doorname, DoorData.getBoolean("unlockable"), DoorData.getBoolean("showHp"));
			_staticItems.put(id, door);
			L2Territory pos = new L2Territory();
			pos.add(DoorData.getInt("ax"), DoorData.getInt("ay"), zmin, zmax, 0);
			pos.add(DoorData.getInt("bx"), DoorData.getInt("by"), zmin, zmax, 0);
			pos.add(DoorData.getInt("cx"), DoorData.getInt("cy"), zmin, zmax, 0);
			pos.add(DoorData.getInt("dx"), DoorData.getInt("dy"), zmin, zmax, 0);
			door.setGeoPos(pos);

			door.getTemplate().collisionHeight = zmax - zmin & 0xfff0;
			door.getTemplate().collisionRadius = Math.max(50, Math.min(posx - pos.getXmin(), posy - pos.getYmin()));

			if (door.getTemplate().collisionRadius > 200 && Config.DEBUG)
			{
				System.out.println("DoorId: " + id + ", collision: " + door.getTemplate().collisionRadius + ", posx: " + posx + ", posy: " + posy + ", xMin: " + pos.getXmin() + ", yMin: " + pos.getYmin());
			}

			if (pos.getXmin() == pos.getXmax() && Config.DEBUG)
			{
				_log.warning("door " + id + " has zero size");
			}
			else if (pos.getYmin() == pos.getYmax() && Config.DEBUG)
			{
				_log.warning("door " + id + " has zero size");
			}

			door.setGeodata(DoorData.getBoolean("geodata"));
			door.setOpen(false);
			door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
			door.setXYZInvisible(posx, posy, zmin);

			// Дверь/стена может быть атакована только осадным оружием
			door.setSiegeWeaponOlnyAttackable(DoorData.getBoolean("siege_weapon"));

			door.spawnMe(door.getX(), door.getY(), door.getZ() + 32);

			ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 500);
			if (clanhall != null)
			{
				clanhall.addDoor(door);
				door.setClanHall(clanhall);
				if (Config.DEBUG)
				{
					_log.warning("door " + door.getDoorName() + " attached to ch " + clanhall.getName());
				}
			}
		}

		_log.config("DoorTable: Loaded " + _staticItems.size() + " doors.");
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	private boolean _initialized = true;

	public L2DoorInstance getDoor(Integer id)
	{
		return _staticItems.get(id);
	}

	public void putDoor(L2DoorInstance door)
	{
		_staticItems.put(door.getDoorId(), door);
	}

	public L2DoorInstance[] getDoors()
	{
		return _staticItems.values().toArray(new L2DoorInstance[_staticItems.size()]);
	}

	/**
	 * Performs a check and sets up a scheduled task for those doors that require auto opening/closing.
	 */
	public void checkAutoOpen()
	{
		for (L2DoorInstance doorInst : getDoors())
		// Garden of Eva (every 7 minutes)
		{
			if (doorInst.getDoorName().startsWith("goe"))
			{
				doorInst.setAutoActionDelay(420000);
			}
			else if (doorInst.getDoorName().startsWith("aden_tower"))
			{
				doorInst.setAutoActionDelay(300000);
			}
		}
	}
}
