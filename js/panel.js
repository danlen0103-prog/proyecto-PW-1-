alIdiomaListo(function () {
  if (!exigirRol(["FUNCIONARIO", "ADMIN"])) return;

  const modal = new bootstrap.Modal(document.getElementById("modalEstado"));
  
  const TRANSICIONES = {
    RECIBIDO: ["EN_REVISION", "RECHAZADO"],
    EN_REVISION: ["EN_PROCESO", "RECHAZADO"],
    EN_PROCESO: ["RESUELTO", "RECHAZADO"]
  };
  let casos = [];
  let actual = null;          
  let peticion = null;

  llenarSelect("#fTipo", RUTAS.tipos);
  llenarSelect("#fBarrio", RUTAS.barrios);
  $("#fTipo, #fBarrio, #fEstado").on("change", cargar);
  $("#btnLimpiar").on("click", function () { $("#fTipo, #fBarrio, #fEstado").val(""); cargar(); });

  function mostrar(estado, texto) {
    $("#msgCargando, #msgVacio, #msgError, #zonaTabla").addClass("d-none");
    if (estado === "cargando") $("#msgCargando").removeClass("d-none");
    if (estado === "vacio") $("#msgVacio").removeClass("d-none");
    if (estado === "error") $("#msgError").removeClass("d-none").find(".detalle").text(texto);
    if (estado === "lista") $("#zonaTabla").removeClass("d-none");
  }

  
  function cargar() {
    const filtros = {};
    if ($("#fTipo").val()) filtros[FILTROS.tipo] = $("#fTipo").val();
    if ($("#fBarrio").val()) filtros[FILTROS.barrio] = $("#fBarrio").val();
    if ($("#fEstado").val()) filtros[FILTROS.estado] = $("#fEstado").val();

    if (peticion) peticion.abort();
    mostrar("cargando");
    peticion = $.ajax({ url: API + RUTAS.panel, data: filtros, headers: Sesion.cabecera() })
      .done(function (datos) {
        casos = (Array.isArray(datos) ? datos : (datos.content || [])).map(function (d) {
          const r = normalizar(d);
          if (r.puntaje == null && d.puntaje != null) r.puntaje = d.puntaje;
          return r;
        });
       
        casos.sort(function (a, b) { return (Number(b.puntaje) || 0) - (Number(a.puntaje) || 0); });
        dibujar();
      })
      .fail(function (x, estado) {
        if (estado === "abort") return;
        mostrar("error", mensajeError(x, t("err.cargarPanel")));
      });
  }

  
  function prioridad(r, maximo) {
    if (CERRADOS.includes(r.estado)) return '<span class="badge text-bg-light border">' + escapar(t("panel.prioCerrado")) + '</span>';
    const p = Number(r.puntaje) || 0;
    const razon = maximo > 0 ? p / maximo : 0;
    const [color, texto] = razon >= 0.66 ? ["danger", t("panel.alta")] : razon >= 0.33 ? ["warning", t("panel.media")] : ["success", t("panel.baja")];
    return '<span class="badge text-bg-' + color + '">' + p + ' · ' + texto + '</span>';
  }

  function dibujar() {
    const maximo = Math.max.apply(null, casos.map(function (r) { return Number(r.puntaje) || 0; }).concat([0]));
    const $cuerpo = $("#tabla tbody").empty();

    casos.forEach(function (r) {
      const resumen = r.descripcion.length > 70 ? r.descripcion.slice(0, 70) + "…" : r.descripcion;
      const cerrado = CERRADOS.includes(r.estado);
      const puedeCambiar = Sesion.rol() === "FUNCIONARIO";  
      $cuerpo.append(
        '<tr>' +
          '<td>' + prioridad(r, maximo) + '</td>' +
          '<th scope="row"><a href="detalle.html?id=' + encodeURIComponent(r.id) + '">' + escapar(r.tipo) + '</a>' +
            '<div class="small text-body-secondary fw-normal">' + escapar(resumen) + '</div></th>' +
          '<td>' + escapar(r.barrio) + '</td>' +
          '<td>' + etiquetaEstado(r.estado) + '</td>' +
          '<td class="text-end">' + (Number(r.votos) || 0) + '</td>' +
          '<td>' + escapar(hace(r.fecha)) + '</td>' +
          '<td class="text-end">' +
            (cerrado || !puedeCambiar
              ? '<span class="text-body-secondary small">' + escapar(t(cerrado ? "panel.sinCambios" : "panel.soloConsulta")) + '</span>'
              : '<button type="button" class="btn btn-primary btn-sm btn-estado" data-id="' + escapar(r.id) + '">' + escapar(t("panel.cambiarEstado")) + '</button>') +
          '</td>' +
        '</tr>');
    });

    $("#contador").text(textoCasos(casos.length));
    mostrar(casos.length ? "lista" : "vacio");
  }

  function resumenModal() {
    return t("panel.resumen", { tipo: actual.tipo, barrio: actual.barrio, estado: nombreEstado(actual.estado) });
  }

  
  $("#tabla").on("click", ".btn-estado", function () {
    const id = String($(this).data("id"));
    actual = casos.find(function (r) { return String(r.id) === id; });
    if (!actual) return;

    limpiarErrores("#formEstado");
    $("#alertaModal").empty();
    $("#modalResumen").text(resumenModal());
    $("#nuevoEstado").val("");
    const permitidos = TRANSICIONES[actual.estado] || [];
    $("#nuevoEstado option").each(function () {
      if (this.value) $(this).prop("disabled", !permitidos.includes(this.value));
    });
    $("#comentario").val("");
    $("#fotoSolucion").val("");
    etiquetaComentario();
    modal.show();
  });

  function etiquetaComentario() {
    const e = $("#nuevoEstado").val();
    const textos = {
      RESUELTO: t("panel.comentarioCierre"),
      RECHAZADO: t("panel.comentarioRechazo")
    };
    $("#etiquetaComentario").text(textos[e] || t("panel.comentario"));
    $("#bloqueFoto").toggleClass("d-none", e !== "RESUELTO");
    if (e !== "RESUELTO") $("#fotoSolucion").val("");
  }
  $("#nuevoEstado").on("change", etiquetaComentario);

  $("#formEstado").on("submit", async function (e) {
    e.preventDefault();
    limpiarErrores(this);
    $("#alertaModal").empty();
    const estado = $("#nuevoEstado").val();
    const comentario = $("#comentario").val().trim();
    const foto = estado === "RESUELTO" ? $("#fotoSolucion")[0].files[0] : null;

    if (!estado) marcar("#nuevoEstado", t("panel.errElige"));
    if (CERRADOS.includes(estado) && !comentario) {
      marcar("#comentario", estado === "RESUELTO" ? t("panel.errCierre") : t("panel.errRechazo"));
    }
    if (comentario.length > 500) marcar("#comentario", t("panel.errMax"));
    if (foto) { const err = await validarImagen(foto); if (err) marcar("#fotoSolucion", err); }
    if ($(this).find(".is-invalid").length) { enfocarPrimerError(this); return; }

   
    const datos = new FormData();
    datos.append("estado", estado);
    datos.append("comentario", comentario);
    if (foto) datos.append("foto", foto);

    $("#btnGuardarEstado").prop("disabled", true).text(t("panel.guardando"));
    $.ajax({
      url: API + RUTAS_CASO.estado(actual.id), method: "POST", headers: Sesion.cabecera(),
      data: datos, processData: false, contentType: false
    })
    .done(function () {
      modal.hide();
      mostrarAlerta("#alerta", t("panel.cambiado", { tipo: actual.tipo, estado: nombreEstado(estado) }), "success");
      cargar();
    })
    .fail(function (x) {
      mostrarAlerta("#alertaModal", mensajeError(x, t("err.cambiarEstado")), "danger");
    })
    .always(function () { $("#btnGuardarEstado").prop("disabled", false).text(t("panel.guardar")); });
  });

 
  $(document).on("i18n:changed", function () {
    if (casos.length) dibujar();
    etiquetaComentario();
    if (actual && $("#modalEstado").hasClass("show")) $("#modalResumen").text(resumenModal());
  });

  cargar();
});