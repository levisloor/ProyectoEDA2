public class Timer {
    
    private long tiempoInicio;
    private long tiempoFin;
    
    public void iniciar() {
        tiempoInicio = System.nanoTime();
    }
    
    public void detener() {
        tiempoFin = System.nanoTime();
    }
    
    public long getTiempoNanosegundos() {
        return tiempoFin - tiempoInicio;
    }
    
    public double getTiempoMilisegundos() {
        return (tiempoFin - tiempoInicio) / 1_000_000.0;
    }

    public double getTiempoSegundos() {
        return (tiempoFin - tiempoInicio) / 1_000_000_000.0;
    }
    
    public void imprimirTiempo() {
        double milisegundos = getTiempoMilisegundos();
        double segundos = getTiempoSegundos();
        
        if (segundos < 1) {
            System.out.println("Tiempo de ejecución: " + milisegundos + " ms");
        } else {
            System.out.println("Tiempo de ejecución: " + segundos + " segundos");
        }
    }
    
    public void reiniciar() {
        tiempoInicio = 0;
        tiempoFin = 0;
    }
}