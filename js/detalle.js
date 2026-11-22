alIdiomaListo(function () {
  const id = leerId();
  const rol = Sesion.rol();
  let mapaCaso = null;
  let casoActual = null, historialActual = null, yaApoyo = false;   // para volver a pintar al cambiar de idioma

  if (!id) { fallo(t("det.falta")); return; }

  function fallo(texto) {
    $("#cargando").addClass("d-none");
    $("#caso").addClass("d-none");
    $("#error").removeClass("d-none").text(texto);
  }

  
  function cargar() {
    $.getJSON(API + RUTAS_CASO.detalle(id))
      .done(function (datos) {
        const r = normalizar(datos);
        if (r.puntaje == null && datos.puntaje != null) r.puntaje = datos.puntaje;
        pintar(r);
        configurarApoyo(r);
        if (Array.isArray(datos.historial)) {
          pintarHistorial(datos.historial.map(normalizarHistorial), r);
        } else {
          $.getJSON(API + RUTAS_CASO.historial(id))
            .done(function (h) { pintarHistorial((Array.isArray(h) ? h : []).map(normalizarHistorial), r); })
            .fail(function () { pintarHistorial([], r); });
        }
      })
      .fail(function (x) {
        fallo(x.status === 404 ? t("det.noExiste") : mensajeError(x, t("err.cargarCaso")));
      });
  }

  
  function pintar(r) {
    casoActual = r;
    document.title = r.tipo + t("titulo.sufijo");
    $("#cargando").addClass("d-none");
    $("#caso").removeClass("d-none");

    $("#dTipo").text(r.tipo);
    $("#dEstado").html(etiquetaEstado(r.estado));
    $("#dFecha").text(r.fecha ? t("det.publicado", { hace: hace(r.fecha), fecha: fechaHora(r.fecha) }) : "");
    $("#dBarrio").text(r.barrio ? t("det.barrio", { barrio: r.barrio }) : "");
    $("#dDescripcion").text(r.descripcion);
    $("#dReferencia").text(r.referencia ? t("det.referencia", { ref: r.referencia }) : t("det.sinReferencia"));
    $("#dFoto").attr({ src: urlFoto(r.foto), alt: t("caso.fotoAlt", { tipo: r.tipo }) })
               .one("error", function () { this.src = fotoVacia(); this.alt = t("foto.sinDisponible"); });
    numeros(r);

    if (Number.isFinite(r.lat) && Number.isFinite(r.lng)) {
      $("#bloqueMapa").removeClass("d-none");
      if (!mapaCaso) {
        mapaCaso = L.map("dMapa", { scrollWheelZoom: false }).setView([r.lat, r.lng], 16);
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
          maxZoom: 19, attribution: "© OpenStreetMap contributors"
        }).addTo(mapaCaso);
        L.marker([r.lat, r.lng], { title: r.tipo, alt: r.tipo }).addTo(mapaCaso);
        setTimeout(function () { mapaCaso.invalidateSize(); }, 200);
      }
    }
  }

  function numeros(r) {
    $("#dVotos").text(Number(r.votos) || 0);
    const cerrado = CERRADOS.includes(r.estado);
    $("#dPuntaje").text(r.puntaje == null || r.puntaje === "" ? "—" : (cerrado ? "0" : r.puntaje));
  }

  
  function configurarApoyo(r) {
    const $z = $("#zonaVoto").empty();
    if (CERRADOS.includes(r.estado)) {
      $z.append($('<p class="text-body-secondary mb-0">').text(t("det.cerrado")));
    } else if (rol === "CIUDADANO") {
      $z.append($('<button id="btnApoyar" type="button" class="btn btn-primary btn-lg" aria-pressed="false">').text(t("det.apoyar")),
                $('<p id="msgVoto" class="mt-2 mb-0" role="status">'));
      if (yaApoyo) {
        $("#btnApoyar").prop("disabled", true).attr("aria-pressed", "true").text(t("det.yaApoyaste"));
        $("#msgVoto").text(t("det.gracias"));
      }
    } else if (rol === "FUNCIONARIO" || rol === "ADMIN") {
      $z.append($('<p class="mb-0">').text(t("det.personalAntes") + " ")
        .append($('<a href="panel.html">').text(t("det.panel"))).append(document.createTextNode(".")));
    } else {
      const volver = "detalle.html?id=" + encodeURIComponent(id);
      $z.append($('<a class="btn btn-primary btn-lg">').attr("href", "login.html?next=" + encodeURIComponent(volver)).text(t("det.ingresaApoyar")));
    }
  }

  $("#zonaVoto").on("click", "#btnApoyar", function () {
    const $b = $(this).prop("disabled", true);
    $.ajax({ url: API + RUTAS_CASO.votar(id), method: "POST", headers: Sesion.cabecera() })
      .done(function () {
        yaApoyo = true;
        $b.attr("aria-pressed", "true").text(t("det.yaApoyaste"));
        $("#msgVoto").removeClass("text-danger").text(t("det.gracias"));
        // Se vuelve a pedir el caso para mostrar el contador y la prioridad actualizados
        $.getJSON(API + RUTAS_CASO.detalle(id)).done(function (datos) {
          const r = normalizar(datos);
          if (r.puntaje == null && datos.puntaje != null) r.puntaje = datos.puntaje;
          casoActual = r;
          numeros(r);
        });
      })
      .fail(function (x) {
        if (x.status !== 409) $b.prop("disabled", false);   // con 409 (ya apoyado o caso cerrado) el botón queda desactivado
        $("#msgVoto").addClass("text-danger").text(mensajeError(x, t("err.apoyar")));
      });
  });

 
  function pintarHistorial(filas, r) {
    historialActual = filas;
    casoActual = r;
    const $ol = $("#historial").empty();
    filas.sort(function (a, b) { return new Date(a.fecha) - new Date(b.fecha); });

    if (!filas.length) {
      $ol.append($('<li class="text-body-secondary">').text(t("hist.vacio")));
    }
    filas.forEach(function (h) {
      const fuerte = function (e) { return "<strong>" + escapar(nombreEstado(e)) + "</strong>"; };
      const accion = h.anterior
        ? t("hist.paso", { de: fuerte(h.anterior), a: fuerte(h.nuevo) })
        : t("hist.registrado", { estado: fuerte(h.nuevo) });
      $ol.append(
        '<li class="paso">' +
          '<div class="paso-titulo">' + accion + '</div>' +
          '<div class="small text-body-secondary">' + escapar(fechaHora(h.fecha)) +
            (h.funcionario ? " · " + escapar(h.funcionario) : "") + '</div>' +
          (h.comentario ? '<blockquote class="paso-comentario mb-0">' + escapar(h.comentario) + '</blockquote>' : "") +
          (h.evidencia ? '<p class="small mb-0 mt-2"><a href="' + escapar(urlFoto(h.evidencia)) +
            '" target="_blank" rel="noopener">' + escapar(t("hist.evidencia")) + '</a></p>' : "") +
        '</li>');
    });

    
    const $c = $("#cierre").empty();
    if (CERRADOS.includes(r.estado)) {
      const ultimo = filas.slice().reverse().find(function (h) { return h.comentario; });
      const texto = r.comentario || (ultimo ? ultimo.comentario : "");
      if (texto) {
        const titulo = r.estado === "RESUELTO" ? t("det.comentarioCierre") : t("det.motivoRechazo");
        $c.append($("<div>").addClass("alert " + (r.estado === "RESUELTO" ? "alert-success" : "alert-secondary") + " mb-4")
          .attr("role", "note")
          .append($("<strong>").text(titulo + ": "), document.createTextNode(texto)));
        if (r.estado === "RESUELTO" && r.fotoSolucion) {
          $c.append($("<img>").addClass("img-fluid foto-detalle w-100 mb-4")
            .attr({ src: urlFoto(r.fotoSolucion), alt: t("det.fotoSolucionAlt") })
            .one("error", function () { $(this).remove(); }));
        }
      }
    }
  }

  
  $(document).on("i18n:changed", function () {
    if (!casoActual) return;
    pintar(casoActual);
    configurarApoyo(casoActual);
    if (historialActual) pintarHistorial(historialActual, casoActual);
  });

  cargar();
});
