package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusteredPSMReport;

import java.util.*;

/**
 * This class is used to Report all the peptide evidences in PRIDE Cluster for a particular Release, the
 * the file consider as peptide Sequence + Modifications.
 * In addition, we provide some important information related with each peptide:
 *   - Map<Long, Clusters> all clusters where the current peptide has been identified.
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/02/2016
 */
public class PeptideReport extends AbstractReport{

    private Map<Long, ClusterReport> clusterID;

    private  double bestClusterPeptideRatio;

    /**
     * The default constructor is the one with Sequence + List Modification
     * @param sequence
     * @param modificationProvider
     */
    public PeptideReport(String sequence, List<ModificationProvider> modificationProvider) {
        this.sequence = sequence;
        this.modifications = modificationProvider;
        this.proteinAccession = new HashSet<String>();
        this.clusterID = new HashMap<Long, ClusterReport>();
        this.projectAccession = new HashSet<String>();
    }

    /**
     * The default constructor is the one with Sequence + List Modification
     * @param sequence
     * @param modificationProvider
    **/
    public PeptideReport(String sequence, List<ModificationProvider> modificationProvider, int rank) {
        this.sequence = sequence;
        this.modifications = modificationProvider;
        this.proteinAccession = new HashSet<String>();
        this.clusterID = new HashMap<Long, ClusterReport>();
        this.projectAccession = new HashSet<String>();
        this.bestRank = (this.bestRank < rank)? rank: bestRank;
    }

    /**
     * Add a new Cluster to the Peptide
     * @param cluster
     */
    public void addClusterDetails(ClusterDetail cluster){
        if(!clusterID.containsKey(cluster.getId())){
            ClusterReport reportCluster = new ClusterReport(cluster);
            clusterID.put(cluster.getId(), reportCluster);
        }
    }

    public void addClusterDetails(ClusteredPSMReport psmDetail){
        if(!clusterID.containsKey(psmDetail.getClusterId())){
            ClusterReport reportCluster = new ClusterReport(psmDetail.getClusterId(), psmDetail.getClusterMaxPeptideRatio(),
                    psmDetail.getClusterNumberPSMs(), psmDetail.getClusterAvgCharge(), psmDetail.getClusterAvgMz(), psmDetail.getClusterNumberProjects(),
                    psmDetail.getQuality());
            clusterID.put(psmDetail.getClusterId(), reportCluster);
        }
    }

    public Map<Long, ClusterReport> getClusterID() {
        return clusterID;
    }

    public void setClusterID(Map<Long, ClusterReport> clusterID) {
        this.clusterID = clusterID;
    }

    /**
     * Retrieve the best peptide ratio score.
     * @return
     */
    public Double getBestClusterPeptideRatio() {
        if(this.clusterID != null){
            double bestClusterPeptideRatio = -1;
            for(ClusterReport clusterReport: this.getClusterID().values()){
                if(clusterReport.getMaxPeptideRatio() > bestClusterPeptideRatio){
                    bestClusterPeptideRatio = clusterReport.getMaxPeptideRatio();
                }
            }
            this.bestClusterPeptideRatio = bestClusterPeptideRatio;
        }
        return bestClusterPeptideRatio;
    }

    /**
     * Get the number of clusters where the present peptide has been found
     * @return
     */
    public int getNumberClusters() {
        return (this.clusterID != null)?this.clusterID.size():0;
    }

    @Override
    public String toString() {
        return "PeptideReport{" +
                "sequence='" + sequence + '\'' +
                ", modificationProvider=" + modifications +
                '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
