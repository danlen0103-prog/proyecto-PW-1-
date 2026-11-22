alIdiomaListo(function () {
 
  const rol = Sesion.rol();
  if (Sesion.activa()) $("#btnCuenta").remove();
  function botonesInicio() {
    if (rol === "CIUDADANO") {
      $("#btnPublicar").attr("href", "publicar.html").text(t("home.publicar"));
    } else if (rol === "FUNCIONARIO" || rol === "ADMIN") {
      $("#btnPublicar").attr("href", "panel.html").text(t("home.irPanel"));
    } else {
      $("#btnPublicar").attr("href", "login.html").text(t("home.ingresarPublicar"));
    }
    $("#btnCuenta").text(t("home.crearCuenta"));
  }
  botonesInicio();

  
  const mapa = L.map("mapa").setView(CENTRO_MAPA, ZOOM_MAPA);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19, attribution: "© OpenStreetMap contributors"
  }).addTo(mapa);
  const capa = L.layerGroup().addTo(mapa);   
  const marcadores = {};                     
  let peticion = null;
  let ultimaLista = null;                     
  $(window).on("load", function () { mapa.invalidateSize(); });

 
  function llenarSelect(selector, ruta) {
    $.getJSON(API + ruta).done(function (lista) {
      lista.forEach(function (x) { $(selector).append($("<option>").val(x.id).text(x.nombre)); });
    });
  }
  llenarSelect("#fTipo", RUTAS.tipos);
  llenarSelect("#fBarrio", RUTAS.barrios);

  $("#fTipo, #fBarrio, #fEstado").on("change", cargar);
  $("#btnLimpiar").on("click", function () {
    $("#fTipo, #fBarrio, #fEstado").val("");
    cargar();
  });


  function mostrar(estado, texto) {
    $("#msgCargando, #msgVacio, #msgError").addClass("d-none");
    if (estado === "cargando") $("#msgCargando").removeClass("d-none");
    if (estado === "vacio") $("#msgVacio").removeClass("d-none");
    if (estado === "error") $("#msgError").removeClass("d-none").find(".detalle").text(texto);
  }


  function cargar() {
    const filtros = {};
    if ($("#fTipo").val()) filtros[FILTROS.tipo] = $("#fTipo").val();
    if ($("#fBarrio").val()) filtros[FILTROS.barrio] = $("#fBarrio").val();
    if ($("#fEstado").val()) filtros[FILTROS.estado] = $("#fEstado").val();

    if (peticion) peticion.abort();          
    mostrar("cargando");
    peticion = $.getJSON(API + RUTAS.reportes, filtros)
      .done(function (datos) {
        const lista = (Array.isArray(datos) ? datos : (datos.content || [])).map(normalizar);
        dibujar(lista);
      })
      .fail(function (x, estado) {
        if (estado === "abort") return;
        capa.clearLayers();
        $("#tarjetas").empty();
        $("#contador").text("");
        mostrar("error", mensajeError(x, t("err.cargarCasos")));
      });
  }

 
  function popup(r) {
    return `<div class="popup-caso"><strong>${escapar(r.tipo)}</strong><br>
            ${etiquetaEstado(r.estado)}<br>
            <a href="detalle.html?id=${encodeURIComponent(r.id)}">${t("caso.ver")} →</a></div>`;
  }

  function tarjeta(r) {
    const detalle = [escapar(r.barrio), hace(r.fecha)].filter(Boolean).join(" · ");
    return `
      <div class="col-12 col-md-6 col-lg-4">
        <article class="card h-100 tarjeta-caso">
          <img src="${escapar(urlFoto(r.foto))}" class="card-img-top" loading="lazy"
               alt="${escapar(t("caso.fotoAlt", { tipo: r.tipo }))}">
          <div class="card-body d-flex flex-column">
            <div class="d-flex justify-content-between align-items-start gap-2 mb-1">
              <h3 class="h5 card-title mb-0">${escapar(r.tipo)}</h3>
              ${etiquetaEstado(r.estado)}
            </div>
            <p class="card-text text-body-secondary small mb-2">${detalle}</p>
            <p class="card-text mb-3"><strong>${Number(r.votos) || 0}</strong> ${t(Number(r.votos) === 1 ? "caso.apoyo" : "caso.apoyos")}</p>
            <div class="mt-auto d-flex flex-wrap gap-2">
              <a class="btn btn-primary btn-sm" href="detalle.html?id=${encodeURIComponent(r.id)}">${t("caso.ver")}</a>
              <button type="button" class="btn btn-outline-secondary btn-sm ver-mapa" data-id="${escapar(r.id)}">${t("caso.verMapa")}</button>
            </div>
          </div>
        </article>
      </div>`;
  }

  function dibujar(lista, ajustar) {
    ultimaLista = lista;
    capa.clearLayers();
    Object.keys(marcadores).forEach(function (k) { delete marcadores[k]; });
    $("#tarjetas").empty();
    const puntos = [];

    lista.forEach(function (r) {
      if (Number.isFinite(r.lat) && Number.isFinite(r.lng)) {
        marcadores[r.id] = L.marker([r.lat, r.lng], { title: r.tipo, alt: r.tipo })
          .bindPopup(popup(r)).addTo(capa);
        puntos.push([r.lat, r.lng]);
      }
      $("#tarjetas").append(tarjeta(r));
    });

    
    $("#tarjetas img").one("error", function () { this.src = fotoVacia(); this.alt = t("foto.sinDisponible"); });

    if (puntos.length && ajustar !== false) mapa.fitBounds(puntos, { padding: [40, 40], maxZoom: 16 });
    $("#contador").text(textoCasos(lista.length));
    mostrar(lista.length ? "lista" : "vacio");
  }


  $("#tarjetas").on("click", ".ver-mapa", function () {
    const m = marcadores[$(this).data("id")];
    if (!m) { return; }
    document.getElementById("mapa").scrollIntoView({ behavior: "smooth", block: "center" });
    mapa.setView(m.getLatLng(), Math.max(mapa.getZoom(), 16));
    m.openPopup();
  });

 
  $(document).on("i18n:changed", function () {
    botonesInicio();
    if (ultimaLista) dibujar(ultimaLista, false);
  });

  cargar();
});
