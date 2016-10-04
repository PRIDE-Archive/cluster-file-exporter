package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/02/2016
 */
public class ClusterReport {

    Long clusterID;

    private float maxPeptideRatio;

    int numberPSMs;

    double chargeState;

    double experimentalMZ;

    int clusterNumberProjects;

    ClusterQuality quality;

    public ClusterReport(Long clusterID, float maxPeptideRatio, int numberPSMs, double chargeState, double experimentalMZ, int clusterNumberProjects, ClusterQuality quality) {
        this.clusterID = clusterID;
        this.maxPeptideRatio = maxPeptideRatio;
        this.numberPSMs = numberPSMs;
        this.chargeState = chargeState;
        this.experimentalMZ = experimentalMZ;
        this.clusterNumberProjects = clusterNumberProjects;
        this.quality = quality;
    }

    public ClusterReport(ClusterDetail cluster) {
        this.clusterID = cluster.getId();
        this.maxPeptideRatio = cluster.getMaxPeptideRatio();
        this.numberPSMs = cluster.getNumberOfPSMs();
        this.chargeState = cluster.getAveragePrecursorCharge();
        this.experimentalMZ = cluster.getAveragePrecursorMz();
        this.clusterNumberProjects = cluster.getNumberOfProjects();
        this.quality = cluster.getQuality();
    }

    public Long getClusterID() {
        return clusterID;
    }

    public void setClusterID(Long clusterID) {
        this.clusterID = clusterID;
    }

    public float getMaxPeptideRatio() {
        return maxPeptideRatio;
    }

    public void setMaxPeptideRatio(float maxPeptideRatio) {
        this.maxPeptideRatio = maxPeptideRatio;
    }

    public int getNumberPSMs() {
        return numberPSMs;
    }

    public void setNumberPSMs(int numberPSMs) {
        this.numberPSMs = numberPSMs;
    }

    public double getChargeState() {
        return chargeState;
    }

    public void setChargeState(double chargeState) {
        this.chargeState = chargeState;
    }

    public double getExperimentalMZ() {
        return experimentalMZ;
    }

    public void setExperimentalMZ(double experimentalMZ) {
        this.experimentalMZ = experimentalMZ;
    }

    public int getClusterNumberProjects() {
        return clusterNumberProjects;
    }

    public void setClusterNumberProjects(int clusterNumberProjects) {
        this.clusterNumberProjects = clusterNumberProjects;
    }

    public ClusterQuality getQuality() {
        return quality;
    }

    public void setQuality(ClusterQuality quality) {
        this.quality = quality;
    }
}
