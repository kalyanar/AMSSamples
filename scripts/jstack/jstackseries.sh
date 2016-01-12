#!/bin/bash
if [ $# -eq 0 ]; then
    echo >&2 "Usage: jstackSeries <pid> <run_user> [ <count> [ <delay> ] ]"
    echo >&2 "    Defaults: count = 10, delay = 0.5 (seconds)"
    exit 1
fi
JSTACK_PATH=`which jstack`
PWD=`pwd`
pid=$1          # required
user=$2         # required
count=${3:-10}  # defaults to 10 times
delay=${4:-0.5} # defaults to 0.5 seconds
while [ $count -gt 0 ]
do
    filename="jstack.$pid.$(date +%H%M%S.%N)"
sudo -u $user $JSTACK_PATH -l $pid >$filename.out
EXINF=$(ps -eLo pcpu,lwp,pmem,etime,cputime,cmd |grep java| grep -v grep | sort -k1,1 -n| while read al; do nid=$(printf "0x%x" $(echo "$al" | awk '{print $2}')); echo "$nid $al"; done)
echo "$EXINF" | while read al; do
    NID=$(echo "$al" | awk '{print $1}')
    ETIME=$(echo "$al" | awk '{print $5}' | tr '-' ':' | awk -F: '{ total=0; m=1; } { for (i=0; i < NF; i++) {total += $(NF-i)*m; m *= i >= 2 ? 24 : 60 }} {print total}')
      ESTR="$(echo "$al" | awk '{print "pcpu%=" $2  "  memory%=" $4 }')"
 ADDNLINFO="$ESTR -- running for $ETIME seconds --  "
echo "sed -i \"s/nid=$NID/nid=$NID $ADDNLINFO/\" $PWD/$filename.out"
done

sleep $delay
    let count--
    echo -n "."
done
