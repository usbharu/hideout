package utils

import com.ninja_squad.dbsetup.Operations
import com.ninja_squad.dbsetup.operation.Insert
import com.ninja_squad.dbsetup_kotlin.dbSetup
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.assertj.db.api.TableRowAssert
import org.assertj.db.api.TableRowValueAssert
import org.assertj.db.type.Changes
import org.assertj.db.type.Table
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.sql.Connection
import javax.sql.DataSource

abstract class AbstractRepositoryTest(private val exposedTable: org.jetbrains.exposed.sql.Table) {

    protected val assertTable: Table
        get() {
            return Table(dataSource, exposedTable.tableName)
        }

    protected fun getTable(name: String): Table {
        return Table(dataSource, name)
    }

    private lateinit var transaction: Transaction

    protected lateinit var change: Changes

    @BeforeEach
    fun setUp() {
        flyway.clean()
        flyway.migrate()
        transaction = TransactionManager.manager.newTransaction(Connection.TRANSACTION_READ_UNCOMMITTED)
        change = Changes(assertTable)
    }

    @AfterEach
    fun tearDown() {
        dbSetup(to = dataSource) {
            execute(Operations.sql("SET REFERENTIAL_INTEGRITY TRUE"))
        }.launch()
        transaction.rollback()
        transaction.close()
    }

    companion object {

        lateinit var dataSource: DataSource
        lateinit var flyway: Flyway

        @JvmStatic
        @BeforeAll
        fun setup() {
            val hikariConfig = HikariConfig()
            hikariConfig.jdbcUrl =
                "jdbc:h2:mem:test;MODE=POSTGRESQL;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true;TRACE_LEVEL_FILE=4;"
            hikariConfig.driverClassName = "org.h2.Driver"
            hikariConfig.transactionIsolation = "TRANSACTION_READ_UNCOMMITTED"
            dataSource = HikariDataSource(hikariConfig)


            flyway = Flyway.configure().cleanDisabled(false).dataSource(dataSource).load()
            Database.connect(dataSource, databaseConfig = DatabaseConfig {
                defaultMaxAttempts = 1

            })
            flyway.clean()
            flyway.migrate()
        }

        @JvmStatic
        @AfterAll
        fun clean() {
//            flyway.clean()
        }
    }
}

fun <T> TableRowAssert.value(column: Column<T>): TableRowValueAssert = value(column.name)
fun <T> TableRowValueAssert.value(column: Column<T>): TableRowValueAssert = value(column.name)

fun <T> TableRowAssert.isEqualTo(column: Column<T>, value: T): TableRowValueAssert {
    return value(column).isEqualTo(value)
}

fun <T> TableRowValueAssert.isEqualTo(column: Column<T>, value: T): TableRowValueAssert {
    return value(column).isEqualTo(value)
}

fun Insert.Builder.columns(columns: List<Column<*>>): Insert.Builder {
    columns(*columns.map { it.name }.toTypedArray())
    return this
}

fun Insert.Builder.columns(vararg columns: Column<*>): Insert.Builder {
    columns(*columns.map { it.name }.toTypedArray())
    return this
}

fun Changes.with(block: () -> Unit) {
    setStartPointNow()
    block()
    setEndPointNow()
}

suspend fun Changes.withSuspend(block: suspend () -> Unit) {
    setStartPointNow()
    block()
    setEndPointNow()
}

val disableReferenceIntegrityConstraints = Operations.sql("SET REFERENTIAL_INTEGRITY FALSE")
val enableReferenceIntegrityConstraints = Operations.sql("SET REFERENTIAL_INTEGRITY TRUE")