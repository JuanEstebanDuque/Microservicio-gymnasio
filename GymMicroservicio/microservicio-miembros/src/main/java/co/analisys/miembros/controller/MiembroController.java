package co.analisys.miembros.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.analisys.miembros.model.Miembro;
import co.analisys.miembros.service.MiembroService;

@RestController
@RequestMapping("/api/miembro")
public class MiembroController {
    
    private final MiembroService miembroService;

    public MiembroController(MiembroService miembroService) {
        this.miembroService = miembroService;
    }

    @PostMapping
    public Miembro registrarMiembro(@RequestBody Miembro miembro) {
        return miembroService.registrarMiembro(miembro);
    }

    @GetMapping
    public List<Miembro> obtenerTodosLosMiembros() {
        return miembroService.obtenerTodosLosMiembros();
    }

}