alIdiomaListo(function () {
  if (!exigirRol(["FUNCIONARIO", "ADMIN"])) return;

  let datosEst = null;   

  function mostrar(estado, texto) {
    $("#msgCargando, #msgVacio, #msgError, #zonaDatos").addClass("d-none");
    if (estado === "cargando") $("#msgCargando").removeClass("d-none");
    if (estado === "vacio") $("#msgVacio").removeClass("d-none");
    if (estado === "error") $("#msgError").removeClass("d-none").find(".detalle").text(texto);
    if (estado === "datos") $("#zonaDatos").removeClass("d-none");
  }

  function fila(f) {
    const n = function (v) { return Number(v) || 0; };
    const pendientes = n(f.pendientes);
    const enProceso = n(f.enProceso != null ? f.enProceso : (f.enproceso != null ? f.enproceso : f.en_proceso));
    const resueltos = n(f.resueltos);
    return {
      barrio: nombreDe(f.barrio) || f.nombre || "",
      pendientes: pendientes, enProceso: enProceso, resueltos: resueltos,
      total: f.total != null ? n(f.total) : pendientes + enProceso + resueltos
    };
  }

  function dibujar() {
    const filas = datosEst.filas, suma = datosEst.suma;
    $("#tTotal").text(suma.tot); $("#tPend").text(suma.pend);
    $("#tProc").text(suma.proc); $("#tRes").text(suma.res);

    const $c = $("#tabla tbody").empty();
    filas.forEach(function (f) {
      $c.append('<tr><th scope="row">' + escapar(f.barrio || t("est.sinBarrio")) + '</th><td class="text-end">' + f.pendientes +
                '</td><td class="text-end">' + f.enProceso + '</td><td class="text-end">' + f.resueltos +
                '</td><td class="text-end fw-semibold">' + f.total + '</td></tr>');
    });
    $("#tabla tfoot").html('<tr><th scope="row">' + escapar(t("est.totalTh")) + '</th><td class="text-end">' + suma.pend +
                '</td><td class="text-end">' + suma.proc + '</td><td class="text-end">' + suma.res +
                '</td><td class="text-end">' + suma.tot + '</td></tr>');

    $("#fechaReporte").text(t("est.generado", { fecha: fechaHora(datosEst.generado) }));
    mostrar(filas.length ? "datos" : "vacio");
  }

  mostrar("cargando");
  $.ajax({ url: API + RUTAS.estadisticas, headers: Sesion.cabecera() })
    .done(function (datos) {
      const filas = (Array.isArray(datos) ? datos : (datos.porBarrio || [])).map(fila);
      filas.sort(function (a, b) { return b.pendientes - a.pendientes || a.barrio.localeCompare(b.barrio, "es"); });

      const suma = filas.reduce(function (s, f) {
        s.pend += f.pendientes; s.proc += f.enProceso; s.res += f.resueltos; s.tot += f.total; return s;
      }, { pend: 0, proc: 0, res: 0, tot: 0 });

      datosEst = { filas: filas, suma: suma, generado: new Date().toISOString() };
      dibujar();
    })
    .fail(function (x) { mostrar("error", mensajeError(x, t("err.cargarEst"))); });

  $("#btnImprimir").on("click", function () { window.print(); });

  
  $(document).on("i18n:changed", function () { if (datosEst) dibujar(); });
});
