package es.taskly.agenda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.taskly.agenda.generated.resources.Res
import es.taskly.agenda.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

//Taskly Agenda de empleados por Nerea Monferrer Navarro 2DAM
expect fun reproducirAviso()

//Modelo y estado
enum class Ruta { LOGIN, HOME, AGENDA, FORM, CFG, PERFIL, PASS, ALERTAS, SALAS, NSALA, INVITAR, INVIT, CHAT }
data class Cita(val id: Int, val titulo: String, val dia: Int, val mes: Int, val inicio: String, val fin: String, val lugar: String, val notas: String, val alerta: Int)
data class Mensaje(val autor: String, val texto: String, val hora: String)
data class Sala(val id: Int, val nombre: String, val desc: String, val miembros: Int, val msgs: List<Mensaje>, val pendiente: Boolean = false)

val MESES = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
val DIAS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
fun diasMes(m: Int) = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)[m - 1]
fun primerDia(m: Int): Int { // 0 = lunes, año 2025
    val t = listOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4); var y = 2025; if (m < 3) y -= 1
    return ((y + y / 4 - y / 100 + y / 400 + t[m - 1] + 1) % 7 + 6) % 7
}
fun claveValida(s: String) = s.length >= 8 && s.any { it.isDigit() } && s.any { !it.isLetterOrDigit() && !it.isWhitespace() }

class AppState {
    var ruta by mutableStateOf(Ruta.LOGIN)
    var oscuro by mutableStateOf(true)
    var acento by mutableStateOf(Color(0xFF8C00FF))
    var usuario by mutableStateOf("")
    var clave by mutableStateOf("")
    var vista by mutableStateOf(0)
    var mes by mutableStateOf(1)
    var dia by mutableStateOf(15)
    var citaSel by mutableStateOf<Int?>(null)
    var salaSel by mutableStateOf(1)
    var volverA by mutableStateOf(Ruta.SALAS)
    var recordatorios by mutableStateOf(true)
    var modal by mutableStateOf(true)
    var sonido by mutableStateOf(true)
    var minutos by mutableStateOf(5)
    var tono by mutableStateOf("Campana suave")
    var aviso by mutableStateOf<Cita?>(null)
    val citas = mutableStateListOf(
        Cita(1, "Reunión de equipo", 15, 1, "10:00", "10:45", "Sala de reuniones", "Revisar avances del equipo.", 5),
        Cita(2, "Revisión", 17, 1, "11:00", "11:45", "", "", 5)
    )
    val salas = mutableStateListOf(
        Sala(1, "Proyecto Taskly", "", 3, listOf(Mensaje("Lucía", "Hola", "09:30"), Mensaje("Lucía", "Nos vemos a las 10:00.", "09:40"))),
        Sala(2, "Compañeros", "", 5, listOf(Mensaje("Marcos", "Ya compartí los avances.", "09:30"))),
        Sala(3, "Equipo de diseño", "Ideas, avances y coordinación del equipo.", 3, listOf(
            Mensaje("Lucía García", "¿Revisamos los avances a las 10:00?", "09:40"),
            Mensaje("Tú", "Sí, ya tengo todo preparado.", "09:41"),
            Mensaje("Marcos Ruiz", "Nos vemos en la sala de reuniones.", "09:42")), pendiente = true)
    )
    fun sala() = salas.first { it.id == salaSel }
    fun ir(r: Ruta) { ruta = r }
}
val st = AppState()

object C {
    val tx get() = if (st.oscuro) Color.White else Color(0xFF0A0F3C)
    val ink = Color(0xFF0A0F3C)
    val card get() = if (st.oscuro) Color(0xFF2B2D66) else Color(0xFFE6E8FB)
    val lt get() = if (st.oscuro) Color(0xFFA8DFFF) else Color.White
    val inp get() = if (st.oscuro) Color(0xFFD9D9D9) else Color.White
    val titulo get() = if (st.oscuro) Color(0xFFF77BE0) else st.acento
    val error = Color(0xFFFF6B8A)
}
val R12 = RoundedCornerShape(12.dp)
val R8 = RoundedCornerShape(8.dp)

