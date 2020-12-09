package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get

fun main() {
    val basePath = "/rekrutteringsbistand-stilling-indekser"
    val app = Javalin.create().start(8222)

    app.routes {
        get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
        get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
    }
}
