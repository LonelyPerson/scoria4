/* This program is free software; you can redistribute it and/or modify 
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
package com.l2scoria.gameserver.skills.effects;

import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2scoria.gameserver.skills.Env;

/**
 * @author eX1steam, l2scoria
 */
public class EffectProtectionBlessing extends L2Effect
{
	public EffectProtectionBlessing(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.PROTECTION_BLESSING;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		if(getEffected().isPlayable)
		{
			((L2PlayableInstance) getEffected()).startProtectionBlessing();
		}
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		if(getEffected().isPlayable)
		{
			((L2PlayableInstance) getEffected()).stopProtectionBlessing(this);
		}
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect 
		return false;
	}
}
