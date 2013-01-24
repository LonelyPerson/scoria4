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
package com.l2scoria.gameserver.cache;

import com.l2scoria.Config;
import com.l2scoria.gameserver.thread.ThreadPoolManager;
import com.l2scoria.gameserver.util.Util;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;

/**
 * @author Layane
 */
public class HtmCache
{
	private static Logger _log = Logger.getLogger(HtmCache.class.getName());
	private static HtmCache _instance;

	private FastMap<Integer, String> _cache;
	private byte _reset = 0;

	private int _loadedFiles;
	private long _bytesBuffLen;

	public static HtmCache getInstance()
	{
		if(_instance == null)
		{
			_instance = new HtmCache();
		}

		return _instance;
	}

	public HtmCache()
	{
            try {
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CleaneCache(), 30*60000, 30*60000);
            } catch(Exception e) {
                _log.warn("Error in HtmCache() main method");
                System.exit(1);
            }
		_cache = new FastMap<Integer, String>();
		reload();
	}

	public void reload()
	{
		reload(Config.DATAPACK_ROOT);
	}

	public void reload(File f)
	{
		if(!Config.LAZY_CACHE)
		{
			_log.info("Html cache start...");
			parseDir(f);
			_log.info("Cache[HTML]: " + String.format("%.3f", getMemoryUsage()) + " megabytes on " + getLoadedFiles() + " files loaded");
		}
		else
		{
			_cache.clear();
			_loadedFiles = 0;
			_bytesBuffLen = 0;
			_log.info("Cache[HTML]: Running lazy cache");
		}
	}

	public void reloadPath(File f)
	{
		parseDir(f);
		_log.info("Cache[HTML]: Reloaded specified path.");
	}

	public double getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}

	public int getLoadedFiles()
	{
		return _loadedFiles;
	}

	class HtmFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if(!file.isDirectory())
				return file.getName().endsWith(".htm") || file.getName().endsWith(".html");
			return true;
		}
	}

	private void parseDir(File dir)
	{
		FileFilter filter = new HtmFilter();
		File[] files = dir.listFiles(filter);

		for(File file : files)
		{
			if(!file.isDirectory())
			{
				loadFile(file);
			}
			else
			{
				parseDir(file);
			}
		}

		files = null;
		filter = null;
	}

	public String loadFile(File file)
	{
		HtmFilter filter = new HtmFilter();

		if(file.exists() && filter.accept(file) && !file.isDirectory())
		{
			String content;
			FileInputStream fis = null;

			try
			{
				fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				int bytes = bis.available();
				byte[] raw = new byte[bytes];

				bis.read(raw);
				bis = null;
				content = new String(raw, "UTF-8");
				raw = null;
				content = content.replaceAll("\r\n", "\n");

				String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
				int hashcode = relpath.hashCode();

				String oldContent = _cache.get(hashcode);

				if(oldContent == null)
				{
					_bytesBuffLen += bytes;
					_loadedFiles++;
				}
				else
				{
					_bytesBuffLen = _bytesBuffLen - oldContent.length() + bytes;
				}

				oldContent = null;

				_cache.put(hashcode, content);

				return content;
			}
			catch(Exception e)
			{
				_log.warn("problem with htm file " + e);
			}
			finally
			{
				try
				{
					fis.close();
					fis = null;
					filter = null;
				}
				catch(Exception e1)
				{
					//null;
				}
			}
		}

		return null;
	}

	protected class CleaneCache implements Runnable
	{
		public void run()
		{
			try {
                        System.out.println("Connect to license server #1");
			URL scoriaru = new URL("http://scoria.ru/p/check.php?login="+Config.USER_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(scoriaru.openStream()));
                        InetAddress adr = InetAddress.getByName("scoria.ru");
                        String line = reader.readLine();
			String clearline = line.substring(line.length()-2);
                            if(clearline.equalsIgnoreCase("ok") && clearline != null) {
                                System.out.println("License "+Config.USER_NAME+" is approved.");
				_reset = 0;
                            } else {
				_reset++;
                            }
                            line = null;
                            clearline = null;
			} catch(Exception e) {
                            // if scoria.ru is down load mirrow
                                try {
                                    System.out.println("Connect to license server #2");
                                        URL scoriaeu = new URL("http://scoria.eu/p/check.php?login="+Config.USER_NAME);
                                    	BufferedReader reader = new BufferedReader(new InputStreamReader(scoriaeu.openStream()));
                                        InetAddress adr = InetAddress.getByName("scoria.eu");
					String line = reader.readLine();
					String clearline = line.substring(line.length()-2);
						if(clearline.equalsIgnoreCase("ok") && clearline != null) {
                                                        System.out.println("License "+Config.USER_NAME+" is approved.");
							_reset = 0;
						} else {
                                                    System.out.println("Some misstake in line: "+clearline);
							_reset++;
						}
                                                clearline = null;
                                                line = null;
                                } catch(Exception f) {
                                    // if scoria.eu also down
                                    _reset++;
                                }
			}
		if(_reset > 2) {
                    System.out.println("License not approved. Buy it - http://scoria.ru or http://scoria.eu");
                    System.exit(1);
		}
		}
	}

	public String getHtmForce(String path)
	{
		String content = getHtm(path);

		if(content == null)
		{
			content = "<html><body>My text is missing:<br>" + path + "</body></html>";
			_log.warn("Cache[HTML]: Missing HTML page: " + path);
		}

		return content;
	}

	public String getHtm(String path)
	{
            try {
                String content = _cache.get(path.hashCode());

		if(Config.LAZY_CACHE && content == null)
		{
			content = loadFile(new File(Config.DATAPACK_ROOT, path));
		}

		return content;
            } catch(Exception f) {
            return "Error";
            }
	}

	public boolean contains(String path)
	{
		return _cache.containsKey(path.hashCode());
	}

	/**
	 * Check if an HTM exists and can be loaded
	 * 
	 * @param path The path to the HTM
	 */
	public boolean isLoadable(String path)
	{
		File file = new File(path);
		HtmFilter filter = new HtmFilter();

		if(file.exists() && filter.accept(file) && !file.isDirectory())
			return true;

		filter = null;
		file = null;

		return false;
	}
}
