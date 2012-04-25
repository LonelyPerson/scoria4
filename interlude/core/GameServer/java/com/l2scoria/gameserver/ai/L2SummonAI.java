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
package com.l2scoria.gameserver.ai;

import static com.l2scoria.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2scoria.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2scoria.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2scoria.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2scoria.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2scoria.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;

import com.l2scoria.gameserver.model.L2Object;
import com.l2scoria.gameserver.model.L2Skill;
import com.l2scoria.gameserver.model.L2Summon;
import com.l2scoria.gameserver.model.L2Character;
import com.l2scoria.gameserver.model.L2Character.AIAccessor;
import com.l2scoria.gameserver.model.actor.instance.L2SiegeSummonInstance;

public class L2SummonAI extends L2CharacterAI
{

	private boolean _thinking; // to prevent recursive thinking
	private boolean _startFollow = ((L2Summon) _actor).getFollowStatus();
	private L2Character _lastAttack = null;

	public L2SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		_startFollow = false;
		onIntentionActive();
	}

	@Override
	protected void onIntentionActive()
	{
		L2Summon summon = (L2Summon) _actor;

		if(_startFollow)
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}

		summon = null;
	}

	private void thinkAttack()
	{
		if(checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}

		if (_actor instanceof L2SiegeSummonInstance && ((L2SiegeSummonInstance)_actor).isOnSiegeMode())
			return;

		if(maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
			return;

		clientStopMoving(null);
		_accessor.doAttack(getAttackTarget());
	}

	private void thinkCast()
	{
		L2Summon summon = (L2Summon) _actor;

		if(checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}

		if(maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
			return;
		boolean val = _startFollow;

		clientStopMoving(null);
		_accessor.doCast(_skill);
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		_startFollow = val;
		summon = null;
	}

	private void thinkPickUp()
	{
		if(_actor.isAllSkillsDisabled())
			return;

		if(checkTargetLost(getTarget()))
			return;

		if(maybeMoveToPawn(getTarget(), 36))
			return;

		setIntention(AI_INTENTION_IDLE);
		((L2Summon.AIAccessor) _accessor).doPickupItem(getTarget());
	}

	private void thinkInteract()
	{
		if(_actor.isAllSkillsDisabled())
			return;

		if(checkTargetLost(getTarget()))
			return;

		if(maybeMoveToPawn(getTarget(), 36))
			return;

		setIntention(AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtThink()
	{
		if(_thinking || _actor.isAllSkillsDisabled())
			return;

		_thinking = true;

		try
		{
			if(getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if(getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if(getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if(getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if (getIntention() == AI_INTENTION_ATTACK || skill.isOffensive())
			_lastAttack = getAttackTarget();
		else
			_lastAttack = null;
		super.onIntentionCast(skill, target);
	}

	@Override
	protected void onEvtFinishCasting()
	{
		((L2Summon) _actor).setFollowStatus(_startFollow);
		if (_lastAttack != null)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, _lastAttack);
			_lastAttack = null;
		}

		super.onEvtFinishCasting();
	}

	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		
		switch (getIntention())
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			case AI_INTENTION_IDLE:
				((L2Summon) _actor).setFollowStatus(_startFollow);
				break;
		}
	}

	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}
}