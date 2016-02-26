#!/bin/sh


##### VARIABLES
# the name to give to the LSF job (to be extended with additional info)
JOB_NAME="FILE_CLUSTER-EXPORTER"
# the job parameters that are going to be passed on to the job (build below)
JOB_PARAMETERS=""
# memory limit
MEMORY_LIMIT=15000
# LSF email notification
JOB_EMAIL="pride-report@ebi.ac.uk"
LOG_FILE_NAME="${JOB_NAME}"
VERSION="2015-04"
QUALITY="2"
$OUTPUT_DIRECTORY="/nfs/pride/work/cluster/cluster-file-exporter"


##### FUNCTIONS
printUsage() {
    echo "Description: File output generation for Clusters, these files will be used buy other consumers such as UNIPROT and ENSEMBL."
    echo "Usage: ./runFileGeneration.sh [-e|--email] "
    echo "     Example: ./runFileGeneration.sh -e pride-report@ebi.ac.uk"
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
bsub -M ${MEMORY_LIMIT} -R "rusage[mem=${MEMORY_LIMIT}]" -q production-rh6 -g /pride_cluster_export -o /dev/null -J ${JOB_NAME} ./runInJava.sh ./log/${LOG_FILE_NAME}.log ${MEMORY_LIMIT}m -cp cluster-file-exporter-1.0.0-SNAPSHOT.jar uk.ac.ebi.pride.tools.cluster.exporter.pipeline.exporter.ClusteringFileLoader -output ${OUPUT_DIRECTORY} -version ${VERSION} -quality ${QUALITY}