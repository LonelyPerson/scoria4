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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package com.l2scoria.gameserver.handler.admincommandhandlers;

import com.l2scoria.Config;
import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;
import com.l2scoria.gameserver.scripting.L2ScriptEngineManager;
import org.apache.log4j.Logger;

import javax.script.ScriptException;
import java.io.File;
import java.util.StringTokenizer;

/**
 * @author Akumu, KidZor
 */

public class Script extends Admin
{
	private static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
	private static final Logger _log = Logger.getLogger(Script.class.getName());

	public Script()
	{
		_commands = new String[]{"admin_load_script"};
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(!super.useAdminCommand(command, activeChar))
		{
			return false;
		}

		if(command.startsWith("admin_load_script"))
		{
			File file;
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			String line = st.nextToken();

			try
			{
				file = new File(SCRIPT_FOLDER, line);

				if(file.isFile())
				{
					try
					{
						L2ScriptEngineManager.getInstance().executeScript(file);
					}
					catch(ScriptException e)
					{
						L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
					}
				}
				else
				{
					_log.warn("Failed loading: (" + file.getCanonicalPath() + " - Reason: doesnt exists or is not a file.");
				}
			}
			catch(Exception e)
			{
				//null
			}
			st = null;
		}

		return true;
	}
}
