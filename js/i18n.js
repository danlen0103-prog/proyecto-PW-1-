
const IDIOMAS = { es: "es-CO", en: "en-US", pt: "pt-BR" };
const I18N = { lang: "es", dict: {} };


function t(key, vars) {
  let s = I18N.dict[key] || key;
  if (vars) Object.keys(vars).forEach(function (k) { s = s.split("{" + k + "}").join(vars[k]); });
  return s;
}


function locale() { return IDIOMAS[I18N.lang] || "es-CO"; }


const I18N_LISTO = $.Deferred();
function alIdiomaListo(fn) { I18N_LISTO.done(fn); }

function aplicarTraducciones() {
  $("[data-i18n]").each(function () { $(this).text(t($(this).data("i18n"))); });
  $("[data-i18n-placeholder]").each(function () { $(this).attr("placeholder", t($(this).data("i18n-placeholder"))); });
  $("[data-i18n-aria]").each(function () { $(this).attr("aria-label", t($(this).data("i18n-aria"))); });
  $("html").attr("lang", I18N.lang);
}

function cargarIdioma(lang) {
  $.getJSON("i18n/" + lang + ".json")
    .done(function (data) {
      I18N.lang = lang;
      I18N.dict = data;
      localStorage.setItem("lang", lang);
      aplicarTraducciones();
      $(document).trigger("i18n:changed");
      I18N_LISTO.resolve();
    })
    .fail(function () {
      if (lang !== "es") cargarIdioma("es");
      else { $(document).trigger("i18n:changed"); I18N_LISTO.resolve(); }
    });
}


$(document).on("change", "#selectorIdioma", function () { cargarIdioma($(this).val()); });

$(function () {
  const guardado = localStorage.getItem("lang");
  const delNavegador = (navigator.language || "es").slice(0, 2).toLowerCase();
  const inicial = IDIOMAS[guardado] ? guardado : (IDIOMAS[delNavegador] ? delNavegador : "es");
  cargarIdioma(inicial);
});
