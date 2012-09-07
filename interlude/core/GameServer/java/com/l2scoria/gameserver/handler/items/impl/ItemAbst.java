package com.l2scoria.gameserver.handler.items.impl;

import com.l2scoria.gameserver.handler.items.IItemHandler;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.actor.instance.L2ItemInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;
import org.apache.log4j.Logger;

/**
 * @author Akumu
 * @date 17:13/06.09.12
 */
public abstract class ItemAbst implements IItemHandler
{
	protected int[] _items = null;

	protected final static Logger _log = Logger.getLogger(ItemAbst.class);

	protected boolean _notWhenSkillsDisabled;
	protected boolean _notWhenMovementDisabled;
	protected boolean _notOnOlympiad;
	protected boolean _notCasting;
	protected boolean _notSitting;
	protected boolean _notInCombat;
	protected boolean _notInObservationMode;
	protected boolean _notInPvP;
	protected boolean _playerUseOnly;
	protected boolean _summonOnly;
	protected boolean _requiresActingPlayer;

	// target related
	protected boolean _requiresTarget;
	protected boolean _targetNotDead;
	protected int _minInteractionDistance;

	public boolean useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(playable == null)
		{
			return false;
		}

		// только игроки
		if(_playerUseOnly)
		{
			// если не игрок
			if(!playable.isPlayer)
			{
				return true;
			}

			// если сидит
			if(_notSitting && playable.getPlayer().isSitting())
			{
				playable.getPlayer().sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
				playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// в режиме обзора
			if(_notInObservationMode && playable.getPlayer().inObserverMode())
			{
				playable.getPlayer().sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// в пвп
			if(_notInPvP && (playable.getPlayer().isInDuel() || playable.getPlayer().getPvpFlag() != 0))
			{
				playable.getPlayer().sendMessage("You can't do that in combat."); //TODO найти подходящий SYSMSG
				playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		// только игровые объекты (чар, суммон, пет и тд.)
		if(_requiresActingPlayer && playable.getPlayer() == null)
		{
			return false;
		}

		// только для сумонов
		if(_summonOnly && !(playable.isSummon || playable.isSummonInstance))
		{
			playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// необходима цель
		if(_requiresTarget)
		{
			if(playable.getTarget() == null) // нету цели
			{
				playable.getPlayer().sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// минимальное расстояние для действия
			if(_minInteractionDistance > 0 && !playable.isInsideRadius(playable.getTarget(), _minInteractionDistance, false, false))
			{
				playable.getPlayer().sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
				playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// цель не мертва
			if(_targetNotDead && (!playable.getTarget().isCharacter || ((L2Character)playable.getTarget()).isDead()))
			{
				playable.getPlayer().sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		// нельзя кастовать
		if(_notCasting && playable.isCastingNow())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// в бою
		if(_notInCombat && playable.isInCombat())
		{
			playable.getPlayer().sendMessage("You can't do that in combat."); //TODO найти подходящий SYSMSG
			playable.getPlayer().sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// нельзя когда все скилы заблокированы
		if(_notWhenSkillsDisabled && playable.isAllSkillsDisabled())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if(_notWhenMovementDisabled && playable.isMovementDisabled())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// нельзя на олимпиаде
		if(_notOnOlympiad && playable.getPlayer().isInOlympiadMode())
		{
			playable.getPlayer().sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}

		return true;
	}

	public int[] getItemIds()
	{
		return _items;
	}
}
