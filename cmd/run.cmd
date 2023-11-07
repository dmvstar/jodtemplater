@ECHO OFF
SET OO_SDK_URE_BIN_DIR="C:/Program Files/LibreOffice/program"
SET OOP="C:/Program Files/LibreOffice/program"

cd ../

rem export OO_SDK_URE_BIN_DIR
SET CP=%OOP%;%OOP%/classes/jurt.jar;%OOP%/classes/juh.jar;%OOP%/classes/unoloader.jar;C:/Users/starzynskid/.m2/repository/org/json/json/20230227/json-20230227.jar;./target/libretempla-1.0-SNAPSHOT.jar

REM ;$OOP/classes/jurt.jar;$OOP/classes/juh.jar;$OOP/classes/unoloader.jar;/home/sdv/.m2/repository/org/json/json/20230227/json-20230227.jar;./target/libretempla-1.0-SNAPSHOT.jar"

echo %CP%

java -classpath %CP% org.stardust.libreoffice.libretempla.TemplateLibreFiller ./res/szablon-1.json



