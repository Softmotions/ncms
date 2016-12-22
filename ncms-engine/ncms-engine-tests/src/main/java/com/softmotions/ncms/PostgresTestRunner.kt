package com.softmotions.ncms

import com.softmotions.kotlin.TimeSpec
import com.softmotions.kotlin.toSeconds
import com.softmotions.runner.ProcessRun
import com.softmotions.runner.ProcessRunner
import com.softmotions.runner.ProcessRunners
import com.softmotions.runner.UnixSignal
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
open class PostgresTestRunner : DatabaseTestRunner {

    private val log = LoggerFactory.getLogger(PostgresTestRunner::class.java)

    private val dbRunner: ProcessRunner = ProcessRunners.serial(verbose = true)

    private val dbBin: String = "/usr/lib/postgresql/9.6/bin"

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

    override fun setupDb(props: Map<String, Any>) {
        shutdownDb()


        System.setProperty("JDBC.env", "pgtest")
        System.setProperty("JDBC.url", "jdbc:postgresql://localhost:${dbPort}/postgres")
        System.setProperty("JDBC.driver", "org.postgresql.Driver")

        val started = AtomicBoolean(false)
        val locale = "en_US.UTF-8"


        dbDir = "/dev/shm/ncmsdb" + System.currentTimeMillis()
        log.info("Setup database, dir: $dbDir")
        with(dbRunner) {
            cmd("mkdir -p $dbDir")
            cmd("$dbBin/initdb" +
                    " --lc-messages=C" +
                    " --lc-collate=$locale --lc-ctype=$locale" +
                    " --lc-monetary=$locale --lc-numeric=$locale --lc-time=$locale" +
                    " -D $dbDir",
                    env = mapOf("LC_ALL".to("C"))) {
                outputLine(it)
            }.waitFor {
                checkExitCode(it)
            }
            cmd("$dbBin/postgres -D $dbDir -p $dbPort -o \"-c fsync=off -c synchronous_commit=off -c full_page_writes=off\"",
                    failOnTimeout = true) {
                outputLine(it)
                if (it.trim().contains("database system is ready to accept connections")) {
                    synchronized(started) {
                        started.set(true)
                        (started as Object).notifyAll()
                    }
                }
            }
        }
        synchronized(started) {
            if (!started.get()) {
                (started as Object).wait(30.toSeconds().toMillis())
            }
        }
        if (!started.get()) {
            throw RuntimeException("Timeout of waiting for postgres server")
        }
    }

    override fun shutdownDb() {
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