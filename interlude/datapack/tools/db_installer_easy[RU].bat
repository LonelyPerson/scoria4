@echo off

REM ###############################################
REM ## Настройте конфигурации вашей базы MySQL ниже  ##
REM ###############################################
REM путь к папке bin в MySQL
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.1\bin

set DateT=%date%

REM Конфигурации loginserver
set lsuser=root
set lspass=db_password
set lsdb=l2jdb
set lshost=localhost

REM Конфигурации к Gameserver
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
echo.   LA2 100NT Team - Базы данных Login Server ` a
echo. ____________________________________________________________
echo.
echo.   1 - Полная заливка баз loginserver`a.
echo.   2 - Перейти к установке баз gameserver`a
echo.   3 - Выставить в городах Гмшоп, евент котов, свадебного нпс
echo.   4 - Выйти нах отсуда - нехера непонятна...
echo. ------------------------------------------------------------------------------------------------------------------------

set Step1prompt=x
set /p Step1prompt= Введите значение:
if /i %Step1prompt%==1 goto LoginInstall
if /i %Step1prompt%==2 goto Step2
if /i %Step1prompt%==3 goto addinstall
if /i %Step1prompt%==4 goto fullend
goto Step1


:LoginInstall
@cls
echo.
echo Очистка БД: %lsdb% и установка баз данных loginserver`a.
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login_install.sql
echo Обновляем таблицу accounts.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/accounts.sql
echo Обновляем таблицу gameservers.sql
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < ../sql/gameservers.sql
echo Базы логин сервера успешно установлены.
pause
goto :Step2

:Step2
@cls
echo. ----------------------------------------------------------------------------------------------------------------------
echo.
echo.   LA2 100NT Team - Операции с базой данных сервера игры
echo. ___________________________________________________________
echo.
echo.   1 - Полная установка баз данных сервера игры.
echo.   2 - Выйти.
echo. ----------------------------------------------------------------------------------------------------------------------

set Step2prompt=x
set /p Step2prompt= Введите значение:
if /i %Step2prompt%==1 goto fullinstall
if /i %Step2prompt%==2 goto fullend
goto Step2

:fullinstall
@cls
echo Чистим содержимое старых баз данных сервера.
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < full_install.sql
set title=установлен
goto CreateTables

