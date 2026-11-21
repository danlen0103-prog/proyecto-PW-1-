package com.reporte.reporteciudadano.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicUpdate;


@Entity @Table(name = "reporte")
@DynamicUpdate
public class Reporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descripcion;
    private String referencia;
    private String foto;
    private Double latitud;
    private Double longitud;
    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.RECIBIDO;
    private LocalDateTime fecha;
    private Integer votos = 0;
    @Column(name = "comentario_estado")
    private String comentarioEstado;
    @Column(name = "foto_solucion")
    private String fotoSolucion;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_tipo")
    private TipoReporte tipo;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_barrio")
    private Barrio barrio;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_funcionario")
    private Usuario funcionario;

    @PrePersist
    void antesDeGuardar() { if (fecha == null) fecha = LocalDateTime.now(); }
    

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public Double getLatitud() {
		return latitud;
	}

	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}

	public Double getLongitud() {
		return longitud;
	}

	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public Integer getVotos() {
		return votos;
	}

	public void setVotos(Integer votos) {
		this.votos = votos;
	}

	public String getComentarioEstado() {
		return comentarioEstado;
	}

	public void setComentarioEstado(String comentarioEstado) {
		this.comentarioEstado = comentarioEstado;
	}

	public String getFotoSolucion() {
		return fotoSolucion;
	}

	public void setFotoSolucion(String fotoSolucion) {
		this.fotoSolucion = fotoSolucion;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public TipoReporte getTipo() {
		return tipo;
	}

	public void setTipo(TipoReporte tipo) {
		this.tipo = tipo;
	}

	public Barrio getBarrio() {
		return barrio;
	}

	public void setBarrio(Barrio barrio) {
		this.barrio = barrio;
	}

	public Usuario getFuncionario() {
		return funcionario;
	}

	public void setFuncionario(Usuario funcionario) {
		this.funcionario = funcionario;
	}
}