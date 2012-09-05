package com.l2scoria.gameserver.model.entity.event.DeathMatch;

import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.taskmanager.Task;
import com.l2scoria.gameserver.taskmanager.TaskManager;

/**
 * @author m095
 * @version 1.0
 */

public class TaskStartDM extends Task
{

	@Override
	public String getName()
	{
		return DeathMatch.getInstance().getName();
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		if (DeathMatch.getInstance() != null && DeathMatch.getInstance().getState() == GameEvent.STATE_INACTIVE)
		{
			DeathMatch.getInstance().start();
		}
	}
}