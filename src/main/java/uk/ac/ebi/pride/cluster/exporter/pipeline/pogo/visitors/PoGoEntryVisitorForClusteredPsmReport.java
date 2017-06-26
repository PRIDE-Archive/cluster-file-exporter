package uk.ac.ebi.pride.cluster.exporter.pipeline.pogo.visitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.proteogenomics.pogo.model.PoGoEntry;
import uk.ac.ebi.pride.proteogenomics.pogo.model.PoGoEntryVisitor;
import uk.ac.ebi.pride.proteogenomics.pogo.model.PoGoEntryVisitorException;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusteredPSMReport;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PRIDEModPTM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project: libpogo
 * Package: uk.ac.ebi.pride.proteogenomics.pogo.model.translator
 * Timestamp: 2017-06-14 10:30
 * ---
 * © 2017 Manuel Bernal Llinares <mbdebian@gmail.com>
 * All rights reserved.
 */

public class PoGoEntryVisitorForClusteredPsmReport implements PoGoEntryVisitor {
    private static final Logger logger = LoggerFactory.getLogger(PoGoEntryVisitorForClusteredPsmReport.class);

    private ClusteredPSMReport clusteredPSMReport = null;

    private String translateSequence() {
        StringBuilder translatedSequence = new StringBuilder("---NO_TRANSLATION---");
        Map<Integer, List<String>> modificationsMap = new HashMap<>();
        // Translate the modifications into their short names and sort them by position
        if (clusteredPSMReport.getModifications() != null) {
            for (ModificationProvider modification
                    : clusteredPSMReport.getModifications()
                    ) {
                String clusterId = clusteredPSMReport.getClusterId() == null ? "-null_cluster_ID-" : clusteredPSMReport.getClusterId().toString();
                String modificationAccession = "null";
                if (modification.getAccession() == null) {
                    logger.error("Modification Accession is 'null' for peptide '{}', cluster ID '{}'",
                            clusteredPSMReport.getSequence(),
                            clusterId);
                } else {
                    modificationAccession = modification.getAccession();
                }
                if (modification.getMainPosition() == null) {
                    logger.error("Main position is 'null', modification'{}', for peptide '{}', cluster ID '{}'",
                            clusteredPSMReport.getSequence(),
                            modificationAccession,
                            clusterId);
                    continue;
                }
                int modificationMainPosition = modification.getMainPosition();
                if ((modificationMainPosition < 1)
                        || (modificationMainPosition > clusteredPSMReport.getSequence().length())) {
                    // TODO - These kind of modifications are not handled by PoGo, but they may be biologically relevant
                    logger.warn("EXCLUDING MODIFICAION '{}', for peptide '{}', main position '{}'",
                            modificationAccession,
                            clusteredPSMReport.getSequence(),
                            modificationMainPosition);
                } else {
                    PRIDEModPTM prideModPTM =
                            ModReader.getInstance()
                                    .getPRIDEModByAccessionAndAmminoAcid(modificationAccession,
                                            String.valueOf(clusteredPSMReport.getSequence().charAt(modification.getMainPosition() - 1)));
                    String modificationShortName = modificationAccession;
                    if ((prideModPTM != null)
                            && prideModPTM.isBiologicalRelevant()) {
                        modificationShortName = prideModPTM.getShortName();
                    }
                    if (modificationsMap.get(modificationMainPosition) == null) {
                        modificationsMap.put(modificationMainPosition, new ArrayList<>());
                    }
                    modificationsMap.get(modificationMainPosition).add(modificationShortName);
                }
            }
        }
        // Embed modifications by position in the sequence
        if (clusteredPSMReport.getSequence().length() > 0) {
            translatedSequence = new StringBuilder();
        }
        if ((clusteredPSMReport.getSequence() != null) &&
                clusteredPSMReport.getSequence().length() > 0) {
            int position = 1;
            for (char character : clusteredPSMReport.getSequence().toCharArray()) {
                translatedSequence.append(character);
                if (modificationsMap.get(position) != null) {
                    translatedSequence.append(String.format("(%s)", String.join(",", modificationsMap.get(position))));
                }
                position++;
            }
        }
        return translatedSequence.toString();
    }

    public PoGoEntryVisitorForClusteredPsmReport(ClusteredPSMReport clusteredPSMReport) {
        this.clusteredPSMReport = clusteredPSMReport;
    }

    @Override
    public PoGoEntry visit(PoGoEntry poGoEntry) {
        if (clusteredPSMReport != null) {
            poGoEntry.setExperiment("---no_cluster_id_available---");
            if (clusteredPSMReport.getClusterId() == null) {
                String msg = String.format("CLUSTER ID IS NULL, Sequence (%s)",
                        clusteredPSMReport.getSequence());
                throw new PoGoEntryVisitorException(msg);
            }
            poGoEntry.setExperiment(String.format("<a href='https://www.ebi.ac.uk/pride/cluster/#/id/%s'>Cluster ID %s</a>",
                    clusteredPSMReport.getClusterId().toString(),
                    clusteredPSMReport.getClusterId().toString()));
            poGoEntry.setPeptide(translateSequence());
            poGoEntry.setPsm(clusteredPSMReport.getClusterNumberPSMs());
            // TODO - This parameter will be left as zero right now, in the future we need to work out a way to leave as it is right now, or compute its value
            poGoEntry.setQuant(0d);
        }
        return poGoEntry;
    }
}
