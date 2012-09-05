package com.l2scoria.gameserver.model.entity.event.CTF;

import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.taskmanager.Task;
import com.l2scoria.gameserver.taskmanager.TaskManager;
import org.apache.log4j.Logger;


public final class TaskCtfStart extends Task
{
	private static final Logger _log = Logger.getLogger(TaskCtfStart.class.getName());

	@Override
	public String getName()
	{
		return CTF.getInstance().getName();
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		if (CTF.getInstance() != null)
		{
			if (CTF.getInstance().getState() == GameEvent.STATE_INACTIVE)
			{
				CTF.getInstance().start();
				_log.info("CTF Event started by Global Task Manager");
			}
		}
	}
}