//Componentes
@Composable fun T(s: String, c: Color = C.tx, sz: Int = 16, b: Boolean = false, m: Modifier = Modifier) =
    Text(s, m, color = c, fontSize = sz.sp, fontWeight = if (b) FontWeight.Bold else null)

@Composable fun Btn(t: String, sec: Boolean = false, onClick: () -> Unit) =
    Button(onClick, Modifier.fillMaxWidth().height(48.dp), shape = R12,
        colors = ButtonDefaults.buttonColors(containerColor = if (sec) C.card else st.acento, contentColor = if (sec) C.tx else Color.White)) { T(t, if (sec) C.tx else Color.White, b = true) }

@Composable fun Tarjeta(claro: Boolean = true, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    var m = Modifier.fillMaxWidth().clip(R12).background(if (claro) C.lt else C.card)
    if (onClick != null) m = m.clickable { onClick() }
    Column(m.padding(14.dp), content = content)
}

@Composable fun Campo(label: String, v: String, pass: Boolean = false, ph: String = "", lineas: Int = 1, onV: (String) -> Unit) {
    T(label, sz = 14, b = true)
    TextField(v, onV, Modifier.fillMaxWidth(), placeholder = { Text(ph) }, singleLine = lineas == 1, minLines = lineas, shape = R12,
        visualTransformation = if (pass) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(focusedContainerColor = C.inp, unfocusedContainerColor = C.inp, focusedTextColor = C.ink,
            unfocusedTextColor = C.ink, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
}

@Composable fun Desplegable(label: String, v: String, op: List<String>, mod: Modifier = Modifier.fillMaxWidth(), onSel: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Column(mod) {
        if (label.isNotEmpty()) T(label, sz = 14, b = true)
        Box {
            Row(Modifier.fillMaxWidth().clip(R12).background(C.inp).clickable { abierto = true }.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                T(v, C.ink); T("⌄", C.ink)
            }
            DropdownMenu(abierto, { abierto = false }) {
                op.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onSel(o); abierto = false }) }
            }
        }
    }
}

@Composable fun Interruptor(t: String, v: Boolean, onC: (Boolean) -> Unit) =
    Row(Modifier.fillMaxWidth().clip(R12).background(C.card).padding(horizontal = 14.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        T(t, b = true); Switch(v, onC, colors = SwitchDefaults.colors(checkedTrackColor = st.acento, checkedThumbColor = Color.White))
    }

@Composable fun Reglas(error: Boolean = false) =
    Box(Modifier.fillMaxWidth().clip(R12).background(C.card).then(if (error) Modifier.border(2.dp, C.error, R12) else Modifier).padding(12.dp)) {
        T("8 caracteres · 1 número\n1 símbolo", sz = 14)
    }

@Composable fun Seg(op: List<String>, sel: Int, onSel: (Int) -> Unit) =
    Row(Modifier.fillMaxWidth().clip(R12).background(C.card).padding(4.dp)) {
        op.forEachIndexed { i, t ->
            Box(Modifier.weight(1f).clip(R12).background(if (i == sel) st.acento else Color.Transparent).clickable { onSel(i) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                T(t, if (i == sel) Color.White else C.tx, b = true)
            }
        }
    }

@Composable fun Marco(titulo: String, pie: @Composable ColumnScope.() -> Unit = {}, cuerpo: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(Res.drawable.logo), null, Modifier.size(48.dp).clip(R12))
            T("  Taskly", C.titulo, 20, true)
        }
        T(titulo, sz = 28, b = true, m = Modifier.padding(vertical = 6.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp), content = cuerpo)
        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = pie)
    }

