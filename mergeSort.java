import java.util.ArrayList;
import java.util.List;

/**
 * MergeSort para List<String[]>, comparando por columna numérica (ej. partyScore).
 */
public class mergeSort {

    public static void mergeSort(List<String[]> datos, int columnaOrdenar, boolean descendente) {
        if (datos == null || datos.size() < 2) return;
        mergeSortRec(datos, 0, datos.size() - 1, columnaOrdenar, descendente);
    }

    public static void mergeSort(List<String[]> datos, int columnaOrdenar) {
        mergeSort(datos, columnaOrdenar, false);
    }

    private static void mergeSortRec(List<String[]> datos, int inicio, int fin, int columna, boolean descendente) {
        if (inicio < fin) {
            int medio = (inicio + fin) / 2;
            mergeSortRec(datos, inicio, medio, columna, descendente);
            mergeSortRec(datos, medio + 1, fin, columna, descendente);
            merge(datos, inicio, medio, fin, columna, descendente);
        }
    }

    private static void merge(List<String[]> datos, int inicio, int medio, int fin, int columna, boolean descendente) {
        List<String[]> izquierda = new ArrayList<>();
        List<String[]> derecha = new ArrayList<>();

        for (int i = inicio; i <= medio; i++) izquierda.add(datos.get(i));
        for (int i = medio + 1; i <= fin; i++) derecha.add(datos.get(i));

        int i = 0, j = 0, k = inicio;
        while (i < izquierda.size() && j < derecha.size()) {
            double vi = parseDoubleSafe(izquierda.get(i)[columna]);
            double vj = parseDoubleSafe(derecha.get(j)[columna]);
            int cmp = Double.compare(vi, vj);
            if (descendente) cmp = -cmp;
            if (cmp <= 0) {
                datos.set(k++, izquierda.get(i++));
            } else {
                datos.set(k++, derecha.get(j++));
            }
        }
        while (i < izquierda.size()) datos.set(k++, izquierda.get(i++));
        while (j < derecha.size()) datos.set(k++, derecha.get(j++));
    }

    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}