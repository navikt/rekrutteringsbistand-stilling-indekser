package rekrutteringsbistand.stilling.indekser.utils

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

fun environment(): Dotenv {
    return dotenv {
        ignoreIfMissing = true
    }
}