//Raíz
@Composable fun App() {
    val fondo = if (st.oscuro) Brush.verticalGradient(listOf(Color(0xFF0A0F3C), Color(0xFF2E4A6A))) else SolidColor(Color(0xFFF2F4FF))
    Box(Modifier.fillMaxSize().background(fondo)) {
        Box(Modifier.align(Alignment.TopCenter).widthIn(max = 402.dp).fillMaxHeight().safeDrawingPadding().padding(18.dp)) {
            when (st.ruta) {
                Ruta.LOGIN -> Login(); Ruta.HOME -> Home(); Ruta.AGENDA -> Agenda(); Ruta.FORM -> Formulario()
                Ruta.CFG -> Config(); Ruta.PERFIL -> Perfil(); Ruta.PASS -> Clave(); Ruta.ALERTAS -> Alertas()
                Ruta.SALAS -> Salas(); Ruta.NSALA -> NuevaSala(); Ruta.INVITAR -> Invitar(); Ruta.INVIT -> Invitacion(); Ruta.CHAT -> Chat()
            }
        }
        st.aviso?.let { Recordatorio(it) }
    }
}

//Pantallas
@Composable fun Login() {
    var u by remember { mutableStateOf(st.usuario) }; var p by remember { mutableStateOf("") }; var err by remember { mutableStateOf(false) }
    Marco("Iniciar sesión", pie = {
        Btn("Entrar") {
            if (u.isBlank() || !claveValida(p) || (st.clave.isNotEmpty() && st.clave != p)) err = true
            else { st.usuario = u; st.clave = p; st.ir(Ruta.HOME) }
        }
    }) {
        Campo("Usuario", u, ph = "Usuario") { u = it }
        Campo("Contraseña", p, pass = true, ph = "••••••••") { p = it; err = false }
        Reglas(err)
    }
}

@Composable fun Home() {
    val prox = st.citas.filter { it.mes == 1 && it.dia >= 15 }
    Marco("Bienvenido, ${st.usuario.ifBlank { "Usuario" }}", pie = {
        prox.firstOrNull()?.let { Tarjeta { T("Próximas citas: ${prox.size}", C.ink, 18, true); T(it.titulo, C.ink, b = true); T("${it.dia} de enero · ${it.inicio} – ${it.fin}", C.ink, 14) } }
    }) {
        Image(painterResource(Res.drawable.logo), null, Modifier.size(96.dp).clip(R12).align(Alignment.CenterHorizontally))
        Btn("Mi agenda") { st.ir(Ruta.AGENDA) }; Btn("Chat de equipo") { st.ir(Ruta.SALAS) }; Btn("Configuración") { st.ir(Ruta.CFG) }
    }
}

@Composable fun Tira() {
    val ini = st.dia - (primerDia(st.mes) + st.dia - 1) % 7; val n = diasMes(st.mes)
    Row(Modifier.fillMaxWidth().clip(R12).background(C.lt).padding(6.dp)) {
        for (i in 0..6) { val d = ini + i
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                T("LMXJVSD"[i].toString(), C.ink, b = true)
                Box(Modifier.clip(R8).background(if (d == st.dia) st.acento else Color.Transparent).clickable { if (d in 1..n) st.dia = d }.padding(horizontal = 6.dp, vertical = 4.dp)) {
                    T(if (d in 1..n) "$d" else "", if (d == st.dia) Color.White else C.ink)
                }
            }
        }
    }
}

