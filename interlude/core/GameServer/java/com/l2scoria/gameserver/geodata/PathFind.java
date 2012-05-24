package com.l2scoria.gameserver.geodata;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.Location;
import com.l2scoria.gameserver.model.actor.instance.L2NpcInstance;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;

import java.util.ArrayList;

/**
 * @Author: Diamond & Drin
 * @Date: 27/04/2009
 */
public class PathFind
{
	private static final byte NSWE_NONE = 0, EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, NSWE_ALL = 15;

	private PathFindBuffers.PathFindBuffer buff;

	private ArrayList<Location> path;

	public PathFind(int x, int y, int z, int destX, int destY, int destZ, L2Object obj)
	{
		Location startpoint = Config.PATHFIND_BOOST == 0 ? new Location(x, y, z) : GeoEngine.moveCheckWithCollision(x, y, z, destX, destY, true);
		Location native_endpoint = new Location(destX, destY, destZ);
		Location endpoint = Config.PATHFIND_BOOST != 2 || Math.abs(destZ - z) > 200 ? native_endpoint.clone() : GeoEngine.moveCheckBackwardWithCollision(destX, destY, destZ, startpoint.x, startpoint.y, true);

		startpoint.world2geo();
		native_endpoint.world2geo();
		endpoint.world2geo();

		startpoint.z = GeoEngine.NgetHeight(startpoint.x, startpoint.y, startpoint.z);
		endpoint.z = GeoEngine.NgetHeight(endpoint.x, endpoint.y, endpoint.z);

		int xdiff = Math.abs(endpoint.x - startpoint.x);
		int ydiff = Math.abs(endpoint.y - startpoint.y);

		if (xdiff == 0 && ydiff == 0)
		{
			if (Math.abs(endpoint.z - startpoint.z) < 32)
			{
				path = new ArrayList<Location>();
				path.add(0, startpoint);
			}
			return;
		}

		if ((buff = PathFindBuffers.alloc(64 + 2 * Math.max(xdiff, ydiff), obj instanceof L2PlayableInstance, startpoint, endpoint, native_endpoint)) != null)
		{
			path = findPath();

			buff.free();

			if (obj instanceof L2NpcInstance)
			{
				L2NpcInstance npc = (L2NpcInstance) obj;
				npc.pathfindCount++;
				npc.pathfindTime += (System.nanoTime() - buff.useStartedNanos) / 1000000.0;
			}
		}
	}

	public ArrayList<Location> findPath()
	{
		buff.firstNode = PathFindBuffers.GeoNode.initNode(buff, buff.startpoint.x - buff.offsetX, buff.startpoint.y - buff.offsetY, buff.startpoint);
		buff.firstNode.closed = true;

		PathFindBuffers.GeoNode nextNode = buff.firstNode, finish = null;
		int i = buff.info.maxIterations;

		while (nextNode != null && i-- > 0)
		{
			if ((finish = handleNode(nextNode)) != null)
			{
				return tracePath(finish);
			}
			nextNode = getBestOpenNode();
		}

		return null;
	}

	private PathFindBuffers.GeoNode getBestOpenNode()
	{
		PathFindBuffers.GeoNode bestNodeLink = null;
		PathFindBuffers.GeoNode oldNode = buff.firstNode;
		PathFindBuffers.GeoNode nextNode = buff.firstNode.link;

		while (nextNode != null)
		{
			if (bestNodeLink == null || nextNode.score < bestNodeLink.link.score)
			{
				bestNodeLink = oldNode;
			}
			oldNode = nextNode;
			nextNode = oldNode.link;
		}

		if (bestNodeLink != null)
		{
			bestNodeLink.link.closed = true;
			PathFindBuffers.GeoNode bestNode = bestNodeLink.link;
			bestNodeLink.link = bestNode.link;
			if (bestNode == buff.currentNode)
			{
				buff.currentNode = bestNodeLink;
			}
			return bestNode;
		}

		return null;
	}

	private ArrayList<Location> tracePath(PathFindBuffers.GeoNode f)
	{
		ArrayList<Location> locations = new ArrayList<Location>();
		do
		{
			locations.add(0, f.getLoc());
			f = f.parent;
		} while (f.parent != null);
		return locations;
	}

