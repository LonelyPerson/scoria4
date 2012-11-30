@echo off

REM ###############################################
REM ## Configurate Database Connections please!!!!             ##
REM ###############################################
REM Please, type here you dir to mysql directory \bin. Example : C:\Program Files\MySQL\Mysql 5.0\bin
set mysqlBinPath=C:\Program Files\VertrigoServ\Mysql\bin

set DateT=%date%

REM Configurate database connection loginserver
set lsuser=root
set lspass=db_password
set lsdb=l2jdb
set lshost=localhost

REM Configurate database connection Gameserver
set gsuser=root
set gspass=db_password
set gsdb=l2jdb
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"


:Step1
cls
echo. ------------------------------------------------------------------------------------------------------------------------
echo.
echo.   Scoria - Database Login Server`s 
echo. ____________________________________________________________
echo.
echo.   1 - Full install database loginserver`s.
echo.   2 - Skip install loginserver db, go to install gamserver databases
echo.   3 - Spawn in town GMshop and other custom NPC
echo.   4 - Exit from installer
echo. ------------------------------------------------------------------------------------------------------------------------

set Step1prompt=x
set /p Step1prompt= Please enter values :
if /i %Step1prompt%==1 goto LoginInstall
if /i %Step1prompt%==2 goto Step2
if /i %Step1prompt%==3 goto addinstall
if /i %Step1prompt%==4 goto fullend
goto Step1


:LoginInstall
@cls
echo.
echo Clear database : %lsdb% and install database loginserver`s.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo Update table accounts.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
echo Update table gameservers.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql
echo Database login server will be installer whis no-error!!
pause
goto :Step2

:Step2
@cls
echo. ----------------------------------------------------------------------------------------------------------------------
echo.
echo.   Scoria - database operation about gameserver
echo. ___________________________________________________________
echo.
echo.   1 - Full Install Gameserver Database`s.
echo.   2 - Exit.
echo. ----------------------------------------------------------------------------------------------------------------------

set Step2prompt=x
set /p Step2prompt= Please, put value:
if /i %Step2prompt%==1 goto fullinstall
if /i %Step2prompt%==2 goto fullend
goto Step2

