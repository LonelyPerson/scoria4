import sys
from com.l2scoria.gameserver.model.quest import State
from com.l2scoria.gameserver.model.quest import QuestState
from com.l2scoria.gameserver.model.quest.jython import QuestJython as JQuest

qn = "8934_Personal"

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player) :
   htmltext=event
   st = player.getQuestState(qn)
   if not st: return
   if event == "GiveNoble":
     player.setNoble(1)
     return "Now you are noble!"
   return


QUEST       = Quest(8934,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)