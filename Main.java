import java.util.List;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        DataLoader loader = new DataLoader();
        Timer timer = new Timer();
        // Definimos la cabecera una sola vez para usarla en ambos guardados
        String header = "artist_name,track_name,year,danceability,energy,party_score";
        
        System.out.println("-------------------------------------------------");
        System.out.println("      PROCESAMIENTO DE DATOS SPOTIFY - JAVA      ");
        System.out.println("-------------------------------------------------");

        // 1. CARGA DE DATOS
        System.out.println("\n Cargando archivo CSV...");
        List<String[]> datos = loader.cargarCsv("spotify_data_limpio.csv"); 
        
        // Limpieza de cabecera si existe
        if (!datos.isEmpty()) {
            String primerDato = datos.get(0)[2];
            if (primerDato.equalsIgnoreCase("year") || !esNumero(primerDato)) {
                datos.remove(0);
            }
        }
        System.out.println("Registros cargados: " + datos.size());

        // 2. CÁLCULO DE PARTYSCORE
        int total = datos.size();
        for (int i = 0; i < total; i++) {
            String[] fila = datos.get(i);
            try {
                double dance = parseDoubleSeguro(fila[3]);
                double energy = parseDoubleSeguro(fila[4]);
                double partyScore = (dance * 0.6) + (energy * 0.4);
                
                String[] nuevaFila = new String[fila.length + 1];
                System.arraycopy(fila, 0, nuevaFila, 0, fila.length);
                nuevaFila[5] = String.format(Locale.US, "%.4f", partyScore);
                datos.set(i, nuevaFila); // Reemplazo en memoria
            } catch (Exception e) { }
        }

        // ---------------------------------------------------------
        // FASE 1: MergeSort por AÑO (Estable)
        // ---------------------------------------------------------
        System.out.println("\nFASE 1: Ordenando ascendente por año con MergeSort...");
        timer.reiniciar();
        timer.iniciar();
        mergeSort.mergeSort(datos, 2, false); 
        timer.detener();
        timer.imprimirTiempo();
        
        imprimirVistaPrevia(datos, "Odenado por AÑO:");
        
        // --- GUARDADO INTERMEDIO ---
        System.out.println("Guardando archivo: 'ordenado_por_fecha_merge.csv'...");
        loader.guardarCsv("ordenado_por_fecha_merge.csv", header, datos);


        // ---------------------------------------------------------
        // FASE 2: QuickSort por PARTYSCORE (Block Sorting)
        // ---------------------------------------------------------
        System.out.println("\nFASE 2: Ordenando descendente por partyScore dentro de Años con QuickSort...");
        timer.reiniciar();
        timer.iniciar();
        
        int n = datos.size();
        int i = 0;
        while (i < n) {
            int j = i + 1;
            while (j < n && datos.get(j)[2].equals(datos.get(i)[2])) {
                j++;
            }
            if (j - i > 1) {
                quickSort.quickSort(datos.subList(i, j), 5, true); 
            }
            i = j;
        }
        timer.detener();
        timer.imprimirTiempo();

        imprimirVistaPrevia(datos, "Ordenado según PARTY SCORE dentro de Años:");

        // --- GUARDADO FINAL ---
        System.out.println("\nGuardando archivo final: 'ordenado_final_quicksort.csv'...");
        loader.guardarCsv("ordenado_final_quicksort.csv", header, datos);
        
        System.out.println("\n=== PROCESO COMPLETADO EXITOSAMENTE ===");
    }

    // --- MÉTODOS AUXILIARES ---
    private static void imprimirVistaPrevia(List<String[]> datos, String titulo) {
        System.out.println("\n-------------------------------------------------------------------------------------------");
        System.out.println(" " + titulo);
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.printf("| %-20s | %-30s | %-6s | %-8s | %-8s | %-8s |%n", 
                "ARTISTA", "CANCION", "AÑO", "DANCE", "ENERGY", "PARTY SCORE");
        System.out.println("|----------------------|--------------------------------|--------|----------|----------|----------|");

        int limiteHead = Math.min(5, datos.size());
        for (int k = 0; k < limiteHead; k++) imprimirFilaTabla(datos.get(k));
        
        if (datos.size() > 10) System.out.println("| ...                  | ...                            | ...    | ...      | ...      | ...      |");

        int inicioTail = Math.max(limiteHead, datos.size() - 5);
        for (int k = inicioTail; k < datos.size(); k++) imprimirFilaTabla(datos.get(k));
        System.out.println("-------------------------------------------------------------------------------------------\n");
    }

    private static void imprimirFilaTabla(String[] fila) {
        String artista = truncar(fila[0], 20);
        String cancion = truncar(fila[1], 30);
        String score = fila.length > 5 ? fila[5] : "N/A";
        System.out.printf("| %-20s | %-30s | %-6s | %-8s | %-8s | %-8s |%n", 
                artista, cancion, fila[2], fila[3], fila[4], score);
    }

    private static String truncar(String texto, int longitud) {
        if (texto == null) return "";
        if (texto.length() <= longitud) return texto;
        return texto.substring(0, longitud - 3) + "...";
    }
    
    private static double parseDoubleSeguro(String valor) {
        try { return Double.parseDouble(valor); } catch (Exception e) { return 0.0; }
    }

    private static boolean esNumero(String str) {
        try { Double.parseDouble(str); return true; } catch (Exception e) { return false; }
    }
}