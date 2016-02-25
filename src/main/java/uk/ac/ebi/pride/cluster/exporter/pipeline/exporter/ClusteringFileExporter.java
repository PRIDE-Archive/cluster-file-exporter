package uk.ac.ebi.pride.cluster.exporter.pipeline.exporter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.PSMReport;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.PeptideReport;
import uk.ac.ebi.pride.cluster.exporter.pipeline.model.Specie;
import uk.ac.ebi.pride.cluster.exporter.pipeline.quality.IClusterQualityDecider;
import uk.ac.ebi.pride.cluster.exporter.pipeline.services.ClusterRepositoryServices;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.PropertyUtils;
import uk.ac.ebi.pride.cluster.exporter.pipeline.utils.SummaryFactory;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.io.IClusterSourceListener;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterReadDao;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterWriteDao;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterQuality;
import uk.ac.ebi.pride.spectracluster.repo.model.ClusterSummary;

import java.io.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public class ClusteringFileExporter {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringFileExporter.class);

    private static final Pattern AMINO_ACID_PATTERN = Pattern.compile("[ABCDEFGHIJKLMNPQRSTUVWXYZ]+");

    public static void main(String[] args) {

        try {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/app-context.xml");
            IClusterReadDao clusterReadDao = (IClusterReadDao) ctx.getBean("clusterReaderDao");

            Map<String, Specie> specieMap = PropertyUtils.loadSpeciesPropertyFile("prop/species_metadata.conf");

            Properties properties         = PropertyUtils.loadProperties("prop/prop.properties");

            CommandLineParser parser = new GnuParser();

            CommandLine commandLine = parser.parse(CliOptions.getOptions(), args);

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printUsage();
                return;
            }

            // OUTPUT
            File file;
            String version;
            if (commandLine.hasOption(CliOptions.OPTIONS.FILE.getValue()) && commandLine.hasOption(CliOptions.OPTIONS.VERSION.getValue())){
                file = new File(commandLine.getOptionValue(CliOptions.OPTIONS.FILE.getValue()));
                version = commandLine.getOptionValue(CliOptions.OPTIONS.VERSION.getValue());
            }
            else
                throw new Exception("Missing required parameter '" + CliOptions.OPTIONS.FILE.getValue() + " or " + CliOptions.OPTIONS.VERSION.getValue() + "'");

            if (!file.exists())
                logger.info("Output .tsv file must be will be re-write with new data");

            writeClusteringResultFile(clusterReadDao, file.getAbsolutePath(), properties, specieMap, version);

        } catch (Exception e) {
            logger.error("Error while running cluster importer", e);
            System.exit(1);
        }
    }

    /**
     * This function allows to write all the output files for the cluster release in the present path.
     *
     * @param clusterReaderDao The cluster instance
     * @param path The path to write all the output files
     * @param properties The property files contains all metadata that should be provided in the file
     * @param species    The species that PRIDE CLuster will export at the very begining
     * @throws Exception
     */
    private static void writeClusteringResultFile(IClusterReadDao clusterReaderDao, String path, Properties properties, Map<String, Specie> species, String version) throws Exception {

        logger.info("Loading clustering file: {}", clusterReaderDao.toString());

        // create data source

        ClusterRepositoryServices service = new ClusterRepositoryServices(clusterReaderDao);

        List<Long> clusters = service.getClusterIdsByQuality(ClusterQuality.HIGH);

        logger.info("Number of HighQuality Clusters: " + clusters.size());

        service.buildPeptidePSMReportLists(clusters);

        logger.info("Number of HighQuality Clusters: " + service.getPeptideReportList().size());

        SummaryFactory.printFile(service, null, path, properties, version);

        for(Specie specie: species.values()){
            SummaryFactory.printFile(service, specie, path, properties, version);
        }
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PRIDE Cluster - Cluster importer", "Imports cluster results into the PRIDE Cluster database.\n", CliOptions.getOptions(), "\n\n", true);
    }




}
