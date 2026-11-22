alIdiomaListo(function () {
  if (!exigirRol(["CIUDADANO"])) return;

  let lista = null;   

  function mostrar(estado, texto) {
    $("#msgCargando, #msgVacio, #msgError, #zonaTabla").addClass("d-none");
    if (estado === "cargando") $("#msgCargando").removeClass("d-none");
    if (estado === "vacio") $("#msgVacio").removeClass("d-none");
    if (estado === "error") $("#msgError").removeClass("d-none").find(".detalle").text(texto);
    if (estado === "lista") $("#zonaTabla").removeClass("d-none");
  }

  
  function historial(filas) {
    if (!filas.length) return '<p class="small text-body-secondary mb-0">' + escapar(t("hist.vacio")) + '</p>';
    const fuerte = function (e) { return "<strong>" + escapar(nombreEstado(e)) + "</strong>"; };
    return '<ol class="linea-tiempo list-unstyled mb-0">' + filas.map(function (h) {
      const accion = h.anterior
        ? t("hist.paso", { de: fuerte(h.anterior), a: fuerte(h.nuevo) })
        : t("hist.registrado", { estado: fuerte(h.nuevo) });
      return '<li class="paso">' +
        '<div>' + accion + '</div>' +
        '<div class="small text-body-secondary">' + escapar(fechaHora(h.fecha)) +
          (h.funcionario ? " · " + escapar(h.funcionario) : "") + '</div>' +
        (h.comentario ? '<blockquote class="paso-comentario mb-0">' + escapar(h.comentario) + '</blockquote>' : "") +
        (h.evidencia ? '<p class="small mb-0 mt-2"><a href="' + escapar(urlFoto(h.evidencia)) +
          '" target="_blank" rel="noopener">' + escapar(t("hist.evidencia")) + '</a></p>' : "") +
        '</li>';
    }).join("") + '</ol>';
  }

  function dibujar() {
    const $cuerpo = $("#tabla tbody").empty();
    lista.forEach(function (r) {
     
      const titulo = r.estado === "RECHAZADO"
        ? '<span class="fw-semibold">' + escapar(r.tipo) + '</span>'
        : '<a href="detalle.html?id=' + encodeURIComponent(r.id) + '">' + escapar(r.tipo) + '</a>';
      const etiqueta = r.estado === "RESUELTO" ? t("det.comentarioCierre") : t("det.motivoRechazo");
      const nota = CERRADOS.includes(r.estado) && r.comentario
        ? '<div class="small mt-1"><strong>' + escapar(etiqueta) + ': </strong>' + escapar(r.comentario) + '</div>' : "";
      const solucion = r.estado === "RESUELTO" && r.fotoSolucion
        ? '<div class="small mt-1"><a href="' + escapar(urlFoto(r.fotoSolucion)) +
          '" target="_blank" rel="noopener">' + escapar(t("mis.verSolucion")) + '</a></div>' : "";
      $cuerpo.append(
        '<tr>' +
          '<th scope="row">' + titulo +
            '<div class="small text-body-secondary fw-normal">' + escapar(r.barrio) + '</div>' +
            '<details class="mt-2 fw-normal"><summary class="small">' + escapar(t("mis.verHistorial")) + '</summary><div class="mt-2">' +
              historial(r.historial) + '</div></details></th>' +
          '<td>' + escapar(hace(r.fecha)) + '</td>' +
          '<td>' + etiquetaEstado(r.estado) + nota + solucion + '</td>' +
          '<td class="text-end">' + (Number(r.votos) || 0) + '</td>' +
        '</tr>');
    });
    $("#contador").text(textoCasos(lista.length));
    mostrar(lista.length ? "lista" : "vacio");
  }

  mostrar("cargando");
  $.ajax({ url: API + RUTAS.misReportes, headers: Sesion.cabecera() })
    .done(function (datos) {
      lista = (Array.isArray(datos) ? datos : (datos.content || [])).map(function (d) {
        const r = normalizar(d);
        r.historial = (Array.isArray(d.historial) ? d.historial : []).map(normalizarHistorial)
          .sort(function (a, b) { return new Date(a.fecha) - new Date(b.fecha); });
        return r;
      });
      lista.sort(function (a, b) { return new Date(b.fecha) - new Date(a.fecha); });
      dibujar();
    })
    .fail(function (x) { mostrar("error", mensajeError(x, t("err.cargarMis"))); });


  $(document).on("i18n:changed", function () { if (lista) dibujar(); });
});
