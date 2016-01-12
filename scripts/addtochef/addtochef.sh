#!/bin/bash
if [ -z "$6" ]
then
	echo -e "\nUsage:"
	echo -e "\naddtochef.sh <topology name from mscentral> <AWS CLI Profile> <AWS Region> <bigbearid> <sshuser> <ssh_password>"
	echo -e "\nExample:"
	echo -e "addtochef.sh taj-stage bpbu11 ap-southeast-1 bigbearid sshuser sshpassword\n\n"
	echo "** Search string is CASE SENSITIVE ** (i.e., \"a\" != \"A\")"
	echo -e "\n"
	exit 0
fi
export EDITOR=vi
logFile="addtochef.log"
topologyName=$1
profile=$2
aws_region=$3
bigbearid=$4
user=$5
password=$6
aws_region_formatted=${aws_region//-/}
if [ ! -f ~/.aws/config ]
then
	echo "AWS CLI appears to not be installed (could not find ~/.aws/config)"
	echo "Download https://s3.amazonaws.com/aws-cli/awscli-bundle.zip using wget or curl, and following instructions at http://docs.aws.amazon.com/cli/latest/userguide/installing.html#install-bundle-other-os"
	exit 0
fi
 
if ! grep -q $profile ~/.aws/config
then
	echo "AWS CLI profile "${profile}" not found."
	exit 0
fi
knife role create $topologyName

echo "----------------------------" >> $logFile
echo "Searching AWS EC2 for\""${topologyName}"\"..." >> $logFile
echo -e "\n"
echo -ne "Searching AWS EC2 for\""${topologyName}"\".\r"
descinstances=`aws --profile awsprod ec2 describe-instances --filters "Name=tag:adobe:ms:topology,Values=$topologyName"`
instances=`echo $descinstances | jq .Reservations | jq ".[] | .Instances" | jq ".[] |  .PublicIpAddress" | cut -d\" -f2`
for instance in  $instances; 
do
echo "Adding the ip to the chef server authorize-security-group-ingress  sg-5bbe5b68" >> $logFile
servername=`echo $descinstances| jq .Reservations | jq ".[] | .Instances" | jq --arg instance "$instance" '.[] | select(.PublicIpAddress == $instance)' | jq .Tags | jq '.[] | select(.Key=="Name")  ' | jq .Value | cut -d\" -f2`
logicalid=`echo $descinstances| jq .Reservations | jq ".[] | .Instances" | jq --arg instance "$instance" '.[] | select(.PublicIpAddress == $instance)' | jq .Tags | jq '.[] | select(.Key=="adobe:ms:logical-id")  ' | jq .Value | cut -d\" -f2`
aws ec2 authorize-security-group-ingress --group-id sg-5bbe5b68 --profile uswest1prod --protocol tcp --port 443 --cidr $instance/32
echo "instance $instance has logicalid $logicalid and servername $servername"
knife bootstrap $instance -x $user -P $password   --sudo
curl -D - -X POST -d '{ "actionId" : "refresh-chef-json" }' -H 'Content-Type: application/json' http://bigbear.ams.adobe.net:443/cxf/bigbear/api/v0.1.0/topologies/$bigbearid/$logicalid/actions
role="role[$topologyName]"
knife node run_list add $servername $role
done;
