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
package com.l2scoria.gameserver.model;

import com.l2scoria.gameserver.datatables.HeroSkillTable;
import com.l2scoria.gameserver.datatables.SkillTable;
import com.l2scoria.gameserver.datatables.sql.SkillTreeTable;
import com.l2scoria.gameserver.geodata.GeoEngine;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.model.actor.instance.*;
import com.l2scoria.gameserver.model.base.ClassId;
import com.l2scoria.gameserver.model.entity.siege.Siege;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import com.l2scoria.gameserver.skills.Env;
import com.l2scoria.gameserver.skills.Formulas;
import com.l2scoria.gameserver.skills.Stats;
import com.l2scoria.gameserver.skills.conditions.Condition;
import com.l2scoria.gameserver.skills.effects.EffectCharge;
import com.l2scoria.gameserver.skills.effects.EffectTemplate;
import com.l2scoria.gameserver.skills.funcs.Func;
import com.l2scoria.gameserver.skills.funcs.FuncTemplate;
import com.l2scoria.gameserver.skills.l2skills.*;
import com.l2scoria.gameserver.templates.StatsSet;
import com.l2scoria.gameserver.util.Util;
import com.l2scoria.util.GArray;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class...
 *
 * @version $Revision: 1.3.3 $ $Date: 2009/04/29 00:08 $
 * @authors ProGramMoS, eX1steam, l2scoria dev&sword dev
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class L2Skill
{
	protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());

	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;

	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;
	public static final int SKILL_FAKE_DEX = 9005;
	public static final int SKILL_FAKE_STR = 9006;

	public static enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE,
		OP_CHANCE
	}

	/**
	 * Target types of skills : SELF, PARTY, CLAN, PET...
	 */
	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_PET,
		TARGET_AREA,
		TARGET_AURA,
		TARGET_FRONT_AURA,
		TARGET_BEHIND_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_AREA_UNDEAD,
		TARGET_MULTIFACE,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_ITEM,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_GROUND,
		TARGET_SIEGE,
		TARGET_TYRANNOSAURUS,
		TARGET_AREA_AIM_CORPSE
	}

	public static enum SkillType
	{
		// Damage
		PDAM,
		MDAM,
		CPDAM,
		MANADAM,
		DOT,
		MDOT,
		DRAIN_SOUL,
		DRAIN(L2SkillDrain.class),
		DEATHLINK,
		FATALCOUNTER,
		BLOW,

		// Disablers
		BLEED,
		POISON,
		STUN,
		ROOT,
		CONFUSION,
		FEAR,
		SLEEP,
		CONFUSE_MOB_ONLY,
		MUTE,
		PARALYZE,
		WEAKNESS,

		// hp, mp, cp
		HEAL,
		HOT,
		BALANCE_LIFE,
		HEAL_PERCENT,
		HEAL_STATIC,
		COMBATPOINTHEAL,
		COMBATPOINTPERCENTHEAL,
		CPHOT,
		MANAHEAL,
		MANA_BY_LEVEL,
		MANAHEAL_PERCENT,
		MANARECHARGE,
		MPHOT,

		// Aggro
		AGGDAMAGE,
		AGGREDUCE,
		AGGREMOVE,
		AGGREDUCE_CHAR,
		AGGDEBUFF,

		// Fishing
		FISHING,
		PUMPING,
		REELING,

		// MISC
		UNLOCK,
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		SOULSHOT,
		SPIRITSHOT,
		SIEGEFLAG,
		TAKECASTLE,
		DELUXE_KEY_UNLOCK,
		SOW,
		HARVEST,
		GET_PLAYER,

		// Creation
		COMMON_CRAFT,
		DWARVEN_CRAFT,
		CREATE_ITEM(L2SkillCreateItem.class),
		SUMMON_TREASURE_KEY,

		// Summons
		SUMMON(L2SkillSummon.class),
		FEED_PET,
		DEATHLINK_PET,
		STRSIEGEASSAULT,
		ERASE,
		BETRAY,

		// Cancel
		CANCEL,
		MAGE_BANE,
		WARRIOR_BANE,
		NEGATE,

		BUFF,
		DEBUFF,
		PASSIVE,
		CONT,
		SIGNET(L2SkillSignet.class),
		SIGNET_CASTTIME(L2SkillSignetCasttime.class),

		RESURRECT,
		CHARGE(L2SkillCharge.class),
		CHARGE_EFFECT(L2SkillChargeEffect.class),
		CHARGEDAM(L2SkillChargeDmg.class),
		MHOT,
		DETECT_WEAKNESS,
		LUCK,
		RECALL,
		SUMMON_FRIEND,
		REFLECT,
		SPOIL,
		SWEEP,
		FAKE_DEATH,
		UNBLEED,
		UNPOISON,
		UNDEAD_DEFENSE,
		SEED(L2SkillSeed.class),
		BEAST_FEED,
		FORCE_BUFF,
		CLAN_GATE,
		GIVE_SP,
		COREDONE,
		ZAKENPLAYER,
		ZAKENSELF,

		// unimplemented
		NOTDONE;

		private final Class<? extends L2Skill> _class;

		public L2Skill makeSkill(StatsSet set)
		{
			try
			{
				Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);

				return c.newInstance(set);
			} catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		private SkillType()
		{
			_class = L2SkillDefault.class;
		}

		private SkillType(Class<? extends L2Skill> classType)
		{
			_class = classType;
		}
	}

	public enum NegateStats
	{
		BUFF,
		DEBUFF,
		WEAKNESS,
		STUN,
		SLEEP,
		CONFUSION,
		MUTE,
		FEAR,
		POISON,
		BLEED,
		PARALYZE,
		ROOT,
		HEAL
	}

	protected ChanceCondition _chanceCondition = null;
	//elements
	public final static int ELEMENT_WIND = 1;
	public final static int ELEMENT_FIRE = 2;
	public final static int ELEMENT_WATER = 3;
	public final static int ELEMENT_EARTH = 4;
	public final static int ELEMENT_HOLY = 5;
	public final static int ELEMENT_DARK = 6;

	//save vs
	public final static int SAVEVS_INT = 1;
	public final static int SAVEVS_WIT = 2;
	public final static int SAVEVS_MEN = 3;
	public final static int SAVEVS_CON = 4;
	public final static int SAVEVS_DEX = 5;
	public final static int SAVEVS_STR = 6;

	//stat effected
	public final static int STAT_PATK = 301; // pAtk
	public final static int STAT_PDEF = 302; // pDef
	public final static int STAT_MATK = 303; // mAtk
	public final static int STAT_MDEF = 304; // mDef
	public final static int STAT_MAXHP = 305; // maxHp
	public final static int STAT_MAXMP = 306; // maxMp
	public final static int STAT_CURHP = 307;
	public final static int STAT_CURMP = 308;
	public final static int STAT_HPREGEN = 309; // regHp
	public final static int STAT_MPREGEN = 310; // regMp
	public final static int STAT_CASTINGSPEED = 311; // sCast
	public final static int STAT_ATKSPD = 312; // sAtk
	public final static int STAT_CRITDAM = 313; // critDmg
	public final static int STAT_CRITRATE = 314; // critRate
	public final static int STAT_FIRERES = 315; // fireRes
	public final static int STAT_WINDRES = 316; // windRes
	public final static int STAT_WATERRES = 317; // waterRes
	public final static int STAT_EARTHRES = 318; // earthRes
	public final static int STAT_HOLYRES = 336; // holyRes
	public final static int STAT_DARKRES = 337; // darkRes
	public final static int STAT_ROOTRES = 319; // rootRes
	public final static int STAT_SLEEPRES = 320; // sleepRes
	public final static int STAT_CONFUSIONRES = 321; // confusRes
	public final static int STAT_BREATH = 322; // breath
	public final static int STAT_AGGRESSION = 323; // aggr
	public final static int STAT_BLEED = 324; // bleed
	public final static int STAT_POISON = 325; // poison
	public final static int STAT_STUN = 326; // stun
	public final static int STAT_ROOT = 327; // root
	public final static int STAT_MOVEMENT = 328; // move
	public final static int STAT_EVASION = 329; // evas
	public final static int STAT_ACCURACY = 330; // accu
	public final static int STAT_COMBAT_STRENGTH = 331;
	public final static int STAT_COMBAT_WEAKNESS = 332;
	public final static int STAT_ATTACK_RANGE = 333; // rAtk
	public final static int STAT_NOAGG = 334; // noagg
	public final static int STAT_SHIELDDEF = 335; // sDef
	public final static int STAT_MP_CONSUME_RATE = 336; // Rate of mp consume per skill use
	public final static int STAT_HP_CONSUME_RATE = 337; // Rate of hp consume per skill use
	public final static int STAT_MCRITRATE = 338; // Magic Crit Rate

	//COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
	public final static int COMBAT_MOD_ANIMAL = 200;
	public final static int COMBAT_MOD_BEAST = 201;
	public final static int COMBAT_MOD_BUG = 202;
	public final static int COMBAT_MOD_DRAGON = 203;
	public final static int COMBAT_MOD_MONSTER = 204;
	public final static int COMBAT_MOD_PLANT = 205;
	public final static int COMBAT_MOD_HOLY = 206;
	public final static int COMBAT_MOD_UNHOLY = 207;
	public final static int COMBAT_MOD_BOW = 208;
	public final static int COMBAT_MOD_BLUNT = 209;
	public final static int COMBAT_MOD_DAGGER = 210;
	public final static int COMBAT_MOD_FIST = 211;
	public final static int COMBAT_MOD_DUAL = 212;
	public final static int COMBAT_MOD_SWORD = 213;
	public final static int COMBAT_MOD_POISON = 214;
	public final static int COMBAT_MOD_BLEED = 215;
	public final static int COMBAT_MOD_FIRE = 216;
	public final static int COMBAT_MOD_WATER = 217;
	public final static int COMBAT_MOD_EARTH = 218;
	public final static int COMBAT_MOD_WIND = 219;
	public final static int COMBAT_MOD_ROOT = 220;
	public final static int COMBAT_MOD_STUN = 221;
	public final static int COMBAT_MOD_CONFUSION = 222;
	public final static int COMBAT_MOD_DARK = 223;

	//conditional values
	public final static int COND_RUNNING = 0x0001;
	public final static int COND_WALKING = 0x0002;
	public final static int COND_SIT = 0x0004;
	public final static int COND_BEHIND = 0x0008;
	public final static int COND_CRIT = 0x0010;
	public final static int COND_LOWHP = 0x0020;
	public final static int COND_ROBES = 0x0040;
	public final static int COND_CHARGES = 0x0080;
	public final static int COND_SHIELD = 0x0100;
	public final static int COND_GRADEA = 0x010000;
	public final static int COND_GRADEB = 0x020000;
	public final static int COND_GRADEC = 0x040000;
	public final static int COND_GRADED = 0x080000;
	public final static int COND_GRADES = 0x100000;

	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final L2Effect[] _emptyEffectSet = new L2Effect[0];

	// these two build the primary key
	private final int _id;
	private final int _level;

	/**
	 * Identifier for a skill that client can't display
	 */
	private int _displayId;

	// not needed, just for easier debug
	private final String _name;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private final boolean _staticReuse;
        private final int _customOlystaticReuse;
	private final boolean _staticHitTime;
        private final int _olyCustomStaticHitTime;
	private final boolean _ignoreShld;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	private final int _itemConsume;
	private final int _itemConsumeId;
	// item consume count over time
	private final int _itemConsumeOT;
	// item consume id over time
	private final int _itemConsumeIdOT;
	// how many times to consume an item
	private final int _itemConsumeSteps;
	// for summon spells:
	// a) What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// b) how much lifetime is lost per second of idleness (non-fighting)
	private final int _summonTimeLostIdle;
	// c) how much time is lost per second of activity (fighting)
	private final int _summonTimeLostActive;

	// item consume time in milliseconds
	private final int _itemConsumeTime;
	private final int _castRange;
	private final int _effectRange;

	// all times in milliseconds
	private final int _hitTime;
        private final int _olyCustomHitTime;
	//private final int _skillInterruptTime;
	private final int _coolTime;
	private final int _reuseDelay;
        private final int _olyCustomReuseDelay;
	private final int _buffDuration;

	/**
	 * Target type of the skill : SELF, PARTY, CLAN, PET...
	 */
	private final SkillTargetType _targetType;

	private final double _power;
	private final int _effectPoints;
	private final int _magicLevel;
	private GArray<NegateStats> _negateStats;
	private final float _negatePower;
	private final int _negateId;
	private final int _levelDepend;

	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int _skillRadius;

	private final SkillType _skillType;
	private final SkillType _effectType;
	private final int _effectPower;
	private final int _effectId;
	private final int _effectLvl;

	private final boolean _ispotion;
	private final int _element;
	private final int _savevs;

	private final boolean _isSuicideAttack;

	private final Stats _stat;

	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	private final int _weaponsAllowed;
	private final int _armorsAllowed;

	private final int _addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
	private final float _mulCrossLearn; // multiplay for others classes, default 2
	private final float _mulCrossLearnRace; // multiplay for others races, default 2
	private final float _mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
	private final List<ClassId> _canLearn; // which classes can learn
	private final List<Integer> _teachers; // which NPC teaches
	private final int _minPledgeClass;

	private final boolean _isOffensive;
	private final int _numCharges;
	private final int _triggeredId;
	private final int _triggeredLevel;

	private final boolean _bestowed;

	private final boolean _isHeroSkill; // If true the skill is a Hero Skill

	private final int _baseCritRate; // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int _lethalEffect1; // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int _lethalEffect2; // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean _directHpDmg; // If true then dmg is being make directly
	private final boolean _isDance; // If true then casting more dances will cost more MP
	private final int _nextDanceCost;
	private final float _sSBoost; //If true skill will have SoulShot boost (power*2)
	private final int _aggroPoints;

	private final float _pvpMulti;
	private final int _blowChance;
	private final boolean _nextActionAttack;

	// Flying support
	private final String _flyType;
	private final int _flyRadius;

	private final boolean _isDebuff;

	protected Condition _preCondition;
	protected Condition _itemPreCondition;
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	protected EffectTemplate[] _effectTemplatesSelf;

	protected L2Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");

		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_magic = set.getBool("isMagic", false);
		_ignoreShld = set.getBool("ignoreShld", false);
		_staticReuse = set.getBool("staticReuse", false);
                _customOlystaticReuse = set.getInteger("olyCustomStaticReuse", 2);
		_staticHitTime = set.getBool("staticHitTime", false);
                _olyCustomStaticHitTime = set.getInteger("olyCustomStaticHitTime", 2);
		_ispotion = set.getBool("isPotion", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);

		_castRange = set.getInteger("castRange", -1);
		_effectRange = set.getInteger("effectRange", -1);

		_hitTime = set.getInteger("hitTime", 0);
                _olyCustomHitTime = set.getInteger("olyCustomHitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		//_skillInterruptTime = set.getInteger("hitTime", _hitTime / 2);
		_reuseDelay = set.getInteger("reuseDelay", 0);
                _olyCustomReuseDelay = set.getInteger("olyCustomReuseDelay", 0);
		_buffDuration = set.getInteger("buffDuration", 0);

		_skillRadius = set.getInteger("skillRadius", 80);

		_targetType = set.getEnum("target", SkillTargetType.class);
		_power = set.getFloat("power", 0.f);
		_effectPoints = set.getInteger("effectPoints", 0);

		String nStats = set.getString("negateStats", "");
		if(nStats.length() > 0)
		{
			String[] negateStats = nStats.split(" ");
			_negateStats = new GArray<NegateStats>();

			for(String stat : negateStats)
			{
				try
				{
					NegateStats ns = NegateStats.valueOf(stat.toUpperCase());
					if(ns != null)
					{
						_negateStats.add(ns);
					}
				}
				catch (Exception e)
				{
					_log.fatal("Negate stat: " + stat.toUpperCase() + " not found.");
					_log.fatal(e.getMessage(), e);
				}
			}
		}

		_negatePower = set.getFloat("negatePower", 0.f);
		_negateId = set.getInteger("negateId", 0);
		_magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
		_levelDepend = set.getInteger("lvlDepend", 0);
		_stat = set.getEnum("stat", Stats.class, null);

		_skillType = set.getEnum("skillType", SkillType.class);
		_effectType = set.getEnum("effectType", SkillType.class, null);
		_effectPower = set.getInteger("effectPower", 0);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getInteger("effectLevel", 0);

		_element = set.getInteger("element", 0);
		_savevs = set.getInteger("save", 0);

		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_armorsAllowed = set.getInteger("armorsAllowed", 0);

		_flyType = set.getString("flyType", null);
		_flyRadius = set.getInteger("flyRadius", 0);

		_addCrossLearn = set.getInteger("addCrossLearn", 1000);
		_mulCrossLearn = set.getFloat("mulCrossLearn", 2.f);
		_mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.f);
		_mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.f);
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		_numCharges = set.getInteger("num_charges", 0);
		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 0);
		_nextActionAttack = set.getBool("nextActionAttack", true);

		_bestowed = set.getBool("bestowed", false);

		if (_operateType == SkillOpType.OP_CHANCE)
		{
			_chanceCondition = ChanceCondition.parse(set);
		}

		_isHeroSkill = HeroSkillTable.isHeroSkill(_id);

		_baseCritRate = set.getInteger("baseCritRate", _skillType == SkillType.PDAM || _skillType == SkillType.BLOW ? 0 : -1);
		_lethalEffect1 = set.getInteger("lethal1", 0);
		_lethalEffect2 = set.getInteger("lethal2", 0);

		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_isDance = set.getBool("isDance", false);
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		_isDebuff = set.getBool("isDebuff", false);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = set.getInteger("aggroPoints", 0);

		_blowChance = set.getInteger("blowChance", 0);
		_pvpMulti = set.getFloat("pvpMulti", 1.f);

		String canLearn = set.getString("canLearn", null);
		if (canLearn == null)
		{
			_canLearn = null;
		}
		else
		{
			_canLearn = new FastList<ClassId>();
			StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");

			while (st.hasMoreTokens())
			{
				String cls = st.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				} catch (Throwable t)
				{
					_log.fatal("Bad class " + cls + " to learn skill", t);
				}
			}
		}

		String teachers = set.getString("teachers", null);
		if (teachers == null)
		{
			_teachers = null;
		}
		else
		{
			_teachers = new FastList<Integer>();
			StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				} catch (Throwable t)
				{
					_log.fatal("Bad teacher id " + npcid + " to teach skill", t);
				}
			}
		}
	}

	public abstract void useSkill(L2Character caster, L2Object[] targets);

	public final boolean isPotion()
	{
		return _ispotion;
	}

	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}

	public final int getConditionValue()
	{
		return _conditionValue;
	}

	public final SkillType getSkillType()
	{
		return _skillType;
	}

	public final boolean hasEffectWhileCasting()
	{
		return getSkillType() == SkillType.SIGNET_CASTTIME;
	}

	public final int getSavevs()
	{
		return _savevs;
	}

	public final int getElement()
	{
		return _element;
	}

	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR>
	 * <BR>
	 */
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final int getCondition()
	{
		return _condition;
	}

	public final boolean isOverhit()
	{
		return _overhit;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	/**
	 * Return the power of the skill.<BR>
	 * <BR>
	 */
	public final double getPower(L2Character activeChar)
	{
		if (_skillType == SkillType.DEATHLINK && activeChar != null)
		{
			return _power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
		}
		else if (_skillType == SkillType.FATALCOUNTER && activeChar != null)
		{
			return _power * 3.5 * (1 - activeChar.getCurrentHp() / activeChar.getMaxHp());
		}
		else
		{
			return _power;
		}
	}

	public final double getPower()
	{
		return _power;
	}

	public final int getEffectPoints()
	{
		return _effectPoints;
	}

	public final GArray<NegateStats> getNegateStats()
	{
		return _negateStats;
	}

	public final float getNegatePower()
	{
		return _negatePower;
	}

	public final int getNegateId()
	{
		return _negateId;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return _staticReuse;
	}
        
        public final boolean isOlyCustomStaticReuse()
        {
            // 2 is a unseted state
            if(_customOlystaticReuse == 1)
            {
                return true;
            }
            else if(_customOlystaticReuse == 0)
            {
                return false;
            }
            else
            {
                return isStaticReuse();
            }
        }

	/**
	 * @return Returns true to set static hittime.
	 */
	public final boolean isStaticHitTime()
	{
		return _staticHitTime;
	}
        
        /**
         * 
         * @return Return skill state static in oly or defaults
         */
        public final boolean isOlyCustomStaticHitTime()
        {
            if(_olyCustomStaticHitTime == 0)
            {
                return false;
            }
            else if(_olyCustomStaticHitTime == 1)
            {
                return true;
            }
            else
            {
                return isStaticHitTime();
            }
        }
        
        /**
         * @return Return skills is static hittime or no. New mechanism, must be used if skill can be changed on custom oly headers
         */
        public final boolean isModedStaticHitTime(L2Character object)
        {
            if(!object.isPlayer)
            {
                return isStaticHitTime(); 
            }
            L2PcInstance player = object.getPlayer();
            if(player.isOlympiadStart() && player.isInOlympiadMode())
            {
                return isOlyCustomStaticHitTime();
            }
            else
            {
                return isStaticHitTime();
            }
        }

	public final int getLevelDepend()
	{
		return _levelDepend;
	}

	/**
	 * Return the additional effect power or base probability.<BR>
	 * <BR>
	 */
	public final int getEffectPower()
	{
		return _effectPower;
	}

	/**
	 * Return the additional effect Id.<BR>
	 * <BR>
	 */
	public final int getEffectId()
	{
		return _effectId;
	}

	/**
	 * Return the additional effect level.<BR>
	 * <BR>
	 */
	public final int getEffectLvl()
	{
		return _effectLvl;
	}

	/**
	 * Return the additional effect skill type (ex : STUN, PARALYZE,...).<BR>
	 * <BR>
	 */
	public final SkillType getEffectType()
	{
		return _effectType;
	}

	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration()
	{
		return _buffDuration;
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return _effectRange;
	}

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the id.
	 */
	public final int getId()
	{
		return _id;
	}

	public int getDisplayId()
	{
		return _displayId;
	}

	public void setDisplayId(int id)
	{
		_displayId = id;
	}

	public int getTriggeredId()
	{
		return _triggeredId;
	}

	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}

	/**
	 * Return the skill type (ex : BLEED, SLEEP, WATER...).<BR>
	 * <BR>
	 */
	public final Stats getStat()
	{
		return _stat;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}

	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}

	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}

	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}

	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}

	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns is ignore Shield defence
	 */
	public final boolean isIgnoreShld()
	{
		return _ignoreShld;
	}

	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}

    /**
     * Skills what required ss use(like body to mind, recharge) and decrease coolTime/hitTime (interlude official)
     * @return
     */
    public final boolean isRequiredSS()
    {
        switch(this.getId())
        {
            case 1157:
            case 1013:
                return true;
            default:
                return false;
        }
    }

	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}

	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
        
        /**
         * @return custom modificator of skill reulse in olympiad mode 
         */
    public final int getCustomOlyReuseDelay()
    {
        if(_olyCustomReuseDelay < 1)
        {
            return getReuseDelay();
        }
        return _olyCustomReuseDelay;
    }

	@Deprecated
	public final int getSkillTime()
	{
		return _hitTime;
	}

	public final int getHitTime()
	{
		return _hitTime;
	}
        
        /*
         * return custom HitTime inclued olympiad modificators
         */
        public final int getCustomHitTime()
        {
            if(_olyCustomHitTime == 0)
            {
                return getHitTime();
            }
            return _olyCustomHitTime;
        }

	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime()
	{
		return _coolTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public final boolean isChance()
	{
		return _operateType == SkillOpType.OP_CHANCE;
	}

	public ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}

	public final boolean isDance()
	{
		return _isDance;
	}

	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}

	public final boolean isDebuff()
	{
		return _isDebuff;
	}

	public final float getSSBoost()
	{
		return _sSBoost;
	}

	public final int getAggroPoints()
	{
		return _aggroPoints;
	}

	public final float getPvpMulti()
	{
		return _pvpMulti;
	}
        
        public final boolean isForceSpell()
        {
            if(getSkillType() == SkillType.FORCE_BUFF)
                return true;
            else
                return false;
        }

	public final boolean useSoulShot()
	{
		return getSkillType() == SkillType.PDAM || getSkillType() == SkillType.STUN || getSkillType() == SkillType.CHARGEDAM || getSkillType() == SkillType.BLOW;
	}

	public final boolean useSpiritShot()
	{
		return isMagic();
	}

	public final boolean useFishShot()
	{
		return getSkillType() == SkillType.PUMPING || getSkillType() == SkillType.REELING;
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public final String getFlyType()
	{
		return _flyType;
	}

	public final int getFlyRadius()
	{
		return _flyRadius;
	}

	public final int getCrossLearnAdd()
	{
		return _addCrossLearn;
	}

	public final float getCrossLearnMul()
	{
		return _mulCrossLearn;
	}

	public final float getCrossLearnRace()
	{
		return _mulCrossLearnRace;
	}

	public final float getCrossLearnProf()
	{
		return _mulCrossLearnProf;
	}

	public final boolean getCanLearn(ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}

	public final boolean canTeachBy(int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}

	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	public int getBlowChance()
	{
		return _blowChance;
	}

	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case AGGREDUCE:
			case AGGDAMAGE:
			case AGGREDUCE_CHAR:
			case CONFUSE_MOB_ONLY:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MANADAM:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case FATALCOUNTER:
			case BETRAY:
				return true;
			default:
				return false;
		}
	}

	public final boolean isOffensive()
	{
		return _isOffensive;
	}

	public final boolean isHeroSkill()
	{
		return _isHeroSkill;
	}

	public final boolean isLifeStoneSkill()
	{
		return this.getId() >= 3080 && this.getId() <= 3259;
	}

	public final int getNumCharges()
	{
		return _numCharges;
	}

	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}

	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}

	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}

	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}

	public boolean bestowed()
	{
		return _bestowed;
	}

	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}

	public boolean isNextActionAttack()
	{
		return _nextActionAttack;
	}

	public final boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case MDOT:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case MANA_BY_LEVEL:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case FATALCOUNTER:
			case BETRAY:
			case DELUXE_KEY_UNLOCK:
			case SOW:
			case HARVEST:
				return true;
			default:
				return this.isDebuff();
		}
	}

	//	int weapons[] = {L2Weapon.WEAPON_TYPE_ETC, L2Weapon.WEAPON_TYPE_BOW,
	//	L2Weapon.WEAPON_TYPE_POLE, L2Weapon.WEAPON_TYPE_DUALFIST,
	//	L2Weapon.WEAPON_TYPE_DUAL, L2Weapon.WEAPON_TYPE_BLUNT,
	//	L2Weapon.WEAPON_TYPE_SWORD, L2Weapon.WEAPON_TYPE_DAGGER};

	public final boolean getWeaponDependancy(L2Character activeChar)
	{
		if (getWeaponDependancy(activeChar, false))
		{
			return true;
		}
		else
		{
			SystemMessage message = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			message.addSkillName(this);
			activeChar.sendPacket(message);

			return false;
		}
	}

	public final boolean getWeaponDependancy(L2Character activeChar, boolean chance)
	{
		int weaponsAllowed = getWeaponsAllowed();
		//check to see if skill has a weapon dependency.
		if (weaponsAllowed == 0)
		{
			return true;
		}

		int mask = 0;
		if (activeChar.getActiveWeaponItem() != null)
		{
			mask |= activeChar.getActiveWeaponItem().getItemType().mask();
		}
		if (activeChar.getSecondaryWeaponItem() != null)
		{
			mask |= activeChar.getSecondaryWeaponItem().getItemType().mask();
		}

		return (mask & weaponsAllowed) != 0;

	}

	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		Condition preCondition = _preCondition;

		if (itemOrWeapon)
		{
			preCondition = _itemPreCondition;
		}

		if (preCondition == null)
		{
			return true;
		}

		Env env = new Env();
		env.player = activeChar;
		if (target.isCharacter)
		{
			env.target = (L2Character) target;
		}

		env.skill = this;
		if (!preCondition.test(env))
		{
			String msg = preCondition.getMessage();
			if (msg != null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString(msg);
				activeChar.sendPacket(sm);
			}

			return false;
		}

		return true;
	}

	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		// Init to null the target of the skill
		L2Character target = null;

		// Get the L2Objcet targeted by the user of the skill at this moment
		L2Object objTarget = activeChar.getTarget();
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget != null && objTarget.isCharacter)
		{
			target = (L2Character) objTarget;
		}

		return getTargetList(activeChar, onlyFirst, target);
	}

	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR>
	 * <BR>
	 * <B><U> Values of skill type</U> :</B><BR>
	 * <BR>
	 * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no
	 * L2PcInstance targeted</li> <li>SELF</li> <li>HOLY, UNDEAD</li> <li>PET</li> <li>AURA, AURA_CLOSE</li> <li>AREA</li>
	 * <li>MULTIFACE</li> <li>PARTY, CLAN</li> <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li> <li>UNLOCKABLE</li> <li>
	 * ITEM</li><BR>
	 * <BR>
	 *
	 * @param activeChar The L2Character who use the skill
	 */
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<L2Character>();

		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		SkillTargetType targetType = getTargetType();

		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		SkillType skillType = getSkillType();

		switch (targetType)
		{
			// The skill can only be used on the L2Character targeted, or on the caster itself
			case TARGET_ONE:
			{
				boolean canTargetSelf = false;
				switch (skillType)
				{
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case NEGATE:
					case REFLECT:
					case UNBLEED:
					case UNPOISON: //case CANCEL: 
					case SEED:
					case COMBATPOINTHEAL:
					case COMBATPOINTPERCENTHEAL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case BETRAY:
					case BALANCE_LIFE:
					case FORCE_BUFF:
						canTargetSelf = true;
						break;
				}

				switch (skillType)
				{
					case CONFUSION:
					case DEBUFF:
					case STUN:
					case ROOT:
					case FEAR:
					case SLEEP:
					case MUTE:
					case WEAKNESS:
					case PARALYZE:
					case CANCEL:
					case MAGE_BANE:
					case WARRIOR_BANE:
						if (checkPartyClan(activeChar, target))
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
							return null;
						}
						break;
				}

				// Check for null target or any other invalid target
				if (target == null || target.isDead() || target == activeChar && !canTargetSelf)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}

				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new L2Character[]{target};
			}
			case TARGET_SELF:
			case TARGET_GROUND:
			{
				return new L2Character[]{activeChar};
			}
			case TARGET_HOLY:
			{
				if (activeChar.isPlayer)
				{
					if (activeChar.getTarget() instanceof L2ArtefactInstance)
					{
						return new L2Character[]{(L2ArtefactInstance) activeChar.getTarget()};
					}
				}

				return null;
			}

			case TARGET_PET:
			{
				target = activeChar.getPet();
				if (target != null && !target.isDead())
				{
					return new L2Character[]{target};
				}

				return null;
			}
			case TARGET_OWNER_PET:
			{
				if (activeChar.isSummon)
				{
					target = ((L2Summon) activeChar).getOwner();
					if (target != null && !target.isDead())
					{
						return new L2Character[]{target};
					}
				}

				return null;
			}
			case TARGET_CORPSE_PET:
			{
				if (activeChar.isPlayer)
				{
					target = activeChar.getPet();
					if (target != null && target.isDead())
					{
						return new L2Character[]{target};
					}
				}

				return null;
			}
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			{
				int radius = getSkillRadius();
				boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);

				// Go through the L2Character _knownList
				for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
				{
					if (obj != null && (obj.isAttackable || obj.isPlayable))
					{
						if (activeChar._event != null && activeChar._event.isRunning())
						{
							if (!activeChar._event.canBeSkillTarget(activeChar, obj, this))
							{
								continue;
							}
						}

						switch (targetType)
						{
							case TARGET_FRONT_AURA:
								if (!obj.isFront(activeChar))
								{
									continue;
								}
								break;
							case TARGET_BEHIND_AURA:
								if (!obj.isBehind(activeChar))
								{
									continue;
								}
								break;
						}

						if (!obj.isAlikeDead())
						{
							if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
							{
								continue;
							}


							if (!onlyFirst)
							{
								targetList.add(obj);
							}
							else
							{
								return new L2Character[]{obj};
							}
						}
					}
				}

				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			{
				if (!(target.isAttackable || target.isPlayable) || //   Target is not L2Attackable or L2PlayableInstance
						getCastRange() >= 0 && (target == null || target == activeChar || target.isAlikeDead())) //target is null or self or dead/faking
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}

				L2Character origin;
				boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
				int radius = getSkillRadius();

				if (getCastRange() >= 0)
				{
					if (!checkForAreaOffensiveSkills(activeChar, target, this, srcInArena))
					{
						return null;
					}

					if (onlyFirst)
					{
						return new L2Character[]{target};
					}

					origin = target;
					targetList.add(origin); // Add target to target list
				}
				else
				{
					origin = activeChar;
				}

				for (L2Character obj : activeChar.getKnownList().getKnownCharacters())
				{
					if (obj == null)
					{
						continue;
					}
					if (!(obj.isAttackable || obj.isPlayable))
					{
						continue;
					}
					if (obj == origin)
					{
						continue;
					}

					if (Util.checkIfInRange(radius, obj, origin, true))
					{
						switch (targetType)
						{
							case TARGET_MULTIFACE:
								if (!obj.isFront(activeChar))
								{
									continue;
								}
								break;
						}

						if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
						{
							continue;
						}

						targetList.add(obj);
					}
				}

				if (targetList.size() == 0)
				{
					return null;
				}

				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY:
			{
				if (onlyFirst)
				{
					return new L2Character[]{activeChar};
				}

				targetList.add(activeChar);

				L2PcInstance player = null;

				if (activeChar.isSummon)
				{
					player = ((L2Summon) activeChar).getOwner();
					targetList.add(player);
				}
				else if (activeChar.isPlayer)
				{
					player = (L2PcInstance) activeChar;
					if (activeChar.getPet() != null)
					{
						targetList.add(activeChar.getPet());
					}
				}

				if (activeChar.getParty() != null)
				{
					// Get all visible objects in a spheric area near the L2Character
					// Get a list of Party Members
					List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();

					for (L2PcInstance partyMember : partyList)
					{
						if (partyMember == null || partyMember == player)
						{
							continue;
						}

						if (activeChar._event != null && !activeChar._event.canBeSkillTarget(activeChar, partyMember, this))
						{
							continue;
						}

						if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
						{
							targetList.add(partyMember);

							if (partyMember.getPet() != null && !partyMember.getPet().isDead())
							{
								targetList.add(partyMember.getPet());
							}
						}
					}
				}

				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if (target != null && target == activeChar || target != null && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID() || target != null && activeChar.isPlayer && target.isSummon && activeChar.getPet() == target || target != null && activeChar.isSummon && target.isPlayer && activeChar == target.getPet())
				{
					if (!target.isDead())
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					{
						return new L2Character[]{target};
					}
					else
					{
						return null;
					}
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
			}
			case TARGET_PARTY_OTHER:
			{
				if (target != null && target != activeChar && target.isPlayer && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
				{
					if (!target.isDead())
					// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
					{
						return new L2Character[]{target};
					}
					else
					{
						return null;
					}
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (activeChar.isPlayer)
				{
					int radius = getSkillRadius();
					L2PcInstance player = (L2PcInstance) activeChar;
					L2Clan clan = player.getClan();

					if (player.isInOlympiadMode())
					{
						return new L2Character[]{player};
					}

					if (targetType != SkillTargetType.TARGET_CORPSE_ALLY)
					{
						if (!onlyFirst)
						{
							targetList.add(player);
						}
						else
						{
							return new L2Character[]{player};
						}
					}

					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (L2Character newTarget : activeChar.getKnownList().getKnownCharacters())
						{
							if (newTarget == null || !(newTarget.isPlayer))
							{
								continue;
							}

							if ((((L2PcInstance) newTarget).getAllyId() == 0 || ((L2PcInstance) newTarget).getAllyId() != player.getAllyId()) && (((L2PcInstance) newTarget).getClan() == null || ((L2PcInstance) newTarget).getClanId() != player.getClanId()))
							{
								continue;
							}

							if (player.isInDuel() && (player.getDuelId() != ((L2PcInstance) newTarget).getDuelId() || player.getParty() != null && !player.getParty().getPartyMembers().contains(newTarget)))
							{
								continue;
							}

							if (activeChar._event != null && !activeChar._event.canBeSkillTarget(activeChar, newTarget, this))
							{
								continue;
							}

							L2Summon pet = newTarget.getPet();
							if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst && (targetType == SkillTargetType.TARGET_CORPSE_ALLY && pet.isDead() || targetType == SkillTargetType.TARGET_ALLY && !pet.isDead()) && player.checkPvpSkill(newTarget, this))
							{
								targetList.add(pet);
							}

							if (targetType == SkillTargetType.TARGET_CORPSE_ALLY)
							{
								if (!newTarget.isDead())
								{
									continue;
								}

								if (getSkillType() == SkillType.RESURRECT && newTarget.isInsideZone(L2Character.ZONE_SIEGE))
								{
									continue;
								}
							}

							if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
							{
								continue;
							}

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}

							if (!onlyFirst)
							{
								targetList.add(newTarget);
							}
							else
							{
								return new L2Character[]{newTarget};
							}

						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (activeChar.isPlayer)
				{
					int radius = getSkillRadius();
					L2PcInstance player = (L2PcInstance) activeChar;
					L2Clan clan = player.getClan();

					if (player.isInOlympiadMode())
					{
						return new L2Character[]{player};
					}

					if (targetType != SkillTargetType.TARGET_CORPSE_CLAN)
					{
						if (!onlyFirst)
						{
							targetList.add(player);
						}
						else
						{
							return new L2Character[]{player};
						}
					}

					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (L2ClanMember member : clan.getMembers())
						{
							L2PcInstance newTarget = member.getPlayerInstance();

							if (newTarget == null || newTarget == player)
							{
								continue;
							}

							if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || player.getParty() == null && player.getParty() != newTarget.getParty()))
							{
								continue;
							}

							if (activeChar._event != null && !activeChar._event.canBeSkillTarget(activeChar, newTarget, this))
							{
								continue;
							}

							L2Summon pet = newTarget.getPet();
							if (pet != null && Util.checkIfInRange(radius, activeChar, pet, true) && !onlyFirst && (targetType == SkillTargetType.TARGET_CORPSE_CLAN && pet.isDead() || targetType == SkillTargetType.TARGET_CLAN && !pet.isDead()) && player.checkPvpSkill(newTarget, this))
							{
								targetList.add(pet);
							}

							if (targetType == SkillTargetType.TARGET_CORPSE_CLAN)
							{
								if (!newTarget.isDead())
								{
									continue;
								}

								if (getSkillType() == SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									Siege siege = SiegeManager.getInstance().getSiege(newTarget);
									if (siege != null && siege.getIsInProgress())
									{
										continue;
									}
								}
							}

							if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
							{
								continue;
							}

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}

							if (!onlyFirst)
							{
								targetList.add(newTarget);
							}
							else
							{
								return new L2Character[]{newTarget};
							}
						}
					}
				}
				else if (activeChar.isNpc)
				{
					L2NpcInstance npc = (L2NpcInstance) activeChar;
					if (npc.getFactionId() == null || npc.getFactionId().isEmpty())
					{
						return new L2Character[]{activeChar};
					}

					if (npc.getFirstEffect(this) == null)
					{
						targetList.add(activeChar);
					}

					for (L2Object newTarget : activeChar.getKnownList().getKnownObjects().values())
					{
						if (newTarget.isNpc && ((L2NpcInstance) newTarget).getFactionId() != null && ((L2NpcInstance) newTarget).getFactionId().equals(npc.getFactionId()))
						{
							if (!Util.checkIfInRange(getSkillRadius(), activeChar, newTarget, true))
							{
								continue;
							}

							if (((L2NpcInstance) newTarget).getFirstEffect(this) == null)
							{
								targetList.add((L2Character) newTarget);
							}
						}
					}
				}

				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if (target != null && target.isDead())
				{
					L2PcInstance player = null;

					if (activeChar.isPlayer)
					{
						player = (L2PcInstance) activeChar;
					}

					L2PcInstance targetPlayer = null;

					if (target.isPlayer)
					{
						targetPlayer = (L2PcInstance) target;
					}

					L2PetInstance targetPet = null;

					if (target.isPet)
					{
						targetPet = (L2PetInstance) target;
					}

					if (player != null && (targetPlayer != null || targetPet != null))
					{
						boolean condGood = true;

						if (getSkillType() == SkillType.RESURRECT)
						{
							// check target is not in a active siege zone
							if (target.isInsideZone(L2Character.ZONE_SIEGE))
							{
								condGood = false;
								player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
							}

							if (targetPlayer != null)
							{
								if (targetPlayer.isReviveRequested())
								{
									if (targetPlayer.isRevivingPet())
									{
										player.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									}
									else
									{
										player.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
									}
									condGood = false;
								}
							}
							else if (targetPet != null)
							{
								if (targetPet.getOwner() != player)
								{
									condGood = false;
									player.sendMessage("You are not the owner of this pet");
								}
							}
						}

						if (condGood)
						{
							if (!onlyFirst)
							{
								targetList.add(target);
								return targetList.toArray(new L2Object[targetList.size()]);
							}
							else
							{
								return new L2Character[]{target};
							}

						}
					}
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

				return null;
			}
			case TARGET_CORPSE_MOB:
			{
				if (!(target.isAttackable) || !target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}

				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				else
				{
					return new L2Character[]{target};
				}

			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if (!(target.isAttackable) || !target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}

				if (!onlyFirst)
				{
					targetList.add(target);
				}
				else
				{
					return new L2Character[]{target};
				}

				boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
				L2PcInstance src = null;

				if (activeChar.isPlayer)
				{
					src = (L2PcInstance) activeChar;
				}

				L2PcInstance trg;

				int radius = getSkillRadius();

				if (activeChar.getKnownList() != null)
				{
					for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
					{
						if (obj == null)
						{
							continue;
						}

						if (!(obj.isAttackable || obj.isPlayable) || ((L2Character) obj).isDead() || obj == activeChar)
						{
							continue;
						}

						if (!Util.checkIfInRange(radius, target, obj, true))
						{
							continue;
						}

						if (!GeoEngine.canSeeTarget(activeChar, obj, activeChar.isFlying()))
						{
							continue;
						}

						if (obj.isPlayer && src != null)
						{
							trg = (L2PcInstance) obj;

							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}

							if (trg.isInsideZone(L2Character.ZONE_PEACE))
							{
								continue;
							}

							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}

								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}

								if (!src.checkPvpSkill(obj, this))
								{
									continue;
								}
							}
						}
						if (obj.isSummon && src != null)
						{
							trg = ((L2Summon) obj).getOwner();

							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}

							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}

								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}

								if (!src.checkPvpSkill(trg, this))
								{
									continue;
								}
							}

							if (((L2Summon) obj).isInsideZone(L2Character.ZONE_PEACE))
							{
								continue;
							}
						}

						targetList.add((L2Character) obj);
					}
				}

				if (targetList.size() == 0)
				{
					return null;
				}

				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target.isDoor) && !(target instanceof L2ChestInstance))
				//activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				{
					return null;
				}

				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				else
				{
					return new L2Character[]{target};
				}

			}
			case TARGET_ITEM:
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target type of skill is not currently handled");
				activeChar.sendPacket(sm);
				return null;
			}
			case TARGET_UNDEAD:
			{
				if (target.isNpc || target.isSummonInstance)
				{
					if (!target.isUndead() || target.isDead())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return null;
					}

					if (!onlyFirst)
					{
						targetList.add(target);
					}
					else
					{
						return new L2Character[]{target};
					}

					return targetList.toArray(new L2Object[targetList.size()]);
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
			}
			case TARGET_AREA_UNDEAD:
			{
				L2Character cha;

				int radius = getSkillRadius();

				if (getCastRange() >= 0 && (target.isNpc || target.isSummonInstance) && target.isUndead() && !target.isAlikeDead())
				{
					cha = target;

					if (!onlyFirst)
					{
						targetList.add(cha); // Add target to target list
					}
					else
					{
						return new L2Character[]{cha};
					}

				}
				else
				{
					cha = activeChar;
				}

				if (cha != null && cha.getKnownList() != null)
				{
					for (L2Object obj : cha.getKnownList().getKnownObjects().values())
					{
						if (obj == null)
						{
							continue;
						}

						if (obj.isNpc)
						{
							target = (L2NpcInstance) obj;
						}
						else if (obj.isSummonInstance)
						{
							target = (L2SummonInstance) obj;
						}
						else
						{
							continue;
						}

						if (!GeoEngine.canSeeTarget(activeChar, target, activeChar.isFlying()))
						{
							continue;
						}

						if (!target.isAlikeDead()) // If target is not dead/fake death and not self
						{
							if (!target.isUndead())
							{
								continue;
							}

							if (!Util.checkIfInRange(radius, cha, obj, true))
							{
								continue;
							}

							if (!onlyFirst)
							{
								targetList.add((L2Character) obj); // Add obj to target lists
							}
							else
							{
								return new L2Character[]{(L2Character) obj};
							}
						}
					}
				}

				if (targetList.size() == 0)
				{
					return null;
				}

				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target != null && target.isSummon)
				{
					L2Summon targetSummon = (L2Summon) target;
					if (activeChar.isPlayer && activeChar.getPet() != targetSummon && !targetSummon.isDead() && (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0 || targetSummon.getOwner().isInDuel()) || targetSummon.getOwner().isInsideZone(L2Character.ZONE_PVP) && activeChar.isInsideZone(L2Character.ZONE_PVP))
					{
						return new L2Character[]{targetSummon};
					}
				}
				return null;
			}
			case TARGET_SIEGE:
			{
				if (target != null && !target.isDead() && (target.isDoor || target instanceof L2ControlTowerInstance))
				{
					return new L2Character[]{target};
				}
				return null;
			}
			case TARGET_TYRANNOSAURUS:
			{
				if (target.isMonster && ((L2MonsterInstance) target).getNpcId() == 22217 || ((L2MonsterInstance) target).getNpcId() == 22216 || ((L2MonsterInstance) target).getNpcId() == 22215)
				{
					return new L2Character[]{target};
				}
				return null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				if (target != null && target.isDead())
				{
					return new L2Character[]{target};
				}
				return null;
			}
			default:
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target type of skill is not currently handled");
				activeChar.sendPacket(sm);
				return null;
			}
		}//end switch
	}

	public final L2Object[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}

	public final L2Object getFirstOfTargetList(L2Character activeChar)
	{
		L2Object[] targets;

		targets = getTargetList(activeChar, true);
		if (targets == null || targets.length == 0)
		{
			return null;
		}

		else
		{
			return targets[0];
		}
	}

	public final Func[] getStatFuncs(L2Effect effect, L2Character player)
	{
		if (!(player.isPlayer) && !(player.isAttackable) && !(player.isSummon))
		{
			return _emptyFunctionSet;
		}

		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}

		List<Func> funcs = new FastList<Func>();

		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.skill = this;
			Func f = t.getFunc(env, this); // skill is owner

			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.size() == 0)
		{
			return _emptyFunctionSet;
		}

		return funcs.toArray(new Func[funcs.size()]);
	}

	public boolean hasEffects()
	{
		return _effectTemplates != null && _effectTemplates.length > 0;
	}

	public final L2Effect[] getEffects(L2Character effector, L2Character effected)
	{
		if (isPassive())
		{
			return _emptyEffectSet;
		}

		if (_effectTemplates == null)
		{
			return _emptyEffectSet;
		}

		if (effector != effected && effected.isInvul())
		{
			return _emptyEffectSet;
		}

		List<L2Effect> effects = new FastList<L2Effect>();

		boolean skillMastery = false;

		if (!isToggle() && Formulas.getInstance().calcSkillMastery(effector))
		{
			skillMastery = true;
		}

		if (getSkillType() == SkillType.BUFF)
		{
			for (L2Effect ef : effector.getAllEffects())
			{
				if (ef == null)
				{
					continue;
				}

				if (ef.getSkill().getId() == getId() && ef.getSkill().getLevel() > getLevel())
				{
					return _emptyEffectSet;
				}
			}
		}

		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effected;
			env.skill = this;
			env.skillMastery = skillMastery;
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				effects.add(e);
			}
		}

		if (effects.size() == 0)
		{
			return _emptyEffectSet;
		}

		return effects.toArray(new L2Effect[effects.size()]);
	}

	public final L2Effect[] getEffectsSelf(L2Character effector)
	{
		if (isPassive())
		{
			return _emptyEffectSet;
		}

		if (_effectTemplatesSelf == null)
		{
			return _emptyEffectSet;
		}

		List<L2Effect> effects = new FastList<L2Effect>();

		for (EffectTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effector;
			env.skill = this;
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				//Implements effect charge
				if (e.getEffectType() == L2Effect.EffectType.CHARGE)
				{
					env.skill = SkillTable.getInstance().getInfo(8, effector.getSkillLevel(8));
					EffectCharge effect = (EffectCharge) env.target.getFirstEffect(L2Effect.EffectType.CHARGE);
					if (effect != null)
					{
						int effectcharge = effect.getLevel();
						if (effectcharge < _numCharges)
						{
							effectcharge++;
							effect.addNumCharges(effectcharge);
							if (env.target.isPlayer)
							{
								env.target.sendPacket(new EtcStatusUpdate((L2PcInstance) env.target));
								SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
								sm.addNumber(effectcharge);
								env.target.sendPacket(sm);
							}
						}
					}
					else
					{
						effects.add(e);
					}
				}
				else
				{
					effects.add(e);
				}
			}
		}
		if (effects.size() == 0)
		{
			return _emptyEffectSet;
		}

		return effects.toArray(new L2Effect[effects.size()]);
	}

	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]{f};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]{effect};
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]{effect};
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}

	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			_itemPreCondition = c;
		}
		else
		{
			_preCondition = c;
		}
	}

	public boolean checkPartyClan(L2Character activeChar, L2Object target)
	{
		if (activeChar.isPlayer && target.isPlayer)
		{
			L2PcInstance targetChar = (L2PcInstance) target;
			L2PcInstance activeCh = (L2PcInstance) activeChar;

			if (activeCh.isInOlympiadMode() && activeCh.isOlympiadStart() && targetChar.isInOlympiadMode() && targetChar.isOlympiadStart())
			{
				return false;
			}

			if (activeCh.isInDuel() && targetChar.isInDuel() && activeCh.getDuelId() == targetChar.getDuelId())
			{
				return false;
			}

			if (activeCh.getParty() != null && targetChar.getParty() != null && //Is in the same party???
					activeCh.getParty().getPartyLeaderOID() == targetChar.getParty().getPartyLeaderOID())
			{
				return true;
			}

			if (activeCh.getClan() != null && targetChar.getClan() != null && //Is in the same clan???
					activeCh.getClan().getClanId() == targetChar.getClan().getClanId())
			{
				return true;
			}
		}
		return false;
	}

	public static boolean checkForAreaOffensiveSkills(L2Character caster, L2Character target, L2Skill skill, boolean sourceInArena)
	{
		if (target == null || target.isDead() || target == caster)
		{
			return false;
		}

		L2PcInstance player = null;
		if (caster.isPlayer)
		{
			player = (L2PcInstance) caster;
		}
		if (caster.isSummon)
		{
			player = ((L2Summon) caster).getOwner();
		}

		L2PcInstance targetPlayer = null;
		if (target.isPlayer)
		{
			targetPlayer = (L2PcInstance) target;
		}
		if (target.isSummon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}

		if (player != null && targetPlayer != null)
		{
			if (targetPlayer == caster || targetPlayer == player)
			{
				return false;
			}

			if (targetPlayer.getAppearance().getInvisible())
			{
				return false;
			}

			if (skill.isOffensive() && player.getSiegeState() > 0 && player.isInsideZone(L2Character.ZONE_SIEGE) && player.getSiegeState() == targetPlayer.getSiegeState())
			{
				return false;
			}

			if (target.isInsideZone(L2Character.ZONE_PEACE))
			{
				return false;
			}

			if (player.isInOlympiadMode() && !player.isOlympiadStart())
			{
				return false;
			}

			if (player.isInParty() && targetPlayer.isInParty())
			{
				// Same party
				if (player.getParty().getPartyLeaderOID() == targetPlayer.getParty().getPartyLeaderOID())
				{
					return false;
				}

				// Same commandchannel
				if (player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel())
				{
					return false;
				}
			}

			if (!sourceInArena && !(targetPlayer.isInsideZone(L2Character.ZONE_PVP) && !targetPlayer.isInsideZone(L2Character.ZONE_SIEGE)))
			{
				if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId())
				{
					return false;
				}

				if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId())
				{
					return false;
				}

				if (!player.checkPvpSkill(targetPlayer, skill))
				{
					return false;
				}
			}
		}

		return GeoEngine.canSeeTarget(caster, target, caster.isFlying());

	}

	@Override
	public String toString()
	{
		return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
}
