OO_SDK_URE_BIN_DIR=/usr/lib/libreoffice/program
OOP=/usr/lib/libreoffice/program

#:$OOP/classes/ridl.jar\
#:$OOP/classes/unoil.jar\
#.:$OOP\

cd ../

export OO_SDK_URE_BIN_DIR
CP="\
$OOP\
:$OOP/classes/jurt.jar\
:$OOP/classes/juh.jar\
:$OOP/classes/unoloader.jar\
:/home/sdv/.m2/repository/org/json/json/20230227/json-20230227.jar\
:./target/libretempla-1.0-SNAPSHOT.jar"

echo $CP

java -classpath $CP org.stardust.libreoffice.libretempla.TemplateLibreFiller $1

#java -classpath $CP org.stardust.libreoffice.libretempla.TextReplace

