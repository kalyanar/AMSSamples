#!/bin/bash
sourceserverip=localhost
sourceserverport=4502
sourceuser=admin
sourcepwd="(%40APr3P0d%23)"
targetserverip=52.69.196.150
targetserverport=4502
targetuser=admin
targetpwd="y~oHo<-1y71C"
targetauth="admin:y~oHo%3C-1y71C"
wf=''
#workflows to be disabled
wfstobedisabled=($(cat ./rcp.properties| tr -d '\r'|sed -n -e '/^workflow/p'))
sourcepaths=($(cat ./rcp.properties| tr -d '\r'|sed -n -e '/^sourcecontentpath/p'))
targetpaths=($(cat rcp.properties| tr -d '\r'| sed -n -e '/^targetcontentpath/p'))
today=$(date +'%d-%m-%Y-%H-%M-%S')
logfilepath=logs/vltsync_${today}.log
export PATH=$PATH:/home/kalyanar/vault-cli-3.1.16/bin
export LD_LIBRARY_PATH=/usr/local/lib
echo -e "Disabling workflows" | tee -a $logfilepath
for i in "${wfstobedisabled[@]}"
do
 :
  IFS='=' read -a workflow <<< "$i"

  wfdisablejson=`curl -u "$targetuser:$targetpwd" -Fenabled=false -H"Accept: application/json" http://$targetserverip:$targetserverport${workflow[1]}`
status=`echo $wfdisablejson | /usr/local/bin/jshon -e status.message -u` 
title=`echo $wfdisablejson | /usr/local/bin/jshon -e title -u` 
if [ "$status" != "OK" ]; then
    echo -e "Error: unable to disable ${workflow[1]}! Status message: $status" | tee -a $logfilepath
    exit 1
fi
echo -e "$title: $status" |tee -a $logfilepath
done
echo -e "Disabled the workflows" | tee -a $logfilepath
started=$(date +%s%3N)
echo "started at: $(date)" >> $logfilepath
pidarr=()
for i in "${!sourcepaths[@]}"
do
  :
  IFS='=' read -a sourcepath <<< "${sourcepaths[$i]}"
  IFS='=' read -a targetpath <<< "${targetpaths[$i]}"
  source="http://$sourceuser:$sourcepwd@$sourceserverip:$sourceserverport/crx/-/jcr:root${sourcepath[1]}"
   target="http://$targetauth@$targetserverip:$targetserverport/crx/-/jcr:root${targetpath[1]}"
   vlt rcp -r -b 100  -n $source $target  |tee -a $logfilepath >> $logfilepath &
pidarr+=($!)   
done
wait ${pidarr[@]}
ended=$(date +%s%3N)
timetaken=`expr $ended - $started`
echo "ended at: $(date)" >> $logfilepath;
echo "took $timetaken milliseconds"
echo "activating to publish"
for i in "${!sourcepaths[@]}"
do
  :
  IFS='=' read -a targetpath <<< "${targetpaths[$i]}"
  curl -s -u $targetauth -X POST -Fpath=${targetpath[1]} -Fcmd=activate -Fonlymodified=true http://$targetserverip:$targetserverport/bin/replicate.json | tee -a $logfilepath  
done
 
echo "activated to publish"
echo -e "Renabling workflows" | tee -a $logfilepath
for i in "${wfstobedisabled[@]}"
do
 :
  IFS='=' read -a workflow <<< "$i"

  wfdisablejson=`curl -u "$targetuser:$targetpwd" -Fenabled=true -H"Accept: application/json" http://$targetserverip:$targetserverport${workflow[1]}`
status=`echo $wfdisablejson | /usr/local/bin/jshon -e status.message -u` 
title=`echo $wfdisablejson | /usr/local/bin/jshon -e title -u` 
if [ "$status" != "OK" ]; then
    echo -e "Error: unable to enable ${workflow[1]}! Status message: $status" | tee -a $logfilepath
    exit 1
fi
echo -e "$title: $status" |tee -a $logfilepath
done
echo -e "Reenabled the workflows" | tee -a $logfilepath

exit 0

# cat vltsync_22-09-2015.log | grep "Exception" | awk -F ":" '{print  $2 "\t" $4$5$6}'