@Composable fun Agenda() {
    val first = primerDia(st.mes); val n = diasMes(st.mes); val ini = st.dia - (first + st.dia - 1) % 7
    val nomDia = DIAS[(first + st.dia - 1) % 7]; val mesL = MESES[st.mes - 1].lowercase()
    fun evs(d: Int) = st.citas.filter { it.dia == d && it.mes == st.mes }.sortedBy { it.inicio }
    Marco("Mi agenda", pie = { Btn("Nueva cita") { st.citaSel = null; st.ir(Ruta.FORM) }; Btn("Inicio", true) { st.ir(Ruta.HOME) } }) {
        Seg(listOf("Mes", "Semana", "Día"), st.vista) { st.vista = it }
        when (st.vista) {
            0 -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    T("‹", sz = 22, m = Modifier.clickable { if (st.mes > 1) st.mes -= 1 }.padding(horizontal = 8.dp))
                    T(MESES[st.mes - 1], b = true, sz = 18)
                    T("›", sz = 22, m = Modifier.clickable { if (st.mes < 12) st.mes += 1 }.padding(horizontal = 8.dp))
                }
                Column(Modifier.fillMaxWidth().clip(R12).background(C.lt)) {
                    Row(Modifier.fillMaxWidth().background(C.card).padding(vertical = 8.dp)) { "LMXJVSD".forEach { Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { T(it.toString(), b = true) } } }
                    for (w in 0 until (first + n + 6) / 7) Row(Modifier.fillMaxWidth()) {
                        for (c in 0..6) { val d = w * 7 + c - first + 1
                            Box(Modifier.weight(1f).height(46.dp), contentAlignment = Alignment.Center) {
                                if (d in 1..n) { val hoy = st.mes == 1 && d == 15
                                    Box(Modifier.size(32.dp).clip(R8).background(if (hoy) st.acento else Color.Transparent).clickable { st.dia = d; st.vista = 2 }, contentAlignment = Alignment.Center) {
                                        T("$d", if (hoy) Color.White else C.ink, b = evs(d).isNotEmpty())
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                T("$ini – ${ini + 6} de $mesL", b = true, sz = 18); Tira()
                for (d in ini..ini + 6) if (d in 1..n && evs(d).isNotEmpty()) {
                    T("${DIAS[(first + d - 1) % 7]} $d", b = true, sz = 18)
                    evs(d).forEach { e -> Tarjeta(onClick = { st.citaSel = e.id; st.ir(Ruta.FORM) }) { T(e.titulo, C.ink, 20, true); T("${e.inicio} – ${e.fin}", C.ink); if (e.lugar.isNotEmpty()) T(e.lugar, C.ink, 14) } }
                }
            }
            else -> {
                T("$nomDia, ${st.dia} de $mesL", b = true, sz = 18); Tira()
                listOf("09:00", "10:00", "11:00").forEach { h ->
                    val e = evs(st.dia).firstOrNull { it.inicio.take(2) == h.take(2) }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        T(h, b = true, m = Modifier.width(48.dp).padding(top = 12.dp))
                        if (e == null) Tarjeta(false) { T("Libre") }
                        else Tarjeta { T(e.titulo, C.ink, 18, true); T("${e.inicio} – ${e.fin}", C.ink, 14); T(e.lugar, C.ink, 14)
                            Spacer(Modifier.height(6.dp)); Btn("Editar cita", true) { st.citaSel = e.id; st.ir(Ruta.FORM) } }
                    }
                }
            }
        }
    }
}

@Composable fun Formulario() {
    val c = st.citas.firstOrNull { it.id == st.citaSel }
    var t by remember { mutableStateOf(c?.titulo ?: "") }; var d by remember { mutableStateOf(c?.dia ?: st.dia) }
    var i by remember { mutableStateOf(c?.inicio ?: "10:00") }; var f by remember { mutableStateOf(c?.fin ?: "10:45") }
    var l by remember { mutableStateOf(c?.lugar ?: "") }; var n by remember { mutableStateOf(c?.notas ?: "") }
    var a by remember { mutableStateOf(c?.alerta ?: 5) }; var err by remember { mutableStateOf("") }
    val horas = (0..23).flatMap { h -> listOf("00", "15", "30", "45").map { m -> "${h.toString().padStart(2, '0')}:$m" } }
    val mesL = MESES[st.mes - 1].lowercase()
    Marco(if (c == null) "Nueva cita" else "Editar cita", pie = {
        if (err.isNotEmpty()) T(err, C.error, 14)
        Btn(if (c == null) "Crear cita" else "Guardar cambios") {
            if (t.isBlank()) err = "Escribe un título" else if (f <= i) err = "La hora de fin debe ser posterior"
            else { val nueva = Cita(c?.id ?: (st.citas.maxOfOrNull { it.id } ?: 0) + 1, t, d, st.mes, i, f, l, n, a)
                if (c == null) st.citas.add(nueva) else st.citas[st.citas.indexOf(c)] = nueva
                st.dia = d; st.ir(Ruta.AGENDA) }
        }
        Btn("Cancelar", true) { st.ir(Ruta.AGENDA) }
    }) {
        Campo("Título", t, ph = "Reunión de equipo") { t = it }
        Desplegable("Fecha", "$d de $mesL", (1..diasMes(st.mes)).map { "$it de $mesL" }) { d = it.substringBefore(" ").toInt() }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Desplegable("Inicio", i, horas, Modifier.weight(1f)) { i = it }
            Desplegable("Fin", f, horas, Modifier.weight(1f)) { f = it }
        }
        Campo("Lugar", l, ph = "Sala de reuniones") { l = it }
        Campo("Notas", n, ph = "Notas") { n = it }
        Desplegable("Alerta", "$a min · Modal y sonido", listOf(5, 10, 15).map { "$it min · Modal y sonido" }) { a = it.substringBefore(" ").toInt() }
    }
}

@Composable fun Config() = Marco("Configuración", pie = { Btn("Guardar preferencias") { st.ir(Ruta.HOME) } }) {
    Tarjeta(false) {
        T("Apariencia", sz = 18, b = true); Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Día" to false, "Noche" to true).forEach { (n, o) ->
                val sel = st.oscuro == o
                Box(Modifier.weight(1f).clip(R12).background(if (sel) st.acento else Color(0xFFD9D9D9)).clickable { st.oscuro = o }.padding(14.dp), contentAlignment = Alignment.Center) {
                    T(if (sel) "$n ✓" else n, if (sel) Color.White else C.ink, b = true)
                }
            }
        }
        Spacer(Modifier.height(10.dp)); T("Color", sz = 18, b = true); Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Violeta" to 0xFF8C00FF, "Azul" to 0xFF1D5CFF, "Rosa" to 0xFFFF1493).forEach { (n, k) ->
                Box(Modifier.weight(1f).clip(R12).background(Color(k)).then(if (st.acento == Color(k)) Modifier.border(2.dp, Color.White, R12) else Modifier)
                    .clickable { st.acento = Color(k) }.padding(14.dp), contentAlignment = Alignment.Center) { T(n, Color.White, 14, true) }
            }
        }
    }
    Btn("Mi perfil", true) { st.ir(Ruta.PERFIL) }; Btn("Configurar alertas", true) { st.ir(Ruta.ALERTAS) }
}

