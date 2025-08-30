import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class VacunacionCovidSets {

    public static void main(String[] args) {
        // Parámetros del enunciado
        final int TOTAL = 500;
        final int PFIZER_COUNT = 75;
        final int ASTRA_COUNT = 75;

        // Seed fija para reproducibilidad (puede quitarse para aleatoriedad real)
        Random rnd = new Random(12345L);

        // 1) Generar ciudadanos
        List<String> ciudadanos = new ArrayList<>(TOTAL);
        for (int i = 1; i <= TOTAL; i++) {
            ciudadanos.add("Ciudadano " + i);
        }

        // 2) Generar conjuntos Pfizer y AstraZeneca (selección aleatoria sin duplicados
        // dentro de cada vacuna, pero permitimos solapamiento entre vacunas)
        Set<String> pfizer = randomSample(ciudadanos, PFIZER_COUNT, rnd);
        Set<String> astra = randomSample(ciudadanos, ASTRA_COUNT, rnd);

        // 3) Asignar número de dosis a cada ciudadano vacunado (1 o 2)
        // Definimos que ~60% de los vacunados tienen 2 dosis (completos)
        Map<String, Integer> dosis = new HashMap<>(); // ciudadano -> número total de dosis (1 o 2)

        Set<String> unionVacunados = new HashSet<>();
        unionVacunados.addAll(pfizer);
        unionVacunados.addAll(astra);

        for (String c : unionVacunados) {
            int d = (rnd.nextDouble() < 0.6) ? 2 : 1; // probabilidad de 60% de estar completo
            dosis.put(c, d);
        }

        // 4) Listados usando teoría de conjuntos
        // Ciudadanos que no se han vacunado = Universo \ (Pfizer ∪ Astra)
        Set<String> notVaccinated = new HashSet<>(ciudadanos);
        notVaccinated.removeAll(unionVacunados);

        // Ciudadanos que han recibido ambas dosis = {c ∈ Universo | dosis.get(c) == 2}
        Set<String> bothDoses = dosis.entrySet().stream()
                .filter(e -> e.getValue() == 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Ciudadanos que solo han recibido la vacuna de Pfizer = Pfizer \ Astra
        Set<String> onlyPfizer = new HashSet<>(pfizer);
        onlyPfizer.removeAll(astra);

        // Ciudadanos que solo han recibido la vacuna de AstraZeneca = Astra \ Pfizer
        Set<String> onlyAstra = new HashSet<>(astra);
        onlyAstra.removeAll(pfizer);

        // 5) Mostrar resultados resumidos por consola
        System.out.println("=== Resumen de vacunación (simulado) ===\n");
        System.out.println("Total ciudadanos: " + TOTAL);
        System.out.println("Vacunados (al menos 1 dosis) - Pfizer: " + pfizer.size());
        System.out.println("Vacunados (al menos 1 dosis) - AstraZeneca: " + astra.size());
        System.out.println("Vacunados (unión Pfizer ∪ Astra): " + unionVacunados.size());
        System.out.println("No vacunados: " + notVaccinated.size());
        System.out.println("Ciudadanos con ambas dosis (completos): " + bothDoses.size());
        System.out.println("Solo Pfizer (Pfizer \u2209 Astra): " + onlyPfizer.size());
        System.out.println("Solo AstraZeneca (Astra \u2209 Pfizer): " + onlyAstra.size());

        // Mostrar los primeros 20 elementos de cada conjunto (para inspección rápida)
        printSample("No vacunados", notVaccinated, 20);
        printSample("Ambas dosis (completos)", bothDoses, 20);
        printSample("Solo Pfizer", onlyPfizer, 20);
        printSample("Solo AstraZeneca", onlyAstra, 20);

        // 6) Guardar resultados completos en archivos CSV
        try {
            writeSetToCsv(notVaccinated, "not_vaccinated.csv");
            writeSetToCsv(bothDoses, "both_doses.csv");
            writeSetToCsv(onlyPfizer, "only_pfizer.csv");
            writeSetToCsv(onlyAstra, "only_astrazeneca.csv");
            System.out.println("\nArchivos CSV generados: not_vaccinated.csv, both_doses.csv, only_pfizer.csv, only_astrazeneca.csv");
        } catch (IOException e) {
            System.err.println("Error al escribir archivos CSV: " + e.getMessage());
        }


        System.out.println("\nEjecución finalizada.");
    }

    
    private static Set<String> randomSample(List<String> list, int n, Random rnd) {
        if (n < 0 || n > list.size())
            throw new IllegalArgumentException("n debe estar entre 0 y el tamaño de la lista");

        Set<String> sample = new HashSet<>(n);
        while (sample.size() < n) {
            int idx = rnd.nextInt(list.size());
            sample.add(list.get(idx));
        }
        return sample;
    }

    private static void printSample(String title, Set<String> set, int limit) {
        System.out.println("\n-- " + title + " (muestra hasta " + limit + ") --");
        int c = 0;
        for (String s : set) {
            System.out.println(s);
            if (++c >= limit) break;
        }
        if (set.isEmpty()) System.out.println("<vacío>");
    }

    private static void writeSetToCsv(Set<String> set, String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("Nombre");
        List<String> sorted = new ArrayList<>(set);
        Collections.sort(sorted);
        for (String s : sorted) lines.add(s);
        Files.write(Paths.get(filename), lines);
    }

    
    
    
    private static void generatePdfReport(Set<String> notVaccinated, Set<String> bothDoses,
                                          Set<String> onlyPfizer, Set<String> onlyAstra) throws IOException {
        // Ejemplo mínimo: crea un PDF con los totales y los nombres (puede mejorarse)
        org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument();
        org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
        doc.addPage(page);

        org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
        org.apache.pdfbox.pdmodel.font.PDType1Font font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;

        int y = 750;
        cs.beginText();
        cs.setFont(font, 12);
        cs.newLineAtOffset(50, y);
        cs.showText("Reporte vacunacion (simulado)");
        cs.newLineAtOffset(0, -20);
        cs.showText("No vacunados: " + notVaccinated.size());
        cs.newLineAtOffset(0, -15);
        cs.showText("Ambas dosis: " + bothDoses.size());
        cs.newLineAtOffset(0, -15);
        cs.showText("Solo Pfizer: " + onlyPfizer.size());
        cs.newLineAtOffset(0, -15);
        cs.showText("Solo AstraZeneca: " + onlyAstra.size());
        cs.endText();

        cs.close();
        doc.save("reporte_vacunacion.pdf");
        doc.close();
        System.out.println("PDF generado: reporte_vacunacion.pdf");
    }
    
}