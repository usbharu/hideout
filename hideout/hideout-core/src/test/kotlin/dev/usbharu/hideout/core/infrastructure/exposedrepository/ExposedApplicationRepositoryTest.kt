package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.application.Application
import dev.usbharu.hideout.core.domain.model.application.ApplicationId
import dev.usbharu.hideout.core.domain.model.application.ApplicationName
import kotlinx.coroutines.test.runTest
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.AbstractRepositoryTest
import utils.columns
import utils.isEqualTo
import utils.withSuspend

class ExposedApplicationRepositoryTest : AbstractRepositoryTest(Applications) {
    @Test
    fun save_idが同じレコードが存在しないとinsert() = runTest {
        val application = Application(ApplicationId(1), ApplicationName("test-application"))

        ExposedApplicationRepository().save(application)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Applications.id, application.applicationId.id)
            .isEqualTo(Applications.name, application.name.name)
    }

    @Test
    fun save_idが同じレコードが存在したらupdate() = runTest {
        dbSetup(to = dataSource) {
            insertInto(Applications.tableName) {
                columns(Applications.columns)
                values(1, "application-test")
            }
        }.launch()

        val application = Application(ApplicationId(1), ApplicationName("test-application"))

        ExposedApplicationRepository().save(application)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Applications.id, application.applicationId.id)
            .isEqualTo(Applications.name, application.name.name)
    }

    @Test
    fun delete_削除される() = runTest {
        dbSetup(to = dataSource) {
            insertInto(Applications.tableName) {
                columns(Applications.columns)
                values(1, "test-application")
            }
        }.launch()

        val application = Application(ApplicationId(1), ApplicationName("test-application"))

        change.withSuspend {
            ExposedApplicationRepository().delete(application)
        }

        assertThat(change)
            .changeOfDeletionOnTable(Applications.tableName)
            .rowAtStartPoint()
            .value(Applications.id.name).isEqualTo(application.applicationId.id)
            .value(Applications.name.name).isEqualTo(application.name.name)


    }
}