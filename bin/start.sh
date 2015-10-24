#!/bin/bash
#!/bin/bash
# For debugging purposes uncomment next line
#set -x

if [ -z $QUEDIT_HOME ]; then
  CURRENT_DIR=$(pwd)
  if [ ! -f "$CURRENT_DIR/quedit-jar-with-dependencies.jar" ]; then
    echo "ERROR: You need to either have QUEDIT_HOME env variable set or run Quedit from its directory"
    exit 1    
  else
    QUEDIT_HOME=$CURRENT_DIR
  fi
fi

APP_NAME="Quedit"
APP_FILENAME="quedit-jar-with-dependencies"
APP_PATH=$QUEDIT_HOME
APP_FILE=$APP_PATH/$APP_FILENAME".jar"
PROPERTIES="quedit.properties"
APP_COMMAND="java -Dfile.encoding=UTF-8 -jar $APP_FILE $PROPERTIES"

echo "Starting $APP_NAME in $APP_PATH ..."
echo "Properties file is $PROPERTIES"
echo ""
cd $QUEDIT_HOME
$APP_COMMAND

exit 0