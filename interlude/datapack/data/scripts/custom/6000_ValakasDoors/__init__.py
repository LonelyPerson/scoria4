import sys
from com.l2scoria.gameserver.datatables.sql import DoorTable
from com.l2scoria.gameserver.model.quest import State
from com.l2scoria.gameserver.model.quest import QuestState
from com.l2scoria.gameserver.model.quest.jython import QuestJython as JQuest

qn = "6000_ValakasDoors"

NPCs = [
    31384, #Gatekeeper of Fire Dragon : Opening some doors
    31686, #Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
    31687, #Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
    ]

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
    npcId = npc.getNpcId()
    htmltext = ""
    if npcId == 31384 : #Gatekeeper of Fire Dragon
        DoorTable.getInstance().getDoor(24210004).openMe()
        return
    elif npcId == 31686 : #Gatekeeper of Fire Dragon
        DoorTable.getInstance().getDoor(24210006).openMe()
        return
    elif npcId == 31687 : #Gatekeeper of Fire Dragon
        DoorTable.getInstance().getDoor(24210005).openMe()
        return
    return htmltext

QUEST       = Quest(-1, qn, "Valakas Doors")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

for npcid in NPCs :
    QUEST.addStartNpc(npcid)
    QUEST.addTalkId(npcid)