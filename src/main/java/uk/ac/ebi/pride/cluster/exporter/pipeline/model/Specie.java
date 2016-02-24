package uk.ac.ebi.pride.cluster.exporter.pipeline.model;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/02/2016
 */
public class Specie {

    String taxonomy;

    String name;

    String scientificName;

    public Specie(String taxonomy, String name, String scientificName) {
        this.taxonomy = taxonomy;
        this.name = name;
        this.scientificName = scientificName;
    }

    public String getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(String taxonomy) {
        this.taxonomy = taxonomy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Specie)) return false;

        Specie specie = (Specie) o;

        if (taxonomy != null ? !taxonomy.equals(specie.taxonomy) : specie.taxonomy != null) return false;
        return !(name != null ? !name.equals(specie.name) : specie.name != null);

    }

    @Override
    public int hashCode() {
        int result = taxonomy != null ? taxonomy.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Specie{" +
                "taxonomy='" + taxonomy + '\'' +
                ", name='" + name + '\'' +
                ", scientificName='" + scientificName + '\'' +
                '}';
    }
}
