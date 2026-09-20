package co.analisys.miembros.dto;

/** Acumulado semanal de un miembro, resultado del stream processor. */
public record ResumenEntrenamiento(int sesiones, int minutosTotales, int caloriasTotales) {

    public ResumenEntrenamiento() {
        this(0, 0, 0);
    }

    public ResumenEntrenamiento actualizar(DatosEntrenamiento datos) {
        return new ResumenEntrenamiento(
                sesiones + 1, minutosTotales + datos.duracionMin(), caloriasTotales + datos.calorias());
    }
}
