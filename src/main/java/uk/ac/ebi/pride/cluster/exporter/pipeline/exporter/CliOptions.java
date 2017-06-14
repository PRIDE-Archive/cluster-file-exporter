package uk.ac.ebi.pride.cluster.exporter.pipeline.exporter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


@SuppressWarnings("static-access")
public class CliOptions {

    public enum OPTIONS {
        // VARIABLES
        FILE("out"),

        VERSION("version"),

        QUALITY("quality"),

        FILTER_OUT_MULTITAXONOMIES("filter_out_multitaxonomies"),

        INCLUDE_POGO_EXPORT("include_pogo_export"),

        // ACTIONS
        HELP("help");

        private String value;

        OPTIONS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final Options options = new Options();

    static {
        // VARIABLES
        Option outPath = OptionBuilder
                .hasArg()
                .withDescription("The file path to export the cluster results.")
                .create(OPTIONS.FILE.getValue());
        options.addOption(outPath);

        Option version = OptionBuilder
                .hasArg()
                .withDescription("The version of PRIDE Cluster to be included in the result files.")
                .create(OPTIONS.VERSION.getValue());
        options.addOption(version);

        Option quality = OptionBuilder
                .hasArg()
                .withDescription("Quality of the clusters to be exported:\n" +
                        "\t 0 : for all clusters low-quality to high-quality\n" +
                        "\t 1 : for all cluster from medium-quality to high-quality\n" +
                        "\t 2 : for all high-quality cluster\n")
                .create(OPTIONS.QUALITY.getValue());
        options.addOption(quality);

        Option filter_out_multitaxonomies = OptionBuilder
                .withDescription("Filter, remove multitaxonomy entries from the summary file")
                .create(OPTIONS.FILTER_OUT_MULTITAXONOMIES.getValue());
        options.addOption(filter_out_multitaxonomies);

        Option include_pogo_export = OptionBuilder
                .withDescription("Include PoGo formatted export for the dataset, this will produce, for each data output file, another .pogo file")
                .create(OPTIONS.INCLUDE_POGO_EXPORT.getValue());
        options.addOption(include_pogo_export);

        // ACTIONS
        Option help = new Option(
                OPTIONS.HELP.toString(),
                "print this message.");
        options.addOption(help);

    }

    public static Options getOptions() {
        return options;
    }
}
