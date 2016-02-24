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
 * - Sequence String: Sequence String
 * - List<ModificationProvider> : List of modifications including the positions
 * - numSpectra : Number of spectra that has identified this particular peptide
 * - clusterMaxPeptideRatio: This is the best score for the cluster
 * - Cluster number of projects
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/02/2016
 */
public class PSMReport {

    //Cluster ID for the current PSM.
    private Long clusterID;

    // Sequence String for the current PSM
    private String sequence;

    // List of modifications
    private List<ModificationProvider> modificationProviders;

    // Number of spectra where this psms has been identified
    private int numSpectra = 0;

    // peptide cluster Ratio fo the specific PMS
    private double clusterMaxPeptideRatio;

    // The number of Projects for the particular PSMS
    private long clusterNumberOFProjects;

    private Set<String> proteinAccession;

    // Project Accession including in the PSM
    private Set<String> projectAccession;

    private Set<Specie> specie;

    /**
     * Default constructor contains the clusterID, the sequence and Modifications
     * @param clusterID
     * @param sequence
     * @param modificationProviders
     */
    public PSMReport(Long clusterID, String sequence, List<ModificationProvider> modificationProviders) {
        this.clusterID = clusterID;
        this.sequence = sequence;
        this.modificationProviders = modificationProviders;
        this.proteinAccession = new HashSet<String>();
        this.projectAccession = new HashSet<String>();
        this.specie = new HashSet<Specie>();
    }

    /**
     * This is the constructor that contains the detials of the psms list the scrores
     * @param sequence the sequence of the peptide
     * @param modifications List of mofications
     * @param cluster The cluster details
     */
    public PSMReport(String sequence, List<ModificationProvider> modifications, ClusterDetail cluster){
        this(cluster.getId(), sequence, modifications);
        this.clusterNumberOFProjects = cluster.getNumberOfProjects();
        this.clusterMaxPeptideRatio = cluster.getMaxPeptideRatio();
    }

    public Long getClusterID() {
        return clusterID;
    }

    public void setClusterID(Long clusterID) {
        this.clusterID = clusterID;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<ModificationProvider> getModificationProviders() {
        return modificationProviders;
    }

    public void setModificationProviders(List<ModificationProvider> modificationProviders) {
        this.modificationProviders = modificationProviders;
    }

    /**
     * Get the number of Spectra for a particulas PSMs
     * @return number of spectra
     */
    public int getNumSpectra() {
        return numSpectra;
    }

    /**
     * Increase the number of spectra
     */
    public void increaseSpectra() {
        this.numSpectra++;
    }

    public double getClusterMaxPeptideRatio() {
        return clusterMaxPeptideRatio;
    }

    public void setClusterMaxPeptideRatio(int clusterMaxPeptideRatio) {
        this.clusterMaxPeptideRatio = clusterMaxPeptideRatio;
    }

    public long getClusterNumberOFProjects() {
        return clusterNumberOFProjects;
    }

    public void setClusterNumberOFProjects(int clusterNumberOFProjects) {
        this.clusterNumberOFProjects = clusterNumberOFProjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSMReport)) return false;

        PSMReport psmReport = (PSMReport) o;

        if (!clusterID.equals(psmReport.clusterID)) return false;
        if (!sequence.equals(psmReport.sequence)) return false;
        return !(modificationProviders != null ? !modificationProviders.equals(psmReport.modificationProviders) : psmReport.modificationProviders != null);

    }

    @Override
    public int hashCode() {
        int result = clusterID.hashCode();
        result = 31 * result + sequence.hashCode();
        result = 31 * result + (modificationProviders != null ? modificationProviders.hashCode() : 0);
        return result;
    }

    /**
     * This functionality add a new protein Accession to the List of proteins
     * @param proteinAccession
     */
    public void addProteinAccession(String proteinAccession){
        if(proteinAccession != null && !proteinAccession.isEmpty())this.proteinAccession.add(proteinAccession);
    }

    public void addProjectAccession(String projectAccession){
        if(projectAccession != null && !projectAccession.isEmpty())
            this.projectAccession.add(projectAccession);
    }

    public void addSpecie(String taxonomy, String specieName){
        if(taxonomy!=null && specieName!= null)
            this.specie.add(new Specie(taxonomy,specieName, null));
    }



}
