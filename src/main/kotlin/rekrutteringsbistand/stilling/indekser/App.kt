package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get

class App {
    companion object {
        fun setup() {
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            val app = Javalin.create().start(8222)

            app.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }
        }
    }
}

fun main() {
    App.setup()
}
