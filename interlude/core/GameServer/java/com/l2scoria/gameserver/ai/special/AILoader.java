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
package com.l2scoria.gameserver.ai.special;

import org.apache.log4j.Logger;

public class AILoader
{
	private static final Logger _log = Logger.getLogger(AILoader.class.getName());

	public static void init()
	{
		_log.info("AI load:");
		_log.info(" - Antharas");
		new Antharas(-1, "antharas", "ai");
		_log.info(" - Baium");
		new Baium(-1, "baium", "ai");
		_log.info(" - Barakiel");
		new Barakiel(-1, "Barakiel", "ai");
		_log.info(" - Core");
		new Core(-1, "core", "ai");
		_log.info(" - Fairy Trees");
		new FairyTrees(-1, "FairyTrees", "ai");
		_log.info(" - Frintezza");
		new Frintezza(-1, "Frintezza", "ai");
		_log.info(" - Golkonda");
		new Golkonda(-1, "Golkonda", "ai");
		_log.info(" - Gordon");
		new Gordon(-1, "Gordon", "ai");
		_log.info(" - Hallate");
		new Hallate(-1, "Hallate", "ai");
		_log.info(" - Ice Fairy Sirra");
		new IceFairySirra(-1, "IceFairySirra", "ai");
		_log.info(" - Kernon");
		new Kernon(-1, "Kernon", "ai");
		_log.info(" - Monastery");
		new Monastery(-1, "monastery", "ai");
		_log.info(" - Queen Ant");
		new QueenAnt(-1, "queen_ant", "ai");
		_log.info(" - Summon Minions");
		new SummonMinions(-1, "SummonMinions", "ai");
		_log.info(" - Transform");
		new Transform(-1, "transform", "ai");
		_log.info(" - Van Halter");
		VanHalter.getInstance();
		_log.info(" - Varka/Ketra Ally");
		new VarkaKetraAlly(-1, "Varka Ketra Ally", "ai");
		_log.info(" - Zaken");
		new Zaken(-1, "Zaken", "ai");
		_log.info(" - Zombie Gatekeepers");
		new ZombieGatekeepers(-1, "ZombieGatekeepers", "ai");
	}
}
