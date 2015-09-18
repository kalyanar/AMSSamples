#!/bin/bash
sourceserverip=localhost
sourceserverport=4502
sourceuser=admin
sourcepwd="admin"
targetserverip=localhost
targetserverport=4503
targetuser=admin
targetauth="admin:admin"
#In the rcp.properties set the content paths. Each path will be copied over in a separate child background process. 
#This is done so that , we can have parallel vlt push to the server , there by saving some time.
#A batch save value of 100 is optimal. Also set some throttle value of 10 ms when migrating pages.
#As always stop workflows before the operation and start those back after completing this.
#run this as a bckground process "sh vlttest.sh &"
sourcepaths=($(cat ./rcp.properties|sed -n -e '/^sourcecontentpath/p'))
targetpaths=($(cat rcp.properties| sed -n -e '/^targetcontentpath/p'))
today=$(date +'%d-%m-%Y')
logfilepath=logs/import_${today}.log
#set the vlt in the path
export PATH=$PATH:/usr/local/vault/bin
echo "started at: $(date)" > $logfilepath
pidarr=()
for i in "${!sourcepaths[@]}"
do
  :
   sourcepath=($(echo ${sourcepaths[$i]} | tr "=" "\n") )
   targetpath=($(echo ${targetpaths[$i]} | tr "=" "\n") )
   source="http://$sourceuser:$sourcepwd@$sourceserverip:$sourceserverport/crx/-/jcr:root${sourcepath[1]}"
target="http://$targetauth@$targetserverip:$targetserverport/crx/-/jcr:root${targetpath[1]}"

 vlt rcp -r -b 100 -n $source $target  |tee -a $logfilepath >> $logfilepath & 
 
pidarr+=($!)

  done
  #wait for all the vlt process to complete 
wait ${pidarr[@]}
#once all child processes are complete, end this operation.
echo "ended at: $(date)" >> $logfilepath;
exit 0
