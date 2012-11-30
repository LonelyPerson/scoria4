@echo off

REM ####################################################
REM ## Укажите путь к папке bin вашего mysql сервера   #
REM ####################################################
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.1\bin

set DateT=%date%

REM ####################################################
REM Конфигурации подключения к loginserver базе        #
REM ####################################################
set lsuser=root
set lspass=db_password
set lsdb=l2jdb
set lshost=localhost

REM ####################################################
REM Конфигурации подключения к gameserver базе         #
REM ####################################################
set gsuser=root
set gspass=db_password
set gsdb=l2jdb
set gshost=localhost
REM ####################################################
REM         AHTUNG! ALARM! ALERT! ВНИМАНИЕ!            #
REM НЕ ТРОГАЙТЕ НИЧЕГО ДАЛЕЕ ЕСЛИ НЕ ХОТИТЕ ИСПОРТИТЬ  #
REM ДАННЫЙ УСТАНОВЩИК И УТЕРЯТЬ ЕГО РАБОТОСПОСОБНОСТЬ  #
REM !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! #
REM ####################################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"


:Step1
cls
echo. ____________________________________________________________
echo.
echo.   Программа по импорту первичных дампов www.scoria.ru
echo. ____________________________________________________________
echo. До запуска программы укажите путь к mysql, логин, пароль, имя базы данных
echo. Если данные не будут указаны - установщик завершит свою работу.
echo. ____________________________________________________________
echo.   1 - Импорт первичного дампа баз loginserver
echo.   2 - Импорт первичного дампа баз gameserver
echo.   3 - Импортировать custom таблицы в базу gameserver
echo.   4 - Выйти из программы
echo. ____________________________________________________________

set Step1prompt=x
set /p Step1prompt=Укажите необходимое действие:
if /i %Step1prompt%==1 goto LoginInstall
if /i %Step1prompt%==2 goto Step2
if /i %Step1prompt%==3 goto addinstall
if /i %Step1prompt%==4 goto fullend
goto Step1


:LoginInstall
@cls
echo.
echo Наполняем базу данных %lsdb% таблицами loginserver.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo Устанавливаем таблицу accounts.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
echo Устанавливаем таблицу gameservers.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql
echo Импорт дампов таблиц завершен.
pause
goto :Step2

:Step2
@cls
echo. ___________________________________________________________
echo.
echo.	Импорт первичных дампов таблиц gameserver
echo. ___________________________________________________________
echo.
echo.   1 - Импортировать дампы, удалив существующие таблицы
echo.   2 - Выйти
echo. ___________________________________________________________

set Step2prompt=x
set /p Step2prompt=Укажите необходимое действие:
if /i %Step2prompt%==1 goto fullinstall
if /i %Step2prompt%==2 goto fullend
goto Step2

:fullinstall
@cls
echo Очистка баз данных gameserver.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql
goto CreateTables

