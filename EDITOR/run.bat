@ECHO OFF
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;path/to/needed/jars/my.jar
export CLASSPATH="./build/:../common:$JARS/bs_user.jar:$JARS/grandcentral.jar:$JARS/jogg-0.0.7.jar:$JARS/jorbis-0.0.15.jar:$JARS/json_simple.jar:$JARS/lwjgl.jar:$JARS/natives-linux.jar:$JARS/slick.jar:$JARS/sqlitejdbc-v056.jar"

%JAVA_HOME%\bin\java -Djava.library.path=%JARS% game.BP_EDITOR
