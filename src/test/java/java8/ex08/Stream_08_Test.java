package java8.ex08;

import java8.data.Data;
import java8.data.domain.Pizza;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Exercice 5 - Files
 */
public class Stream_08_Test {

    // Chemin vers un fichier de données des naissances
    private static final String NAISSANCES_DEPUIS_1900_CSV = "./naissances_depuis_1900.csv";

    private static final String DATA_DIR = "./pizza-data";


    // Structure modélisant les informations d'une ligne du fichier
    class Naissance {
        String annee;
        String jour;
        Integer nombre;

        public Naissance(String annee, String jour, Integer nombre) {
            this.annee = annee;
            this.jour = jour;
            this.nombre = nombre;
        }

        public String getAnnee() {
            return annee;
        }

        public void setAnnee(String annee) {
            this.annee = annee;
        }

        public String getJour() {
            return jour;
        }

        public void setJour(String jour) {
            this.jour = jour;
        }

        public Integer getNombre() {
            return nombre;
        }

        public void setNombre(Integer nombre) {
            this.nombre = nombre;
        }
    }


    @Test
    public void test_group() throws IOException {

        // TODO utiliser la méthode java.nio.file.Files.lines pour créer un stream de lignes du fichier naissances_depuis_1900.csv
        // Le bloc try(...) permet de fermer (close()) le stream après utilisation
        try (Stream<String> lines = Files.lines(Path.of(NAISSANCES_DEPUIS_1900_CSV))) {

            // TODO construire une MAP (clé = année de naissance, valeur = somme des nombres de naissance de l'année)
            Map<String, Integer> result = lines.map(line -> line.split(";"))
                    .skip(1)
                    .map(naissanceInfo -> {
                        int number = Integer.parseInt(naissanceInfo[3]);
                        return new Naissance(
                                naissanceInfo[1],
                                naissanceInfo[2],
                                number
                        );
                    })
                    .collect(Collectors.groupingBy(naissance -> naissance.getAnnee(), Collectors.summingInt(naissance -> naissance.getNombre())));


            assertThat(result.get("2015"), is(8097));
            assertThat(result.get("1900"), is(5130));
        }
    }

    @Test
    public void test_max() throws IOException {

        // TODO utiliser la méthode java.nio.file.Files.lines pour créer un stream de lignes du fichier naissances_depuis_1900.csv
        // Le bloc try(...) permet de fermer (close()) le stream après utilisation
        try (Stream<String> lines = Files.lines(Path.of(NAISSANCES_DEPUIS_1900_CSV))) {

            // TODO trouver l'année où il va eu le plus de nombre de naissance
            Optional<Naissance> result = lines.map(line -> line.split(";"))
                    .skip(1)
                    .map(naissanceInfo -> {
                        int number = Integer.parseInt(naissanceInfo[3]);
                        return new Naissance(
                                naissanceInfo[1],
                                naissanceInfo[2],
                                number
                        );
                    })
                    .max(Comparator.comparingInt(Naissance::getNombre));


            assertThat(result.get().getNombre(), is(48));
            assertThat(result.get().getJour(), is("19640228"));
            assertThat(result.get().getAnnee(), is("1964"));
        }
    }

    @Test
    public void test_collectingAndThen() throws IOException {
        // TODO utiliser la méthode java.nio.file.Files.lines pour créer un stream de lignes du fichier naissances_depuis_1900.csv
        // Le bloc try(...) permet de fermer (close()) le stream après utilisation
        try (Stream<String> lines = Files.lines(Path.of(NAISSANCES_DEPUIS_1900_CSV))) {

            // TODO construire une MAP (clé = année de naissance, valeur = maximum de nombre de naissances)
            // TODO utiliser la méthode "collectingAndThen" à la suite d'un "grouping"
            Map<String, Naissance> result = lines.map(line -> line.split(";"))
                    .skip(1)
                    .map(naissanceInfo -> {
                        int number = Integer.parseInt(naissanceInfo[3]);
                        return new Naissance(
                                naissanceInfo[1],
                                naissanceInfo[2],
                                number
                        );
                    })
                    .collect(Collectors.groupingBy(
                            Naissance::getAnnee,
                            Collectors.collectingAndThen(
                                    Collectors.maxBy(Comparator.comparingInt(Naissance::getNombre)),
                                    Optional::get
                            )
                    ));

            assertThat(result.get("2015").getNombre(), is(38));
            assertThat(result.get("2015").getJour(), is("20150909"));
            assertThat(result.get("2015").getAnnee(), is("2015"));

            assertThat(result.get("1900").getNombre(), is(31));
            assertThat(result.get("1900").getJour(), is("19000123"));
            assertThat(result.get("1900").getAnnee(), is("1900"));
        }
    }

    // Des données figurent dans le répertoire pizza-data
    // TODO explorer les fichiers pour voir leur forme
    // TODO compléter le test

    @Test
    public void test_pizzaData() throws IOException {
        // TODO utiliser la méthode java.nio.file.Files.list pour parcourir un répertoire
        try (Stream<Path> pizzasPath = Files.list(Path.of(DATA_DIR))) {

            // TODO trouver la pizza la moins chère
            String pizzaNamePriceMin = pizzasPath
                    .map(pizzaPath -> {
                        try (Stream<String> line = Files.lines(pizzaPath)) {
                            String[] pizzaInfo = line.findFirst().orElseThrow().split(";");
                            int price = Integer.parseInt(pizzaInfo[1]);
                            return new Pizza((int) (Math.random()*100), pizzaInfo[0], price);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .min(Comparator.comparingInt(Pizza::getPrice))
                    .map(Pizza::getName)
                    .orElseThrow();

            assertThat(pizzaNamePriceMin, is("L'indienne"));
        }

    }

    // TODO Optionel
    // TODO Créer un test qui exporte des données new Data().getPizzas() dans des fichiers
    // TODO 1 fichier par pizza
    // TODO le nom du fichier est de la forme ID.txt (ex. 1.txt, 2.txt)

    @Test
    public void test_creation_data() {
        List<Pizza> pizzas = new Data().getPizzas();

        pizzas.stream()
                .map(pizza -> new String[]{pizza.getId().toString(), pizza.getName() + ";" + pizza.getPrice()})
                .forEach(dataFileInfo -> {
                    try (FileWriter fileWriter = new FileWriter(DATA_DIR + '/' + dataFileInfo[0])) {
                        fileWriter.write(dataFileInfo[1]);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}