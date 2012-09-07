package com.l2scoria.gameserver.handler.admin.impl;

import com.l2scoria.gameserver.instancemanager.InstanceManager;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.model.entity.Instance;
import com.l2scoria.gameserver.network.SystemMessageId;
import com.l2scoria.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Akumu
 * @date 19:42/05.09.12
 */
public class InstanceControl extends AdminAbst
{
	public InstanceControl()
	{
		_commands = new String[] {"admin_setinstance", "admin_createinstance", "admin_destroyinstance", "admin_listinstances"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		String[] params = command.split(" ");

		if (command.startsWith("admin_createinstance"))
		{
			if (params.length < 2)
			{
				activeChar.sendMessage("Формат: //createinstance <id> <templatefile>");
			}
			else
			{
				try
				{
					int id = Integer.parseInt(params[1]);
					if (InstanceManager.getInstance().createInstanceFromTemplate(id, params[2]) && id < 300000)
					{
						activeChar.sendMessage("Инстанс создан");
						return true;
					}

					activeChar.sendMessage("Невозможно создать инстанс");
					return false;
				} catch (Exception e)
				{
					activeChar.sendMessage("Ошибка загрузки: " + params[2]);
					return false;
				}
			}
		}
		else if (command.startsWith("admin_listinstances"))
		{
			for (Instance temp : InstanceManager.getInstance().getInstances().values())
			{
				activeChar.sendMessage("Id: " + temp.getId() + " Name: " + temp.getName());
			}
		}
		else if (command.startsWith("admin_setinstance"))
		{
			try
			{
				int val = Integer.parseInt(params[1]);
				if (InstanceManager.getInstance().getInstance(val) == null)
				{
					activeChar.sendMessage("Инстанс " + val + " не существует");
					return false;
				}

				L2Object target = activeChar.getTarget();
				if (target == null || target.isSummon)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					return false;
				}

				target.setInstanceId(val);
				if (target.isPlayer)
				{
					L2PcInstance player = (L2PcInstance) target;
					player.sendMessage("GM отправил Вас в инстанс:" + val);
					InstanceManager.getInstance().getInstance(val).addPlayer(player.getObjectId());
					player.teleToLocation(player.getX(), player.getY(), player.getZ());
					L2Summon pet = player.getPet();
					if (pet != null)
					{
						pet.setInstanceId(val);
						pet.teleToLocation(pet.getX(), pet.getY(), pet.getZ());
						player.sendMessage("GM отправил Вашего питомца " + pet.getName() + " в инстанс:" + val);
					}
				}
				activeChar.sendMessage("Игрок " + target.getName() + " отправлен в инстанс " + target.getInstanceId());

			} catch (Exception e)
			{
				activeChar.sendMessage("Используйте: //setinstance id");
				return false;
			}
		}
		else if (command.startsWith("admin_destroyinstance"))
		{
			try
			{
				int val = Integer.parseInt(params[1]);
				InstanceManager.getInstance().destroyInstance(val);
				activeChar.sendMessage("Инстанс удален");

			} catch (Exception e)
			{
				activeChar.sendMessage("Используйте: //destroyinstance id");
				return false;
			}
		}

		return true;
	}
}
