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
package com.l2scoria.gameserver.skills.effects;

import com.l2scoria.gameserver.model.L2Effect;
import com.l2scoria.gameserver.skills.Env;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
final class EffectImobileBuff extends L2Effect
{

	public EffectImobileBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.IMMOBILE;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		for(L2Effect e : getEffector().getAllEffects())
			if(e.getEffectType() == EffectType.IMMOBILE && e != this)
			{
				e.exit();
			}
		getEffector().setIsImobilised(true);
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffector().setIsImobilised(false);
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
