
const Sesion = {
  guardar(r) {
    const rol = String(r.rol || r.role || "").replace(/^ROLE_/, "");
    localStorage.setItem("token", r.token || r.accessToken || "");
    localStorage.setItem("rol", rol);
    localStorage.setItem("nombre", r.nombre || r.name || "");
  },
  token()  { return localStorage.getItem("token"); },
  rol()    { return localStorage.getItem("rol"); },
  nombre() { return localStorage.getItem("nombre"); },
  activa() { return !!Sesion.token(); },
  cabecera() { return Sesion.token() ? { Authorization: "Bearer " + Sesion.token() } : {}; },
  cerrar(destino) {
    ["token", "rol", "nombre"].forEach(k => localStorage.removeItem(k));
    window.location.href = destino || "login.html";
  }
};

function inicioPara(rol) { return INICIO_POR_ROL[rol] || "index.html"; }


function exigirRol(roles) {
  if (!Sesion.activa()) {
    const pagina = location.pathname.split("/").pop() + location.search;
    window.location.replace("login.html?next=" + encodeURIComponent(pagina));
  } else if (!roles.includes(Sesion.rol())) {
    window.location.replace("index.html");
  }
  return Sesion.activa() && roles.includes(Sesion.rol());
}


function escapar(t) {
  return String(t == null ? "" : t).replace(/[&<>"']/g, c =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}


const COLOR_ESTADO = { RECIBIDO: "secondary", EN_REVISION: "info", EN_PROCESO: "warning", RESUELTO: "success", RECHAZADO: "dark" };
// Siempre texto + color (el color solo no basta para accesibilidad)
function etiquetaEstado(e) {
  return `<span class="badge text-bg-${COLOR_ESTADO[e] || "secondary"}">${escapar(nombreEstado(e))}</span>`;
}


function fotoVacia() { return "data:image/svg+xml;charset=utf-8," + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="640" height="400" viewBox="0 0 640 400">' +
  '<rect width="640" height="400" fill="#E6EBF1"/>' +
  '<text x="320" y="210" font-family="Arial" font-size="28" text-anchor="middle" fill="#5B6B7C">' + escapar(t("foto.sin")) + '</text></svg>'); }


function urlFoto(f) {
  if (!f) return fotoVacia();
  if (/^https?:/i.test(f)) return f;
  if (f.charAt(0) === "/") return API + f;
  return API + "/uploads/" + f;
}


function hace(iso) {
  const f = new Date(iso);
  if (isNaN(f)) return "";
  const dias = Math.floor((Date.now() - f.getTime()) / 86400000);
  if (dias <= 0) return t("fecha.hoy");
  if (dias === 1) return t("fecha.ayer");
  if (dias < 30) return t("fecha.hace", { n: dias });
  return f.toLocaleDateString(locale(), { day: "numeric", month: "short", year: "numeric" });
}


function nombreDe(v) { return v && typeof v === "object" ? (v.nombre || v.name || "") : (v || ""); }


function normalizar(r) {
  const x = r.reporte || r;
  return {
    id: x.id,
    tipo: nombreDe(x.tipo) || x.tipoNombre || t("caso.generico"),
    barrio: nombreDe(x.barrio) || x.barrioNombre || "",
    estado: x.estado,
    descripcion: x.descripcion || "",
    referencia: x.referencia || "",
    foto: x.foto || x.fotoUrl || "",
    lat: Number(x.latitud != null ? x.latitud : x.lat),
    lng: Number(x.longitud != null ? x.longitud : (x.lng != null ? x.lng : x.lon)),
    votos: x.votos != null ? x.votos : (x.apoyos != null ? x.apoyos : 0),
    fecha: x.fecha,
    puntaje: x.puntaje,
    comentario: x.comentarioEstado || "",
    fotoSolucion: x.fotoSolucion || ""
  };
}


function traducirServidor(msg) {
  const m = /^No se puede pasar de (\w+) a (\w+)$/.exec(msg);
  if (m) return t("srv.pasar", { a: nombreEstado(m[1]), b: nombreEstado(m[2]) });
  return t(msg);
}


function mensajeError(x, porDefecto) {
  if (x.status === 0) return t("err.sinConexion");
  const j = x.responseJSON;
  if (j) {
    if (typeof j === "string") return traducirServidor(j);
    
    if (j.campos && typeof j.campos === "object") {
      const lista = Object.values(j.campos).filter(m => typeof m === "string").map(traducirServidor);
      if (lista.length) return lista.join(". ");
    }
    
    if (typeof j.error === "string" && j.error && !j.timestamp) return traducirServidor(j.error);
    const m = j.mensaje || j.message;
    if (typeof m === "string" && m) return traducirServidor(m);
  }
  if ([400, 401, 403, 404, 409, 413, 500].includes(x.status)) return t("err." + x.status);
  return porDefecto || t("err.inesperado");
}


$(document).ajaxError(function (e, x, ajaxSettings) {
  const esLogin = String(ajaxSettings.url).indexOf(RUTAS.login) !== -1;
  if (x.status === 401 && Sesion.activa() && !esLogin) {
    sessionStorage.setItem("aviso", "msg.sesionExpirada");   
    Sesion.cerrar();
  }
});


function esCorreo(c) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(c); }


function marcar(selector, mensaje) {
  const $c = $(selector);
  const $f = $("#" + $c.attr("id") + "Error");
  const ids = [$c.data("ayuda"), $f.attr("id")].filter(Boolean).join(" ");
  $c.addClass("is-invalid").attr({ "aria-invalid": "true", "aria-describedby": ids });
  $f.text(mensaje);
}
function limpiarErrores(form) {
  $(form).find(".is-invalid").each(function () {
    const ayuda = $(this).data("ayuda");
    $(this).removeClass("is-invalid").removeAttr("aria-invalid");
    if (ayuda) $(this).attr("aria-describedby", ayuda); else $(this).removeAttr("aria-describedby");
  });
  $(form).find(".invalid-feedback").text("");
}
function enfocarPrimerError(form) { $(form).find(".is-invalid").first().trigger("focus"); }

function mostrarAlerta(selector, texto, tipo) {
  $(selector).empty().append(
    $("<div>").addClass("alert alert-" + (tipo || "danger")).attr("role", "alert").text(texto));
}


$(document).on("click", ".btn-mostrar", function () {
  const $campo = $($(this).data("target"));
  const visible = $campo.attr("type") === "text";
  $campo.attr("type", visible ? "password" : "text");
  $(this).text(t(visible ? "comun.mostrar" : "comun.ocultar")).attr("aria-pressed", String(!visible));
});


function iniciarSesion(correo, clave) {
  return $.ajax({
    url: API + RUTAS.login, method: "POST", contentType: "application/json",
    data: JSON.stringify({ correo: correo, clave: clave })
  });
}


function destinoTras(respuesta) {
  const next = new URLSearchParams(location.search).get("next");
  if (next && /^[a-z0-9-]+\.html(\?[\w=&.-]*)?$/i.test(next)) return next;
  return inicioPara(String(respuesta.rol || respuesta.role || "").replace(/^ROLE_/, ""));
}


function leerId() { return new URLSearchParams(location.search).get("id"); }

function fechaHora(iso) {
  const f = new Date(iso);
  return isNaN(f) ? "" : f.toLocaleString(locale(), { dateStyle: "medium", timeStyle: "short" });
}

function num(v) {
  const n = Number(String(v == null ? "" : v).replace(",", "."));
  return String(v == null ? "" : v).trim() === "" ? NaN : n;
}


function normalizarHistorial(h) {
  return {
    anterior: h.estadoAnterior != null ? h.estadoAnterior : (h.estado_anterior != null ? h.estado_anterior : null),
    nuevo: h.estadoNuevo || h.estado_nuevo || h.estado || "",
    fecha: h.fecha,
    comentario: h.comentario || "",
    evidencia: h.evidencia || "",
    funcionario: nombreDe(h.funcionario) || h.nombreFuncionario || ""
  };
}

function nombreEstado(e) { return COLOR_ESTADO[e] ? t("estado." + e) : (e || t("estado.sinEstado")); }
const CERRADOS = ["RESUELTO", "RECHAZADO"];


function llenarSelect(selector, ruta) {
  return $.getJSON(API + ruta).done(function (lista) {
    lista.forEach(function (x) { $(selector).append($("<option>").val(x.id).text(x.nombre)); });
  });
}


function textoCasos(n) { return n + " " + t(n === 1 ? "caso.uno" : "caso.varios"); }


$(document).on("i18n:changed", function () {
  $(".btn-mostrar").each(function () {
    const visible = $($(this).data("target")).attr("type") === "text";
    $(this).text(t(visible ? "comun.ocultar" : "comun.mostrar"));
  });
});


async function validarImagen(f) {
  if (f.size > 5 * 1024 * 1024) return t("pub.errFotoTamano");
  const b = new Uint8Array(await f.slice(0, 8).arrayBuffer());
  const jpg = b[0] === 0xFF && b[1] === 0xD8 && b[2] === 0xFF;
  const png = b[0] === 0x89 && b[1] === 0x50 && b[2] === 0x4E && b[3] === 0x47 &&
              b[4] === 0x0D && b[5] === 0x0A && b[6] === 0x1A && b[7] === 0x0A;
  return (jpg || png) ? null : t("pub.errFotoTipo");
}
