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
    Database.connect("jdbc:h2:~/test-db", driver = "org.h2.Driver")

    val movie = transaction {
        SchemaUtils.create(StarWarsFilms)

        val movie = StarWarsFilm.new {
            name = "The Last Jedi"
            sequelId = 8
            director = "Rian Johnson"
        }

        StarWarsFilms.update({ StarWarsFilms.id eq movie.id }) {
            it[StarWarsFilms.name] = "Not the very last Jedi"
        }

        val dslName = StarWarsFilms.select { StarWarsFilms.id eq movie.id }.single().let { it[StarWarsFilms.name] }
        val daoName = StarWarsFilm[movie.id].name

        println("dslName = $dslName") // prints "Not the very last Jedi"
        println("daoName = $daoName") // prints "The Last Jedi"
        movie
    }
    val newName = transaction { StarWarsFilm[movie.id].name }
    println("newName = $newName") // prints "Not the very last Jedi"
}