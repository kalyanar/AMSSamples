#!/bin/bash
sourceserverip=localhost
sourceserverport=4502
sourceuser=admin
sourcepwd=admin
targetserverip=localhost
targetserverport=4503
targetuser=admin
targetpwd=admin
. ./rcp.properties
sourcepaths=($(cat ./rcp.properties| sed -n -e '/^sourcecontentpath/p'))
targetpaths=($(cat ./rcp.properties| sed -n -e '/^targetcontentpath/p'))
today=$(date +'%d-%m-%Y')
logfilepath=logs/import_${today}.log
echo "started at: $(date)" > $logfilepath
pidarr=()
for i in "${!sourcepaths[@]}"
do
  :
   sourcepath=($(echo ${sourcepaths[$i]} | tr "=" "\n") )
   targetpath=($(echo ${targetpaths[$i]} | tr "=" "\n") )
   source="http://$sourceuser:$sourcepwd@$sourceserverip:$sourceserverport/crx/-/jcr:root${sourcepath[1]}"
target="http://$targetuser:$targetpwd@$targetserverip:$targetserverport/crx/-/jcr:root${targetpath[1]}"

 vlt rcp -r -b 100 -n $source $target  |tee -a $logfilepath >> $logfilepath & 
 
pidarr+=($!)

  done
wait ${pidarr[@]}
echo "ended at: $(date)" >> $logfilepath;
exit 0
