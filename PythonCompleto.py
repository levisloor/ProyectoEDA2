import csv
import time
import sys
import multiprocessing
import heapq 
from multiprocessing import Pool, cpu_count

# Configuración: Aumento del límite de recursión para QuickSort en modo Serial
sys.setrecursionlimit(2000000)

# Constantes (Índices de las columnas)
IDX_ANIO = 2
IDX_PUNTAJE = 5


def mergesort_core(datos, indice_col):
    """ Implementación recursiva de MergeSort (Estable). """
    if len(datos) <= 1: return datos
    medio = len(datos) // 2
    izq = mergesort_core(datos[:medio], indice_col)
    der = mergesort_core(datos[medio:], indice_col)
    return logica_merge(izq, der, indice_col)

def logica_merge(izq, der, indice_col):
    res = []
    i = j = 0
    while i < len(izq) and j < len(der):
        try:
            val_izq = float(izq[i][indice_col])
            val_der = float(der[j][indice_col])
        except ValueError: val_izq, val_der = 0.0, 0.0
        
        # Ascendente (para Año)
        if val_izq <= val_der:
            res.append(izq[i]); i += 1
        else:
            res.append(der[j]); j += 1
    res.extend(izq[i:]); res.extend(der[j:])
    return res

def quicksort_core(datos, indice_col, descendente=True):
    """ Implementación recursiva de QuickSort (3-Way Partition). """
    if len(datos) <= 1: return datos
    pivote = datos[len(datos) // 2]
    try: val_piv = float(pivote[indice_col])
    except ValueError: val_piv = 0.0
    
    menores, iguales, mayores = [], [], []
    for fila in datos:
        try: val = float(fila[indice_col])
        except ValueError: val = 0.0
        if val < val_piv: menores.append(fila)
        elif val > val_piv: mayores.append(fila)
        else: iguales.append(fila)
    
    if descendente:
        return quicksort_core(mayores, indice_col, True) + iguales + quicksort_core(menores, indice_col, True)
    else:
        return quicksort_core(menores, indice_col, False) + iguales + quicksort_core(mayores, indice_col, False)


def worker_mergesort_anio(chunk):
    return list(mergesort_core(chunk, IDX_ANIO))

def worker_quicksort_puntaje(chunk):
    return list(quicksort_core(chunk, IDX_PUNTAJE, descendente=True))

def cargar_dataset(ruta):
    print(f"Cargando dataset desde: {ruta}")
    datos = []
    cabecera = []
    try:
        with open(ruta, 'r', encoding='utf-8') as f:
            lector = csv.reader(f)
            cabecera = next(lector)
            if "party_score" not in cabecera: cabecera.append("party_score")
            for fila in lector:
                try:
                    if len(fila) < 5: continue
                    dance = float(fila[3])
                    energy = float(fila[4])
                    score = (dance * 0.6) + (energy * 0.4)
                    fila.append(f"{score:.4f}")
                    datos.append(fila)
                except ValueError: continue
    except FileNotFoundError: return [], []
    return cabecera, datos

def mostrar_preview(datos, titulo):
    print(f"\n--- PREVIEW: {titulo} ---")
    print(f"{'ARTISTA':<25} | {'TRACK':<35} | {'AÑO':<6} | {'SCORE':<8}")
    print("-" * 85)
    limite = min(5, len(datos))
    # Head
    for i in range(limite):
        row = datos[i]
        art = (row[0][:22] + '..') if len(row[0]) > 22 else row[0]
        trk = (row[1][:32] + '..') if len(row[1]) > 32 else row[1]
        print(f"{art:<25} | {trk:<35} | {row[IDX_ANIO]:<6} | {row[IDX_PUNTAJE]:<8}")
    if len(datos) > 10:
        print(f"{'...':<25} | {'...':<35} | {'...':<6} | {'...':<8}")
    # Tail
    inicio_tail = max(limite, len(datos) - 5)
    for i in range(inicio_tail, len(datos)):
        row = datos[i]
        art = (row[0][:22] + '..') if len(row[0]) > 22 else row[0]
        trk = (row[1][:32] + '..') if len(row[1]) > 32 else row[1]
        print(f"{art:<25} | {trk:<35} | {row[IDX_ANIO]:<6} | {row[IDX_PUNTAJE]:<8}")
    print("-" * 85)

def benchmark_mergesort(datos, num_cpus):
    print("\nMergeSort (Ordenar por Año)")
    # Serial
    cp = datos[:]
    t0 = time.time()
    mergesort_core(cp, IDX_ANIO)
    t_ser = time.time() - t0
    print(f"   > Serial:   {t_ser:.4f} s")
    
    # Parallel
    cp = datos[:]
    t0 = time.time()
    sz = len(cp) // num_cpus
    chunks = [cp[i:i + sz] for i in range(0, len(cp), sz)]
    with Pool(num_cpus) as pool:
        res = pool.map(worker_mergesort_anio, chunks)
    list(heapq.merge(*res, key=lambda x: float(x[IDX_ANIO])))
    t_par = time.time() - t0
    print(f"   > Multiprocesamiento: {t_par:.4f} s")
    return t_ser, t_par

def benchmark_quicksort(datos, num_cpus):
    print("\nQuickSort (Ordenar por partyScore)")
    # Serial
    cp = datos[:]
    t0 = time.time()
    try:
        quicksort_core(cp, IDX_PUNTAJE, True)
        t_ser = time.time() - t0
        print(f"   > Serial:   {t_ser:.4f} s")
    except RecursionError:
        print("   > Serial:   Fallo (Recursion)")
        t_ser = 0.0
    
    # Parallel
    cp = datos[:]
    t0 = time.time()
    sz = len(cp) // num_cpus
    chunks = [cp[i:i + sz] for i in range(0, len(cp), sz)]
    with Pool(num_cpus) as pool:
        res = pool.map(worker_quicksort_puntaje, chunks)
    list(heapq.merge(*res, key=lambda x: float(x[IDX_PUNTAJE]), reverse=True))
    t_par = time.time() - t0
    print(f"   > Multiprocesamiento: {t_par:.4f} s")
    return t_ser, t_par

def benchmark_hibrido(datos, num_cpus):
    print("\nHíbrido (Año -> Merge, Score -> Quick)")
    # Serial
    cp = datos[:]
    t0 = time.time()
    sorted_yr = mergesort_core(cp, IDX_ANIO)
    final_res = []
    if sorted_yr:
        grps, blk, cur_yr = [], [], sorted_yr[0][IDX_ANIO]
        for r in sorted_yr:
            if r[IDX_ANIO] == cur_yr: blk.append(r)
            else: grps.append(blk); blk = [r]; cur_yr = r[IDX_ANIO]
        grps.append(blk)
        for g in grps: final_res.extend(quicksort_core(g, IDX_PUNTAJE, True))
    t_ser = time.time() - t0
    print(f"   > Serial:   {t_ser:.4f} s")

    # Parallel
    cp = datos[:]
    t0 = time.time()
    # Fase 1
    sz = len(cp) // num_cpus
    chunks = [cp[i:i + sz] for i in range(0, len(cp), sz)]
    with Pool(num_cpus) as pool:
        res = pool.map(worker_mergesort_anio, chunks)
    sorted_yr = list(heapq.merge(*res, key=lambda x: float(x[IDX_ANIO])))
    
    # Fase 2
    final_par = []
    if sorted_yr:
        grps, blk, cur_yr = [], [], sorted_yr[0][IDX_ANIO]
        for r in sorted_yr:
            if r[IDX_ANIO] == cur_yr: blk.append(r)
            else: grps.append(blk); blk = [r]; cur_yr = r[IDX_ANIO]
        grps.append(blk)
        with Pool(num_cpus) as pool:
            res_grps = pool.map(worker_quicksort_puntaje, grps)
        for g in res_grps: final_par.extend(g)
        
    t_par = time.time() - t0
    print(f"   > Multiprocesamiento: {t_par:.4f} s")
    return t_ser, t_par, final_par

def main():
    ruta = 'spotify_data_limpio.csv'
    cabecera, datos = cargar_dataset(ruta)
    if not datos: return

    num_cpus = cpu_count()
    print("=" * 60)
    print(f" {len(datos)} registros, {num_cpus} CPUs")
    print("=" * 60)

    # Ejecutar Benchmarks
    t1s, t1p = benchmark_mergesort(datos, num_cpus)
    t2s, t2p = benchmark_quicksort(datos, num_cpus)
    t3s, t3p, datos_hibrido = benchmark_hibrido(datos, num_cpus)

    # Tabla Resumen
    print("\n" + "=" * 80)
    print(f"{'ALGORITMO':<30} | {'SERIAL':<10} | {'MULTIPROCESAMIENTO':<15} | {'TIEMPO GANADO':<15}")
    print("-" * 80)
    
    gain1 = max(0, t1s - t1p)
    gain2 = max(0, t2s - t2p)
    gain3 = max(0, t3s - t3p)
    
    print(f"{'MergeSort (Año)':<30} | {t1s:<10.4f} | {t1p:<10.4f} | {gain1:<10.4f} s")
    print(f"{'QuickSort (Score)':<30} | {t2s:<10.4f} | {t2p:<10.4f} | {gain2:<10.4f} s")
    print(f"{'Híbrido (Final)':<30} | {t3s:<10.4f} | {t3p:<10.4f} | {gain3:<10.4f} s")
    print("-" * 80)

    # Guardar y Preview SOLO del Híbrido
    archivo_final = 'spotify_final_ordenado.csv'
    print(f"\nGenerando archivo final: {archivo_final} ...")
    try:
        with open(archivo_final, 'w', newline='', encoding='utf-8') as f:
            wr = csv.writer(f)
            wr.writerow(cabecera)
            wr.writerows(datos_hibrido)
        print("Archivo guardado exitosamente.")
    except: print("[Error] No se pudo guardar.")

    mostrar_preview(datos_hibrido, "Resumen Final Ordenado")

if __name__ == '__main__':
    multiprocessing.freeze_support()
    main()