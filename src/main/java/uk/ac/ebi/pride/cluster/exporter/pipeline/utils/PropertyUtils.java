package uk.ac.ebi.pride.cluster.exporter.pipeline.utils;

import uk.ac.ebi.pride.cluster.exporter.pipeline.model.Specie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility methods for accessing properties
 *
 * @author Yasset Perez-Riverol
 * @version $Id$
 */
public final class PropertyUtils {
    /**
     * Load data source properties from property file
     *
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Properties loadProperties(String resourceUrl) throws URISyntaxException, IOException {
        ClassLoader classLoader = PropertyUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceUrl);

        Properties properties = new Properties();
        properties.load(inputStream);

        return properties;
    }

    /**
     * The species URL configuration file parsing
     * @param speciesURL URL of the species file to be parse
     * @return a Map of the species
     * @throws IOException
     */
    public static Map<String, Specie> loadSpeciesPropertyFile(String speciesURL) {
        Map<String, Specie> species = new HashMap<String, Specie>();
        ClassLoader classLoader = PropertyUtils.class.getClassLoader();
        BufferedReader bufferStream = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(speciesURL)));

        String line;
        int header = 0;
        try {
            while((line = bufferStream.readLine()) != null){
                String[] token = line.split("\t");
                if( header != 0 && token.length == 3)
                    species.put(token[2], new Specie(token[2], token[1], token[0]));
                header++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return species;
    }

    /***
     * Modification File URL that should be load from the project Path
     * @param modFileURL the modification File
     * @return
     */
    public static InputStream loadModificationStream(String modFileURL){
        ClassLoader classLoader = PropertyUtils.class.getClassLoader();
        return classLoader.getResourceAsStream(modFileURL);
    }
}
