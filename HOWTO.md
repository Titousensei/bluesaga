This version of BlueSaga compiles with "ant" and runs from the command line. It should work for Linux and Mac OSX. For Windows is should work too under powershell. I will add/merge support for Windows if there is demand for it (It's just a matter of creating run.bat for each run script).

All executables need a config directory that contains the options and all the data. Some of this data is shared: the map saved by the editor will be loaded by the server; the same images will be displayed by the client and the editor. You can make any number of copies of config directories to try different changes.

SETUP
=====

Install java JDK 1.8 and apache ant.

create local copies of configs:
1. mkdir SERVER/configs
2. cp -a SERVER/config_classic SERVER/configs
3. mkdir CLIENT/configs
4. cp -a CLIENT/config_classic SERVER/configs

COMPILE AND RUN THE SERVER
==========================

1. cd SERVER
2. ant
3. ./run configs/config_classic

COMPILE AND RUN THE CLIENT
==========================

1. cd CLIENT
2. ant
3. ./run configs/config_classic

COMPILE AND RUN THE EDITOR
==========================

1. cd EDITOR
2. ant
3. ./run ../SERVER/configs/config_classic

INSTALL SELF-CONTAINED SERVER
=============================

1. mkdir ~/BlueSagaServer
2. cp -a JARS ~/BlueSagaServer
3. cp SERVER/run_server_infinite ~/BlueSagaServer
4. cp -a SERVER/configs/config_classic ~/BlueSagaServer

UPDATE SELF-CONTAINED SERVER
============================

1. cd SERVER
2. ant dist
3. cp SERVER/dist/server.jar ~/BlueSagaServer

RUN SELF-CONTAINED SERVER
=========================

1. cd ~/BlueSagaServer
2. ./run_server_infinite config_classic

MAKE CLIENT AVAILABLE FOR PLAYERS
=================================

First make sure you've updated the client version number in the source code of both the client and the server, and the server was updated.

1. "ant dist" to create gameData.jar
2. Upload the gameData.jar file to a webhost like github release
2. Go to http://www.bluesaga.org/myservers/ and click on "Edit" next to your server
3. Paste in the url to the gamedata.jar file you uploaded in the Client Url field
4. Write a short description of the latest update for the players to read, this text will be seen in the game launcher window
5. Click Save
6. Players will now be able to get your latest update and connect to your server with the Blue Saga launcher