@Composable fun Perfil() {
    var u by remember { mutableStateOf(st.usuario) }
    Marco("Mi perfil", pie = { Btn("Guardar cambios") { st.usuario = u; st.ir(Ruta.CFG) }; Btn("Volver", true) { st.ir(Ruta.CFG) } }) {
        Campo("Nombre", u, ph = "Usuario") { u = it }
        Btn("Cambiar contraseña", true) { st.ir(Ruta.PASS) }
    }
}

@Composable fun Clave() {
    var p0 by remember { mutableStateOf("") }; var p1 by remember { mutableStateOf("") }; var p2 by remember { mutableStateOf("") }; var err by remember { mutableStateOf("") }
    Marco("Cambiar contraseña", pie = {
        if (err.isNotEmpty()) T(err, C.error, 14)
        Btn("Guardar contraseña") {
            err = if (st.clave.isNotEmpty() && p0 != st.clave) "La contraseña actual no es correcta"
            else if (!claveValida(p1)) "La nueva contraseña no cumple el formato"
            else if (p1 != p2) "Las contraseñas no coinciden" else ""
            if (err.isEmpty()) { st.clave = p1; st.ir(Ruta.PERFIL) }
        }
        Btn("Cancelar", true) { st.ir(Ruta.PERFIL) }
    }) {
        Campo("Contraseña actual", p0, true, "••••••••") { p0 = it }
        Campo("Nueva contraseña", p1, true, "••••••••") { p1 = it }
        Campo("Confirmar contraseña", p2, true, "••••••••") { p2 = it }
        Reglas(p1.isNotEmpty() && !claveValida(p1))
    }
}

