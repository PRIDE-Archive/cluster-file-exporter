package uk.ac.ebi.pride.cluster.exporter.pipeline.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.Constants;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.ModificationMapper;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterReadDao;
import uk.ac.ebi.pride.spectracluster.repo.model.*;


import java.lang.Long;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



/**
 * Building a repository for accessing cluster database
 *
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public class ClusterRepositoryServices {

    private static final Logger logger = LoggerFactory.getLogger(ClusterRepositoryServices.class);

    /**
     * Cluster reader DAO enable to read information from
     * PRIDE Cluster Database.
     */
    private IClusterReadDao clusterReaderDao;

    /**
     * This lists contains all PSMs grouped by cluster IDs, the idea is been able to recompute and correct the rank of
     * each peptideform in the list.
     */
    private Map<PeptideForm, List<ClusteredPSMReport>> peptideReportMap;

    /**
     * Constructor to overwrite the clusterReaderDao object. The modFetcher provide a way to
     * convert String modifications to Modification objects.
     *
     * @param clusterReaderDao
     */
    public ClusterRepositoryServices(IClusterReadDao clusterReaderDao) {
        this.clusterReaderDao = clusterReaderDao;
        peptideReportMap = new ConcurrentHashMap<PeptideForm, List<ClusteredPSMReport>>();
    }

    /**
     * Read all clusters Ids from the database and compile them into a general List
     * @return
     */
    public List<Long> getAllClusterIds(){
        List<Long> ids = new ArrayList<java.lang.Long>();

        long totalClusters = clusterReaderDao.getNumberOfClusters();
        int nPage = (int) (totalClusters/Constants.CLUSTER_READ_STEP) + 2;

        logger.debug("Number of Clusters in Release: " + totalClusters + " clusters");

        for(int i = 1; i < nPage ; i++){
            ids.addAll(clusterReaderDao.getAllClusterIds(i, Constants.CLUSTER_READ_STEP).getPageItems());
            logger.debug("Step:" + i + "\t" + ids.size());
        }
        return ids;
    }

    /**
     * Get the spectra by Quality Filter
     * @param clusterQualityFilter
     * @return
     */
    public List<java.lang.Long> getClusterIdsByQuality(ClusterQuality clusterQualityFilter){

        List<java.lang.Long> ids = new ArrayList<java.lang.Long>();

        long totalClusters = clusterReaderDao.getNumberOfClustersByQuality(clusterQualityFilter);
        int nPage = (int) (totalClusters/Constants.CLUSTER_READ_STEP) + 2;

        logger.debug("Number of HighQuality Clusters in Release: " + totalClusters + " clusters");

        for(int i = 1; i < nPage; i++){
            ids.addAll(clusterReaderDao.getAllClusterIdsByQuality(i, Constants.CLUSTER_READ_STEP, clusterQualityFilter).getPageItems());
            logger.debug("Step:" + i + "\t" + ids.size());
        }

        return ids;

    }

    /**
     * Read all clusters Ids from the database and compile them into a general List
     * @return
     */
    public List<ClusterSummary> getAllClusterSummaries(){
        List<ClusterSummary> clusters = new ArrayList<ClusterSummary>();

        long totalClusters = clusterReaderDao.getNumberOfClusters();
        int nPage = (int) (totalClusters/Constants.CLUSTER_READ_STEP) + 2;

        logger.debug("Number of Clusters in Release: " + totalClusters + " clusters");

        for(int i = 1; i < nPage ; i++){
            clusters.addAll(clusterReaderDao.getAllClusterSummaries(i, Constants.CLUSTER_READ_STEP).getPageItems());
            logger.debug("Step:" + i + "\t" + clusters.size());
        }

        return clusters;
    }

    /**
     * Get the spectra by Quality Filter
     * @param clusterQualityFilter
     * @return
     */
    public List<ClusterSummary> getClusterSummariesByQuality(ClusterQuality clusterQualityFilter){

        List<ClusterSummary> clusters = new ArrayList<ClusterSummary>();

        long totalClusters = clusterReaderDao.getNumberOfClustersByQuality(clusterQualityFilter);
        int nPage = (int) (totalClusters/Constants.CLUSTER_READ_STEP) + 2;

        logger.debug("Number of HighQuality Clusters in Release: " + totalClusters + " clusters");

        for(int i = 1; i < nPage; i++){
            clusters.addAll(clusterReaderDao.getAllClusterSummariesByQuality(i, Constants.CLUSTER_READ_STEP, clusterQualityFilter).getPageItems());
            logger.debug("Step:" + i + "\t" + clusters.size());
        }

        return clusters;

    }


    /**
     * Get The Cluster details List including the Cluster information such as
     * Peptides, Assays, etc.
     *
     */
    public  void buildPeptidePSMReportLists(ClusterQuality quality){

        ConcurrentHashMap<Long, List<ClusteredPSMReport>> clusterReportMap = new ConcurrentHashMap<Long, List<ClusteredPSMReport>>();

        List<AssayReport> assays = clusterReaderDao.readFullAssaySet();
        Map<java.lang.Long, AssayReport> assayMap = new HashMap<java.lang.Long, AssayReport>();
        if(assays != null){
            for(AssayReport assayReport: assays)
                assayMap.put(assayReport.getId(), assayReport);
        }


        java.lang.Long numberCLusteredPSMs = clusterReaderDao.getNumberClusteredPSMs(quality);
        int nPage = (int) (numberCLusteredPSMs/Constants.CLUSTER_READ_STEP) + 2;

        for(int i = 1; i < nPage; i++){

            long time = System.currentTimeMillis();

            List<ClusteredPSMReport> psmReportList = clusterReaderDao.getClusteredPSMsReport(i, Constants.CLUSTER_READ_STEP, numberCLusteredPSMs, quality).getPageItems();

            psmReportList = psmReportList.parallelStream().map(psm -> {
                List<ModificationProvider> modifications = ModificationMapper.asModifications(psm.getModifications(), psm.getSequence());
                if(ModificationMapper.checkWrongAnnotation(modifications, psm.getSequence()))
                    psm.addWrongAnnotation(Constants.PTM_WRONG_ANNOTATED);
                psm.setModifications(modifications);
                psm.setAssay(assayMap.get(psm.getAssayID()));
                return psm;
            }).collect(Collectors.toList());

            // This groups the data using the clusterId and return a map ClusterID -> PSMReport
            Map<Long, List<ClusteredPSMReport>> map = psmReportList.parallelStream().collect(Collectors.groupingBy(a -> a.getClusterId()));

            map.entrySet().parallelStream().forEach(e -> clusterReportMap.merge(e.getKey(), e.getValue(), (v1, v2) -> {
                Set<ClusteredPSMReport> set = new TreeSet<>(v1);
                set.addAll(v2);
                return new ArrayList<>(set);
            }));

            logger.debug("Number of Peptide: " + map.size());
            logger.debug("Milliseconds Time: " + (System.currentTimeMillis() - time) + " for " + psmReportList.size());
            break;
        }

        clusterReportMap.entrySet().parallelStream().forEach(
                (cluster) -> {

                    Map<PeptideForm, List<ClusteredPSMReport>> clustered = cluster.getValue().parallelStream().collect(Collectors.groupingBy(psm -> psm.getPeptideForm()));

                    clustered = clustered.entrySet().stream()
                            .sorted(Comparator.<Map.Entry<PeptideForm, List<ClusteredPSMReport>>>comparingInt(e->e.getValue().size()).reversed())
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (a,b) -> {throw new AssertionError();},
                                    LinkedHashMap::new
                            ));

                    Integer sum = clustered.values().stream().mapToInt(List::size).sum();
                    int rank = 0;
                    int currentSize = -1;
                    for(Map.Entry peptideFormEntry: clustered.entrySet()){
                        List<ClusteredPSMReport> psms = (List<ClusteredPSMReport>) peptideFormEntry.getValue();
                        for(ClusteredPSMReport psm: psms){
                            if(currentSize != psms.size()){
                                rank++;
                                currentSize = psms.size();
                            }
                            psm.setRank(rank);
                            psm.setPsmRatio(((float)psms.size()/(float)sum));
                        }

                    }
                }
        );

        peptideReportMap = clusterReportMap.entrySet().parallelStream()
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList())
                .parallelStream().collect(Collectors.groupingBy(psm -> psm.getPeptideForm()));

    }

    public Map<PeptideForm, List<ClusteredPSMReport>> getPeptideReportMap() {
        return peptideReportMap;
    }
}
