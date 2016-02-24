package uk.ac.ebi.pride.cluster.exporter.pipeline.model;



import java.util.List;

/**
 * A list of PTM counts
 *
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public class ClusterModificationCounts {

    private List<ModificationCount> modificationCounts;

    public List<ModificationCount> getModificationCounts() {
        return modificationCounts;
    }

    public void setModificationCounts(List<ModificationCount> modificationCounts) {
        this.modificationCounts = modificationCounts;
    }
}
