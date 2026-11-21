package com.reporte.reporteciudadano.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voto", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_reporte"}))
public class Voto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_reporte")
    private Reporte reporte;

    @PrePersist
    void antesDeGuardar() { if (fecha == null) fecha = LocalDateTime.now(); }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Reporte getReporte() {
		return reporte;
	}

	public void setReporte(Reporte reporte) {
		this.reporte = reporte;
	}
}