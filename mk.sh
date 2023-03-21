#/bin/sh

CLASSPATH=$CLASSPATH:/usr/lib/libreoffice/program
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/libreoffice/program:.:

export CLASSPATH LD_LIBRARY_PATH

mvn $*


