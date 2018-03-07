#!/bin/bash

# initialize configuration variables
MAIN_CLASS="com.bushpath.jmx_cli.Main"
CLASSPATH_FILES=(
    "build/libs/jmx-cli.jar"
)

# initialize script variables
BASEDIR=$(dirname $0)
JAVA_OPTS=""
CLASSPATH=""

# initialize classpath
for CLASSPATH_FILE in "${CLASSPATH_FILES[@]}"
do
    # ensure file exists
    if [ ! -f $BASEDIR/$CLASSPATH_FILE ]
    then
        echo "file $BASEDIR/$CLASSPATH_FILE does not exist."
        exit 1
    fi

    # add to classpath
    if [[ -z "$CLASSPATH" ]]
    then
        CLASSPATH="$BASEDIR/$CLASSPATH_FILE"
    else
        CLASSPATH="$CLASSPATH:$BASEDIR/$CLASSPATH_FILE"
    fi
done

java -cp $CLASSPATH $JAVA_OPTS $MAIN_CLASS $@
