package com.reporte.reporteciudadano.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "historial_estado")
public class HistorialEstado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "estado_anterior")
    private String estadoAnterior;
    @Column(name = "estado_nuevo")
    private String estadoNuevo;
    private LocalDateTime fecha;
    private String comentario;
    private String evidencia;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_reporte")
    private Reporte reporte;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_funcionario")
    private Usuario funcionario;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEstadoAnterior() {
		return estadoAnterior;
	}
	public void setEstadoAnterior(String estadoAnterior) {
		this.estadoAnterior = estadoAnterior;
	}
	public String getEstadoNuevo() {
		return estadoNuevo;
	}
	public void setEstadoNuevo(String estadoNuevo) {
		this.estadoNuevo = estadoNuevo;
	}
	public LocalDateTime getFecha() {
		return fecha;
	}
	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	public String getEvidencia() {
		return evidencia;
	}
	public void setEvidencia(String evidencia) {
		this.evidencia = evidencia;
	}
	public Reporte getReporte() {
		return reporte;
	}
	public void setReporte(Reporte reporte) {
		this.reporte = reporte;
	}
	public Usuario getFuncionario() {
		return funcionario;
	}
	public void setFuncionario(Usuario funcionario) {
		this.funcionario = funcionario;
	}
}