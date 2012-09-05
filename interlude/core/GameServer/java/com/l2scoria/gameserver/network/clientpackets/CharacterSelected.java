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
import com.l2scoria.crypt.nProtect;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.network.L2GameClient.GameClientState;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.CharSelected;
import org.apache.log4j.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends L2GameClientPacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	private static Logger _log = Logger.getLogger(CharacterSelected.class.getName());

	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		/*_unk1 = */readH();
		/*_unk2 = */readD();
		/*_unk3 = */readD();
		/*_unk4 = */readD();
	}

	@Override
	protected void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will
		// be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to
		// be a  [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		//playLogFile(getConnection()); // try to play log file

		// we should always be abble to acquire the lock
		// but if we cant lock then nothing should be done (ie repeated packet)
		if(getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if(getClient().getActiveChar() == null)
				{
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if(Config.DEBUG)
					{
						_log.info("selected slot:" + _charSlot);
					}

					//load up character from disk
					L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);

					if(cha == null)
					{
						_log.fatal("Character could not be loaded (slot:" + _charSlot + ")");
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					if(cha.getAccessLevel().getLevel() < 0)
					{
						cha.deleteMe();
						return;
					}

					cha.setClient(getClient());
					getClient().setActiveChar(cha);
					nProtect.getInstance().sendRequest(getClient());
					getClient().setState(GameClientState.IN_GAME);
					CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
					sendPacket(cs);

				}
			}
			catch(Exception e)
			{
				//never happen :)
				e.printStackTrace();
			}
			finally
			{
				getClient().getActiveCharLock().unlock();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2scoria.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}
}
