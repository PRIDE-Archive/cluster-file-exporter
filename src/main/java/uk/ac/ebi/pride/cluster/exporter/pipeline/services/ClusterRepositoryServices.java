package uk.ac.ebi.pride.cluster.exporter.pipeline.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.PSMReport;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.PeptideReport;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.Constants;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.ModificationMapper;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.PropertyUtils;
import uk.ac.ebi.pride.jmztab.model.SplitList;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterReadDao;
import uk.ac.ebi.pride.spectracluster.repo.model.*;
import uk.ac.ebi.pride.spectracluster.repo.utils.ModificationDetailFetcher;


import java.io.InputStream;
import java.util.*;



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
     * Modification Details Fetcher provides a way to standarize the modifications
     * from cluster.
     */
    private ModificationDetailFetcher modFetcher;

    /**
     * This lists contains all the peptides collapsed for the list of clusters
     * PeptideReport is the combination of the Sequence + modification-localization
     */
    private List<PeptideReport> peptideReportList;

    /**
     * This list contains all psms collapsed for a particular cluster
     * List<PSMReport> contains all the psm  Report
     */
    private List<PSMReport> psmReportList;


    /**
     * Constructor to overwrite the clusterReaderDao object. The modFetcher provide a way to
     * convert String modifications to Modification objects.
     *
     * @param clusterReaderDao
     */
    public ClusterRepositoryServices(IClusterReadDao clusterReaderDao) {
        this.clusterReaderDao = clusterReaderDao;
        InputStream unimod = PropertyUtils.loadModificationStream("mod/unimod.xml");
        InputStream psiMod = PropertyUtils.loadModificationStream("mod/PSI-MOD.obo");
        peptideReportList = new ArrayList<PeptideReport>();
        psmReportList = new ArrayList<PSMReport>();
        modFetcher = new ModificationDetailFetcher(psiMod, unimod);
    }

    /**
     * Read all clusters Ids from the database and compile them into a general List
     * @return
     */
    public List<Long> getAllClusterIds(){
        List<Long> ids = new ArrayList<Long>();

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
    public List<Long> getClusterIdsByQuality(ClusterQuality clusterQualityFilter){

        List<Long> ids = new ArrayList<Long>();

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
     * List of PeptideReports. It should be build using the method buildPeptidePSMReportLists
     * @return List<PeptideReport >
     */
    public List<PeptideReport> getPeptideReportList() {
        return peptideReportList;
    }

    /**
     * Get The Cluster details List including the Cluster information such as
     * Peptides, Assays, etc.
     *
     * @param clusterIds clusters Identifiers
     * @return List of possible clusters.
     */
    public  void buildPeptidePSMReportLists(List<Long> clusterIds){

        for (Long clusterId : clusterIds) {

            ClusterDetail cluster = clusterReaderDao.findCluster(clusterId);

            List<ClusteredPSMDetail> psmDetails = cluster.getClusteredPSMDetails();

            for(ClusteredPSMDetail psmDetail: psmDetails){
                if (psmDetail.getRank() < 2.0f) {   // This filter all psms for the first consensus peptide

                    if(peptideReportList.size() == 3326){
                        System.out.println("NP");
                    }
                    //Create the Peptide
                    String sequence = psmDetail.getSequence();
                    List<ModificationProvider> modifications = ModificationMapper.asModifications(psmDetail.getPsmDetail().getAnchorModifications(),modFetcher);
                    AssayDetail assayDetail = cluster.getAssayDetail(psmDetail.getPsmDetail().getAssayId());

                    PeptideReport peptideReport = new PeptideReport(sequence, modifications);
                    peptideReport.addProteinAccession(psmDetail.getPsmDetail().getProteinAccession());

                    // Create the PSM Cluster
                    PSMReport psmReport = new PSMReport(sequence, modifications, cluster, psmDetail.getRank());

                    int index;
                    if((index = psmReportList.indexOf(psmReport)) != -1){
                        PSMReport currentPSM = psmReportList.get(index);
                        currentPSM.increaseSpectra();
                        currentPSM.addProteinAccession(psmDetail.getPsmDetail().getProteinAccession());
                        currentPSM.addSpecie(assayDetail.getTaxonomyId(), assayDetail.getSpecies());
                        currentPSM.addProjectAccession(assayDetail.getProjectAccession());
                        currentPSM.setBestRank(psmDetail.getRank());
                    }else{
                        psmReport.increaseSpectra();
                        psmReport.addProteinAccession(psmDetail.getPsmDetail().getProteinAccession());
                        psmReport.addSpecie(assayDetail.getTaxonomyId(), assayDetail.getSpecies());
                        psmReport.addProjectAccession(assayDetail.getProjectAccession());
                        psmReportList.add(psmReport);
                    }

                    if((index = peptideReportList.indexOf(peptideReport)) != -1){
                        PeptideReport currentPeptide = peptideReportList.get(index);
                        currentPeptide.addClusterDetails(cluster);
                        currentPeptide.setBestRank(psmDetail.getRank());
                        currentPeptide.addSpecie(assayDetail.getSpecies(), assayDetail.getTaxonomyId());
                        currentPeptide.addProteinAccession(psmDetail.getPsmDetail().getProteinAccession());
                        currentPeptide.addProjectAccessions(assayDetail.getProjectAccession());
                        currentPeptide.increaseSpectra();
                    }else{
                        peptideReport.addClusterDetails(cluster);
                        peptideReport.addSpecie(assayDetail.getSpecies(), assayDetail.getTaxonomyId());
                        peptideReport.addProjectAccessions(assayDetail.getProjectAccession());
                        peptideReport.addProteinAccession(psmDetail.getPsmDetail().getProteinAccession());
                        peptideReport.setBestRank(psmDetail.getRank());
                        peptideReport.getBestClusterPeptideRatio();
                        peptideReport.increaseSpectra();
                        peptideReportList.add(peptideReport);
                    }

                    logger.debug("Number of Peptide: " + peptideReportList.size());
                }
            }
        }

    }

    /**
     * Retrieve the PSM Report List
     * @return List<PSMReport> List of reported PSMs
     */
    public List<PSMReport> getPsmReportList() {
        return psmReportList;
    }
}
