package uk.ac.ebi.pride.cluster.exporter.pipeline.exporter;

/**
 * @author Manuel Bernal Llinares
 *         Project: cluster-file-exporter
 *         Package: uk.ac.ebi.pride.cluster.exporter.pipeline.exporter
 *         Timestamp: 2017-06-08 15:15
 *         ---
 *         © 2017 Manuel Bernal Llinares <mbdebian@gmail.com>
 *         All rights reserved.
 */
public class ConfigurationService {
    private static ConfigurationService instance = new ConfigurationService();

    private boolean filterOutMultitaxonomies = false;

    protected ConfigurationService() {}

    public static ConfigurationService getService() {
        return instance;
    }

    // Configuration properties
    public void setFilterOutMultitaxonomies() {
        this.filterOutMultitaxonomies = true;
    }

    public void unsetFilterOutMultitaxonomies() {
        this.filterOutMultitaxonomies = false;
    }

    public boolean isFilterOutMultitaxonomies() {
        return filterOutMultitaxonomies;
    }
}
