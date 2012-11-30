package com.l2scoria.gameserver.model.actor.instance;

import com.l2scoria.gameserver.ai.CtrlIntention;
import com.l2scoria.gameserver.cache.HtmCache;
import com.l2scoria.gameserver.datatables.sql.NpcTable;
import com.l2scoria.gameserver.model.L2World;
import com.l2scoria.gameserver.network.serverpackets.ActionFailed;
import com.l2scoria.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2scoria.gameserver.templates.L2NpcTemplate;
import com.l2scoria.gameserver.model.entity.event.TvT.TvT;
import com.l2scoria.gameserver.model.entity.event.LastHero.LastHero;
import com.l2scoria.gameserver.model.entity.event.CTF.CTF;
import com.l2scoria.gameserver.model.entity.event.DeathMatch.DeathMatch;
import com.l2scoria.gameserver.network.serverpackets.MyTargetSelected;
import com.l2scoria.gameserver.network.serverpackets.SocialAction;
import com.l2scoria.gameserver.network.serverpackets.ValidateLocation;
import com.l2scoria.util.random.Rnd;

import javolution.text.TextBuilder;

/**
 *
 * @author zenn
 */
public final class L2EventNpcInstance extends L2NpcInstance
{
    public static L2EventNpcInstance EventNpc = new L2EventNpcInstance(70005, NpcTable.getInstance().getTemplate(70005));
    
    	static
	{
		L2World.storeObject(EventNpc);
	}

	/**
	 * @param template
	 */
	public L2EventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
        
        @Override
	public void onAction(L2PcInstance player)
	{
            	if(!canTarget(player))
                    return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if(this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
				// to display a social action of the L2NpcInstance on their client
				SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
				broadcastPacket(sa);
				sa = null;

				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                String textbody = HtmCache.getInstance().getHtm("data/html/event/entery.htm");
                                String eventButtons = getActiveEventsNow(player);
                                textbody = textbody.replace("{entery}", eventButtons);
                                html.setHtml(textbody);
                                player.sendPacket(html);
			}
		}
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
        
        private String getActiveEventsNow(L2PcInstance player)
        {
            String resultButton = "";
            if(TvT.getInstance().canRegister(player, true)) 
            {
                resultButton += "<button value=\"TvT\" action=\"bypass -h Customevent tvt\" width=\"95\" height=\"24\" back=\"L2UI_CH3.bigbutton_down\" fore=\"L2UI_CH3.bigbutton\">";
            }
            if(LastHero.getInstance().canRegister(player, true))
            {
                resultButton += "<button value=\"LastHero\" action=\"bypass -h Customevent lh\" width=\"95\" height=\"24\" back=\"L2UI_CH3.bigbutton_down\" fore=\"L2UI_CH3.bigbutton\">";
            }
            if(CTF.getInstance().canRegister(player, true))
            {
                resultButton += "<button value=\"CTF\" action=\"bypass -h Customevent ctf\" width=\"95\" height=\"24\" back=\"L2UI_CH3.bigbutton_down\" fore=\"L2UI_CH3.bigbutton\">";
            }
            if(DeathMatch.getInstance().canRegister(player, true))
            {
                resultButton += "<button value=\"DeathMatch\" action=\"bypass -h Customevent dm\" width=\"95\" height=\"24\" back=\"L2UI_CH3.bigbutton_down\" fore=\"L2UI_CH3.bigbutton\">";
            }
            if(resultButton.equals(""))
            {
                resultButton = "<br>Not allowed event now! Come latter.";
            }
            return resultButton;
        }
}
