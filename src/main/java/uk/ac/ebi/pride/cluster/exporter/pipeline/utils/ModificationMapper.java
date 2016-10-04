package uk.ac.ebi.pride.cluster.exporter.pipeline.utils;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

import uk.ac.ebi.pride.cluster.exporter.pipeline.model.Modification;
import uk.ac.ebi.pride.spectracluster.repo.utils.ModificationDetailFetcher;
import uk.ac.ebi.pride.spectracluster.repo.utils.ModificationUtils;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Mapper for mapping modifications using David's mapping for PRIDE Archive
 *
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public class ModificationMapper {
    /**
     * This function wrapper all the information for the modifications in the web-services level
     * @param repoMods
     * @param modificationDetailFetcher
     * @return
     */
    public static List<ModificationProvider> asModifications(List<ModificationProvider> repoMods, ModificationDetailFetcher modificationDetailFetcher) {
        List<ModificationProvider> mods = new ArrayList<uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider>();

        if (repoMods != null) {
            for (ModificationProvider repoMod : repoMods) {
                Modification modificationProvider = new Modification();
                modificationProvider.setAccession(repoMod.getAccession());
                modificationProvider.setMainPosition(repoMod.getMainPosition());
                modificationProvider.setName(repoMod.getName());
                modificationProvider.setNeutralLoss(repoMod.getNeutralLoss());
                modificationProvider.setPositionMap(new HashMap<Integer, CvParamProvider>(repoMod.getPositionMap()));

                // mono mass
                PTM ptm = modificationDetailFetcher.getPTMbyAccession(repoMod.getAccession());
                if (ptm != null) {
                    modificationProvider.setMonoMass(ptm.getMonoDeltaMass());
                }

                mods.add(modificationProvider);
            }
        }

        return mods;
    }

    /**
     * This function wrapper all the information for the modifications in the web-services level
     * @param repoMods
     * @param sequence Sequence of the peptide
     * @return
     */
    public static List<ModificationProvider> asModifications(List<ModificationProvider> repoMods, String sequence) {
        return ModificationUtils.constructAnchorModification(repoMods, sequence);
    }

    public static boolean checkWrongAnnotation(List<ModificationProvider> modifications, String sequence) {
       return ModificationUtils.checkWrongAnnotation(modifications, sequence);
    }
}