:fullinstall
@cls
echo Drop and clean old gamserver database`s.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql
set title=installed
goto CreateTables

:CreateTables
@cls
echo.
echo Now be install database gameserver`s.
echo Press some button on keybord to start proccess
pause
echo Install table access_levels.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/access_levels.sql 
echo Install table armor.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql 
echo Install table auction.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction.sql 
echo Install table auction_bid.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_bid.sql 
echo Install table auction_watch.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_watch.sql 
echo Install table augmentations.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/augmentations.sql 
echo Install table doors.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/doors.sql 
echo Install table auto_announcements.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_announcements.sql 
echo Install table auto_chat.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat.sql 
echo Install table auto_chat_text.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat_text.sql 
echo Install table buff_profile.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/buff_profile.sql 
echo Install table castle.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql 
echo Install table castle_door.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql 
echo Install table castle_doorupgrade.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql 
echo Install table castle_siege_guards.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql 
echo Install table char_templates.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql 
echo Install table character_friends.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql 
echo Install table character_hennas.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql 
echo Install table character_macroses.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql 
echo Install table character_quests.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql 
echo Install table character_raid_points.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_raid_points.sql 
echo Install table character_recipebook.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql 
echo Install table char_creation_items.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_creation_items.sql 
echo Install table character_recommends.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recommends.sql 
echo Install table character_shortcuts.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql 
echo Install table character_settings.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_settings.sql 
echo Install table character_skills.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql 
echo Install table character_votes.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_votes.sql 
echo Install table character_skills_save.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql 
echo Install table character_subclasses.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql 
echo Install table character_offline_trade_items.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_offline_trade_items.sql 
echo Install table character_offline_trade.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_offline_trade.sql 
echo Install table characters.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql 
echo Install table clan_data.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql 
echo Install table clan_privs.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_privs.sql 
echo Install table clan_skills.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_skills.sql 
echo Install table clan_subpledges.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_subpledges.sql 
echo Install table clan_wars.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql 
echo Install table clan_notices.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_notices.sql 
echo Install table clanhall.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall.sql 
echo Install table clanhall_functions.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_functions.sql 
echo Install table class_list.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql 
echo Install table cursed_weapons.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/cursed_weapons.sql 
echo Install table dimensional_rift.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dimensional_rift.sql 
echo Install table droplist.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql 
echo Install table enchant_skill_trees.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/enchant_skill_trees.sql 
echo Install table etcitem.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql 
echo Install table etcitem.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql 
echo Install table fish.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fish.sql 
echo Install table fishing_skill_trees.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fishing_skill_trees.sql 
echo Install table forums.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/forums.sql 
echo Install table games.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/games.sql 
echo Install table global_tasks.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql 
echo Install table grandboss_data.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_data.sql 
echo Install table grandboss_list.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_list.sql 
echo Install table helper_buff_list.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/helper_buff_list.sql 
echo Install table henna_trees.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql 
echo Install table heroes.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/heroes.sql 
echo Install table items.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql 
echo Install table itemsonground.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/itemsonground.sql 
echo Install table locations.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql 
echo Install table lvlupgain.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql 
echo Install table merchant_areas_list.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql 
echo Install table merchant_buylists.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql 
echo Install table merchant_lease.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql 
echo Install table merchant_shopids.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql 
echo Install table merchants.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql 
echo Install table minions.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql 
echo Install table mods_wedding.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mods_wedding.sql 
echo Install table npc.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql 
echo Install table npc_to_pc_polymorph.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc_to_pc_polymorph.sql 
echo Install table npcskills.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql 
echo Install table olympiad_nobles.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/olympiad_nobles.sql 
echo Install table pets.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql 
echo Install table pets_stats.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets_stats.sql 
echo Install table pledge_skill_trees.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pledge_skill_trees.sql 
echo Install table posts.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/posts.sql 
echo Install table raidboss_spawnlist.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/raidboss_spawnlist.sql 
echo Install table random_spawn.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql 
echo Install table random_spawn_loc.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql 
echo Install table seven_signs.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql 
echo Install table seven_signs_festival.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql 
echo Install table seven_signs_status.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_status.sql 
echo Install table siege_clans.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql 
echo Install table skill_learn.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql 
echo Install table skill_spellbooks.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql 
echo Install table skill_trees.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql 
echo Install table spawnlist.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql 
echo Install table teleport.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql 
echo Install table topic.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/topic.sql 
echo Install table weapon.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql 
echo Install table zone_vertices.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone_vertices.sql 
echo Install table quest_global_data.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/quest_global_data.sql 
echo Install table castle_manor_procure.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_procure.sql 
echo Install table castle_manor_production.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_production.sql 
echo Install table custom_weapon.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_weapon.sql 
echo Install table custom_npc.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql 
echo Install table custom_teleport.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_teleport.sql 
echo Install table custom_armor.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armor.sql 
echo Install table custom_armorsets.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armorsets.sql 
echo Install table custom_contestable_clanhalls.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_contestable_clanhalls.sql 
echo Install table custom_merchant_shopids.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_shopids.sql 
echo Install table custom_droplist.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_droplist.sql 
echo Install table custom_merchant_buylists.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_buylists.sql 
echo Install table custom_etcitem.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_etcitem.sql 
echo Install table rebirth_manager.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/rebirth_manager.sql 
echo Install table remove_unneeded_spawns.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/remove_unneeded_spawns.sql 
echo Install table admin_command_access_rights.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/admin_command_access_rights.sql 
echo Install table pkkills.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pkkills.sql 
echo Install table characters_custom_data.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters_custom_data.sql 
echo Install table tvt.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt.sql 
echo Install table tvt_teams.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt_teams.sql 
echo Install table character_recipebook.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql 
echo Install table fort_door.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_door.sql 
echo Install table fort_doorupgrade.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_doorupgrade.sql 
echo Install table fort_siege_guards.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_siege_guards.sql 
echo Install table fort.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort.sql 
echo Install table fortsiege_clans.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fortsiege_clans.sql 
echo Install table four_sepulchers_spawnlist.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/four_sepulchers_spawnlist.sql 
echo Install table custom_spawnlist_clear.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist_clear.sql 
echo Install table vanhalter_spawnlist.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vanhalter_spawnlist.sql 
echo Install table clanhall_siege.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_siege.sql 
echo Install table powerpak.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/powerpak.sql 
echo Install table paystream.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/paystream.sql 
echo Install table l2votes.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2votes.sql 
echo Install table smsonline.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/smsonline.sql 
echo Install table bw_teams.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/bw_teams.sql 
echo Install table ctf_teams.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf_teams.sql 
echo Install table custom_npc.sql in database %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql
echo GameServer Database installed.
pause
goto :Step1

:addinstall
@cls
echo.
echo Put in database spawn GmShop, Classmaster and other goods...
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist.sql
echo Table custom_spawnlist.sql installed
pause
:end
echo.
echo Installed Sucessfull.
echo.
pause
goto :Step1

:end
echo.
echo Installing sucessfull.
echo.
pause

:fullend
