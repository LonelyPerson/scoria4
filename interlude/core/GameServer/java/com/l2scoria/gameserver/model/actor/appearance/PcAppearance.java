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
package com.l2scoria.gameserver.model.actor.appearance;

import com.l2scoria.gameserver.model.actor.instance.L2PcInstance;

public class PcAppearance
{
	// =========================================================
	// Data Field
	private byte _face;
	private byte _hairColor;
	private byte _hairStyle;
	private boolean _sex; //Female true(1)
	/**
	 * true if the player is invisible
	 */
	private boolean _invisible = false;
	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int _nameColor = 0xFFFFFF;
	private int _baseNameColor = 0xFFFFFF;
	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int _titleColor = 0xFFFF77;
	private int _baseTitleColor = 0xFFFF77;
	private boolean _saveNameColor = true;
	private boolean _saveTitleColor = true;
	private L2PcInstance _owner;

	// =========================================================
	// Constructor
	public PcAppearance(byte Face, byte HColor, byte HStyle, boolean Sex)
	{
		_face = Face;
		_hairColor = HColor;
		_hairStyle = HStyle;
		_sex = Sex;
	}

	// =========================================================
	// Method - Public

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	public final byte getFace()
	{
		return _face;
	}

	/**
	 * @param byte value
	 */
	public final void setFace(int value)
	{
		_face = (byte) value;
	}

	public final byte getHairColor()
	{
		return _hairColor;
	}

	/**
	 * @param byte value
	 */
	public final void setHairColor(int value)
	{
		_hairColor = (byte) value;
	}

	public final byte getHairStyle()
	{
		return _hairStyle;
	}

	/**
	 * @param byte value
	 */
	public final void setHairStyle(int value)
	{
		_hairStyle = (byte) value;
	}

	public final boolean getSex()
	{
		return _sex;
	}

	/**
	 * @param boolean isfemale
	 */
	public final void setSex(boolean isfemale)
	{
		_sex = isfemale;
	}

	public void setInvisible()
	{
		_invisible = true;
	}

	public void setVisible()
	{
		_invisible = false;
	}

	public boolean getInvisible()
	{
		return _invisible;
	}

	public int getNameColor()
	{
		return _nameColor;
	}

	public int getOldNameColor()
	{
		return _baseNameColor;
	}

	public void setNameColor(int nameColor)
	{
		setNameColor(nameColor, true);
		_baseNameColor = nameColor;
	}

	public void setNameColor(int nameColor, boolean save)
	{
		_nameColor = nameColor;
		_saveNameColor = save;
	}

	public boolean getSaveName()
	{
		return _saveNameColor;
	}

	private int _eventTitleColor = 0;

	public int getTitleColor()
	{
		if (_owner._event != null && _owner._event.isRunning())
		{
			if (_eventTitleColor == 0)
			{
				_eventTitleColor = _titleColor;
			}
			return _eventTitleColor;
		}
		return _titleColor;
	}

	public int getOldTitleColor()
	{
		return _baseTitleColor;
	}

	public void setTitleColor(int titleColor)
	{
		setTitleColor(titleColor, true);
		_baseTitleColor = titleColor;
	}

	public void setTitleColor(int titleColor, boolean save)
	{
		_titleColor = titleColor;
		_saveTitleColor = save;
	}

	public boolean getSaveTitle()
	{
		return _saveTitleColor;
	}

	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(L2PcInstance owner)
	{
		_owner = owner;
	}

	/**
	 * @return Returns the owner.
	 */
	public L2PcInstance getOwner()
	{
		return _owner;
	}
}
