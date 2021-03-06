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
package com.l2scoria.gameserver.skills;

import com.l2scoria.Config;
import com.l2scoria.gameserver.managers.ClanHallManager;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.model.*;
import com.l2scoria.gameserver.model.L2Skill.SkillType;
import com.l2scoria.gameserver.model.actor.instance.*;
import com.l2scoria.gameserver.model.entity.ClanHall;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2scoria.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2scoria.gameserver.model.entity.siege.Siege;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.conditions.ConditionPlayerState;
import com.l2scoria.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import com.l2scoria.gameserver.skills.conditions.ConditionUsingItemType;
import com.l2scoria.gameserver.skills.funcs.Func;
import com.l2scoria.gameserver.templates.L2Armor;
import com.l2scoria.gameserver.templates.L2PcTemplate;
import com.l2scoria.gameserver.templates.L2Weapon;
import com.l2scoria.gameserver.templates.L2WeaponType;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.random.Rnd;
import org.apache.log4j.Logger;

/**
 * Global calculations, can be modified by server admins
 * 
 * @author l2scoria dev
 */
public final class Formulas
{
	/** Regen Task period */
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs

	public static final int MAX_STAT_VALUE = 100;

	private static final double[] STRCompute = new double[]
	{
			1.036, 34.845
	}; //{1.016, 28.515}; for C1
	private static final double[] INTCompute = new double[]
	{
			1.020, 31.375
	}; //{1.020, 31.375}; for C1
	private static final double[] DEXCompute = new double[]
	{
			1.009, 19.360
	}; //{1.009, 19.360}; for C1
	private static final double[] WITCompute = new double[]
	{
			1.050, 20.000
	}; //{1.050, 20.000}; for C1
	private static final double[] CONCompute = new double[]
	{
			1.030, 27.632
	}; //{1.015, 12.488}; for C1
	private static final double[] MENCompute = new double[]
	{
			1.010, -0.060
	}; //{1.010, -0.060}; for C1

	protected static final double[] WITbonus = new double[MAX_STAT_VALUE];
	protected static final double[] MENbonus = new double[MAX_STAT_VALUE];
	protected static final double[] INTbonus = new double[MAX_STAT_VALUE];
	protected static final double[] STRbonus = new double[MAX_STAT_VALUE];
	protected static final double[] DEXbonus = new double[MAX_STAT_VALUE];
	protected static final double[] CONbonus = new double[MAX_STAT_VALUE];

