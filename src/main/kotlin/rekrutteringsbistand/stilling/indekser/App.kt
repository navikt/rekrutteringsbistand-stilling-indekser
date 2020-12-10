package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get

fun main() {
    val app = Javalin.create().start(8222)

    app.routes {
        get("/internal/isAlive") { ctx -> ctx.status(200) }
        get("/internal/isReady") { ctx -> ctx.status(200) }
    }
}
