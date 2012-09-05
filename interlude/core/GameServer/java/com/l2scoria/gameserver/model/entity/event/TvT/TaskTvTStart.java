package com.l2scoria.gameserver.model.entity.event.TvT;

import com.l2scoria.gameserver.model.entity.event.GameEvent;
import com.l2scoria.gameserver.taskmanager.Task;
import com.l2scoria.gameserver.taskmanager.TaskManager;
import org.apache.log4j.Logger;


public final class TaskTvTStart extends Task
{
	private static final Logger _log = Logger.getLogger(TaskTvTStart.class.getName());

	@Override
	public String getName()
	{
		return TvT.getInstance().getName();
	}

	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{

		if (TvT.getInstance() == null || TvT.getInstance().getState() != GameEvent.STATE_INACTIVE)
		{
			return;
		}
		_log.info("TeamVsTeam Event started by Global Task Manager");
		TvT.getInstance().start();
	}
}