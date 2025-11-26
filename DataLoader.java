import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public List<String[]> cargarCsv(String rutaArchivo) {
        List<String[]> datos = new ArrayList<>();
        String linea;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            // Leer la primera línea (cabecera) y descartarla para no procesarla
            br.readLine(); 

            while ((linea = br.readLine()) != null) {
                // EXPLICACIÓN DEL CAMBIO:
                // En lugar de linea.split(","), usamos esta expresión regular (Regex).
                // Lo que hace es: "Busca comas que tengan un número PAR de comillas después de ellas".
                // Esto asegura que NO cortamos las comas que están dentro de un texto entrecomillado.
                String[] columnas = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Limpieza extra: Al dividir así, los textos pueden quedar con comillas (ej: "Austin, TX").
                // Este bucle las quita para dejar el texto limpio.
                for (int i = 0; i < columnas.length; i++) {
                    columnas[i] = columnas[i].replace("\"", "").trim();
                }

                datos.add(columnas);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datos;
    }

    public void guardarCsv(String rutaArchivo, String cabecera, List<String[]> datos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write(cabecera);
            bw.newLine();
            
            for (String[] fila : datos) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < fila.length; i++) {
                    // Si el dato tiene una coma, lo envolvemos en comillas para respetar el formato CSV
                    String dato = fila[i];
                    if (dato.contains(",")) {
                        dato = "\"" + dato + "\"";
                    }
                    
                    sb.append(dato);
                    if (i < fila.length - 1) sb.append(",");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            System.out.println("Archivo guardado exitosamente: " + rutaArchivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}