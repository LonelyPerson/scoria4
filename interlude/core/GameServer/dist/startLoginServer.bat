@echo off
REM color 07
title Scoria 4.3RC LoginServer
:start
echo Starting L2J-Scoria Core Login Server.
echo Official webiste : http://scoria.ru
echo Enjoy by server core. Be happy!
echo ------------------------------
echo.

REM ----------- Set Class Paths and Calls setenv.bat -----------------
SET OLDCLASSPATH=%CLASSPATH%
call setlog.bat
REM ------------------------------------------------------------------

java -Xbootclasspath/p:./lib/jsr167.jar -Dfile.encoding=UTF8 -Xmx256m -XX:+UseParallelGC -XX:+AggressiveOpts com.l2scoria.loginserver.L2LoginServer

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restarted ...
echo Send you bug to : http://scoria.ru
echo.
goto start
:error
echo.
echo LoginServer terminated abnormaly
echo Send you bug to : http://scoria.ru
echo.
:end
echo.
echo LoginServer terminated
echo Send you bug to : http://scoria.ru
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
