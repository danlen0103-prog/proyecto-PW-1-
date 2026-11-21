package com.reporte.reporteciudadano.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.reporte.reporteciudadano.model.*;
import com.reporte.reporteciudadano.repository.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final TipoReporteRepository tipos;
    private final BarrioRepository barrios;
    private final PasswordEncoder encoder;

    public DataInitializer(UsuarioRepository usuarios, TipoReporteRepository tipos,
                           BarrioRepository barrios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.tipos = tipos;
        this.barrios = barrios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
      
        crearTipo("Hueco en la vía", 3);
        crearTipo("Alumbrado público dañado", 4);
        crearTipo("Acumulación de basuras", 2);
        crearTipo("Fuga de agua", 5);
        crearTipo("Andén o espacio público deteriorado", 2);
        crearTipo("Señalización o semáforo dañado", 3);
        crearTipo("Árbol caído o en riesgo", 4);
        crearTipo("Alcantarilla obstruida o sin tapa", 4);
        crearTipo("Otro", 2);

       
        String[] listaBarrios = {
        	    "Centro", "Quirinal", "Cándido Leguízamo", "Las Granjas", "La Toma",
        	    "Altico", "Ipanema", "Canaima", "Timanco", "Santa Inés",
        	    "Sevilla", "El Jardín", "Los Parques", "Campo Núñez", "La Gaitana",
        	    "Tenerife", "Prado Norte", "Buganviles", "Diego de Ospina", "Santa Isabel",
        	    "Álamos", "Granjas de San Antonio", "Alberto Galindo", "Las Américas", "Monserrate","El Jardin"
        	};
        	for (String nombre : listaBarrios) {
        	    crearBarrio(nombre);
        	}

        
        crearUsuario("Administrador", "admin@reporte.com", "Admin123*", Rol.ADMIN);
        crearUsuario("Funcionario", "funcionario@reporte.com", "Func123*", Rol.FUNCIONARIO);
    }

    private void crearTipo(String nombre, int peso) {
        if (!tipos.existsByNombre(nombre)) {
            TipoReporte t = new TipoReporte();
            t.setNombre(nombre);
            t.setPeso(peso);
            tipos.save(t);
        }
    }

    private void crearBarrio(String nombre) {
        if (!barrios.existsByNombre(nombre)) {
            Barrio b = new Barrio();
            b.setNombre(nombre);
            barrios.save(b);
        }
    }

    private void crearUsuario(String nombre, String correo, String clave, Rol rol) {
        if (!usuarios.existsByCorreoIgnoreCase(correo)) {
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setClave(encoder.encode(clave));
            u.setRol(rol);
            usuarios.save(u);
        }
    }
}