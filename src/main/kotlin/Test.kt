import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object StarWarsFilms : IntIdTable() {
    val sequelId: Column<Int> = integer("sequel_id").uniqueIndex()
    val name: Column<String> = varchar("name", 50)
    val director: Column<String> = varchar("director", 50)
}

class StarWarsFilm(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<StarWarsFilm>(StarWarsFilms)
    var sequelId by StarWarsFilms.sequelId
    var name     by StarWarsFilms.name
    var director by StarWarsFilms.director
}

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")

    transaction {
        SchemaUtils.create (StarWarsFilms)

        val movie = StarWarsFilm.new {
            name = "The Last Jedi"
            sequelId = 8
            director = "Rian Johnson"
        }
        val movieId = movie.id

        StarWarsFilms.update({ StarWarsFilms.id eq movieId }) {
            it[StarWarsFilms.name] = "Not the very last Jedi"
        }

        val dslName = StarWarsFilms.select { StarWarsFilms.id eq movieId }.single().let { it[StarWarsFilms.name] }
        val daoName = StarWarsFilm[movieId].name
        StarWarsFilm.reload(movie, flush = false)
        val reloadedDaoName = StarWarsFilm[movieId].name

        println("dslName = $dslName")
        println("daoName = $daoName")
        println("reloadedDaoName = $reloadedDaoName")
    }
}