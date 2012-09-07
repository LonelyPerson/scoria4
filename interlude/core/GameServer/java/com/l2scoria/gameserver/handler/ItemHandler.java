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
package com.l2scoria.gameserver.handler;

import com.l2scoria.gameserver.GameServer;
import com.l2scoria.gameserver.handler.items.impl.*;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class manages handlers of items
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class ItemHandler
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());

	private static ItemHandler _instance;

	private Map<Integer, ItemAbst> _datatable;

	/**
	 * Create ItemHandler if doesn't exist and returns ItemHandler
	 * 
	 * @return ItemHandler
	 */
	public static ItemHandler getInstance()
	{
		if(_instance == null)
		{
			_instance = new ItemHandler();
		}

		return _instance;
	}

	/**
	 * Returns the number of elements contained in datatable
	 * 
	 * @return int : Size of the datatable
	 */
	public int size()
	{
		return _datatable.size();
	}

	/**
	 * Constructor of ItemHandler
	 */
	private ItemHandler()
	{
		_datatable = new TreeMap<Integer, ItemAbst>();
		register(new ScrollOfEscape());
		register(new ScrollOfResurrection());
		register(new SoulShots());
		register(new SpiritShot());
		register(new BlessedSpiritShot());
		register(new BeastSoulShot());
		register(new BeastSpiritShot());
		register(new ChestKey());
		register(new CustomPotions());
		register(new PaganKeys());
		register(new Maps());
		register(new MapForestOfTheDead());
		register(new Potions());
		register(new Recipes());
		register(new RollingDice());
		register(new MysteryPotion());
		register(new EnchantScrolls());
		register(new EnergyStone());
		register(new Book());
		register(new Remedy());
		register(new Scrolls());
		register(new CrystalCarol());
		register(new SoulCrystals());
		register(new SevenSignsRecord());
		register(new CharChangePotions());
		register(new Firework());
		register(new Seed());
		register(new Harvester());
		register(new MercTicket());
		register(new Nectar());
		register(new FishShots());
		register(new ExtractableItems());
		register(new SpecialXMas());
		register(new SummonItems());
		register(new BeastSpice());
		register(new JackpotSeed());
		register(new NobleCustomItem());
		register(new HeroCustomItem());
		register(new MOSKey());
		register(new BreakingArrow());
		register(new ChristmasTree());
		register(new Crystals());
		_log.info("ItemHandler: Loaded " + _datatable.size() + " handlers.");
	}

	/**
	 * Adds handler of item type in <I>datatable</I>.<BR>
	 * <BR>
	 * <B><I>Concept :</I></U><BR>
	 * This handler is put in <I>datatable</I> Map &lt;Integer ; IItemHandler &gt; for each ID corresponding to an item
	 * type (existing in classes of package itemhandlers) sets as key of the Map.
	 * 
	 * @param handler (IItemHandler)
	 */
	public void register(ItemAbst handler)
	{
		// Get all ID corresponding to the item type of the handler
		int[] ids = handler.getItemIds();

		if(ids == null)
		{
			return;
		}

		// Add handler for each ID found
		for(int id : ids)
		{
			_datatable.put(id, handler);
		}
	}

	/**
	 * Returns the handler of the item
	 * 
	 * @param itemId : int designating the itemID
	 * @return IItemHandler
	 */
	public ItemAbst getItemHandler(int itemId)
	{
		return _datatable.get(itemId);
	}
}
