alIdiomaListo(function () {
  if (!exigirRol(["CIUDADANO"])) return;

  llenarSelect("#tipo", RUTAS.tipos);
  llenarSelect("#barrio", RUTAS.barrios);

  
  const mapa = L.map("mapa").setView(CENTRO_MAPA, ZOOM_MAPA);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19, attribution: "© OpenStreetMap contributors"
  }).addTo(mapa);
  $(window).on("load", function () { mapa.invalidateSize(); });
  let marcador = null;

  
  let puntoActual = null, ubiClave = "";
  function textoPunto() {
    $("#puntoElegido").text(puntoActual
      ? t("pub.puntoElegido", { lat: puntoActual[0].toFixed(5), lng: puntoActual[1].toFixed(5) })
      : t("pub.sinPunto"));
  }
  function mensajeUbicacion(clave) { ubiClave = clave; $("#msgUbicacion").text(clave ? t(clave) : ""); }

  function fijar(lat, lng) {
    $("#latitud").val(lat.toFixed(6));
    $("#longitud").val(lng.toFixed(6));
    puntoActual = [lat, lng];
    textoPunto();
    $("#mapa").removeClass("is-invalid");
    $("#mapaError").text("");
  }

  function ponerPunto(lat, lng, acercar) {
    if (marcador) {
      marcador.setLatLng([lat, lng]);
    } else {
      marcador = L.marker([lat, lng], { draggable: true, title: t("pub.lugarProblema") }).addTo(mapa);
      marcador.on("dragend", function (e) { const p = e.target.getLatLng(); fijar(p.lat, p.lng); });
    }
    fijar(lat, lng);
    if (acercar) mapa.setView([lat, lng], Math.max(mapa.getZoom(), 16));
  }

  mapa.on("click", function (e) { ponerPunto(e.latlng.lat, e.latlng.lng, false); });

  
  $("#latitud, #longitud").on("change", function () {
    const lat = num($("#latitud").val()), lng = num($("#longitud").val());
    if (Number.isFinite(lat) && Number.isFinite(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180) ponerPunto(lat, lng, true);
  });

 
  $("#btnUbicacion").on("click", function () {
    mensajeUbicacion("");
    if (!navigator.geolocation) { mensajeUbicacion("pub.ubiNoSoporta"); return; }
    mensajeUbicacion("pub.ubiBuscando");
    navigator.geolocation.getCurrentPosition(
      function (pos) { mensajeUbicacion(""); ponerPunto(pos.coords.latitude, pos.coords.longitude, true); },
      function () { mensajeUbicacion("pub.ubiFallo"); },
      { enableHighAccuracy: true, timeout: 10000 });
  });

  
  $("#descripcion").on("input", function () { $("#contadorDesc").text(this.value.length + " / 500"); });

  
  const validarFoto = validarImagen;

  let urlPrevia = null;
  function quitarPrevia() {
    if (urlPrevia) { URL.revokeObjectURL(urlPrevia); urlPrevia = null; }
    $("#previa").addClass("d-none").removeAttr("src");
  }

  $("#foto").on("change", async function () {
    $("#foto").removeClass("is-invalid"); $("#fotoError").text("");
    quitarPrevia();
    const f = this.files[0];
    if (!f) return;
    const err = await validarFoto(f);
    if (err) { marcar("#foto", err); this.value = ""; return; }
    urlPrevia = URL.createObjectURL(f);
    $("#previa").attr("src", urlPrevia).removeClass("d-none");
  });

 
  $("#formCaso").on("submit", async function (e) {
    e.preventDefault();
    limpiarErrores(this);
    $("#alerta").empty();

    const tipo = $("#tipo").val(), barrio = $("#barrio").val();
    const desc = $("#descripcion").val().trim(), ref = $("#referencia").val().trim();
    const lat = num($("#latitud").val()), lng = num($("#longitud").val());
    const foto = $("#foto")[0].files[0];

    if (!tipo) marcar("#tipo", t("pub.errTipo"));
    if (!barrio) marcar("#barrio", t("pub.errBarrio"));
    if (desc.length < 10 || desc.length > 500) marcar("#descripcion", t("pub.errDesc"));
    if (!ref) marcar("#referencia", t("pub.errRefObligatoria"));
    else if (ref.length > 150) marcar("#referencia", t("pub.errRef"));
    // El mapa es opcional: sin punto, el barrio y la referencia bastan (el caso queda en el centro de la ciudad)
    const hayPunto = $("#latitud").val().trim() !== "" || $("#longitud").val().trim() !== "";
    const puntoValido = Number.isFinite(lat) && Number.isFinite(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180;
    if (hayPunto && !puntoValido) marcar("#mapa", t("pub.errMapa"));
    if (!foto) marcar("#foto", t("pub.errSinFoto"));
    else { const err = await validarFoto(foto); if (err) marcar("#foto", err); }

    if ($(this).find(".is-invalid").length) { enfocarPrimerError(this); return; }

   
    const datos = new FormData();
    datos.append("tipoId", tipo);
    datos.append("barrioId", barrio);
    datos.append("descripcion", desc);
    datos.append("referencia", ref);
    datos.append("latitud", hayPunto ? lat : CENTRO_MAPA[0]);
    datos.append("longitud", hayPunto ? lng : CENTRO_MAPA[1]);
    datos.append("foto", foto);

    $("#btnPublicar").prop("disabled", true).text(t("pub.publicando"));
    $.ajax({
      url: API + RUTAS.reportes, method: "POST", headers: Sesion.cabecera(),
      data: datos, processData: false, contentType: false
    })
    .done(function (r) {
      const nuevo = r && (r.id || (r.reporte && r.reporte.id));
      window.location.href = nuevo ? "detalle.html?id=" + encodeURIComponent(nuevo) : "mis-casos.html";
    })
    .fail(function (x) {
      mostrarAlerta("#alerta", mensajeError(x, t("err.publicar")), "danger");
      $("#btnPublicar").prop("disabled", false).text(t("pub.publicar"));
      document.getElementById("alerta").scrollIntoView({ behavior: "smooth", block: "center" });
    });
  });

  
  textoPunto();
  $(document).on("i18n:changed", function () { textoPunto(); mensajeUbicacion(ubiClave); });
});