@Composable fun Alertas() = Marco("Alertas", pie = { Btn("Guardar alertas") { st.ir(Ruta.CFG) } }) {
    Interruptor("Recordatorios", st.recordatorios) { st.recordatorios = it }
    Desplegable("Antes de cada evento", "${st.minutos} minutos", listOf(5, 10, 15).map { "$it minutos" }) { st.minutos = it.substringBefore(" ").toInt() }
    Interruptor("Ventana modal", st.modal) { st.modal = it }
    Interruptor("Sonido", st.sonido) { st.sonido = it }
    Desplegable("Tono", st.tono, listOf("Campana suave", "Pitido", "Digital")) { st.tono = it }
    Btn("Probar aviso", true) { if (st.sonido) reproducirAviso(); if (st.modal) st.aviso = st.citas.firstOrNull() }
}

@Composable fun Recordatorio(e: Cita) =
    Box(Modifier.fillMaxSize().background(Color(0xAA000000)).clickable(remember { MutableInteractionSource() }, null) {}.padding(20.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 362.dp).fillMaxWidth().clip(R12).background(Color(0xFFA8DFFF)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T("En ${st.minutos} minutos", C.ink, 26, true); T(e.titulo, C.ink, 20, true)
            T("${e.inicio} – ${e.fin}", C.ink); T(e.lugar, C.ink)
            Btn("Ver cita") { st.aviso = null; st.dia = e.dia; st.mes = e.mes; st.vista = 2; st.ir(Ruta.AGENDA) }
            Btn("Silenciar", true) { st.aviso = null }; Btn("Entendido", true) { st.aviso = null }
        }
    }

@Composable fun Salas() {
    var q by remember { mutableStateOf("") }
    Marco("Chat de equipo", pie = { Btn("Crear sala") { st.ir(Ruta.NSALA) }; Btn("Inicio", true) { st.ir(Ruta.HOME) } }) {
        Campo("Buscar sala", q, ph = "Nombre de la sala") { q = it }
        st.salas.filter { it.nombre.contains(q, true) }.forEach { s ->
            if (s.pendiente) Tarjeta(false) { T(s.nombre, sz = 18, b = true); Spacer(Modifier.height(8.dp)); Btn("Ver invitación") { st.salaSel = s.id; st.ir(Ruta.INVIT) } }
            else Tarjeta(onClick = { st.salaSel = s.id; st.ir(Ruta.CHAT) }) {
                T(s.nombre, C.ink, 20, true); T("${s.miembros} miembros · ${s.msgs.size} mensajes", C.ink, 14)
                s.msgs.lastOrNull()?.let { T("${it.autor}: ${it.texto}", C.ink) }
            }
        }
    }
}

@Composable fun NuevaSala() {
    var n by remember { mutableStateOf("") }; var d by remember { mutableStateOf("") }
    Marco("Crear sala", pie = {
        Btn("Crear sala") { if (n.isNotBlank()) { val id = (st.salas.maxOfOrNull { it.id } ?: 0) + 1; st.salas.add(Sala(id, n, d, 1, emptyList())); st.salaSel = id; st.volverA = Ruta.SALAS; st.ir(Ruta.INVITAR) } }
        Btn("Cancelar", true) { st.ir(Ruta.SALAS) }
    }) { Campo("Nombre de la sala", n, ph = "Equipo de diseño") { n = it }; Campo("Descripción", d, ph = "Ideas, avances y coordinación del equipo.", lineas = 3) { d = it } }
}

