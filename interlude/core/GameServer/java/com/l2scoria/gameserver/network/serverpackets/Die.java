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
package com.l2scoria.gameserver.network.serverpackets;

import com.l2scoria.gameserver.managers.FortSiegeManager;
import com.l2scoria.gameserver.managers.SiegeManager;
import com.l2scoria.gameserver.model.L2Attackable;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.L2SiegeClan;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.siege.FortSiege;
import com.l2scoria.gameserver.model.entity.siege.Siege;

/**
 * sample 0b 952a1048 objectId 00000000 00000000 00000000 00000000 00000000 00000000 format dddddd rev 377 format
 * ddddddd rev 417
 * 
 * @version $Revision: 1.3.3 $ $Date: 2009/04/29 00:46:18 $
 */
public class Die extends L2GameServerPacket
{
	private static final String _S__0B_DIE = "[S] 06 Die";
	private int _charObjId;

	L2Character _activeChar;

	private boolean _sweepable;
	private boolean _inFunEvent;
	private boolean	_fallDown;

	private int					_showVillage;
	private int					_showClanhall;
	private int					_showCastle;
	private int					_showFlag;
	private int					_fixedres = 0;

	/**
	 * @param _characters
	 */
	public Die(L2Character cha)
	{
		_activeChar = cha;
		L2Clan clan = null;
		if (cha.isPlayer)
		{
			L2PcInstance player = (L2PcInstance) cha;
			clan = player.getClan();
			if (player.isInFunEvent())
				_inFunEvent = true;
			if (player._event!=null)
				_inFunEvent = !player._event.canTeleportOnDie(player);
			_fixedres = player.isGM()?1:0;
		}

		_charObjId = cha.getObjectId();
		_fallDown = cha.isDead();

		if (cha.isAttackable)
			_sweepable = ((L2Attackable) cha).isSweepActive();

		if (clan != null)
		{
			_showClanhall = clan.getHasHideout() <= 0 ? 0 : 1;
			_showCastle = clan.getHasCastle() <= 0 ? 0 : 1;

			L2SiegeClan siegeClan = null;
			boolean isInDefense = false;
			Siege siege = SiegeManager.getInstance().getSiege(_activeChar);
			if (siege != null && siege.getIsInProgress())
			{
				siegeClan = siege.getAttackerClan(clan);
				if (siegeClan == null && siege.checkIsDefender(clan))
					isInDefense = true;
			}
			else
			{
				FortSiege fsiege = FortSiegeManager.getInstance().getSiege(_activeChar);
				if (fsiege != null && fsiege.getIsInProgress())
				{
					siegeClan = fsiege.getAttackerClan(clan);
					if (siegeClan == null && fsiege.checkIsDefender(clan))
						isInDefense = true;
				}
			}

			_showFlag = (siegeClan == null || isInDefense || siegeClan.getFlag().size() <= 0) ? 0 : 1;
		}
		_showVillage = 1;

	}

	@Override
	protected final void writeImpl()
	{
		if (!_fallDown)
			return;

		writeC(0x6);
		writeD(_charObjId);
		writeD(_inFunEvent ? 0x00 : _showVillage);
		writeD(_inFunEvent ? 0x00 : _showClanhall);
		writeD(_inFunEvent ? 0x00 : _showCastle);
		writeD(_showFlag);
		writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
		writeD(_inFunEvent ? 0x00 :_fixedres );
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__0B_DIE;
	}
}
