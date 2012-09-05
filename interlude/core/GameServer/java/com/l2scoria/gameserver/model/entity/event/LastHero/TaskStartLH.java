package com.l2scoria.gameserver.model.entity.event.LastHero;

import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.taskmanager.Task;
import com.l2scoria.gameserver.taskmanager.TaskManager;

public class TaskStartLH extends Task
{

	@Override
	public String getName()
	{
		return LastHero.getInstance().getName();
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		if (LastHero.getInstance() != null && LastHero.getInstance().getState() == GameEvent.STATE_INACTIVE)
		{
			LastHero.getInstance().start();
		}
	}
}