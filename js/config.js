
const API = "https://localhost:8443";

// Rutas del back-end
const RUTAS = {
  login: "/api/auth/login",
  registro: "/api/auth/registro",
  tipos: "/api/tipos",
  barrios: "/api/barrios",
  reportes: "/api/reportes",
  misReportes: "/api/reportes/mis",
  panel: "/api/panel/reportes",
  estadisticas: "/api/estadisticas"
};


const FILTROS = { tipo: "tipoId", barrio: "barrioId", estado: "estado" };


const CENTRO_MAPA = [2.93, -75.28];
const ZOOM_MAPA = 13;


const INICIO_POR_ROL = {
  CIUDADANO: "index.html",
  FUNCIONARIO: "panel.html",
  ADMIN: "panel.html"
};


const RUTAS_CASO = {
  detalle:   id => "/api/reportes/" + id,
  historial: id => "/api/reportes/" + id + "/historial",
  votar:     id => "/api/reportes/" + id + "/votos",
  estado:    id => "/api/reportes/" + id + "/estado"
};
