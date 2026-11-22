
function pintarEncabezado() {
  const rol = Sesion.rol();
  const actual = location.pathname.split("/").pop() || "index.html";
  const items = [["index.html", t("nav.inicio")]];
  if (rol === "CIUDADANO") items.push(["publicar.html", t("nav.publicar")], ["mis-casos.html", t("nav.miscasos")]);
  if (rol === "FUNCIONARIO" || rol === "ADMIN") items.push(["panel.html", t("nav.panel")], ["estadisticas.html", t("nav.estadisticas")]);

  const enlaces = items.map(([href, texto]) =>
    `<li class="nav-item"><a class="nav-link${href === actual ? " active" : ""}" href="${href}"` +
    `${href === actual ? ' aria-current="page"' : ""}>${texto}</a></li>`).join("");

  const selector = `
    <div class="me-3">
      <label for="selectorIdioma" class="visually-hidden">${t("lang.label")}</label>
      <select id="selectorIdioma" class="form-select form-select-sm" style="width:auto">
        <option value="es" lang="es"${I18N.lang === "es" ? " selected" : ""}>Español</option>
        <option value="en" lang="en"${I18N.lang === "en" ? " selected" : ""}>English</option>
        <option value="pt" lang="pt"${I18N.lang === "pt" ? " selected" : ""}>Português</option>
      </select>
    </div>`;

  const acciones = Sesion.activa()
    ? `<span class="navbar-text me-3">${t("nav.hola")}, ${escapar(Sesion.nombre() || "")}</span>
       <button id="btnSalir" type="button" class="btn btn-outline-light btn-sm">${t("nav.salir")}</button>`
    : `<a class="btn btn-outline-light btn-sm me-2" href="login.html">${t("nav.ingresar")}</a>
       <a class="btn btn-acento btn-sm" href="registro.html">${t("nav.registrarme")}</a>`;

  $("#encabezado").html(`
    <a class="visually-hidden-focusable saltar" href="#contenido">${t("nav.saltar")}</a>
    <nav class="navbar navbar-expand-lg navbar-dark barra-principal" aria-label="${t("nav.principal")}">
      <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="index.html">
          <svg width="22" height="22" viewBox="0 0 24 24" aria-hidden="true" fill="currentColor">
            <path d="M12 2a7 7 0 0 0-7 7c0 5.2 7 13 7 13s7-7.8 7-13a7 7 0 0 0-7-7zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5z"/>
          </svg>
          Reporta tu barrio
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuPrincipal"
                aria-controls="menuPrincipal" aria-expanded="false" aria-label="${t("nav.menu")}">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="menuPrincipal">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">${enlaces}</ul>
          <div class="d-flex align-items-center">${selector}${acciones}</div>
        </div>
      </div>
    </nav>`);

  $("#btnSalir").on("click", function () { Sesion.cerrar(); });
}

function pintarPie() {
  $("#pie").html(`
    <div class="container py-4 text-center">
      ${t("pie.texto")}
    </div>`);
}


$(document).on("i18n:changed", function () { pintarEncabezado(); pintarPie(); });
