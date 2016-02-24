#!/bin/sh


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="data-provider"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
MEMORY_LIMIT=15000
# LSF email notification
JOB_EMAIL="pride-report@ebi.ac.uk"

##### FUNCTIONS
printUsage() {
    echo "Description: File output generation for Clusters, these files will be used buy other consumers such as UNIPROT and ENSEMBL."
    echo "Usage: ./runDataProvider.sh [-e|--email] "
    echo "     Example: ./runDataProvider.sh -e pride-report@ebi.ac.uk"
    echo "     (optional) email   :  Email to send LSF notification"
}


##### PARSE the provided parameters
while [ "$1" != "" ]; do
    case $1 in
      "-e" | "--email")
        shift
        JOB_EMAIL=$1
        ;;
    esac
    shift
done


##### RUN it on the production LSF cluster #####
##### NOTE: you can change LSF group to modify the number of jobs can be run concurrently #####
bsub -e error.txt -o output.txt -M ${MEMORY_LIMIT} -q production-rh6 -J ${JOB_NAME} -N -u ${JOB_EMAIL} java -jar ${project.build.finalName}.jar