	// These values are 100% matching retail tables, no need to change and no need add
	// calculation into the stat bonus when accessing (not efficient),
	// better to have everything precalculated and use values directly (saves CPU)
	static
	{
		for(int i = 0; i < STRbonus.length; i++)
		{
			STRbonus[i] = Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) * 100 + .5d) / 100;
		}
		for(int i = 0; i < INTbonus.length; i++)
		{
			INTbonus[i] = Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) * 100 + .5d) / 100;
		}
		for(int i = 0; i < DEXbonus.length; i++)
		{
			DEXbonus[i] = Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) * 100 + .5d) / 100;
		}
		for(int i = 0; i < WITbonus.length; i++)
		{
			WITbonus[i] = Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) * 100 + .5d) / 100;
		}
		for(int i = 0; i < CONbonus.length; i++)
		{
			CONbonus[i] = Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) * 100 + .5d) / 100;
		}
		for(int i = 0; i < MENbonus.length; i++)
		{
			MENbonus[i] = Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) * 100 + .5d) / 100;
		}
	}

	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

		static Func getInstance(final Stats stat)
		{
			final int pos = stat.ordinal();

			if(_instancies[pos] == null)
			{
				_instancies[pos] = new FuncAddLevel3(stat);
			}
			return _instancies[pos];
		}

		private FuncAddLevel3(final Stats pStat)
		{
			super(pStat, 0x10, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value += env.player.getLevel() / 3.0;
		}
	}

	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

		static Func getInstance(final Stats stat)
		{
			final int pos = stat.ordinal();

			if(_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultLevelMod(stat);
			}
			return _instancies[pos];
		}

		private FuncMultLevelMod(final Stats pStat)
		{
			super(pStat, 0x20, null);
		}

		@Override
		public void calc(final Env env)
		{
			env.value *= env.player.getLevelMod();
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

		/**
		 * Return the Func object corresponding to the state concerned.<BR>
		 * <BR>
		 */
		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();

			if(_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultRegenResting(stat);
			}

			return _instancies[pos];
		}

		/**
		 * Constructor of the FuncMultRegenResting.<BR>
		 * <BR>
		 */
		private FuncMultRegenResting(Stats pStat)
		{
			super(pStat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}

		/**
		 * Calculate the modifier of the state concerned.<BR>
		 * <BR>
		 */
		@Override
		public void calc(Env env)
		{
			if(!cond.test(env))
				return;

			env.value *= 1.45;
		}
	}

	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

		static Func getInstance()
		{
			return _fpa_instance;
		}

		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			env.value *= STRbonus[env.player.getSTR()] * env.player.getLevelMod();
		}
	}

	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

		static Func getInstance()
		{
			return _fma_instance;
		}

		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			double intb = INTbonus[env.player.getINT()];
			double lvlb = env.player.getLevelMod();
			env.value *= lvlb * lvlb * intb * intb;
		}
	}

	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.player.isPlayer)
			{
				L2PcInstance p = (L2PcInstance) env.player;
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				{
					env.value -= 5;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				{
					env.value -= 5;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				{
					env.value -= 9;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				{
					env.value -= 9;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				{
					env.value -= 13;
				}
			}
			env.value *= MENbonus[env.player.getMEN()] * env.player.getLevelMod();
		}
	}

	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod _fmm_instance = new FuncPDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			if(env.player.isPlayer)
			{
				L2PcInstance p = (L2PcInstance) env.player;
				boolean magePDef = (p.getClassId().isMage() || p.getClassId().getId() == 0x31); // orc mystics are a special case
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
				{
					env.value -= 12;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
				{
					env.value -= magePDef ? 15 : 31;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
				{
					env.value -= magePDef ? 8 : 18;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
				{
					env.value -= 8;
				}
				if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
				{
					env.value -= 7;
				}
			}
			env.value *= env.player.getLevelMod();
		}
	}

	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

		static Func getInstance()
		{
			return _fbar_instance;
		}

		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
			setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}

		@Override
		public void calc(Env env)
		{
			if(!cond.test(env))
				return;
			env.value += 460;
		}
	}

	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

		static Func getInstance()
		{
			return _faa_instance;
		}

		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			//[Square(DEX)]*6 + lvl + weapon hitbonus;
			env.value += Math.sqrt(p.getDEX()) * 6;
			env.value += p.getLevel();
			if(p.isSummon)
			{
				env.value += p.getLevel() < 60 ? 4 : 5;
			}
		}
	}

	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

		static Func getInstance()
		{
			return _fae_instance;
		}

		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			//[Square(DEX)]*6 + lvl;
			env.value += Math.sqrt(p.getDEX()) * 6;
			env.value += p.getLevel();
		}
	}

	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			if(p.isSummon)
			{
				env.value = 40;
			}
			else if(p.isPlayer && p.getActiveWeaponInstance() == null)
			{
				env.value = 40 * DEXbonus[p.getDEX()];
			}
			else
			{
				env.value *= DEXbonus[p.getDEX()];
				env.value *= 10;
			}
			env.baseValue = env.value;
		}
	}

	static class FuncMAtkCritical extends Func
	{
		static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncMAtkCritical()
		{
			super(Stats.MCRITICAL_RATE, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			L2Character p = env.player;
			if(p.isSummon)
			{
				env.value = 8;
			}
			else if(p.isPlayer && p.getActiveWeaponInstance() == null)
			{
				env.value = 8;
			}
			else
			{
				env.value *= WITbonus[p.getWIT()];
			}
		}
	}

	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

		static Func getInstance()
		{
			return _fms_instance;
		}

		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= WITbonus[p.getWIT()];
		}
	}

	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fh_instance = new FuncHennaSTR();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if(pc != null)
			{
				env.value += pc.getHennaStatSTR();
			}
		}
	}

	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fh_instance = new FuncHennaDEX();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if(pc != null)
			{
				env.value += pc.getHennaStatDEX();
			}
		}
	}

	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fh_instance = new FuncHennaINT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if(pc != null)
			{
				env.value += pc.getHennaStatINT();
			}
		}
	}

	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if(pc != null)
			{
				env.value += pc.getHennaStatMEN();
			}
		}
	}

	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fh_instance = new FuncHennaCON();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if(pc != null)
			{
				env.value += pc.getHennaStatCON();
			}
		}
	}

	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fh_instance = new FuncHennaWIT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if(pc != null)
			{
				env.value += pc.getHennaStatWIT();
			}
		}
	}

	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();

		static Func getInstance()
		{
			return _fmha_instance;
		}

		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.classBaseLevel;
			double hpmod = t.lvlHpMod * lvl;
			double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			double hpmin = t.lvlHpAdd * lvl + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}

	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

		static Func getInstance()
		{
			return _fmhm_instance;
		}

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= CONbonus[p.getCON()];
		}
	}

	static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();

		static Func getInstance()
		{
			return _fmca_instance;
		}

		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.classBaseLevel;
			double cpmod = t.lvlCpMod * lvl;
			double cpmax = (t.lvlCpAdd + cpmod) * lvl;
			double cpmin = t.lvlCpAdd * lvl + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}

	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

		static Func getInstance()
		{
			return _fmcm_instance;
		}

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= CONbonus[p.getCON()];
		}
	}

	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();

		static Func getInstance()
		{
			return _fmma_instance;
		}

		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			int lvl = env.player.getLevel() - t.classBaseLevel;
			double mpmod = t.lvlMpMod * lvl;
			double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			double mpmin = t.lvlMpAdd * lvl + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}

	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

		static Func getInstance()
		{
			return _fmmm_instance;
		}

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}

		@Override
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			env.value *= MENbonus[p.getMEN()];
		}
	}

	private static final Formulas _instance = new Formulas();

	public static Formulas getInstance()
	{
		return _instance;
	}

	private Formulas()
	{}

	/**
	 * Return the period between 2 regenerations task (3s for L2Character, 5 min for L2DoorInstance).<BR>
	 * <BR>
	 */
	public int getRegeneratePeriod(L2Character cha)
	{
		if(cha.isDoor)
			return HP_REGENERATE_PERIOD * 100; // 5 mins

		return HP_REGENERATE_PERIOD; // 3s
	}

	/**
	 * Return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP,
	 * REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a
	 * mathematic function : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called
	 * <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 */
	public Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		// Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

		// Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		return std;
	}

	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP,
	 * REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a
	 * mathematic function : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * 
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public void addFuncsToNewCharacter(L2Character cha)
	{
		if(cha.isPlayer)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpAdd.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());

			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
		}
		else if(cha.isPet)
		{
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
		}
		else if(cha.isSummon)
		{
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
		}
	}

	/**
	 * Calculate the HP regen rate (base + modifiers).<BR>
	 * <BR>
	 */
	public final double calcHpRegen(L2Character cha)
	{
		double init = cha.getTemplate().baseHpReg;
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;

		if(Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
		{
			hpRegenMultiplier *= Config.L2JMOD_CHAMPION_HP_REGEN;
		}

		if(cha.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate correct baseHpReg value for certain level of PC
			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;

			// SevenSigns Festival modifier
			if(SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				hpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			else
			{
				double siegeModifier = calcSiegeRegenModifer(player);
				if(siegeModifier > 0)
				{
					hpRegenMultiplier *= siegeModifier;
				}
			}

			if(player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null)
			{
				int clanHallIndex = player.getClan().getHasHideout();
				if(clanHallIndex > 0)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if(clansHall != null)
						if(clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100;
						}
				}
			}

			// Mother Tree effect is calculated at last
			if(player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				hpRegenBonus += 2;
			}

			// Calculate Movement bonus
			if(player.isSitting())
			{
				hpRegenMultiplier *= 1.5; // Sitting
			}
			else if(!player.isMoving())
			{
				hpRegenMultiplier *= 1.1; // Staying
			}
			else if(player.isRunning())
			{
				hpRegenMultiplier *= 0.7; // Running
			}

			// Add CON bonus
			init *= cha.getLevelMod() * CONbonus[cha.getCON()];
		}

		if(init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).<BR>
	 * <BR>
	 */
	public final double calcMpRegen(L2Character cha)
	{
		double init = cha.getTemplate().baseMpReg;
		double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;

		if(cha.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate correct baseMpReg value for certain level of PC
			init += 0.3 * (player.getLevel() - 1) / 10.0;

			// SevenSigns Festival modifier
			if(SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				mpRegenMultiplier *= calcFestivalRegenModifier(player);
			}

			// Mother Tree effect is calculated at last
			if(player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				mpRegenBonus += 1;
			}

			if(player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null)
			{
				int clanHallIndex = player.getClan().getHasHideout();
				if(clanHallIndex > 0)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if(clansHall != null)
						if(clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100;
						}
				}
			}

			// Calculate Movement bonus
			if(player.isSitting())
			{
				mpRegenMultiplier *= 1.5; // Sitting
			}
			else if(!player.isMoving())
			{
				mpRegenMultiplier *= 1.1; // Staying
			}
			else if(player.isRunning())
			{
				mpRegenMultiplier *= 0.7; // Running
			}

			// Add MEN bonus
			init *= cha.getLevelMod() * MENbonus[cha.getMEN()];
		}

		if(init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
	}

	/**
	 * Calculate the CP regen rate (base + modifiers).<BR>
	 * <BR>
	 */
	public final double calcCpRegen(L2Character cha)
	{
		double init = cha.getTemplate().baseHpReg;
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		double cpRegenBonus = 0;

		if(cha.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate correct baseHpReg value for certain level of PC
			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;

			// Calculate Movement bonus
			if(player.isSitting())
			{
				cpRegenMultiplier *= 1.5; // Sitting
			}
			else if(!player.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if(player.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		else
		{
			// Calculate Movement bonus
			if(!cha.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if(cha.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}

		// Apply CON bonus
		init *= cha.getLevelMod() * CONbonus[cha.getCON()];
		if(init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
	}

	@SuppressWarnings("deprecation")
	public final double calcFestivalRegenModifier(L2PcInstance activeChar)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;

		// If the player isn't found in the festival, leave the regen rate as it is.
		if(festivalId < 0)
			return 0;

		// Retrieve the X and Y coords for the center of the festival arena the player is in.
		if(oracle == SevenSigns.CABAL_DAWN)
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
		}
		else
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
		}

		// Check the distance between the player and the player spawn point, in the center of the arena.
		double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);

		if(Config.DEBUG)
		{
			_log.info("Distance: " + distToCenter + ", RegenMulti: " + distToCenter * 2.5 / 50);
		}

		return 1.0 - distToCenter * 0.0005; // Maximum Decreased Regen of ~ -65%;
	}

	public final double calcSiegeRegenModifer(L2PcInstance activeChar)
	{
		if(activeChar == null || activeChar.getClan() == null)
			return 0;

		Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
		if(siege == null || !siege.getIsInProgress())
			return 0;

		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if(siegeClan == null || siegeClan.getFlag().size() == 0 || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
			return 0;

		return 1.5; // If all is true, then modifer will be 50% more
	}

	/** Calculate blow damage based on cAtk */
	public double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean ss)
	{
		double power = skill.getPower();
		double damage = 0;
		double defence = target.getPDef(attacker);
		double ssboost = ss ? (skill.getSSBoost() > 0 ? skill.getSSBoost() : 2.04) : 1; // 104% bonus with SS
		double proximityBonus = 1;
		double pvpBonus = 1;

		if ((attacker.isPlayable) && (target.isPlayable))
		{
			pvpBonus *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
		}

		if (attacker.isBehind(target))
		{
			proximityBonus = 1.2; // +20% crit dmg when back stabbed
		}
		else if (!attacker.isFront(target))
		{
			proximityBonus = 1.1; // +10% crit dmg when side stabbed
		}

		if (skill.getSSBoost() > 0)
			damage += 70. * (attacker.getPAtk(target) + power) / defence * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill))
					* ssboost * proximityBonus * pvpBonus + (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70 / defence);
		else
			damage += 70. * (power + (attacker.getPAtk(target) * ssboost)) / defence * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill))
					* proximityBonus * pvpBonus + (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70 / defence);

		// get the natural vulnerability for the template
		if(target.isNpc)
		{
			damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
		}

		// get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage += Rnd.get() * attacker.getRandomDamage(target);

		// Sami: Must be removed, after armor resistances are checked.
		// These values are a quick fix to balance dagger gameplay and give
		// armor resistances vs dagger. daggerWpnRes could also be used if a skill
		// was given to all classes. The values here try to be a compromise.
		// They were originally added in a late C4 rev (2289).
		if(target.isPlayer)
		{
			L2Armor armor = target.getPlayer().getActiveChestArmorItem();
			if(armor != null)
			{
				if(target.getPlayer().isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				if(target.getPlayer().isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				if(target.getPlayer().isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}
		return damage < 1 ? 1. : damage;
	}

	/**
	 * Calculated damage caused by ATTACK of attacker on target, called separatly for each weapon, if dual-weapon is
	 * used.
	 * 
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param miss one of ATTACK_XXX constants
	 * @param crit if the ATTACK have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was charged by soulshot
	 * @return damage points
	 */
	public final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
	{
		if(attacker.isPlayer)
		{
			L2PcInstance pcInst = (L2PcInstance) attacker;
			if(pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
				return 0;
		}

		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if(ss)
		{
			damage *= 2;
		}

		if(skill != null)
		{
			double skillpower = skill.getPower(attacker);
			float ssboost = skill.getSSBoost();
			if(ssboost <= 0)
			{
				damage += skillpower;
			}
			else if(ssboost > 0)
			{
				if(ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
			}
		}

		// In C5 summons make 10 % less dmg in PvP.
		if(attacker.isSummon && target.isPlayer)
		{
			damage *= 0.9;
		}

		// After C4 nobles make 4% more dmg in PvP.
		if(attacker.isPlayer && ((L2PcInstance) attacker).isNoble() && (target.isPlayer || target.isSummon))
		{
			damage *= 1.04;
		}

		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if(weapon != null)
		{
			switch(weapon.getItemType())
			{
				case BOW:
					stat = Stats.BOW_WPN_VULN;
					break;
				case BLUNT:
				case BIGBLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
			}
		}

		/*if(crit)
		{
			damage += attacker.getCriticalDmg(target, damage);
		}*/
		if (crit)
		{
			damage *= 2 * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill);
			damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill);
		}

		if(shld && !Config.ALT_GAME_SHIELD_BLOCKS)
		{
			defence += target.getShldDef();
		}

		damage *= 70. / defence;

		if(stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
			if(target.isNpc)
			{
				// get the natural vulnerability for the template
				damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(stat);
			}
		}

		damage += Rnd.nextDouble() * damage / 10;
		//		damage += _rnd.nextDouble()* attacker.getRandomDamage(target);
		//		}
		if(shld && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if(damage < 0)
			{
				damage = 0;
			}
		}

		if(target.isPlayer && weapon != null && weapon.getItemType() == L2WeaponType.DAGGER && skill != null)
		{
			L2Armor armor = target.getPlayer().getActiveChestArmorItem();
			if(armor != null)
			{
				if(target.getPlayer().isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				if(target.getPlayer().isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				if(target.getPlayer().isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}

		if(attacker.isNpc)
		{
			switch(((L2NpcInstance) attacker).getTemplate().getRace())
			{
				case UNDEAD:
					damage /= attacker.getPDefUndead(target);
					break;
				case PLANT:
					damage /= attacker.getPDefPlants(target);
					break;
				case BUG:
					damage /= attacker.getPDefInsects(target);
					break;
				case ANIMAL:
					damage /= attacker.getPDefAnimals(target);
					break;
				case BEAST:
					damage /= attacker.getPDefMonsters(target);
					break;
				case DRAGON:
					damage /= attacker.getPDefDragons(target);
					break;
				case GIANT:
					damage /= attacker.getPDefGiants(target);
					break;
				case MAGICCREATURE:
					damage /= attacker.getPDefMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		if(target.isNpc)
		{
			switch(((L2NpcInstance) target).getTemplate().getRace())
			{
				case UNDEAD:
					damage *= attacker.getPAtkUndead(target);
					break;
				case BEAST:
					damage *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					damage *= attacker.getPAtkGiants(target);
					break;
				case MAGICCREATURE:
					damage *= attacker.getPAtkMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		if(shld)
		{
			if(100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
			{
				damage = 1;
				target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
			}
		}

		if(damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if(damage < 0)
		{
			damage = 0;
		}

		// Dmg bonusses in PvP fight
		if((attacker.isPlayer || attacker.isSummon) && (target.isPlayer || target.isSummon))
		{
			if(skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}

		if(attacker.isPlayer)
		{
			if(((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
			}
		}
		else if(attacker.isSummon)
		{
			damage = damage * Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
		}
		else if(attacker.isNpc)
		{
			damage = damage * Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
		}

		return damage;
	}

	public final static double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss, boolean mcrit)
	{
		if(attacker.isPlayer)
		{
			L2PcInstance pcInst = (L2PcInstance) attacker;
			if(pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
				return 0;
		}

		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		if(bss)
		{
			mAtk *= 4;
		}
		else if(ss)
		{
			mAtk *= 2;
		}

		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);

		// In C5 summons make 10 % less dmg in PvP.
		if(attacker.isSummon && target.isPlayer)
		{
			damage *= 0.9;
		}

		// After C4 nobles make 4% more dmg in PvP.
		if(attacker.isPlayer && ((L2PcInstance) attacker).isNoble() && (target.isPlayer || target.isSummon))
		{
			damage *= 1.04;
		}

		// Failure calculation
		if(Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if(attacker.isPlayer)
			{
				if(calcMagicSuccess(attacker, target, skill) && target.getLevel() - attacker.getLevel() <= 9)
				{
					if(skill.getSkillType() == SkillType.DRAIN)
					{
						attacker.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
					}
					else
					{
						attacker.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					}

					damage /= 2;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getId());
					attacker.sendPacket(sm);

					damage = 1;
				}
			}

			if(target.isPlayer)
			{
				if(skill.getSkillType() == SkillType.DRAIN)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
			}
		}
		else if(mcrit)
		{
			damage *= 4;
		}

		// Pvp bonusses for dmg
		if((attacker.isPlayer || attacker.isSummon) && (target.isPlayer || target.isSummon))
		{
			if(skill.isMagic())
			{
				damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}

		if(attacker.isPlayer)
		{
			if(((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
			}
		}
		else if(attacker.isSummon)
		{
			damage = damage * Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
		}
		else if(attacker.isNpc)
		{
			damage = damage * Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
		}

		if(skill != null)
		{

			if(target.isPlayable)
			{
				damage *= skill.getPvpMulti();
			}

			if(skill.getSkillType() == SkillType.DEATHLINK)
			{
				damage = damage * (1.0 - attacker.getStatus().getCurrentHp() / attacker.getMaxHp()) * 2.0;
			}
		}

		return damage;
	}

	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}

	private static double FRONT_MAX_ANGLE = 100;
	private static double BACK_MAX_ANGLE = 40;

	/** Calcul value of blow success */
	public final boolean calcBlow(L2Character activeChar, L2Character target, L2Skill skill)
	{
		int blowChance = skill.getBlowChance();

		if (blowChance == 0)
		{
			_log.info("Skill " + skill.getId() + " - " + skill.getName() + " has 0 blow land chance, yet its a blow skill!");
			//lets add 20 for now, till all skills are corrected
			blowChance = 20;
		}

		if (isBehind(target, activeChar))
		{
			blowChance *= 2; //double chance from behind
		}
		else if (isInFrontOf(target, activeChar))
		{
			//base chance from front

			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
				return false;
		}
		else
		{
			blowChance *= 1.5; //50% better chance from side
		}

		return activeChar.calcStat(Stats.BLOW_RATE, blowChance * (1.0 + (activeChar.getDEX()) / 100.), target, null) > Rnd.get(100);
	}

	/**
	 * Those are altered formulas for blow lands
	 * Return True if the target is IN FRONT of the L2Character.<BR><BR>
	 */
	public static boolean isInFrontOf(L2Character target, L2Character attacker)
	{
		if (target == null)
			return false;

		double angleChar, angleTarget, angleDiff;
		angleTarget = Util.calculateAngleFrom(target, attacker);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + FRONT_MAX_ANGLE)
			angleDiff += 360;
		if (angleDiff >= 360 - FRONT_MAX_ANGLE)
			angleDiff -= 360;
		if (Math.abs(angleDiff) <= FRONT_MAX_ANGLE)
			return true;
		return false;
	}

	/**
	 * Those are altered formulas for blow lands
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public static boolean isBehind(L2Character target, L2Character attacker)
	{
		if (target == null)
			return false;

		double angleChar, angleTarget, angleDiff;
		angleChar = Util.calculateAngleFrom(attacker, target);
		angleTarget = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + BACK_MAX_ANGLE)
			angleDiff += 360;
		if (angleDiff >= 360 - BACK_MAX_ANGLE)
			angleDiff -= 360;
		if (Math.abs(angleDiff) <= BACK_MAX_ANGLE)
			return true;
		return false;
	}

	/** Calcul value of lethal chance */
	public final double calcLethal(L2Character activeChar, L2Character target, int baseLethal)
	{
		return activeChar.calcStat(Stats.LETHAL_RATE, (baseLethal * (double) activeChar.getLevel() / target.getLevel()), target, null);
	}

	public final static boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}

	/** Returns true in case when ATTACK is canceled due to hit */
	public final static boolean calcAtkBreak(L2Character target, double dmg)
	{
		if(target.isPlayer)
		{
			if(target.getPlayer().getForceBuff() != null)
				return true;

			//			if (target.isCastingNow()&& target.getLastSkillCast() != null)
			//				if (target.getLastSkillCast().isCancelIfHit())
			//					return true;
		}
		double init = 0;

		if(Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}

		if(Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			L2Weapon wpn = target.getActiveWeaponItem();
			if(wpn != null && wpn.getItemType() == L2WeaponType.BOW)
			{
				init = 15;
			}
		}

		if(target.isRaid() || target.isInvul() || init <= 0)
			return false; // No attack break

		// Chance of break is higher with higher dmg
		init += Math.sqrt(13 * dmg);

		// Chance is affected by target MEN
		init -= MENbonus[target.getMEN()] * 100 - 100;

		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);

		// Adjust the rate to be between 1 and 99
		if(rate > 99)
		{
			rate = 99;
		}
		else if(rate < 1)
		{
			rate = 1;
		}

		return Rnd.get(100) < rate;
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public final int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if(rate < 2)
			return 2700;
		else
			return (int) (470000 / rate);
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, L2Character target, L2Skill skill, double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333 / attacker.getMAtkSpd());
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if(skill.isMagic())
			return (int) (skillTime * 333 / attacker.getMAtkSpd());
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Returns true if hit missed (taget evaded) */
	public boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		// accuracy+dexterity => probability to hit in percents
		int acc_attacker;
		int evas_target;
		acc_attacker = attacker.getAccuracy();
		evas_target = target.getEvasionRate(attacker);
		int d = 85 + acc_attacker - evas_target;
		return d < Rnd.get(100);
	}

	/** Returns true if shield defence successfull */
	public boolean calcShldUse(L2Character attacker, L2Character target)
	{
		L2Weapon at_weapon = attacker.getActiveWeaponItem();
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * DEXbonus[target.getDEX()];
		if(shldRate == 0.0)
			return false;
		// Check for passive skill Aegis (316) or Aegis Stance (318)
		if(target.getKnownSkill(316) == null && target.getFirstEffect(318) == null)
			if(!target.isFront(attacker))
				return false;
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		if(at_weapon != null && at_weapon.getItemType() == L2WeaponType.BOW)
		{
			shldRate *= 1.3;
		}
		return shldRate > Rnd.get(100);
	}

	public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		SkillType type = skill.getSkillType();
		double defence = 0;
		if(skill.isActive() && skill.isOffensive())
		{
			defence = target.getMDef(actor, skill);
		}

		double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		double d = (attack - defence) / (attack + defence);
		if(target.isRaid() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP || type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.AGGDEBUFF))
		{
			if(d > 0 && Rnd.get(1000) == 1)
				return true;
			else
				return false;
		}

		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}

	public static double calcSkillVulnerability(L2Character target, L2Skill skill)
	{
		double multiplier = 1; // initialize...

		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if(skill != null)
		{
			// first, get the natural template vulnerability values for the target
			Stats stat = skill.getStat();
			if(stat != null)
			{
				switch(stat)
				{
					case AGGRESSION:
						multiplier *= target.getTemplate().baseAggressionVuln;
						break;
					case BLEED:
						multiplier *= target.getTemplate().baseBleedVuln;
						break;
					case POISON:
						multiplier *= target.getTemplate().basePoisonVuln;
						break;
					case STUN:
						multiplier *= target.getTemplate().baseStunVuln;
						break;
					case ROOT:
						multiplier *= target.getTemplate().baseRootVuln;
						break;
					case MOVEMENT:
						multiplier *= target.getTemplate().baseMovementVuln;
						break;
					case CONFUSION:
						multiplier *= target.getTemplate().baseConfusionVuln;
						break;
					case SLEEP:
						multiplier *= target.getTemplate().baseSleepVuln;
						break;
					case FIRE:
						multiplier *= target.getTemplate().baseFireVuln;
						break;
					case WIND:
						multiplier *= target.getTemplate().baseWindVuln;
						break;
					case WATER:
						multiplier *= target.getTemplate().baseWaterVuln;
						break;
					case EARTH:
						multiplier *= target.getTemplate().baseEarthVuln;
						break;
					case HOLY:
						multiplier *= target.getTemplate().baseHolyVuln;
						break;
					case DARK:
						multiplier *= target.getTemplate().baseDarkVuln;
						break;
				}
			}

			// Next, calculate the elemental vulnerabilities
			switch(skill.getElement())
			{
				case L2Skill.ELEMENT_EARTH:
					multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_FIRE:
					multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WATER:
					multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WIND:
					multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_HOLY:
					multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_DARK:
					multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
					break;
			}

			// Finally, calculate skilltype vulnerabilities
			SkillType type = skill.getSkillType();

			// For additional effects on PDAM and MDAM skills (like STUN, SHOCK, PARALYZE...)
			if(type != null && (type == SkillType.PDAM || type == SkillType.MDAM))
			{
				type = skill.getEffectType();
			}

			if(type != null)
			{
				switch(type)
				{
					case BLEED:
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					case POISON:
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					case STUN:
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					case ROOT:
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					case MUTE:
					case FEAR:
					case BETRAY:
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case ERASE:
					case CONFUSION:
					case CONFUSE_MOB_ONLY:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case DEBUFF:
					case WEAKNESS:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					case BUFF:
						multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
						break;
					default:
						;
				}
			}

		}
		return multiplier;
	}

	public double calcSkillStatModifier(SkillType type, L2Character target)
	{
		double multiplier = 1;
		if(type == null)
			return multiplier;
		switch(type)
		{
			case STUN:
			case BLEED:
				multiplier = 2 - Math.sqrt(CONbonus[target.getCON()]);
				break;
			case POISON:
			case SLEEP:
			case DEBUFF:
			case WEAKNESS:
			case ERASE:
			case ROOT:
			case MUTE:
			case FEAR:
			case BETRAY:
			case CONFUSION:
			case AGGREDUCE_CHAR:
			case PARALYZE:
				multiplier = 2 - Math.sqrt(MENbonus[target.getMEN()]);
				break;
			default:
				return multiplier;
		}
		if(multiplier < 0)
		{
			multiplier = 0;
		}
		return multiplier;
	}

	public boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss)
	{
		SkillType type = skill.getSkillType();

		if(target.isRaid() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP || type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.AGGDEBUFF))
			return false; // these skills should not work on RaidBoss

		if(target.isInvul() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP || type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.CANCEL || type == SkillType.NEGATE || type == SkillType.WARRIOR_BANE || type == SkillType.MAGE_BANE))
			return false; // these skills should not work on Invulable persons

		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();

		if(type == SkillType.PDAM || type == SkillType.MDAM) // For additional effects on PDAM skills (like STUN, SHOCK,...)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}
		// TODO: Temporary fix for skills with EffectPower = 0 or EffectType not set
		if(value == 0 || type == null)
		{
			if(skill.getSkillType() == SkillType.PDAM)
			{
				value = 50;
				type = SkillType.STUN;
			}
			if(skill.getSkillType() == SkillType.MDAM)
			{
				value = 30;
				type = SkillType.PARALYZE;
			}
		}

		// TODO: Temporary fix for skills with Power = 0 or LevelDepend not set
		if(value == 0)
		{
			value = type == SkillType.PARALYZE ? 50 : type == SkillType.FEAR ? 40 : 80;
		}
		if(lvlDepend == 0)
		{
			lvlDepend = type == SkillType.PARALYZE || type == SkillType.FEAR ? 1 : 2;
		}

		// TODO: Temporary fix for NPC skills with MagicLevel not set
		// int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
		int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel()) * lvlDepend;
		double statmodifier = calcSkillStatModifier(type, target);
		double resmodifier = calcSkillVulnerability(target, skill);

		int ssmodifier = (bss ? 200 : (sps || ss ? 150 : 100));

		int rate = (int) (value * statmodifier + lvlmodifier);
		if(skill.isMagic())
		{
			rate = (int) (rate * Math.pow((double) attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.2));
		}

		if(!Config.SS_ANTI_CRY_STUN && ssmodifier != 100)
		{
			if(rate > 10000 / (100 + ssmodifier))
			{
				rate = 100 - (100 - rate) * 100 / ssmodifier;
			}
			else
			{
				rate = rate * ssmodifier / 100;
			}
		}
                
        rate *= resmodifier;
        // В Interlude, максимальный шанс успешности прохождения - 95%, минимальный не ограничен
        // В грациях - минимальный 10%, максимальный - 90%
		if(rate > 95)
		{
			rate = 95;
		}
		else if(rate < 1)
		{
			rate = 1;
		}

        if(Config.DEVELOPER_SKILL_CHANCE)
        {
            if(attacker.isPlayer)
             {
                 attacker.sendPacket(SystemMessage.sendString(skill.getName()+": "+rate+"%"));
             }
        }
		if(Config.DEVELOPER)
		{
			System.out.println(skill.getName() + ": " + value + ", " + statmodifier + ", " + lvlmodifier + ", " + resmodifier + ", " + ((int) (Math.pow((double) attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.2) * 100) - 100) + ", " + ssmodifier + " ==> " + rate);
		}
		return Rnd.get(100) < rate;
	}

	public boolean calcBuffSuccess(L2Character target, L2Skill skill)
	{
		int rate = 100 * (int)calcSkillVulnerability(target, skill);
		return Rnd.get(100) < rate;
	}

	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		double lvlDifference = target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
		int rate = Math.round((float) (Math.pow(1.3, lvlDifference) * 100));

		return Rnd.get(10000) > rate;
	}

	public boolean calculateUnlockChance(L2Skill skill)
	{
		int level = skill.getLevel();
		int chance = 0;
		switch(level)
		{
			case 1:
				chance = 30;
				break;

			case 2:
				chance = 50;
				break;

			case 3:
				chance = 75;
				break;

			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
				chance = 100;
				break;
		}
		if(Rnd.get(120) > chance)
			return false;
		return true;
	}

	public double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
	{
		//Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		double mp = target.getMaxMp();
		if(bss)
		{
			mAtk *= 4;
		}
		else if(ss)
		{
			mAtk *= 2;
		}

		double damage = Math.sqrt(mAtk) * skill.getPower(attacker) * mp / 97 / mDef;
		damage *= calcSkillVulnerability(target, skill);
		return damage;
	}

	public double calculateSkillResurrectRestorePercent(double baseRestorePercent, int casterWIT)
	{
		double restorePercent = baseRestorePercent;
		double modifier = WITbonus[casterWIT];

		if(restorePercent != 100 && restorePercent != 0)
		{

			restorePercent = baseRestorePercent * modifier;

			if(restorePercent - baseRestorePercent > 20.0)
			{
				restorePercent = baseRestorePercent + 20.0;
			}
		}

		if(restorePercent > 100)
		{
			restorePercent = 100;
		}
		if(restorePercent < baseRestorePercent)
		{
			restorePercent = baseRestorePercent;
		}

		return restorePercent;
	}

	public double getSTRBonus(L2Character activeChar)
	{
		return STRbonus[activeChar.getSTR()];
	}

	public boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		if(skill.isMagic() || skill.getCastRange() > 40)
			return false;

		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}

	public boolean calcSkillMastery(L2Character actor)
	{
		if(actor == null)
			return false;

		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);

		if(actor.isPlayer)
		{
			if(((L2PcInstance) actor).isMageClass())
			{
				val *= INTbonus[actor.getINT()];
			}
			else
			{
				val *= STRbonus[actor.getSTR()];
			}
		}

		return Rnd.get(100) < val;
	}
}
