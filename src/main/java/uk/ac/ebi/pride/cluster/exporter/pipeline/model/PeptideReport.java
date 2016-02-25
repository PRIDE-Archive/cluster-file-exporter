package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusteredPSMDetail;

import java.util.*;

/**
 * This class is used to Report all the peptide evidences in PRIDE Cluster for a particular Release, the
 * the file consider as peptide Sequence + Modifications.
 * In addition, we provide some important information related with each peptide:
 *   - List<String> proteinIDs original identifiers in PRIDE.
 *   - List<String> projectIDs original PX identifiers from PRIDE
 *   - Map<Long, Clusters> all clusters where the current peptide has been identified.
 *   - List<Specie> A list of species where the current peptide has been identified
 *   - List<ModificationProvider> A list of modifications associated with the Peptide
 *   - bestSearchEngineScore The best searchEngine score from the original submissions.
 *   - bestClusterScore The best Cluster score from all the clusters where this peptide has been reported.
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/02/2016
 */
public class PeptideReport {

    private String sequence;

    private Set<String> proteinAccessions;

    private Set<String> projectAccessions;

    private Map<Long, ClusterReport> clusterID;

    private Set<Specie> species;

    private List<ModificationProvider> modificationProvider;

    private Double bestClusterPeptideRatio;

    private float bestRank = -1;

    // Number of spectra where this particular PSMs has been idenfitied
    private int numberSpectra = 0;


    /**
     * The default constructor is the one with Sequence + List Modification
     * @param sequence
     * @param modificationProvider
     */
    public PeptideReport(String sequence, List<ModificationProvider> modificationProvider) {
        this.sequence = sequence;
        this.modificationProvider = modificationProvider;
        this.proteinAccessions = new HashSet<String>();
        this.clusterID = new HashMap<Long, ClusterReport>();
        this.projectAccessions = new HashSet<String>();
    }

    /**
     * The default constructor is the one with Sequence + List Modification
     * @param sequence
     * @param modificationProvider
     */
    public PeptideReport(String sequence, List<ModificationProvider> modificationProvider, int rank) {
        this.sequence = sequence;
        this.modificationProvider = modificationProvider;
        this.proteinAccessions = new HashSet<String>();
        this.clusterID = new HashMap<Long, ClusterReport>();
        this.projectAccessions = new HashSet<String>();
        this.bestRank = (this.bestRank < rank)? rank: bestRank;
    }

    public void setBestRank(float rank) {
        this.bestRank = (this.bestRank < rank)? rank: bestRank;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }


    public List<ModificationProvider> getModificationProvider() {
        return modificationProvider;
    }

    public void setModificationProvider(List<ModificationProvider> modificationProvider) {
        this.modificationProvider = modificationProvider;
    }

    public Set<String> getProteinAccession() {
        return proteinAccessions;
    }

    public Set<String> getProjectAccessions() {
        return projectAccessions;
    }

    public void addProjectAccessions(String projectAccessions) {
        if(projectAccessions != null)
            this.projectAccessions.add(projectAccessions);
    }

    public Set<Specie> getSpecies() {
        return species;
    }

    public void setSpecies(Set<Specie> species) {
        this.species = species;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeptideReport)) return false;

        PeptideReport that = (PeptideReport) o;

        if (!sequence.equals(that.sequence)) return false;
        return !(modificationProvider != null ? !modificationProvider.equals(that.modificationProvider) : that.modificationProvider != null);

    }

    @Override
    public int hashCode() {
        int result = sequence.hashCode();
        result = 31 * result + (modificationProvider != null ? modificationProvider.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PeptideReport{" +
                "sequence='" + sequence + '\'' +
                ", modificationProvider=" + modificationProvider +
                '}';
    }

    public void addProteinAccession(String proteinAccession) {
        if(proteinAccession != null)
            this.proteinAccessions.add(proteinAccession);
    }

    public void addSpecie(String species, String taxonomyID) {
        if(this.species == null)
            this.species = new HashSet<Specie>();
        this.species.add(new Specie(taxonomyID, species, null));
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

    public void setBestClusterPeptideRatio(Double bestClusterPeptideRatio) {
        this.bestClusterPeptideRatio = bestClusterPeptideRatio;
    }

    public int getNumberSpectra() {
        return numberSpectra;
    }

    public void setNumberSpectra(int numberSpectra) {
        this.numberSpectra = numberSpectra;
    }

    public int getNumberOFProjects() {
        return this.getProjectAccessions().size();
    }

    /**
     * Increase the number of Spectra by one.
     */
    public void increaseSpectra(){
        numberSpectra++;
    }

    public float getBestRank() {
        return bestRank;
    }
}
