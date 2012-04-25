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
package com.l2scoria.gameserver.model.entity.olympiad;

import java.util.Map;

import javolution.util.FastMap;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.util.L2FastList;
import com.l2scoria.util.random.Rnd;

class OlympiadManager implements Runnable
{
	private static OlympiadManager _instance;

	private Map<Integer, L2OlympiadGame> _olympiadInstances;
	private Map<Integer, OlympiadGameTask> _gamesQueue;

	public OlympiadManager()
	{
		_olympiadInstances = new FastMap<Integer, L2OlympiadGame>();
		_gamesQueue = new FastMap<Integer, OlympiadGameTask>();
	}
	
	public static OlympiadManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new OlympiadManager();
		}
		return _instance;
	}

	public synchronized void run()
	{
		if(Olympiad.getInstance().isOlympiadEnd())
		{
			return;
		}

		if(Olympiad.getInstance().inCompPeriod())
		{
			if(Olympiad.getInstance().getNobles() == null)
			{
				return;
			}

			//_compStarted = true;
			boolean classBasedCanStart = false;
			for(L2FastList<L2PcInstance> classList : Olympiad.getInstance().getClassBased().values())
			{
				if(classList.size()>= Config.ALT_OLY_CLASSED)
				{
					classBasedCanStart = true;
					break;
				}
			}
			if(_gamesQueue.size() > 0 || classBasedCanStart || Olympiad.getInstance().getNonClassBased().size() >= Config.ALT_OLY_NONCLASSED)
			{
				//set up the games queue
				for(int i = 0; i < Olympiad.STADIUMS.length; i++)
				{
					if(!existNextOpponents(Olympiad.getInstance().getNonClassBased()) && !existNextOpponents(getRandomClassList(Olympiad.getInstance().getClassBased())))
					{
						break;
					}
					if(Olympiad.STADIUMS[i].isFreeToUse())
					{
						if(existNextOpponents(Olympiad.getInstance().getNonClassBased()))
						{
							try
							{
								_olympiadInstances.put(i, new L2OlympiadGame(i, OlympiadType.NON_CLASSED, nextOpponents(Olympiad.getInstance().getNonClassBased()), Olympiad.STADIUMS[i].getCoordinates()));
								_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
								Olympiad.STADIUMS[i].setStadiaBusy();
							}
							catch(Exception ex)
							{
								if(_olympiadInstances.get(i) != null)
								{
									for(L2PcInstance player : _olympiadInstances.get(i).getPlayers())
									{
										player.sendMessage("Your olympiad registration was canceled due to an error");
										player.setIsInOlympiadMode(false);
										player.setIsOlympiadStart(false);
										player.setOlympiadSide(-1);
										player.setOlympiadGameId(-1);
									}
									_olympiadInstances.remove(i);
								}
								if(_gamesQueue.get(i) != null)
								{
									_gamesQueue.remove(i);
								}
								Olympiad.STADIUMS[i].setStadiaFree();

								//try to reuse this stadia next time
								i--;
							}
						}
						else if(existNextOpponents(getRandomClassList(Olympiad.getInstance().getClassBased())))
						{
							try
							{
								_olympiadInstances.put(i, new L2OlympiadGame(i, OlympiadType.CLASSED, nextOpponents(getRandomClassList(Olympiad.getInstance().getClassBased())), Olympiad.STADIUMS[i].getCoordinates()));
								_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
								Olympiad.STADIUMS[i].setStadiaBusy();
							}
							catch(Exception ex)
							{
								if(_olympiadInstances.get(i) != null)
								{
									for(L2PcInstance player : _olympiadInstances.get(i).getPlayers())
									{
										player.sendMessage("Your olympiad registration was canceled due to an error");
										player.setIsInOlympiadMode(false);
										player.setIsOlympiadStart(false);
										player.setOlympiadSide(-1);
										player.setOlympiadGameId(-1);
									}
									_olympiadInstances.remove(i);
								}
								if(_gamesQueue.get(i) != null)
								{
									_gamesQueue.remove(i);
								}
								Olympiad.STADIUMS[i].setStadiaFree();

								//try to reuse this stadia next time
								i--;
							}
						}
					}
				}
				int _gamesQueueSize = _gamesQueue.size();
				for(int i = 0; i < _gamesQueueSize; i++)
				{
					if(_gamesQueue.get(i) == null || _gamesQueue.get(i).isTerminated() || _gamesQueue.get(i)._game == null)
					{
						if(_gamesQueue.containsKey(i))
						{
							//removes terminated games from the queue
							try
							{
								_olympiadInstances.remove(i);
								_gamesQueue.remove(i);
								Olympiad.STADIUMS[i].setStadiaFree();
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							_gamesQueueSize = _gamesQueueSize + 1;
						}
					}
					else if(_gamesQueue.get(i) != null && !_gamesQueue.get(i).isStarted())
					{
						//start new games
						Thread T = new Thread(_gamesQueue.get(i));
						T.start();
					}
				}
			}
		}
		else
		{
			//when comp time finish wait for all games terminated before execute the cleanup code
			boolean allGamesTerminated = false;
			//wait for all games terminated
			while(!allGamesTerminated)
			{
				try
				{
					wait(30000);
				}
				catch(InterruptedException e)
				{
					//null
				}
				if(_gamesQueue.size() == 0)
				{
					allGamesTerminated = true;
				}
				else
				{
					for(OlympiadGameTask game : _gamesQueue.values())
					{
						if (!game.isTerminated())
						{
							allGamesTerminated = false;
							break;
						}
						allGamesTerminated = true;
					}
				}
			}
			//when all games terminated clear all 
			_gamesQueue.clear();
			_olympiadInstances.clear();
			
			Olympiad.getInstance().clearClassBased();
			Olympiad.getInstance().clearNonClassBased();

			Olympiad.setBattleStarted(false);
		}
	}

	protected L2OlympiadGame getOlympiadInstance(int index)
	{
		if(_olympiadInstances != null && _olympiadInstances.size() > 0)
			return _olympiadInstances.get(index);
		return null;
	}

	public Map<Integer, L2OlympiadGame> getOlympiadGames()
	{
		return _olympiadInstances;
	}

	private L2FastList<L2PcInstance> getRandomClassList(Map<Integer, L2FastList<L2PcInstance>> list)
	{
		if(list.size() == 0)
			return null;

		Map<Integer, L2FastList<L2PcInstance>> tmp = new FastMap<Integer, L2FastList<L2PcInstance>>();
		int tmpIndex = 0;
		for(L2FastList<L2PcInstance> l : list.values())
		{
			if(l.size() >= Config.ALT_OLY_CLASSED)
			{
				tmp.put(tmpIndex, l);
				tmpIndex++;
			}
		}

		L2FastList<L2PcInstance> rndList = new L2FastList<L2PcInstance>();
		int classIndex = 0;
		if(tmp.size() == 1)
		{
			classIndex = 0;
		}
		else
		{
			classIndex = Rnd.nextInt(tmp.size());
		}
		rndList = tmp.get(classIndex);
		return rndList;
	}

	private L2FastList<L2PcInstance> nextOpponents(L2FastList<L2PcInstance> list)
	{
		L2FastList<L2PcInstance> opponents = new L2FastList<L2PcInstance>();
		if(list.size() == 0)
			return opponents;

		if(list.size() / 2 < 1)
			return opponents;

		int first = 0;
		int second = 0;

		if (Config.ALT_OLY_PORT_RANDOM)
		{
			first = Rnd.nextInt(list.size());
		}
		opponents.add(list.get(first));
		list.remove(first);

		if (Config.ALT_OLY_PORT_RANDOM)
		{
			second = Rnd.nextInt(list.size());
		}
		opponents.add(list.get(second));
		list.remove(second);

		return opponents;
	}

	private boolean existNextOpponents(L2FastList<L2PcInstance> list)
	{
		if(list == null)
			return false;
		if(list.size() == 0)
			return false;
		int loopCount = list.size() >> 1;

		if(loopCount < 1)
			return false;
		else
			return true;
	}
	
	public FastMap<Integer, String> getAllTitles()
	{
		FastMap<Integer, String> titles = new FastMap<Integer, String>();

		for(L2OlympiadGame instance : _olympiadInstances.values())
		{
			if (instance == null || instance._gamestarted != true)
				continue;

			titles.put(instance._stadiumID, instance.getTitle());
		}
		return titles;
	}
}