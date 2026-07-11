package br.edu.utfpr.unihelper.core.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.edu.utfpr.unihelper.disciplina.data.repository.DisciplinaRepository
import br.edu.utfpr.unihelper.agenda.data.repository.AgendaRepository
import br.edu.utfpr.unihelper.documento.data.repository.DocumentoRepository
import br.edu.utfpr.unihelper.nota.data.repository.NotaRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val disciplinaRepository: DisciplinaRepository by inject()
    private val agendaRepository: AgendaRepository by inject()
    private val documentoRepository: DocumentoRepository by inject()
    private val notaRepository: NotaRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            disciplinaRepository.syncPending()
            agendaRepository.syncPending()
            documentoRepository.syncPending()
            notaRepository.syncPending()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "unihelper_sync"

        fun agendar(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // don't re-schedule if already scheduled
                request
            )
        }
    }
}
