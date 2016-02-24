package uk.ac.ebi.pride.cluster.exporter.pipeline.quality;

import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;

/**
 * Interface for deciding the quality of a cluster
 *
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public interface IClusterQualityDecider<T> {

    ClusterQuality decideQuality(T cluster);
}
