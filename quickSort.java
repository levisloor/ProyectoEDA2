import java.util.List;

/**
 * QuickSort optimizado para List<String[]>.
 * Compara por la columna numérica indicada (ej. índice 4 = partyScore).
 *
 * Mejoras:
 * - Cacheo previo de los valores numéricos en un double[] (evita parseDouble repetido).
 * - Intercambio sincronizado de la lista y del array de claves (keys).
 * - Eliminación de recursión por cola: siempre se recurre a la sub-partición más pequeña y
 *   se itera sobre la sub-partición más grande (reduce profundidad de pila).
 * - Mediana de tres usando las claves cacheadas.
 * - Insertion sort para subarrays pequeños (umbral configurable).
 */
public class quickSort {

    // Umbral para usar insertion sort en subarrays pequeños
    private static final int UMBRAL_INSERTION_SORT = 16;

    public static void quickSort(List<String[]> datos, int columnaOrdenar, boolean descendente) {
        if (datos == null || datos.size() < 2) return;

        // Cachear los valores numéricos en un array de doubles para evitar parseos repetidos
        double[] keys = new double[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            String[] row = datos.get(i);
            String s = (row != null && row.length > columnaOrdenar && row[columnaOrdenar] != null)
                    ? row[columnaOrdenar] : "0.0";
            keys[i] = parseDoubleSafe(s);
        }

        quickSortWithKeys(datos, keys, 0, datos.size() - 1, descendente);
    }

    public static void quickSort(List<String[]> datos, int columnaOrdenar) {
        quickSort(datos, columnaOrdenar, false);
    }

    // Núcleo de QuickSort que trabaja con la lista y el array de claves
    private static void quickSortWithKeys(List<String[]> datos, double[] keys, int inicio, int fin, boolean descendente) {
        // Eliminación de recursión por cola: procesar iterativamente la parte mayor
        while (inicio < fin) {
            int len = fin - inicio + 1;
            if (len <= UMBRAL_INSERTION_SORT) {
                insertionSortWithKeys(datos, keys, inicio, fin, descendente);
                return;
            }

            int pivotIndex = partitionWithKeys(datos, keys, inicio, fin, descendente);

            // Recurre en la partición más pequeña, itera en la más grande
            int leftSize = pivotIndex - inicio;
            int rightSize = fin - pivotIndex;
            if (leftSize < rightSize) {
                quickSortWithKeys(datos, keys, inicio, pivotIndex - 1, descendente);
                inicio = pivotIndex + 1; // itero sobre la parte derecha
            } else {
                quickSortWithKeys(datos, keys, pivotIndex + 1, fin, descendente);
                fin = pivotIndex - 1; // itero sobre la parte izquierda
            }
        }
    }

    // Particionamiento usando keys cacheadas
    private static int partitionWithKeys(List<String[]> datos, double[] keys, int inicio, int fin, boolean descendente) {
        int medio = inicio + (fin - inicio) / 2;
        medianaDeTresWithKeys(datos, keys, inicio, medio, fin);

        // Mover pivote (medio) al final
        swap(datos, keys, medio, fin);
        double pivote = keys[fin];

        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            double val = keys[j];
            int cmp = Double.compare(val, pivote);
            if (descendente) cmp = -cmp;
            if (cmp <= 0) {
                i++;
                swap(datos, keys, i, j);
            }
        }
        swap(datos, keys, i + 1, fin);
        return i + 1;
    }

    // Mediana de tres sobre keys (y swaps sincronizados en datos/keys)
    private static void medianaDeTresWithKeys(List<String[]> datos, double[] keys, int a, int b, int c) {
        if (keys[a] > keys[b]) swap(datos, keys, a, b);
        if (keys[a] > keys[c]) swap(datos, keys, a, c);
        if (keys[b] > keys[c]) swap(datos, keys, b, c);
    }

    // Insertion sort que respeta las keys y el flag descendente
    private static void insertionSortWithKeys(List<String[]> datos, double[] keys, int inicio, int fin, boolean descendente) {
        for (int i = inicio + 1; i <= fin; i++) {
            String[] claveRow = datos.get(i);
            double claveVal = keys[i];
            int j = i - 1;
            while (j >= inicio) {
                double curVal = keys[j];
                int cmp = Double.compare(curVal, claveVal);
                if (descendente) cmp = -cmp;
                if (cmp > 0) {
                    // mover hacia la derecha
                    datos.set(j + 1, datos.get(j));
                    keys[j + 1] = keys[j];
                    j--;
                } else {
                    break;
                }
            }
            datos.set(j + 1, claveRow);
            keys[j + 1] = claveVal;
        }
    }

    // Intercambia elemento en lista y su clave correspondiente
    private static void swap(List<String[]> datos, double[] keys, int i, int j) {
        if (i == j) return;
        String[] tmpRow = datos.get(i);
        datos.set(i, datos.get(j));
        datos.set(j, tmpRow);

        double tmpKey = keys[i];
        keys[i] = keys[j];
        keys[j] = tmpKey;
    }

    // Parseo seguro de Strings a double (0.0 ante fallo)
    private static double parseDoubleSafe(String s) {
        try {
            if (s == null) return 0.0;
            return Double.parseDouble(s.replaceAll("\"", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}