:CreateTables
@cls
echo.
echo Сейчас будут %title%ы базы gameserver`a.
pause
@cls
echo ^ Завершено 1 проц. ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/access_levels.sql
@cls
echo ^ Завершено 2 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/armor.sql
@cls
echo ^ Завершено 4 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction.sql
@cls
echo ^ Завершено 5 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_bid.sql
@cls
echo ^ Завершено 6 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auction_watch.sql
@cls
echo ^ Завершено 7 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/augmentations.sql
@cls
echo ^ Завершено 8 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_announcements.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat.sql
@cls
echo ^ Завершено 9 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/auto_chat_text.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/buff_profile.sql
@cls
echo ^ Завершено 12 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle.sql
@cls
echo ^ Завершено 13 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_door.sql
@cls
echo ^ Завершено 14 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_doorupgrade.sql
@cls
echo ^ Завершено 15 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_siege_guards.sql
@cls
echo ^ Завершено 16 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_templates.sql
@cls
echo ^ Завершено 17 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_friends.sql
@cls
echo ^ Завершено 18 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_hennas.sql
@cls
echo ^ Завершено 19 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_macroses.sql
@cls
echo ^ Завершено 20 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_quests.sql
@cls
echo ^ Завершено 21 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_raid_points.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/char_creation_items.sql
@cls
echo ^ Завершено 22 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recommends.sql
@cls
echo ^ Завершено 23 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_shortcuts.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_settings.sql
@cls
echo ^ Завершено 24 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills.sql
@cls
echo ^ Завершено 25 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_skills_save.sql
@cls
echo ^ Завершено 26 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_subclasses.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_offline_trade_items.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_offline_trade.sql
@cls
echo ^ Завершено 27 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters.sql
@cls
echo ^ Завершено 28 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_data.sql
@cls
echo ^ Завершено 29 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_privs.sql
@cls
echo ^ Завершено 30 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_skills.sql
@cls
echo ^ Завершено 31 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_subpledges.sql
@cls
echo ^ Завершено 32 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_wars.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clan_notices.sql
@cls
echo ^ Завершено 33 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall.sql
@cls
echo ^ Завершено 34 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_functions.sql
@cls
echo ^ Завершено 35 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/class_list.sql
@cls
echo ^ Завершено 36 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/cursed_weapons.sql
@cls
echo ^ Завершено 37 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/dimensional_rift.sql
@cls
echo ^ Завершено 38 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/droplist.sql
@cls
echo ^ Завершено 39 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/enchant_skill_trees.sql
@cls
echo ^ Завершено 40 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/etcitem.sql
@cls
echo ^ Завершено 41 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fish.sql
@cls
echo ^ Завершено 42 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fishing_skill_trees.sql
@cls
echo ^ Завершено 43 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/forums.sql
@cls
echo ^ Завершено 44 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/games.sql
@cls
echo ^ Завершено 45 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/global_tasks.sql
@cls
echo ^ Завершено 46 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_data.sql
@cls
echo ^ Завершено 47 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/grandboss_list.sql
@cls
echo ^ Завершено 48 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/helper_buff_list.sql
@cls
echo ^ Завершено 49 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/henna_trees.sql
@cls
echo ^ Завершено 50 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/heroes.sql
@cls
echo ^ Завершено 51 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/items.sql
@cls
echo ^ Завершено 52 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/itemsonground.sql
@cls
echo ^ Завершено 53 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/locations.sql
@cls
echo ^ Завершено 54 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/lvlupgain.sql
@cls
echo ^ Завершено 55 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_areas_list.sql
@cls
echo ^ Завершено 56 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_buylists.sql
@cls
echo ^ Завершено 57 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_lease.sql
@cls
echo ^ Завершено 58 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchant_shopids.sql
@cls
echo ^ Завершено 59 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/merchants.sql
@cls
echo ^ Завершено 60 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/minions.sql
@cls
echo ^ Завершено 61 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/mods_wedding.sql
@cls
echo ^ Завершено 62 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npc_to_pc_polymorph.sql
@cls
echo ^ Завершено 63 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/npcskills.sql
@cls
echo ^ Завершено 64 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/olympiad_nobles.sql
@cls
echo ^ Завершено 65 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets.sql
@cls
echo ^ Завершено 66 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pets_stats.sql
@cls
echo ^ Завершено 67 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pledge_skill_trees.sql
@cls
echo ^ Завершено 68 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/posts.sql
@cls
echo ^ Завершено 70 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/raidboss_spawnlist.sql
@cls
echo ^ Завершено 71 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn.sql
@cls
echo ^ Завершено 72 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/random_spawn_loc.sql
@cls
echo ^ Завершено 73 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs.sql
@cls
echo ^ Завершено 74 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_festival.sql
@cls
echo ^ Завершено 75 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/seven_signs_status.sql
@cls
echo ^ Завершено 76 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/siege_clans.sql
@cls
echo ^ Завершено 77 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_learn.sql
@cls
echo ^ Завершено 78 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_spellbooks.sql
@cls
echo ^ Завершено 79 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/skill_trees.sql
@cls
echo ^ Завершено 80 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/spawnlist.sql
@cls
echo ^ Завершено 81 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/teleport.sql
@cls
echo ^ Завершено 82 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/topic.sql
@cls
echo ^ Завершено 84 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/weapon.sql
@cls
echo ^ Завершено 85 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/zone_vertices.sql
@cls
echo ^ Завершено 87 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/quest_global_data.sql
@cls
echo ^ Завершено 89 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_procure.sql
@cls
echo ^ Завершено 90 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/castle_manor_production.sql
@cls
echo ^ Завершено 91 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_weapon.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_teleport.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armor.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_armorsets.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_contestable_clanhalls.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_shopids.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_droplist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_merchant_buylists.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_etcitem.sql
@cls
echo ^ Завершено 92 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/rebirth_manager.sql
@cls
echo ^ Завершено 93 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/remove_unneeded_spawns.sql
@cls
echo ^ Завершено 94 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/admin_command_access_rights.sql
@cls
echo ^ Завершено 95 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/pkkills.sql
@cls
echo ^ Завершено 96 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/characters_custom_data.sql
@cls
echo ^ Завершено 97 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/tvt_teams.sql
@cls
echo ^ Завершено 98 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/character_recipebook.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_door.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_doorupgrade.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort_siege_guards.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fort.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/fortsiege_clans.sql
@cls
echo ^ Завершено 99 проц ***
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/four_sepulchers_spawnlist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist_clear.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/vanhalter_spawnlist.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/clanhall_siege.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/powerpak.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/paystream.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/l2votes.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/smsonline.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/bw_teams.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/ctf_teams.sql
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_npc.sql

@cls
echo ^ Завершено 100 проц ***
echo.
echo Cервер игры %title%.
pause
goto :Step1

:addinstall
@cls
echo.
echo Расставляем гм шопа, свадебного нпс и прочих ...
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < ../sql/custom_spawnlist.sql
echo всё, нпс расставлены - находятся в custom_spawnlist в базе вашей
pause
:end
echo.
echo Установка завершена.
echo.
pause
goto :Step1

:end
echo.
echo Установка завершена.
echo.
pause

:fullend
