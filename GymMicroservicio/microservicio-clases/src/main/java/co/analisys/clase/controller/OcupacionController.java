package co.analisys.clase.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.analisys.clase.dto.OcupacionClase;
import co.analisys.clase.dto.OcupacionRequest;
import co.analisys.clase.kafka.DashboardOcupacion;
import co.analisys.clase.kafka.OcupacionClaseProducer;
import co.analisys.clase.model.CheckpointOffset;
import co.analisys.clase.model.OcupacionHistorial;
import co.analisys.clase.repository.CheckpointOffsetRepository;
import co.analisys.clase.repository.OcupacionHistorialRepository;
import co.analisys.clase.service.ClaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ocupacion")
@Tag(name = "Ocupacion (Kafka)", description = "Monitoreo en tiempo real de la ocupacion de clases con Kafka")
public class OcupacionController {

    private final ClaseService claseService;
    private final OcupacionClaseProducer producer;
    private final DashboardOcupacion dashboard;
    private final OcupacionHistorialRepository historialRepository;
    private final CheckpointOffsetRepository checkpointRepository;

    public OcupacionController(ClaseService claseService, OcupacionClaseProducer producer,
            DashboardOcupacion dashboard, OcupacionHistorialRepository historialRepository,
            CheckpointOffsetRepository checkpointRepository) {
        this.claseService = claseService;
        this.producer = producer;
        this.dashboard = dashboard;
        this.historialRepository = historialRepository;
        this.checkpointRepository = checkpointRepository;
    }

    @Operation(summary = "Publicar la ocupacion de una clase", description = "Envia la ocupacion al topic ocupacion-clases. Roles permitidos: ADMIN, TRAINER.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Actualizacion publicada en Kafka"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content),
            @ApiResponse(responseCode = "404", description = "La clase no existe", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/{claseId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publicar(@PathVariable Long claseId, @RequestBody OcupacionRequest request) {
        claseService.obtenerClasePorId(claseId); // 404 si la clase no existe
        producer.actualizarOcupacion(claseId, request.ocupacionActual());
    }

    @Operation(summary = "Dashboard de ocupacion", description = "Ultima ocupacion conocida por clase, alimentada por el consumidor Kafka. Roles permitidos: ADMIN, TRAINER, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public Map<Long, OcupacionClase> dashboard() {
        return dashboard.instantanea();
    }

    @Operation(summary = "Historial recuperado desde el log de Kafka", description = "Ultimos 50 registros procesados por el proceso de recuperacion. Roles permitidos: ADMIN, TRAINER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/historial")
    public List<OcupacionHistorial> historial() {
        return historialRepository.findTop50ByOrderByIdDesc();
    }

    @Operation(summary = "Checkpoints de offsets", description = "Ultimo offset procesado por particion (punto de reinicio). Roles permitidos: ADMIN, TRAINER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/checkpoint")
    public List<CheckpointOffset> checkpoints() {
        return checkpointRepository.findAll();
    }
}
