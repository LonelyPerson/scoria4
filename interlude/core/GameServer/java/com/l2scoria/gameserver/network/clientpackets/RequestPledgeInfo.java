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
package com.l2scoria.gameserver.network.clientpackets;

import com.l2scoria.Config;
import com.l2scoria.gameserver.datatables.sql.ClanTable;
import com.l2scoria.gameserver.model.L2Clan;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.serverpackets.PledgeInfo;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPledgeInfo extends L2GameClientPacket
{
	private static final String _C__66_REQUESTPLEDGEINFO = "[C] 66 RequestPledgeInfo";

	private static Logger _log = Logger.getLogger(RequestPledgeInfo.class.getName());

	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		if(Config.DEBUG)
		{
			_log.info("infos for clan " + _clanId + " requested");
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		L2Clan clan = ClanTable.getInstance().getClan(_clanId);

		if(clan == null)
		{
			_log.warn("Clan data for clanId " + _clanId + " is missing");
			return; // we have no clan data ?!? should not happen
		}

		PledgeInfo pc = new PledgeInfo(clan);
		if(activeChar != null)
		{
			activeChar.sendPacket(pc);

			/*
			 * if (clan.getClanId() == activeChar.getClanId())
			 {
			 * activeChar.sendPacket(new PledgeShowMemberListDeleteAll());
			 * PledgeShowMemberListAll pm = new PledgeShowMemberListAll(clan,
			 * activeChar); activeChar.sendPacket(pm);
			 }
			 */
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__66_REQUESTPLEDGEINFO;
	}
}
