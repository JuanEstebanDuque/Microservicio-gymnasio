package co.analisys.clase.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.analisys.clase.model.Clase;
import co.analisys.clase.service.ClaseService;

@RestController
@RequestMapping("/api/clase")
public class ClaseController {

    private final ClaseService claseService;

    public ClaseController(ClaseService claseService) {
        this.claseService = claseService;
    }

    @PostMapping
    public Clase programarClase(@RequestBody Clase clase) {
        return claseService.programarClase(clase);
    }

    @GetMapping
    public List<Clase> obtenerTodasLasClases() {
        return claseService.obtenerTodasLasClases();
    }

    @GetMapping("/{id}")
    public Clase obtenerClasePorId(@PathVariable Long id) {
        return claseService.obtenerClasePorId(id);
    }
}
