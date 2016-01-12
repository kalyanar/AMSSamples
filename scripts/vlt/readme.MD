create a logs folder underthis.
in rcp.properties , add the various content paths in the following format.


sourcecontentpath=/content/geometrixx/en
targetcontentpath=/content/geometrixx_en
sourcecontentpath_1=/content/geometrixx/fr
targetcontentpath_1=/content/geometrixx_fr1

Each of this will run in parallel.



run as "sh vlttest.sh &"

To view the paths that were not migrated successfully, run this command on the logfile as follows
suppose import_11-09-2015.log  is the log file.

cat logs/import_11-09-2015.log | grep "ERROR" | awk '{print $7 $8}' | sort -u | awk -F ':' '{print $1 "\t" $2}'

NOTE:
Copy over your vault to /usr/local/vault . The vlt executable should be at /usr/local/vault/bin
