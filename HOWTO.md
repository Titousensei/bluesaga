This version of BlueSaga compiles with "ant" and runs from the command line. It should work for Linux and Mac OSX. I will add/merge support for Windows if there is demand for it (It's just a matter of creating run.bat for each run script).

SETUP
=====

Install java JDK 1.8 and apache ant.

COMPILE AND RUN THE SERVER
==========================

1. cd SERVER
2. ant
3. ./run -dev

COMPILE AND RUN THE CLIENT
==========================

1. cd CLIENT
2. ant
3. ./run -dev

COMPILE AND RUN THE EDITOR
==========================

1. cd EDITOR
2. ant
3. ./run

PACKAGE SERVER FOR RELEASE
==========================

//TBD//

MAKE CLIENT AVAILABLE FOR PLAYERS
=================================

1. "ant jar" to create gameData.jar (NOT WORKING YET)
2. Upload the gameData.jar file to a webhost
2. Go to http://www.bluesaga.org/myservers/ and click on "Edit" next to your server
3. Paste in the url to the gamedata.jar file you uploaded in the Client Url field
4. Write a short description of the latest update for the players to read, this text will be seen in the game launcher window
5. Click Save
6. Players will now be able to get your latest update and connect to your server with the Blue Saga launcher 