:CreateTables
@cls
echo.
echo Импортируем дамп базы gameserver в базу %gsdb%
echo Нажмите любую кнопку для начала процесса
pause
echo Устанавливаем таблицу access_levels.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/access_levels.sql 
echo Устанавливаем таблицу armor.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql 
echo Устанавливаем таблицу auction.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction.sql 
echo Устанавливаем таблицу auction_bid.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_bid.sql 
echo Устанавливаем таблицу auction_watch.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_watch.sql 
echo Устанавливаем таблицу augmentations.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/augmentations.sql 
echo Устанавливаем таблицу doors.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/doors.sql 
echo Устанавливаем таблицу delay_item.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/delay_item.sql
echo Устанавливаем таблицу auto_announcements.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_announcements.sql 
echo Устанавливаем таблицу auto_chat.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat.sql 
echo Устанавливаем таблицу auto_chat_text.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat_text.sql 
echo Устанавливаем таблицу buff_profile.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/buff_profile.sql 
echo Устанавливаем таблицу castle.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql 
echo Устанавливаем таблицу castle_door.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql 
echo Устанавливаем таблицу castle_doorupgrade.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql 
echo Устанавливаем таблицу castle_siege_guards.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql 
echo Устанавливаем таблицу char_templates.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql 
echo Устанавливаем таблицу character_friends.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql 
echo Устанавливаем таблицу character_hennas.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql 
echo Устанавливаем таблицу character_macroses.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql 
echo Устанавливаем таблицу character_quests.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql 
echo Устанавливаем таблицу character_raid_points.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_raid_points.sql 
echo Устанавливаем таблицу character_recipebook.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql 
echo Устанавливаем таблицу char_creation_items.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_creation_items.sql 
echo Устанавливаем таблицу character_recommends.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recommends.sql 
echo Устанавливаем таблицу character_shortcuts.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql 
echo Устанавливаем таблицу character_settings.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_settings.sql 
echo Устанавливаем таблицу character_skills.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql 
echo Устанавливаем таблицу character_votes.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_votes.sql 
echo Устанавливаем таблицу character_skills_save.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql 
echo Устанавливаем таблицу character_subclasses.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql 
echo Устанавливаем таблицу character_offline_trade_items.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_offline_trade_items.sql 
echo Устанавливаем таблицу character_offline_trade.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_offline_trade.sql 
echo Устанавливаем таблицу characters.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql 
echo Устанавливаем таблицу clan_data.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql 
echo Устанавливаем таблицу clan_privs.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_privs.sql 
echo Устанавливаем таблицу clan_skills.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_skills.sql 
echo Устанавливаем таблицу clan_subpledges.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_subpledges.sql 
echo Устанавливаем таблицу clan_wars.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql 
echo Устанавливаем таблицу clan_notices.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_notices.sql 
echo Устанавливаем таблицу clanhall.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall.sql 
echo Устанавливаем таблицу clanhall_functions.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_functions.sql 
echo Устанавливаем таблицу class_list.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql 
echo Устанавливаем таблицу cursed_weapons.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/cursed_weapons.sql 
echo Устанавливаем таблицу dimensional_rift.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dimensional_rift.sql 
echo Устанавливаем таблицу droplist.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql 
echo Устанавливаем таблицу enchant_skill_trees.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/enchant_skill_trees.sql 
echo Устанавливаем таблицу etcitem.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql 
echo Устанавливаем таблицу etcitem.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql 
echo Устанавливаем таблицу fish.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fish.sql 
echo Устанавливаем таблицу fishing_skill_trees.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fishing_skill_trees.sql 
echo Устанавливаем таблицу forums.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/forums.sql 
echo Устанавливаем таблицу games.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/games.sql 
echo Устанавливаем таблицу global_tasks.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql 
echo Устанавливаем таблицу grandboss_data.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_data.sql 
echo Устанавливаем таблицу grandboss_list.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_list.sql 
echo Устанавливаем таблицу helper_buff_list.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/helper_buff_list.sql 
echo Устанавливаем таблицу henna_trees.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql 
echo Устанавливаем таблицу heroes.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/heroes.sql 
echo Устанавливаем таблицу items.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql 
echo Устанавливаем таблицу itemsonground.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/itemsonground.sql 
echo Устанавливаем таблицу locations.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql 
echo Устанавливаем таблицу lvlupgain.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql 
echo Устанавливаем таблицу merchant_areas_list.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql 
echo Устанавливаем таблицу merchant_buylists.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql 
echo Устанавливаем таблицу merchant_lease.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql 
echo Устанавливаем таблицу merchant_shopids.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql 
echo Устанавливаем таблицу merchants.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql 
echo Устанавливаем таблицу minions.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql 
echo Устанавливаем таблицу mods_wedding.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mods_wedding.sql 
echo Устанавливаем таблицу npc.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql 
echo Устанавливаем таблицу npc_to_pc_polymorph.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc_to_pc_polymorph.sql 
echo Устанавливаем таблицу npcskills.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql 
echo Устанавливаем таблицу olympiad_nobles.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/olympiad_nobles.sql 
echo Устанавливаем таблицу pets.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql 
echo Устанавливаем таблицу pets_stats.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets_stats.sql 
echo Устанавливаем таблицу pledge_skill_trees.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pledge_skill_trees.sql 
echo Устанавливаем таблицу posts.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/posts.sql 
echo Устанавливаем таблицу raidboss_spawnlist.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/raidboss_spawnlist.sql 
echo Устанавливаем таблицу random_spawn.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql 
echo Устанавливаем таблицу random_spawn_loc.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql 
echo Устанавливаем таблицу seven_signs.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql 
echo Устанавливаем таблицу seven_signs_festival.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql 
echo Устанавливаем таблицу seven_signs_status.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_status.sql 
echo Устанавливаем таблицу siege_clans.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql 
echo Устанавливаем таблицу skill_learn.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql 
echo Устанавливаем таблицу skill_spellbooks.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql 
echo Устанавливаем таблицу skill_trees.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql 
echo Устанавливаем таблицу spawnlist.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql 
echo Устанавливаем таблицу teleport.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql 
echo Устанавливаем таблицу topic.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/topic.sql 
echo Устанавливаем таблицу weapon.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql 
echo Устанавливаем таблицу zone_vertices.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone_vertices.sql 
echo Устанавливаем таблицу quest_global_data.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/quest_global_data.sql 
echo Устанавливаем таблицу castle_manor_procure.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_procure.sql 
echo Устанавливаем таблицу castle_manor_production.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_production.sql 
echo Устанавливаем таблицу custom_weapon.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_weapon.sql 
echo Устанавливаем таблицу custom_npc.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql 
echo Устанавливаем таблицу custom_teleport.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_teleport.sql 
echo Устанавливаем таблицу custom_armor.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armor.sql 
echo Устанавливаем таблицу custom_armorsets.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armorsets.sql 
echo Устанавливаем таблицу custom_contestable_clanhalls.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_contestable_clanhalls.sql 
echo Устанавливаем таблицу custom_merchant_shopids.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_shopids.sql 
echo Устанавливаем таблицу custom_droplist.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_droplist.sql 
echo Устанавливаем таблицу custom_merchant_buylists.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_buylists.sql 
echo Устанавливаем таблицу custom_etcitem.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_etcitem.sql 
echo Устанавливаем таблицу rebirth_manager.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/rebirth_manager.sql 
echo Устанавливаем таблицу remove_unneeded_spawns.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/remove_unneeded_spawns.sql 
echo Устанавливаем таблицу admin_command_access_rights.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/admin_command_access_rights.sql 
echo Устанавливаем таблицу pkkills.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pkkills.sql 
echo Устанавливаем таблицу characters_custom_data.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters_custom_data.sql 
echo Устанавливаем таблицу tvt.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt.sql 
echo Устанавливаем таблицу tvt_teams.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt_teams.sql 
echo Устанавливаем таблицу character_recipebook.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql 
echo Устанавливаем таблицу fort_door.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_door.sql 
echo Устанавливаем таблицу fort_doorupgrade.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_doorupgrade.sql 
echo Устанавливаем таблицу fort_siege_guards.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_siege_guards.sql 
echo Устанавливаем таблицу fort.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort.sql 
echo Устанавливаем таблицу fortsiege_clans.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fortsiege_clans.sql 
echo Устанавливаем таблицу four_sepulchers_spawnlist.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/four_sepulchers_spawnlist.sql 
echo Устанавливаем таблицу custom_spawnlist_clear.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist_clear.sql 
echo Устанавливаем таблицу vanhalter_spawnlist.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vanhalter_spawnlist.sql 
echo Устанавливаем таблицу clanhall_siege.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_siege.sql 
echo Устанавливаем таблицу powerpak.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/powerpak.sql 
echo Устанавливаем таблицу paystream.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/paystream.sql 
echo Устанавливаем таблицу l2votes.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2votes.sql 
echo Устанавливаем таблицу smsonline.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/smsonline.sql 
echo Устанавливаем таблицу bw_teams.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/bw_teams.sql 
echo Устанавливаем таблицу ctf_teams.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf_teams.sql 
echo Устанавливаем таблицу custom_npc.sql в базу %gsdb%
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql
echo Установка завершена нажмите любую клавишу
pause
goto :Step1

:addinstall
@cls
echo.
echo Устанавливаем нестандартные точки расположений npc - custom_spawnlist
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist.sql
echo Импортируем таблицу custom_spawnlist.sql в базу %gsdb%
pause
:end
echo.
echo Импорт завершен
echo.
pause
goto :Step1

:end
echo.
echo Досвидания
echo.
pause

:fullend
