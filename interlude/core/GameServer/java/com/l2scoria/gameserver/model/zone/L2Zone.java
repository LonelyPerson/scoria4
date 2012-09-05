package com.l2scoria.gameserver.model.zone;

import com.l2scoria.gameserver.model.L2Character;

/**
 * @author Akumu
 * @date 1:31/29.08.12
 */
public abstract class L2Zone
{
	protected abstract void onEnter(L2Character character);

	protected abstract void onExit(L2Character character);

	protected abstract void onDieInside(L2Character character);

	protected abstract void onReviveInside(L2Character character);
}
