package co.analisys.clase.kafka;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import co.analisys.clase.dto.OcupacionClase;

/** Vista en memoria de la ocupacion mas reciente de cada clase (el dashboard en tiempo real). */
@Component
public class DashboardOcupacion {

    private final Map<Long, OcupacionClase> ultimaOcupacion = new ConcurrentHashMap<>();

    public void actualizar(OcupacionClase ocupacion) {
        ultimaOcupacion.put(ocupacion.claseId(), ocupacion);
    }

    public Map<Long, OcupacionClase> instantanea() {
        return Map.copyOf(ultimaOcupacion);
    }
}
