package com.softmotions.ncms

import com.softmotions.kotlin.TimeSpec
import com.softmotions.kotlin.toSeconds
import com.softmotions.runner.ProcessRun
import com.softmotions.runner.ProcessRunner
import com.softmotions.runner.ProcessRunners
import com.softmotions.runner.UnixSignal
import org.slf4j.LoggerFactory

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class PostgresTestRunner : DatabaseTestRunner {

    private val log = LoggerFactory.getLogger(PostgresTestRunner::class.java)

    private val dbRunner: ProcessRunner = ProcessRunners.serial(verbose = true)

    private val dbBin: String = "/usr/lib/postgresql/9.5/bin"

    private val dbPort: Int = 9231

    private var dbDir: String? = null

    protected fun outputLine(line: String): Unit {
        log.info(line)
    }

    protected fun checkExitCode(pr: ProcessRun) {
        val ecode = pr.process.exitValue()
        if (ecode != 0) {
            throw RuntimeException("Process failed with exit code: $ecode command: ${pr.command}")
        }
    }

    override fun setupDB(props: Map<String, Any>) {
        shutdownDB()

        System.setProperty("JDBC.env", "pgtest")
        System.setProperty("JDBC.url", "jdbc:postgresql://localhost:${dbPort}/postgres")
        System.setProperty("JDBC.driver", "org.postgresql.Driver")

        dbDir = "/dev/shm/ncmsdb" + System.currentTimeMillis()
        log.info("Setup database, dir: $dbDir")
        with(dbRunner) {
            cmd("mkdir -p $dbDir")
            cmd("$dbBin/initdb -D $dbDir") {
                outputLine(it)
            }.waitFor {
                checkExitCode(it)
            }
            cmd("$dbBin/postgres -D $dbDir -p $dbPort -o \"-c fsync=off -c synchronous_commit=off -c full_page_writes=off\"") {
                outputLine(it)
            }
        }
    }

    override fun shutdownDB() {
        dbRunner.reset(TimeSpec.HALF_MIN, UnixSignal.SIGINT)
        dbDir?.let {
            log.info("Remove database dir: $dbDir")
            with(dbRunner) {
                cmd("rm -rf $dbDir")
            }.haltRunner(30.toSeconds())
            dbDir = null
        }
    }
}