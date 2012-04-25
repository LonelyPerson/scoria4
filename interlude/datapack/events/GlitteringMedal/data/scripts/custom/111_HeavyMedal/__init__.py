import sys
from com.l2scoria.gameserver.model.quest import State
from com.l2scoria.gameserver.model.quest import QuestState
from com.l2scoria.gameserver.model.quest.jython import QuestJython as JQuest

qn = "111_HeavyMedal"

#cats
ROY = 31228
WINNIE = 31229
#item
EVENT_GLITTERING_MEDAL = 6393
#marks
RABBIT = 6399
HYENA = 6400
FOX = 6401
WOLF = 6402

CHANCE = 35

PAY = 5


class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    cond = st.getInt("cond")
    if event == "game" :
      if cond == 0 and st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*1 :
        st.playSound("ItemSound.quest_accept")
        htmltext = "game.htm"
      elif cond == 1 and st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*2 :
        st.playSound("ItemSound.quest_accept")
        htmltext = "game.htm"
      elif cond == 2 and st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*4 :
        st.playSound("ItemSound.quest_accept")
        htmltext = "game.htm"  
      elif cond == 3 and st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*8 :
        st.playSound("ItemSound.quest_accept")
        htmltext = "game.htm"  
      else: htmltext = "less.htm"
  
    elif event == "play" :
      if st.getRandom(100)<CHANCE :
        if cond == 0 :
           if st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*1 :
              st.takeItems(EVENT_GLITTERING_MEDAL,PAY*1)
              st.giveItems(RABBIT,1)
              st.set("cond","1")
              st.playSound("ItemSound.quest_itemget")
              htmltext = "win-1.htm"
           else :
              htmltext = "less.htm" 
        elif cond == 1 :
           if st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*2 :
              st.takeItems(EVENT_GLITTERING_MEDAL,PAY*2)
              st.takeItems(RABBIT,1)
              st.giveItems(HYENA,1)
              st.set("cond","2")
              st.playSound("ItemSound.quest_itemget")
              htmltext = "win-1.htm"
           else :
              htmltext = "less.htm"             
        elif cond == 2 :
           if st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*4 :
              st.takeItems(EVENT_GLITTERING_MEDAL,PAY*4)
              st.takeItems(HYENA,1)
              st.giveItems(FOX,1)
              st.set("cond","3")
              st.playSound("ItemSound.quest_itemget")
              htmltext = "win-1.htm"
           else :
              htmltext = "less.htm"               
        elif cond == 3 :
           if st.getQuestItemsCount(EVENT_GLITTERING_MEDAL) >= PAY*8 :
              st.takeItems(EVENT_GLITTERING_MEDAL,PAY*8)
              st.takeItems(FOX,1)
              st.giveItems(WOLF,1)
              st.set("cond","4")
              st.playSound("ItemSound.quest_finish")
              htmltext = "win.htm"
           else :
              htmltext = "less.htm" 
      else: 
        htmltext = "lose.htm"
        if cond == 0 :
            st.takeItems(EVENT_GLITTERING_MEDAL,PAY*1)
        elif cond == 1:
            st.takeItems(EVENT_GLITTERING_MEDAL,PAY*2)
        elif cond == 2 :
            st.takeItems(EVENT_GLITTERING_MEDAL,PAY*4)
        elif cond == 3 :
            st.takeItems(EVENT_GLITTERING_MEDAL,PAY*8)
    return htmltext


 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   cond = st.getInt("cond")
   htmltext = "<html><body>I have nothing to say you.</body></html>"
   if not st : return htmltext
   if npcId == WINNIE :
     if st.getQuestItemsCount(RABBIT) == 0 and st.getQuestItemsCount(HYENA) == 0 and st.getQuestItemsCount(FOX) == 0 and st.getQuestItemsCount(WOLF) == 0 :
       htmltext = "5.htm"
       st.set("cond","0")
     elif cond == 1 :
       htmltext = "10.htm"
     elif cond == 2 :
       htmltext = "20.htm"
     elif cond == 3 :
       htmltext = "40.htm"
     elif st.getQuestItemsCount(WOLF) == 1 :
       htmltext = "win.htm"
     else: htmltext = "win.htm"
   
   elif npcId == ROY :
     if st.getQuestItemsCount(RABBIT) == 0 and st.getQuestItemsCount(HYENA) == 0 and st.getQuestItemsCount(FOX) == 0 and st.getQuestItemsCount(WOLF) == 0 :
       htmltext = "roy.htm"
     elif st.getQuestItemsCount(RABBIT) == 1 :
       htmltext = "roy-1.htm"
     elif st.getQuestItemsCount(HYENA) == 1 :
       htmltext = "roy-2.htm"
     elif st.getQuestItemsCount(FOX) == 1 :
       htmltext = "roy-3.htm"
     elif st.getQuestItemsCount(WOLF) == 1 :
       htmltext = "roy-4.htm"
   return htmltext

QUEST       = Quest(111,qn,"custom")
CREATED     = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)

# Quest NPC starter initialization
QUEST.addStartNpc(ROY)
QUEST.addStartNpc(WINNIE)
QUEST.addTalkId(ROY)
QUEST.addTalkId(WINNIE)