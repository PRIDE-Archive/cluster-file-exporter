package uk.ac.ebi.pride.cluster.exporter.pipeline.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.repo.assay.Assay;
import uk.ac.ebi.pride.archive.repo.assay.AssaySampleCvParam;
import uk.ac.ebi.pride.archive.repo.assay.instrument.Instrument;
import uk.ac.ebi.pride.archive.repo.assay.instrument.InstrumentModel;
import uk.ac.ebi.pride.archive.repo.assay.software.Software;
import uk.ac.ebi.pride.archive.repo.project.Project;
import uk.ac.ebi.pride.archive.repo.project.ProjectTag;
import uk.ac.ebi.pride.cluster.exporter.pipeline.exporter.ConfigurationService;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.Specie;
import uk.ac.ebi.pride.cluster.exporter.pipeline.quality.IClusterQualityDecider;
import uk.ac.ebi.pride.cluster.exporter.pipeline.services.ClusterRepositoryServices;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.SplitList;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.IModification;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.repo.model.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Factory methods for converting external objects to data source friendly version
 *
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public final class SummaryFactory {
    private static final Logger logger = LoggerFactory.getLogger(SummaryFactory.class);

    private static final String TAB_SEP = "\t";

    private static final String NULL_VALUE = "NULL";
    private static final String END_LINE = "\n";

    /**
     * Print in the current file the header for the file including a general description
     * and specie specific information.
     * @param stream The File to print the information
     * @param properties properties containing the information to be printed
     * @param version the version of cluster
     * @param specie specie to filter the data.
     */
    private static void printHeaderFile(PrintStream stream, Properties properties, String version, Specie specie) {

        String specieString = (specie != null)? specie.getName(): "ALL";

        stream.format(properties.getProperty("release.title"), version);
        stream.println(properties.getProperty("cluster.url"));
        stream.format(properties.getProperty("file.specie"), specieString);
        stream.println();
        stream.println(properties.getProperty("release.description"));
        stream.println();
        stream.println(properties.getProperty("peptide.field.description"));
        stream.println();
        stream.println(properties.getProperty("cluster.field.description"));
        stream.println();

    }

    /**
     * This print in the output the header of the Peptide Section
     * @param stream output file
     * @param properties propeties containing the header of the section
     */
    public static void printPeptideHeader(PrintStream stream, Properties properties) {
        stream.println(properties.getProperty("peptide.field.header"));
    }

    /**
     * This print in the output the header of the Peptide Section
     * @param stream output file
     * @param properties propeties containing the header of the section
     */
    public static void printClusterPeptideHeader(PrintStream stream, Properties properties) {
        stream.println(properties.getProperty("cluster.peptide.field.header"));
    }

    /**
     * This function print a Peptide entry on the Peptide Section including its information
     * @param stream the output file
     * @param peptideReport the peptideReport
     * @param properties the propeties containing the header of the file
     */
    public static void printPeptideEntry(PrintStream stream, Map.Entry<PeptideForm, List<ClusteredPSMReport>> peptideReport, Properties properties) {

        PeptideForm peptide = peptideReport.getKey();
        float filter = Float.parseFloat(properties.getProperty("peptide.rank.filter"));
        float filterScore = Float.parseFloat(properties.getProperty("peptide.score.filter"));

        TreeSet<ClusteredPSMReport> collect = peptideReport.getValue().parallelStream()
                .map(c -> (ClusteredPSMReport) c)
                .filter(c -> c.getRank() <= filter)
                .filter(c -> c.getPsmRatio() > filterScore)
                .collect(Collectors.toCollection(
                        () -> new TreeSet<ClusteredPSMReport>((p1, p2) -> p1.getClusterId().compareTo(p2.getClusterId()))
                ));

        float bestRank  = Float.MAX_VALUE;
        float bestRatio = Float.MIN_VALUE;
        int numSpectra  = 0;
        boolean wrongAnnotated = false;

        Set<String> taxonomies = new TreeSet<>();
        Set<String> projects   = new TreeSet<>();

        for(ClusteredPSMReport object: collect){
            ClusteredPSMReport cluster =  object;
            bestRank = (cluster.getRank() < bestRank)?cluster.getRank():bestRank;
            bestRatio = (cluster.getPsmRatio() > bestRatio)?cluster.getPsmRatio():bestRatio;
            numSpectra += cluster.getNumberOfSpectra();
            if(cluster.getAssay() != null && cluster.getAssay().getTaxonomyId() != null){
                String[] taxonomyArr = cluster.getAssay().getTaxonomyId().split(",");
                taxonomies.addAll(Arrays.asList(taxonomyArr));
            }

            projects.add(cluster.getAssay().getProjectAccession());
        }


        if(collect != null && !collect.isEmpty()){
            // This is the real injection point for the multitaxonomy filter
            if (ConfigurationService.getService().isFilterOutMultitaxonomies()
                    && taxonomies.size() > 1) {
                // TODO - This filter should also be applied before calling this method, as it is not its responsibility to filter the data, but just printing it
                // When active, this filter removes those entries in the "PEP" table whose collapsed clustering
                // information presents several taxonomies
                logger.debug("FILTER_OUT_MULTITAXONOMIES ---> {}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                        properties.getProperty("peptide.start.header"),
                        peptide.getSequence(),
                        printValue(summariseMoficiation(peptide.getModifications())),
                        printValue(bestRank),
                        printFloat(bestRatio),
                        printValue(numSpectra),
                        printValue(projects.size()),
                        printValue(collect.size()),
                        printValue(summariseSpecie(taxonomies)),
                        printValue(summariseProjects(projects))
                );
            } else {
                stream.print(properties.getProperty("peptide.start.header") + TAB_SEP);
                stream.print(peptide.getSequence() + TAB_SEP);
                stream.print(printValue(summariseMoficiation(peptide.getModifications())) + TAB_SEP);
                stream.print(printValue(bestRank) + TAB_SEP);
                stream.print(printFloat(bestRatio) + TAB_SEP);
                stream.print(printValue(numSpectra) + TAB_SEP);
                stream.print(printValue(projects.size() + TAB_SEP));
                stream.print(printValue(collect.size()) + TAB_SEP);
                stream.print(printValue(summariseSpecie(taxonomies)) + TAB_SEP);
                stream.print(printValue(summariseProjects(projects) + END_LINE));
            }
        }
    }


    public static void printClusterPeptideEntry(PrintStream stream, Map.Entry<PeptideForm, List<ClusteredPSMReport>> peptideReport, Properties properties) {

        float filter = Float.parseFloat(properties.getProperty("peptide.rank.filter"));
        float filterScore = Float.parseFloat(properties.getProperty("peptide.score.filter"));
        AtomicBoolean filterMultitaxonomy = new AtomicBoolean(false);
        TreeSet<ClusteredPSMReport> collect = peptideReport.getValue().parallelStream()
                .map(c -> (ClusteredPSMReport) c)
                .filter(c -> c.getRank() <= filter)
                .filter(c -> c.getPsmRatio() > filterScore)
                .filter(c -> {
                    if (filterMultitaxonomyClusteredPsmReport(c)) {
                        filterMultitaxonomy.set(true);
                        // Filter it out of the output dataset
                        return false;
                    }
                    // Filter it in, in the output dataset
                    return true;
                })
                .collect(Collectors.toCollection(
                        () -> new TreeSet<ClusteredPSMReport>((p1, p2) -> p1.getClusterId().compareTo(p2.getClusterId()))
                ));

        if (filterMultitaxonomy.get()) {
            // TODO - to be honest, this filter should be implemented outside this method, and applied before calling it, as this method's sole responsibility should be printing the entry
            String sequence = "---N/A---";
            if ((collect != null) && (!collect.isEmpty()) && (collect.first() != null) && (collect.first().getSequence() != null)) {
                sequence = collect.first().getSequence();
            }
            logger.debug("FILTER_OUT_MULTITAXONOMIES ---> {} {} With multiple taxonomies",
                    properties.getProperty("cluster.peptide.start.header"),
                    sequence
            );
        } else {
            for(ClusteredPSMReport object: collect){
                stream.print(properties.getProperty("cluster.peptide.start.header")+TAB_SEP);
                stream.print(object.getClusterId() + TAB_SEP);
                stream.print(object.getSequence() + TAB_SEP);
                stream.print(printValue(summariseMoficiation(object.getModifications())) + TAB_SEP);
                stream.print(printValue(object.getRank()) + TAB_SEP);
                stream.print(printFloat(object.getPsmRatio()) + TAB_SEP);
                stream.print(printFloat(object.getDeltaMZ()) + TAB_SEP);
                stream.print(printValue(object.getClusterNumberSpectra()) + TAB_SEP);
                stream.print(printValue(object.getClusterNumberProjects() + TAB_SEP));
                stream.print(printValue(printValue(object.getAssay().getTaxonomyId())) + TAB_SEP);
    //            stream.print(printValue(printValue(object.isWrongAnnotated())) + TAB_SEP);
                stream.print(printValue(printValue(object.getAssay().getProjectAccession()) + END_LINE));
            }
        }
    }

    private static boolean filterClusteredPsmReport(ClusteredPSMReport clusteredPSMReport, Specie specie) {
        if(specie == null || (clusteredPSMReport.getAssay() != null && clusteredPSMReport.getAssay().getTaxonomyId() != null && clusteredPSMReport.getAssay().getTaxonomyId().contains(specie.getTaxonomy())))
            return true;
        return false;
    }

    /**
     * Implements a filtering criteria for when a clustered PSM report has multiple taxonomies and the filter for
     * multitaxonomy entries is on
     *
     * @param clusteredPSMReport clustered PSM report to test against the filter
     * @return true when the given report matches the criteria, false otherwise
     */
    private static boolean filterMultitaxonomyClusteredPsmReport(ClusteredPSMReport clusteredPSMReport) {
        if ((clusteredPSMReport != null)
                && (clusteredPSMReport.getAssay() != null)
                && (clusteredPSMReport.getAssay().getTaxonomyId() != null)
                && (clusteredPSMReport.getAssay().getTaxonomyId().split(",").length > 1)
                ) {
            return true;
        }
        return false;
    }

    /**
     * This function print the corresponding peptides and psms to the file. If the species provided is null
     * the current method export all the information to the big file.
     * @param service the service that provides the peptides and cluster peptides
     * @param specie the species to filter the file
     * @param path the path where the file will be located
     * @param properties The properties to be use to generate the file
     */
    public static void printFile(ClusterRepositoryServices service, Specie specie, String path, Properties properties, String version) throws FileNotFoundException {
        String filePath = (specie == null)? path + File.separator + properties.getProperty("file.name.title")+"_ALL.tsv":
                path + File.separator + properties.getProperty("file.name.title")+"_" + specie.getName()+".tsv";

        if(service != null){

            PrintStream stream = new PrintStream(new File(filePath));
            printHeaderFile(stream, properties, version, specie);
            SummaryFactory.printPeptideHeader(stream, properties);

            service.getPeptideReportMap().entrySet().stream()
                    .filter((a) -> {
                        List<ClusteredPSMReport> clusters = a.getValue();
                        if(clusters != null && !clusters.isEmpty()){
                            clusters = clusters.parallelStream()
                                    .filter( (cluster) -> filterClusteredPsmReport(cluster, specie) )
                                    .collect(Collectors.toList());
                        }
                        if(clusters != null && !clusters.isEmpty())
                            return true;
                        return false;
                    })
                    .forEach(e -> SummaryFactory.printPeptideEntry(stream, e, properties));

            stream.println();

            SummaryFactory.printClusterPeptideHeader(stream, properties);

            service.getPeptideReportMap().entrySet().stream()
                    .filter((a) -> {
                        List<ClusteredPSMReport> clusters = a.getValue();
                        if(clusters != null && !clusters.isEmpty()){
                            clusters = clusters.parallelStream()
                                    .filter( (cluster) -> filterClusteredPsmReport(cluster, specie) )
                                    .collect(Collectors.toList());
                        }
                        if(clusters != null && !clusters.isEmpty())
                            return true;
                        return false;
                    })
                    .forEach(e -> SummaryFactory.printClusterPeptideEntry(stream, e, properties));

            stream.println();
            // TODO - I'll put PoGo export in this method, using the same given file name, but changing the file extension to .pogo

        }
    }

    private static Object summariseProjects(Set<String> projects) {
        String projectString = null;
        if(projects != null){
            projectString = "";
            for(String project: projects){
                projectString = projectString + project + ",";
            }
            projectString = removeEndComma(projectString);
        }
        return projectString;
    }

    private static Object summariseSpecie(Set<String> species) {
        String specieString = null;
        if(species != null){
            specieString = "";
            for(String specie: species){
                specieString = specieString + specie + ",";
            }
            specieString = removeEndComma(specieString);
        }
        return specieString;
    }


    private static Object printValue(Object s) {
        return (s == null) ? NULL_VALUE:s;
    }

    private static String printFloat(Float s){
        return (s == null)? NULL_VALUE: String.format("%.02f", s);
    }
    /**
     * This function remove form the String the latest ,
     * @param stringValue the current String
     * @return the resulted String
     */
    private static String removeEndComma(String stringValue){
        if(stringValue != null && stringValue.contains(","))
         return stringValue.substring(0, stringValue.length() -1); // Remove the latest comma
        return stringValue;
    }

    public static String summariseMoficiation(List<ModificationProvider> modifications){
        String modificationString = null;
        if(modifications != null && !modifications.isEmpty()){
            modificationString = "";
            for(ModificationProvider mod: modifications){
                for(Integer pos: mod.getPositionMap().keySet()) {

                    if (!modificationString.equals("")) {
                        modificationString += ",";
                    }
                    modificationString += pos + "-" + mod.getAccession();
                }
            }

        }
        return modificationString;
    }


    public static AssayDetail summariseAssay(Project project, Assay assay) {
        AssayDetail assaySummary = new AssayDetail();

        assaySummary.setAccession(assay.getAccession());
        assaySummary.setProjectAccession(project.getAccession());
        assaySummary.setProjectTitle(project.getTitle());
        assaySummary.setAssayTitle(assay.getTitle());

        Collection<AssaySampleCvParam> samples = assay.getSamples();
        String species = "";
        String taxonomyIds = "";
        String disease = "";
        String tissue = "";
        int speciesCount = 0;

        for (AssaySampleCvParam sample : samples) {
            String cvLabel = sample.getCvLabel();
            String accession = sample.getAccession();
            String name = sample.getName();
            if (Constants.NEWT.equals(cvLabel)) {
                // species
                species += name + Constants.COMMA;
                taxonomyIds += accession + Constants.COMMA;
                speciesCount++;
            } else if (Constants.BRENDA.equals(cvLabel)) {
                // tissues
                tissue += name + Constants.COMMA;
            } else if (Constants.DISEASE.equals(cvLabel)) {
                // diseases
                disease += name + Constants.COMMA;
            }
        }

        // species
        if (species.length() > 1)
            assaySummary.setSpecies(species.substring(0, species.length() - 1));

        // multi-species count
        assaySummary.setMultiSpecies(speciesCount > 1);

        // species taxonomy id
        if (taxonomyIds.length() > 1)
            assaySummary.setTaxonomyId(taxonomyIds.substring(0, taxonomyIds.length() - 1));

        // disease
        if (disease.length() > 1)
            assaySummary.setDisease(disease.substring(0, disease.length() - 1));

        // tissue
        if (tissue.length() > 1)
            assaySummary.setTissue(tissue.substring(0, tissue.length() - 1));

        // search engine
        Collection<Software> softwares = assay.getSoftwares();
        if (softwares != null && softwares.size() > 0) {
            String searchEngine = "";
            for (Software software : softwares) {
                searchEngine += software.getName() + Constants.COMMA;
            }

            if (searchEngine.length() > 1)
                assaySummary.setSearchEngine(searchEngine.substring(0, searchEngine.length() - 1));
        }

        // instrument
        Collection<Instrument> instruments = assay.getInstruments();
        if (instruments != null && instruments.size() > 0) {
            String instrument = "";
            for (Instrument intru : instruments) {
                InstrumentModel model = intru.getModel();
                if (model != null) {
                    String value = model.getValue();
                    instrument += (value == null ? model.getName() : value) + Constants.COMMA;
                }
            }

            if (instrument.length() > 1)
                assaySummary.setInstrument(instrument.substring(0, instrument.length() - 1));
        }

        // biomedical
        Collection<ProjectTag> projectTags = project.getProjectTags();
        if (projectTags != null && !projectTags.isEmpty()) {
            for (ProjectTag projectTag : projectTags) {
                if (projectTag.getTag().toLowerCase().contains("biomedical")) {
                    assaySummary.setBioMedical(true);
                }
            }
        }

        return assaySummary;
    }

    /**
     * Create a new summary on PSM
     * <p/>
     * NOTE: spectrum id is not set, this needs to be added before persisted into the database
     *
     * @param psm     PSM generated by mzTab reader
     * @param assayId assay id is the primary key assigned at the data store level
     * @return
     */
    public static PSMDetail summarisePSM(PSM psm, String projectAccession,
                                         java.lang.Long assayId, String assayAccession,
                                         int numOfMsRun) {
        PSMDetail psmSummary = new PSMDetail();

        psmSummary.setAssayId(assayId);

        // archive psm id
        String archivePSMId = buildArchivePSMId(projectAccession, assayAccession, psm);
        psmSummary.setArchivePSMId(archivePSMId);

        // peptide sequence
        psmSummary.setSequence(psm.getSequence());

        // modification
        SplitList<Modification> modifications = psm.getModifications();
        if (modifications != null && !modifications.isEmpty()) {
            Collections.sort(modifications, ModificationComparator.getInstance());
            psmSummary.setModifications(modifications.toString());

            // standardised modification
            //todo: this implementation needs to be check with Noe
        }

        // search engine
        String searchEngine = "";
        SplitList<Param> searchEngines = psm.getSearchEngine();
        if (searchEngines != null) {
            for (Param param : searchEngines) {
                searchEngine += param.getName() + Constants.COMMA;
            }
        }
        if (searchEngine.length() > 1)
            psmSummary.setSearchEngine(searchEngine.substring(0, searchEngine.length() - 1));

        //search engine scores
        String searchEngineScores = "";
        for (int i = 0; i < numOfMsRun; i++) {
            Double searchEngineScore = psm.getSearchEngineScore(i + 1);
            if (searchEngineScore != null)
                searchEngineScores += searchEngineScore + Constants.COMMA;
        }
        if (searchEngineScores.length() > 1)
            psmSummary.setSearchEngineScores(searchEngineScores.substring(0, searchEngineScores.length() - 1));

        // search database
        psmSummary.setSearchDatabase(psm.getDatabase());

        // protein accession
        psmSummary.setProteinAccession(psm.getAccession());

        // protein group
        //todo: protein group is at the protein level

        // protein name
        //todo: protein name are not given at the psm level

        // start position
        Integer start = psm.getStart();
        if (start != null)
            psmSummary.setStartPosition(start);

        // stop position
        Integer end = psm.getEnd();
        if (end != null)
            psmSummary.setStopPosition(end);

        // pre amino acid
        psmSummary.setPreAminoAcid(psm.getPre());

        // post amino acid
        psmSummary.setPostAminoAcid(psm.getPost());

        // delta mass
        Double calcMassToCharge = psm.getCalcMassToCharge();
        Double expMassToCharge = psm.getExpMassToCharge();
        if (calcMassToCharge != null && calcMassToCharge > 0 && expMassToCharge != null && expMassToCharge > 0) {
            psmSummary.setDeltaMZ((float) (calcMassToCharge - expMassToCharge));
        }

        // quantification label
        // todo: to be implemented, quantification information is at the peptide level rather than psm level

        return psmSummary;
    }



    private static String buildArchivePSMId(String projectAccession, String assayAccession, PSM psm) {
        return projectAccession + "_"
                + assayAccession + "_"
                + psm.getPSM_ID() + "_"
                + psm.getAccession() + "_"
                + PeptideUtils.cleanPeptideSequence(psm.getSequence());
    }

    public static SpectrumDetail summariseSpectrum(ISpectrum spectrum, java.lang.Long assayId, boolean identified) {
        SpectrumDetail spectrumSummary = new SpectrumDetail();

        spectrumSummary.setAssayId(assayId);
        spectrumSummary.setReferenceId(spectrum.getId());
        spectrumSummary.setPrecursorMz(spectrum.getPrecursorMz());
        spectrumSummary.setPrecursorCharge(spectrum.getPrecursorCharge());
        spectrumSummary.setIdentified(identified);

        return spectrumSummary;
    }


    public static ClusterDetail summariseCluster(ICluster cluster, IClusterQualityDecider<ClusterSummary> clusterQualityDecider) throws IOException {
        ClusterDetail clusterSummary = new ClusterDetail();

        clusterSummary.setUUID(cluster.getId());

        clusterSummary.setAveragePrecursorMz(cluster.getAvPrecursorMz());

        Collection<ISpectrumReference> spectrumReferences = cluster.getSpectrumReferences();

        // remove duplicated spectra
        UniqueSpectrumFunction uniqueSpectrumFunction = new UniqueSpectrumFunction();
        spectrumReferences = uniqueSpectrumFunction.apply(spectrumReferences);

        int averagePrecursorCharge = calculateAveragePrecursorCharge(spectrumReferences);
        clusterSummary.setAveragePrecursorCharge(averagePrecursorCharge);

        List<Float> consensusMzValues = cluster.getConsensusMzValues();
        String consensusSpectrumMz = convertFloatListToString(consensusMzValues, Constants.COMMA);
        clusterSummary.setConsensusSpectrumMz(consensusSpectrumMz);

        List<Float> consensusIntensValues = cluster.getConsensusIntensValues();
        String consensusSpectrumIntensity = convertFloatListToString(consensusIntensValues, Constants.COMMA);
        clusterSummary.setConsensusSpectrumIntensity(consensusSpectrumIntensity);

        clusterSummary.setMaxPeptideRatio(cluster.getMaxRatio());

        String maxSequence = cluster.getMaxSequence();
        Set<String> projects = new HashSet<String>();
        Set<String> totalProjects = new HashSet<String>();
        Set<String> spectra = new HashSet<String>();
        Set<String> species = new HashSet<String>();
        Set<String> totalSpecies = new HashSet<String>();
        Set<String> ptms = new HashSet<String>();
        Set<String> totalPtms = new HashSet<String>();
        int numberOfPsms = 0;


        for (ISpectrumReference spectrumReference : spectrumReferences) {
            ClusteredSpectrumDetail clusteredSpectrumSummary = new ClusteredSpectrumDetail();
            String spectrumId = spectrumReference.getSpectrumId();
            clusteredSpectrumSummary.setReferenceId(spectrumId);
            clusteredSpectrumSummary.setSimilarityScore(spectrumReference.getSimilarityScore());
            clusterSummary.addClusteredSpectrumDetail(clusteredSpectrumSummary);

            String[] spectrumIdParts = spectrumId.split(";");
            String spec = spectrumReference.getSpecies();
            String projectAccession = spectrumIdParts[0];
            if (hasMaxSequence(spectrumReference, maxSequence)) {
                // get project accession
                projects.add(projectAccession);

                // spectra
                spectra.add(spectrumId);

                // species
                if (spec != null) {
                    species.addAll(Arrays.asList(spec.split(",")));
                }

                // psms
                HashSet<IPeptideSpectrumMatch> uniquePsms = new HashSet<IPeptideSpectrumMatch>(spectrumReference.getPSMs());
                numberOfPsms += uniquePsms.size();

                // ptm
                for (IPeptideSpectrumMatch peptideSpectrumMatch : uniquePsms) {
                    for (IModification modification : peptideSpectrumMatch.getModifications()) {
                        ptms.add(modification.getAccession());
                    }
                }
            }

            // total numbers
            totalProjects.add(projectAccession);
            if (spec != null) {
                totalSpecies.addAll(Arrays.asList(spec.split(",")));
            }
            for (IPeptideSpectrumMatch peptideSpectrumMatch : spectrumReference.getPSMs()) {
                for (IModification modification : peptideSpectrumMatch.getModifications()) {
                    totalPtms.add(modification.getAccession());
                }
            }
        }

        clusterSummary.setNumberOfSpectra(spectra.size());
        clusterSummary.setTotalNumberOfSpectra(cluster.getSpecCount());

        clusterSummary.setNumberOfPSMs(numberOfPsms);
        clusterSummary.setTotalNumberOfPSMs(cluster.getPsmCount());

        clusterSummary.setNumberOfProjects(projects.size());
        clusterSummary.setTotalNumberOfProjects(totalProjects.size());

        clusterSummary.setNumberOfSpecies(species.size());
        clusterSummary.setTotalNumberOfSpecies(totalSpecies.size());

        clusterSummary.setNumberOfModifications(ptms.size());
        clusterSummary.setTotalNumberOfModifications(totalPtms.size());

        // cluster quality
        ClusterQuality clusterQuality = clusterQualityDecider.decideQuality(clusterSummary);
        clusterSummary.setQuality(clusterQuality);

        return clusterSummary;
    }

    private static boolean hasMaxSequence(ISpectrumReference spectrumReference, String maxSequence) {
        for (IPeptideSpectrumMatch peptideSpectrumMatch : spectrumReference.getPSMs()) {
            if (peptideSpectrumMatch.getSequence().equalsIgnoreCase(maxSequence)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculate the average precursor charge for a list of spectra
     *
     * @param spectrumReferences a list of spectra
     * @return average precursor charge
     */
    public static int calculateAveragePrecursorCharge(Collection<ISpectrumReference> spectrumReferences) {
        int chargeSum = 0;
        for (ISpectrumReference spectrumReference : spectrumReferences) {
            int charge = spectrumReference.getCharge();
            if (charge <= 0)
                return 0;
            chargeSum += charge;
        }
        return chargeSum / spectrumReferences.size();
    }

    private static String convertFloatListToString(List<Float> nums, String delimiter) {
        if (nums.isEmpty()) {
            throw new IllegalStateException("List of float number cannot be empty");
        }

        String concat = "";
        for (Float num : nums) {
            concat += num + delimiter;
        }

        return concat.substring(0, concat.length() - delimiter.length());
    }



}