	public PathFindBuffers.GeoNode handleNode(PathFindBuffers.GeoNode node)
	{
		PathFindBuffers.GeoNode result = null;

		int clX = node._x;
		int clY = node._y;
		short clZ = node._z;

		getHeightAndNSWE(clX, clY, clZ);
		short NSWE = buff.hNSWE[1];

		if (Config.PATHFIND_DIAGONAL)
		{
			// Юго-восток
			if ((NSWE & SOUTH) == SOUTH && (NSWE & EAST) == EAST)
			{
				getHeightAndNSWE(clX + 1, clY, clZ);
				if ((buff.hNSWE[1] & SOUTH) == SOUTH)
				{
					getHeightAndNSWE(clX, clY + 1, clZ);
					if ((buff.hNSWE[1] & EAST) == EAST)
					{
						result = getNeighbour(clX + 1, clY + 1, node, true);
						if (result != null)
						{
							return result;
						}
					}
				}
			}

			// Юго-запад
			if ((NSWE & SOUTH) == SOUTH && (NSWE & WEST) == WEST)
			{
				getHeightAndNSWE(clX - 1, clY, clZ);
				if ((buff.hNSWE[1] & SOUTH) == SOUTH)
				{
					getHeightAndNSWE(clX, clY + 1, clZ);
					if ((buff.hNSWE[1] & WEST) == WEST)
					{
						result = getNeighbour(clX - 1, clY + 1, node, true);
						if (result != null)
						{
							return result;
						}
					}
				}
			}

			// Северо-восток
			if ((NSWE & NORTH) == NORTH && (NSWE & EAST) == EAST)
			{
				getHeightAndNSWE(clX + 1, clY, clZ);
				if ((buff.hNSWE[1] & NORTH) == NORTH)
				{
					getHeightAndNSWE(clX, clY - 1, clZ);
					if ((buff.hNSWE[1] & EAST) == EAST)
					{
						result = getNeighbour(clX + 1, clY - 1, node, true);
						if (result != null)
						{
							return result;
						}
					}
				}
			}

			// Северо-запад
			if ((NSWE & NORTH) == NORTH && (NSWE & WEST) == WEST)
			{
				getHeightAndNSWE(clX - 1, clY, clZ);
				if ((buff.hNSWE[1] & NORTH) == NORTH)
				{
					getHeightAndNSWE(clX, clY - 1, clZ);
					if ((buff.hNSWE[1] & WEST) == WEST)
					{
						result = getNeighbour(clX - 1, clY - 1, node, true);
						if (result != null)
						{
							return result;
						}
					}
				}
			}
		}

		// Восток
		if ((NSWE & EAST) == EAST)
		{
			result = getNeighbour(clX + 1, clY, node, false);
			if (result != null)
			{
				return result;
			}
		}

		// Запад
		if ((NSWE & WEST) == WEST)
		{
			result = getNeighbour(clX - 1, clY, node, false);
			if (result != null)
			{
				return result;
			}
		}

		// Юг
		if ((NSWE & SOUTH) == SOUTH)
		{
			result = getNeighbour(clX, clY + 1, node, false);
			if (result != null)
			{
				return result;
			}
		}

		// Север
		if ((NSWE & NORTH) == NORTH)
		{
			result = getNeighbour(clX, clY - 1, node, false);
		}

		return result;
	}

	public PathFindBuffers.GeoNode getNeighbour(int x, int y, PathFindBuffers.GeoNode from, boolean d)
	{
		int nX = x - buff.offsetX, nY = y - buff.offsetY;
		if (nX >= buff.info.MapSize || nX < 0 || nY >= buff.info.MapSize || nY < 0)
		{
			return null;
		}

		boolean isOldNull = PathFindBuffers.GeoNode.isNull(buff.nodes[nX][nY]);
		if (!isOldNull && buff.nodes[nX][nY].closed)
		{
			return null;
		}

		PathFindBuffers.GeoNode n = isOldNull ? PathFindBuffers.GeoNode.initNode(buff, nX, nY, x, y, from._z, from) : buff.tempNode.reuse(buff.nodes[nX][nY], from);

		int height = Math.abs(n._z - from._z);

		if (height > Config.PATHFIND_MAX_Z_DIFF || n._nswe == NSWE_NONE)
		{
			return null;
		}

		double weight = d ? 1.414213562373095 * Config.WEIGHT0 : Config.WEIGHT0;

		if (n._nswe != NSWE_ALL || height > 16)
		{
			weight = Config.WEIGHT1;
		}
		else
		// Цикл только для удобства
		{
			while (buff.isPlayer || Config.SIMPLE_PATHFIND_FOR_MOBS)
			{
				getHeightAndNSWE(x + 1, y, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}

				getHeightAndNSWE(x - 1, y, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}

				getHeightAndNSWE(x, y + 1, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}

				getHeightAndNSWE(x, y - 1, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}

				break;
			}
		}

		int diffx = buff.endpoint.x - x;
		int diffy = buff.endpoint.y - y;
		//int diffx = Math.abs(buff.endpoint.x - x);
		//int diffy = Math.abs(buff.endpoint.y - y);
		int dz = Math.abs(buff.endpoint.z - n._z);

		n.moveCost += from.moveCost + weight;
		n.score = n.moveCost + (Config.PATHFIND_DIAGONAL ? Math.sqrt(diffx * diffx + diffy * diffy + dz * dz / 256) : Math.abs(diffx) + Math.abs(diffy) + dz / 16); // 256 = 16*16
		//n.score = n.moveCost + diffx + diffy + dz / 16;

		if (x == buff.endpoint.x && y == buff.endpoint.y && dz < 64)
		{
			return n; // ура, мы дошли до точки назначения :)
		}

		if (isOldNull)
		{
			if (buff.currentNode == null)
			{
				buff.firstNode.link = n;
			}
			else
			{
				buff.currentNode.link = n;
			}
			buff.currentNode = n;

		} // если !isOldNull, значит эта клетка уже присутствует, в n находится временный Node содержимое которого нужно скопировать
		else if (n.moveCost < buff.nodes[nX][nY].moveCost)
		{
			buff.nodes[nX][nY].copy(n);
		}

		return null;
	}

	private void getHeightAndNSWE(int x, int y, short z)
	{
		int nX = x - buff.offsetX, nY = y - buff.offsetY;
		if (nX >= buff.info.MapSize || nX < 0 || nY >= buff.info.MapSize || nY < 0)
		{
			buff.hNSWE[1] = NSWE_NONE; // Затычка
			return;
		}

		PathFindBuffers.GeoNode n = buff.nodes[nX][nY];
		if (n == null)
		{
			n = PathFindBuffers.GeoNode.initNodeGeo(buff, nX, nY, x, y, z);
		}

		buff.hNSWE[0] = n._z;
		buff.hNSWE[1] = n._nswe;
	}

	public ArrayList<Location> getPath()
	{
		return path;
	}
}
