package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.PSMDetail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PSM report can be seen as the peptide+modification in a particular cluster. Different to
 * PEPTIDES the psms are specific to a cluster. Some of the properties:
 *
 * - clusterID: Cluster ID
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/02/2016
 */
public class PSMReport extends AbstractReport{

    //Cluster ID for the current PSM.
    private Long clusterID;

    /**
     * Default constructor contains the clusterID, the sequence and Modifications
     * @param clusterID
     * @param sequence
     * @param modificationProviders
     */
    public PSMReport(Long clusterID, String sequence, List<ModificationProvider> modificationProviders) {
        this.clusterID = clusterID;
        this.sequence = sequence;
        this.modifications = modificationProviders;
        this.proteinAccession = new HashSet<String>();
        this.projectAccession = new HashSet<String>();
        this.species = new HashSet<Specie>();
    }

    /**
     * This is the constructor that contains the detials of the psms list the scrores
     * @param sequence the sequence of the peptide
     * @param modifications List of mofications
     * @param cluster The cluster details
     */
    public PSMReport(String sequence, List<ModificationProvider> modifications, ClusterDetail cluster, float rank){
        this(cluster.getId(), sequence, modifications);
        this.clusterMaxPeptideRatio = cluster.getMaxPeptideRatio();
        this.bestRank = (bestRank < rank)?rank: bestRank;
    }

    public Long getClusterID() {
        return clusterID;
    }

    public void setClusterID(Long clusterID) {
        this.clusterID = clusterID;
    }

    public double getClusterMaxPeptideRatio() {
        return clusterMaxPeptideRatio;
    }

    public void setClusterMaxPeptideRatio(int clusterMaxPeptideRatio) {
        this.clusterMaxPeptideRatio = clusterMaxPeptideRatio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSMReport)) return false;
        if (!super.equals(o)) return false;

        PSMReport psmReport = (PSMReport) o;

        return clusterID.equals(psmReport.clusterID);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + clusterID.hashCode();
        return result;
    }
}
