alIdiomaListo(function () {

  if (Sesion.activa()) { window.location.replace(inicioPara(Sesion.rol())); return; }

  
  const aviso = sessionStorage.getItem("aviso");
  if (aviso) { sessionStorage.removeItem("aviso"); mostrarAlerta("#alerta", t(aviso), "warning"); }

  $("#formLogin").on("submit", function (e) {
    e.preventDefault();
    limpiarErrores(this);
    $("#alerta").empty();

    const correo = $("#correo").val().trim();
    const clave = $("#clave").val();
    if (!esCorreo(correo)) marcar("#correo", t("val.correo"));
    if (!clave) marcar("#clave", t("login.escribeClave"));
    if ($(this).find(".is-invalid").length) { enfocarPrimerError(this); return; }

    $("#btnEntrar").prop("disabled", true).text(t("login.ingresando"));
    iniciarSesion(correo, clave)
      .done(function (r) {
        Sesion.guardar(r);
        window.location.href = destinoTras(r);
      })
      .fail(function (x) {
        mostrarAlerta("#alerta", x.status === 401 ? t("login.incorrecto") : mensajeError(x), "danger");
        $("#btnEntrar").prop("disabled", false).text(t("login.entrar"));
      });
  });
});
