package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract Report contains all the information that would be use to report.
 *
 * - Sequence String: Sequence String
 * - List<ModificationProvider> : List of modifications including the positions
 * - numSpectra : Number of spectra that has identified this particular peptide
 * - clusterMaxPeptideRatio: This is the best score for the cluster
 * - Cluster number of projects
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 25/02/2016
 */
public class AbstractReport {

    // Sequence String for the current PSM
    String sequence;

    // List of modifications
    List<ModificationProvider> modifications;

    // Number of spectra where this psms has been identified
    private int numSpectra = 0;

    // peptide cluster Ratio fo the specific PMS
    double clusterMaxPeptideRatio;

    float bestRank = -1;

    //Protein Accessions
    Set<String> proteinAccession;

    // Project Accession including in the PSM
    Set<String> projectAccession;

    // Species related with the particular Peptide or ClusteredPeptide
    Set<Specie> species;

    AbstractReport(){
        species = new HashSet<Specie>();
        projectAccession = new HashSet<String>();
        proteinAccession = new HashSet<String>();
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<ModificationProvider> getModifications() {
        return modifications;
    }

    public void setModifications(List<ModificationProvider> modifications) {
        this.modifications = modifications;
    }

    public int getNumSpectra() {
        return numSpectra;
    }

    public void setNumSpectra(int numSpectra) {
        this.numSpectra = numSpectra;
    }

    public double getClusterMaxPeptideRatio() {
        return clusterMaxPeptideRatio;
    }

    public void setClusterMaxPeptideRatio(double clusterMaxPeptideRatio) {
        this.clusterMaxPeptideRatio = clusterMaxPeptideRatio;
    }

    public float getBestRank() {
        return bestRank;
    }

    public void setBestRank(float rank) {
        this.bestRank = (rank > bestRank)? rank:bestRank;
    }

    public Set<String> getProteinAccession() {
        return proteinAccession;
    }

    public void setProteinAccession(Set<String> proteinAccession) {
        this.proteinAccession = proteinAccession;
    }

    public Set<String> getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(Set<String> projectAccession) {
        this.projectAccession = projectAccession;
    }

    public Set<Specie> getSpecies() {
        return species;
    }

    public void setSpecies(Set<Specie> species) {
        this.species = species;
    }

    /**
     * Increase the number of spectra
     */
    public void increaseSpectra() {
        this.numSpectra++;
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
            this.species.add(new Specie(taxonomy,specieName, null));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractReport)) return false;

        AbstractReport that = (AbstractReport) o;

        if (!sequence.equals(that.sequence)) return false;
        return !(modifications != null ? !modifications.equals(that.modifications) : that.modifications != null);

    }

    @Override
    public int hashCode() {
        int result = sequence.hashCode();
        result = 31 * result + (modifications != null ? modifications.hashCode() : 0);
        return result;
    }

    public int getNumberProjects(){
        return (projectAccession != null)? projectAccession.size(): 0;
    }

    public boolean containsSpecie(Specie specie) {
        if(specie == null)
            return true;
        for(Specie currentSpecie: species)
            if(currentSpecie.getTaxonomy().equalsIgnoreCase(specie.getTaxonomy()))
                return true;
        return false;
    }
}
