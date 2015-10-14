#!bin/bash
DBHOST="127.0.0.1"
DBPORT="27018"
BACKUPDIR="/Users/kalyanar/mongo"
COMPRESSION_TYPE=gzip
OPLOG="yes"
USE_SLAVE="yes"
# Datestamp e.g 2015-09-21
DATE=`date +%Y-%m-%d_%Hh%Mm`
DOW=`date +%A`
LOGFILE=$BACKUPDIR/$DBHOST-`date +%H%M`.log
LOGERR=$BACKUPDIR/ERRORS_$DBHOST-`date +%H%M`.log
BACKUPFILES=""
OPT=""
if [ "$OPLOG" = "yes" ]; then
    OPT="$OPT --oplog"
fi
#create the required directories
mkdir -p $BACKUPDIR/daily || echo 'failed to create directories'
# IO redirection for logging
touch $LOGFILE
exec 6>&1 #stdout saved
exec > $LOGFILE 
touch $LOGERR
exec 7>&2
exec 2> $LOGERR

#mongodump function
backupmongo(){
	mongodump --host=$DBHOST:$DBPORT --out=$1 $OPT
[ -e "$1" ] && return 0
    echo "ERROR: mongodump failed to create dumpfile: $1" >&2
    return 1
}
function check_is_secondary(){
local __return=$1
 is_secondary=$(mongo --quiet --host $DBHOST:$DBPORT --eval 'rs.isMaster().secondary')
eval $__return="'$is_secondary'"
}
compressbackupfile(){
tarsuffix=".zip"
dir=$(dirname $1)
file=$(basename $1)
echo "compressing $file to $file$tarsuffix"
cd "$dir" && tar -czvf $file$tarsuffix $file
cd - >/dev/null || return 1
echo "file compressed. Removing the folder"
rm -rf "$1"
return 0
}
uploadtoS3(){
tarsuffix=".zip"
dir=$(dirname $1)
file=$(basename $1)
cd "$dir" && aws s3 mv $file$tarsuffix s3://$S3_BUCKET/$ARCHIVE_FOLDER/$file$tarsuffix
echo "successfully uploaded"
return 0
}
if [ "$DBHOST" = "localhost" -o "$DBHOST" = "127.0.0.1" ]; then
    HOST=`hostname`
    if [ "$SOCKET" ]; then
        OPT="$OPT --socket=$SOCKET"
    fi
else
    HOST=$DBHOST
fi
check_is_secondary is_secondary
if [ "$is_secondary" = "false" ]; then
echo "ERROR:$DBHOST:$DBPORT is not a secondary server. Hence backup will not be taken"
exit 1
fi
echo ======================================================================
echo "Mongo Backup"
echo
echo
echo Backup of Database Server - $HOST on $DBHOST
echo ======================================================================
echo Backup Start `date`
echo ======================================================================
echo Daily Backup of All Databases is about to run.
echo
echo
FILE="$BACKUPDIR/daily/$DATE.$DOW"
mkdir -p $FILE
backupmongo $FILE && compressbackupfile $FILE && uploadtoS3 $FILE
echo ======================================================================
echo Backup End Time `date`
echo ======================================================================
echo Total disk space used for backup storage..
echo Size - Location
echo `du -hs "$BACKUPDIR"`
echo
echo ======================================================================
exec 1>&6 2>&7 6>&- 7>&-

if [ -s "$LOGERR" ]; then
        cat "$LOGFILE"
        echo
        echo "###### WARNING ######"
        echo "STDERR written to during mongodump execution."
        echo "The backup probably succeeded, as mongodump sometimes writes to STDERR, but you may wish to scan the error log below:"
        cat "$LOGERR"
    else
        cat "$LOGFILE"
    fi
STATUS=0
if [ -s "$LOGERR" ]; then
    STATUS=1
fi

# Clean up Logfile
rm -f "$LOGFILE" "$LOGERR"

exit $STATUS
