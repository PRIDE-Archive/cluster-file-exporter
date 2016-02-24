package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

import java.util.Map;

/**
 * Modification for the web service
 *
 * @author Rui Wang
 * @version $Id$
 */
public class Modification implements ModificationProvider{

    private String accession;
    private String name;
    private Integer mainPosition;
    private Double monoMass;
    private CvParamProvider neutralLoss;
    private Map<Integer, CvParamProvider> positionMap;

    @Override
    public String getAccession() {
        return accession;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getMainPosition() {
        return mainPosition;
    }

    public Double getMonoMass() {
        return monoMass;
    }

    @Override
    public CvParamProvider getNeutralLoss() {
        return neutralLoss;
    }

    @Override
    public Map<Integer, CvParamProvider> getPositionMap() {
        return positionMap;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMainPosition(Integer mainPosition) {
        this.mainPosition = mainPosition;
    }

    public void setMonoMass(Double monoMass) {
        this.monoMass = monoMass;
    }

    public void setNeutralLoss(CvParamProvider neutralLoss) {
        this.neutralLoss = neutralLoss;
    }

    public void setPositionMap(Map<Integer, CvParamProvider> positionMap) {
        this.positionMap = positionMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Modification)) return false;

        Modification that = (Modification) o;

        if (accession != null ? !accession.equals(that.accession) : that.accession != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (mainPosition != null ? !mainPosition.equals(that.mainPosition) : that.mainPosition != null) return false;
        if (monoMass != null ? !monoMass.equals(that.monoMass) : that.monoMass != null) return false;
        if (neutralLoss != null ? !neutralLoss.equals(that.neutralLoss) : that.neutralLoss != null) return false;
        return !(positionMap != null ? !positionMap.equals(that.positionMap) : that.positionMap != null);

    }

    @Override
    public int hashCode() {
        int result = accession != null ? accession.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (mainPosition != null ? mainPosition.hashCode() : 0);
        result = 31 * result + (monoMass != null ? monoMass.hashCode() : 0);
        result = 31 * result + (neutralLoss != null ? neutralLoss.hashCode() : 0);
        result = 31 * result + (positionMap != null ? positionMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Modification{" +
                "accession='" + accession + '\'' +
                ", name='" + name + '\'' +
                ", mainPosition=" + mainPosition +
                ", monoMass=" + monoMass +
                ", neutralLoss=" + neutralLoss +
                ", positionMap=" + positionMap +
                '}';
    }
}
