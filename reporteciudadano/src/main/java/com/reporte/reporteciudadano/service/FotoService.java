package com.reporte.reporteciudadano.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


@Service
public class FotoService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[0-9a-f\\-]{36}\\.(jpg|png)");
    private final Path carpeta;

    
    public FotoService(@Value("${app.uploads.dir}") String directorio) throws IOException {
        this.carpeta = Paths.get(directorio).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    
    public String errorDeFoto(MultipartFile f) {
        if (f == null || f.isEmpty()) {
            return "La foto es obligatoria";
        }
        if (f.getSize() > MAX_BYTES) {
            return "La foto supera el tamaño máximo de 5 MB";
        }
        if (extensionSegunContenido(f) == null) {
            return "La foto debe ser JPG o PNG";
        }
        return null;
    }

    
    public String guardar(MultipartFile f) {
        String nombre = UUID.randomUUID() + "." + extensionSegunContenido(f);
        try {
            f.transferTo(carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la foto");
        }
        return nombre;
    }

    public Resource cargar(String nombre) {
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La foto no existe");
        }
        Path ruta = carpeta.resolve(nombre).normalize();
        if (!ruta.startsWith(carpeta) || !Files.isReadable(ruta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La foto no existe");
        }
        return new FileSystemResource(ruta);
    }

   
    private String extensionSegunContenido(MultipartFile f) {
        try (InputStream in = f.getInputStream()) {
            byte[] c = in.readNBytes(8);
            if (c.length >= 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) {
                return "jpg";
            }
            if (c.length >= 8 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G'
                    && c[4] == 0x0D && c[5] == 0x0A && c[6] == 0x1A && c[7] == 0x0A) {
                return "png";
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}