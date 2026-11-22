alIdiomaListo(function () {
  if (Sesion.activa()) { window.location.replace(inicioPara(Sesion.rol())); return; }

  $("#formRegistro").on("submit", function (e) {
    e.preventDefault();
    limpiarErrores(this);
    $("#alerta").empty();

    const nombre = $("#nombre").val().trim();
    const correo = $("#correo").val().trim();
    const clave = $("#clave").val();
    const confirmar = $("#confirmar").val();

    if (nombre.length < 3 || nombre.length > 100) marcar("#nombre", t("reg.errNombre"));
    if (!esCorreo(correo)) marcar("#correo", t("val.correo"));
    if (clave.length < 8 || clave.length > 60) marcar("#clave", t("reg.errClave"));
    if (confirmar !== clave) marcar("#confirmar", t("reg.errConfirmar"));
    if ($(this).find(".is-invalid").length) { enfocarPrimerError(this); return; }

    $("#btnRegistrar").prop("disabled", true).text(t("reg.creando"));

    $.ajax({
      url: API + RUTAS.registro, method: "POST", contentType: "application/json",
      data: JSON.stringify({ nombre: nombre, correo: correo, clave: clave })
    })
    .done(function () {
      // Cuenta creada: se inicia sesión automáticamente
      iniciarSesion(correo, clave)
        .done(function (r) { Sesion.guardar(r); window.location.href = inicioPara("CIUDADANO"); })
        .fail(function () { window.location.href = "login.html"; });
    })
    .fail(function (x) {
      if (x.status === 409) {
        marcar("#correo", t("reg.errCorreoExiste"));
        enfocarPrimerError("#formRegistro");
      } else {
        mostrarAlerta("#alerta", mensajeError(x, t("err.crearCuenta")), "danger");
      }
      $("#btnRegistrar").prop("disabled", false).text(t("reg.crear"));
    });
  });
});
