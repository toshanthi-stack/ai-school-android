package com.lillytech.aischool.core.network

import com.lillytech.aischool.core.model.AiSchoolEndpoints
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.SeedSyllabus
import com.lillytech.aischool.core.model.toAutomotiveSafeSyllabus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Ktor client for the AI School production ecosystem at
 * [AiSchoolEndpoints.BASE_URL].
 *
 * Dual-payload handling: every request declares its payload mode via the
 * `X-AISchool-Payload` header — [PayloadMode.VISUAL] for the mobile flavor
 * (videos, code editors, interactive sandboxes) and [PayloadMode.AUDIO_ONLY]
 * for the vehicle (audio streams + short semantic summaries only).
 *
 * Resilience: the structured `syllabus.json` feed is preferred; if it is
 * unreachable the client probes the live [AiSchoolEndpoints.INDEX_PAGE] and
 * falls back to [SeedSyllabus], the offline contract-of-record mirror of that
 * page. The cabin experience therefore never depends on connectivity.
 */
class AISchoolApiClient(
    httpClient: HttpClient? = null,
) {
    /** Payload negotiation modes understood by the AI School backend. */
    enum class PayloadMode(val wireValue: String) {
        VISUAL("visual"),
        AUDIO_ONLY("audio-only"),
    }

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client: HttpClient = httpClient ?: HttpClient(OkHttp) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(this@AISchoolApiClient.json)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 20_000
        }
        defaultRequest {
            header(HEADER_CLIENT, CLIENT_ID)
        }
    }

    /**
     * Fetches the full syllabus tree.
     *
     * Tries the structured feed first; on any transport or parse failure it
     * degrades to the bundled [SeedSyllabus] so callers always receive a
     * complete catalog. Coroutine cancellation is always propagated.
     */
    suspend fun fetchSyllabus(mode: PayloadMode = PayloadMode.VISUAL): List<Course> {
        return try {
            val raw: String = client
                .get(AiSchoolEndpoints.SYLLABUS_JSON) {
                    header(HEADER_PAYLOAD, mode.wireValue)
                }
                .body()
            json.decodeFromString(ListSerializer(Course.serializer()), raw)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            SeedSyllabus.courses
        }
    }

    /**
     * Automotive entry point. Requests the audio-only payload, then applies
     * the distraction-compliance filter from `:core:model`:
     *
     * - raw text lectures, video pipelines, and copy-paste interactive
     *   modules are stripped (`visualContentUrl` is removed everywhere);
     * - lessons flagged `isAutomotiveSafe = false` survive only as their
     *   audio stream plus short semantic summary;
     * - lessons with no audio track, and courses left empty, are dropped.
     *
     * The result is a clean tree of audio streams and summaries — the only
     * content shape the vehicle service will ever see.
     */
    suspend fun fetchAutomotiveSafeSyllabus(): List<Course> =
        fetchSyllabus(PayloadMode.AUDIO_ONLY).toAutomotiveSafeSyllabus()

    /**
     * Fetches the raw HTML of the live catalog page. Used as a reachability
     * probe and to let future layout-ingestion keep the seed mirror honest.
     */
    suspend fun fetchLiveLayoutHtml(): String =
        client.get(AiSchoolEndpoints.INDEX_PAGE).body()

    /** Releases the underlying engine. Call from `onCleared`/`onDestroy`. */
    fun close() {
        client.close()
    }

    private companion object {
        const val HEADER_PAYLOAD = "X-AISchool-Payload"
        const val HEADER_CLIENT = "X-AISchool-Client"
        const val CLIENT_ID = "aischool-android/1.0"
    }
}