@Composable fun Invitar() {
    val gente = listOf("Lucía García" to "lucia@equipo.com", "Marcos Ruiz" to "marcos@equipo.com", "Elena López" to "elena@equipo.com")
    val sel = remember { mutableStateListOf(0, 1) }; var q by remember { mutableStateOf("") }
    Marco("Invitar compañeros", pie = {
        Btn("Enviar ${sel.size} invitaciones") { val s = st.sala(); st.salas[st.salas.indexOf(s)] = s.copy(miembros = s.miembros + sel.size); st.ir(Ruta.CHAT) }
        Btn("Volver", true) { st.ir(st.volverA) }
    }) {
        T(st.sala().nombre, sz = 18, b = true)
        Campo("Buscar compañeros", q, ph = "Nombre o correo") { q = it }
        gente.forEachIndexed { i, (n, m) ->
            if (n.contains(q, true) || m.contains(q, true)) Tarjeta(i in sel, { if (i in sel) sel.remove(i) else sel.add(i) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { T(n, if (i in sel) C.ink else C.tx, b = true); T(m, if (i in sel) C.ink else C.tx, 14) }
                    T(if (i in sel) "✔" else "○", if (i in sel) st.acento else C.tx, 22, true)
                }
            }
        }
    }
}

@Composable fun Invitacion() {
    val s = st.sala()
    Marco("Invitación", pie = {
        Btn("Aceptar y entrar") { st.salas[st.salas.indexOf(s)] = s.copy(pendiente = false); st.volverA = Ruta.SALAS; st.ir(Ruta.CHAT) }
        Btn("Ahora no", true) { st.ir(Ruta.SALAS) }
    }) {
        Tarjeta { T(s.nombre, C.ink, 22, true); Spacer(Modifier.height(8.dp)); T("Lucía García te invita", C.ink); Spacer(Modifier.height(8.dp)); T(s.desc, C.ink) }
        T("Miembros", sz = 18, b = true); T("Lucía García\nMarcos Ruiz")
    }
}

@Composable fun Chat() {
    val s = st.sala(); var m by remember { mutableStateOf("") }
    Marco(s.nombre, pie = {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(m, { m = it }, Modifier.weight(1f), placeholder = { Text("Mensaje...") }, singleLine = true, shape = R12,
                colors = TextFieldDefaults.colors(focusedContainerColor = C.inp, unfocusedContainerColor = C.inp, focusedTextColor = C.ink, unfocusedTextColor = C.ink, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            Button({ if (m.isNotBlank()) { st.salas[st.salas.indexOf(s)] = s.copy(msgs = s.msgs + Mensaje("Tú", m, "ahora")); m = "" } }, Modifier.height(48.dp), shape = R12,
                colors = ButtonDefaults.buttonColors(containerColor = st.acento)) { T("Enviar", Color.White, b = true) }
        }
        Btn("Volver", true) { st.ir(Ruta.SALAS) }
    }) {
        Btn("Invitar compañeros", true) { st.volverA = Ruta.CHAT; st.ir(Ruta.INVITAR) }
        T("15 de enero", sz = 14, m = Modifier.align(Alignment.CenterHorizontally))
        // No hay opción de borrar mensajes ni salas (requisito del enunciado)
        s.msgs.forEach { x -> val yo = x.autor == "Tú"
            Box(Modifier.fillMaxWidth(), contentAlignment = if (yo) Alignment.CenterEnd else Alignment.CenterStart) {
                Column(Modifier.widthIn(max = 290.dp).clip(R12).background(if (yo) st.acento else C.lt).padding(12.dp)) {
                    val c = if (yo) Color.White else C.ink
                    T(x.autor, c, 14, true); T(x.texto, c); T(x.hora, c, 12)
                }
            }
        }
    }
}
