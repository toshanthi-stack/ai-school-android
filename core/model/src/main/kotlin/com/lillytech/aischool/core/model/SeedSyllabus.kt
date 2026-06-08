package com.lillytech.aischool.core.model

import com.lillytech.aischool.core.model.AiSchoolEndpoints.AUDIO_BASE

/**
 * Live topic page on the production site (e.g. `"ai-prompts".liveTopicPage()`
 * → `https://www.lillytechsystems.com/ai-prompts/index.html`). These are the
 * real, published AI School pages rendered by the mobile flavor's WebView.
 */
private fun String.liveTopicPage(): String =
    "https://www.lillytechsystems.com/$this/index.html"

/**
 * Structured mirror of the AI School catalog published at
 * [AiSchoolEndpoints.INDEX_PAGE].
 *
 * The network layer prefers the live `syllabus.json` feed; this seed keeps
 * both app flavors fully functional offline (cold cabin start, tunnels,
 * parking garages) and is the contract-of-record for the three pillars.
 */
object SeedSyllabus {

    val courses: List<Course> = listOf(

        // ── Pillar 1: Generative AI Skills ─────────────────────────────────
        Course(
            id = "genai-foundations",
            title = "Generative AI Foundations",
            description = "From tokens to transformers — the working mental model every AI practitioner needs.",
            category = AiSchoolPillars.GENERATIVE_AI,
            lessons = listOf(
                Lesson(
                    id = "genai-101",
                    title = "How Large Language Models Actually Work",
                    durationSeconds = 540,
                    audioUrl = AUDIO_BASE + "genai-101.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "A plain-language tour of next-token prediction, attention, and why scale changes behavior.",
                ),
                Lesson(
                    id = "genai-102",
                    title = "Prompt Orchestration Patterns",
                    durationSeconds = 660,
                    audioUrl = AUDIO_BASE + "genai-102.mp3",
                    visualContentUrl = "ai-prompts".liveTopicPage(),
                    isAutomotiveSafe = false, // interactive prompt-playground sandbox
                    audioSummary = "Chaining, routing, and critic-loop prompt patterns, narrated without the on-screen playground.",
                ),
                Lesson(
                    id = "genai-103",
                    title = "Context Windows, Tokens, and Cost",
                    durationSeconds = 480,
                    audioUrl = AUDIO_BASE + "genai-103.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "How context length, tokenization, and caching drive latency and unit economics.",
                ),
                Lesson(
                    id = "genai-104",
                    title = "Building with the Claude & GPT APIs",
                    durationSeconds = 720,
                    audioUrl = AUDIO_BASE + "genai-104.mp3",
                    visualContentUrl = "ai-apis".liveTopicPage(),
                    isAutomotiveSafe = false, // live code editor with copy-paste snippets
                    audioSummary = "API design choices — system prompts, tools, streaming — explained as an audio walkthrough.",
                ),
            ),
        ),
        Course(
            id = "genai-agents",
            title = "Agentic AI in Production",
            description = "Tool use, multi-agent orchestration, and the guardrails that make agents shippable.",
            category = AiSchoolPillars.GENERATIVE_AI,
            lessons = listOf(
                Lesson(
                    id = "agents-201",
                    title = "Tool Use & Function Calling",
                    durationSeconds = 600,
                    audioUrl = AUDIO_BASE + "agents-201.mp3",
                    visualContentUrl = "ai-agents".liveTopicPage(),
                    isAutomotiveSafe = false, // raw JSON tool schemas on screen
                    audioSummary = "How models call tools: schemas, dispatch loops, and failure handling — summarized for listening.",
                ),
                Lesson(
                    id = "agents-202",
                    title = "Multi-Agent Orchestration",
                    durationSeconds = 540,
                    audioUrl = AUDIO_BASE + "agents-202.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "Planner-worker, debate, and pipeline topologies — when each wins and where they break.",
                ),
                Lesson(
                    id = "agents-203",
                    title = "Guardrails & Responsible AI",
                    durationSeconds = 480,
                    audioUrl = AUDIO_BASE + "agents-203.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "Evaluation harnesses, policy layers, and data-loss-prevention patterns for production agents.",
                ),
            ),
        ),

        // ── Pillar 2: AI Infrastructure & Hardware ─────────────────────────
        Course(
            id = "infra-blackwell",
            title = "NVIDIA Blackwell Architecture Deep Dive",
            description = "What the B200 generation changes for training and inference economics.",
            category = AiSchoolPillars.INFRASTRUCTURE,
            lessons = listOf(
                Lesson(
                    id = "bw-301",
                    title = "Inside the Blackwell B200 Superchip",
                    durationSeconds = 600,
                    audioUrl = AUDIO_BASE + "bw-301.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "Dual-die design, the transformer engine, and what 'superchip' really means.",
                ),
                Lesson(
                    id = "bw-302",
                    title = "NVLink, HBM3e, and Memory Bandwidth",
                    durationSeconds = 660,
                    audioUrl = AUDIO_BASE + "bw-302.mp3",
                    visualContentUrl = "ai-hardware".liveTopicPage(),
                    isAutomotiveSafe = false, // dense interconnect/architecture diagrams
                    audioSummary = "Why memory bandwidth, not FLOPs, bounds modern inference — narrated without the diagrams.",
                ),
                Lesson(
                    id = "bw-303",
                    title = "From Hopper to Blackwell: What Changed",
                    durationSeconds = 480,
                    audioUrl = AUDIO_BASE + "bw-303.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "A generational comparison: precision formats, power envelopes, and rack-scale design.",
                ),
            ),
        ),
        Course(
            id = "infra-cuda",
            title = "CUDA Optimization Masterclass",
            description = "Hands-on GPU performance engineering, from execution model to profiler.",
            category = AiSchoolPillars.INFRASTRUCTURE,
            lessons = listOf(
                Lesson(
                    id = "cuda-401",
                    title = "The CUDA Execution Model",
                    durationSeconds = 720,
                    audioUrl = AUDIO_BASE + "cuda-401.mp3",
                    visualContentUrl = "ai-architectures".liveTopicPage(),
                    isAutomotiveSafe = false, // kernel source code on screen
                    audioSummary = "Grids, blocks, warps, and occupancy — the execution model explained conversationally.",
                ),
                Lesson(
                    id = "cuda-402",
                    title = "Kernel Profiling with Nsight",
                    durationSeconds = 660,
                    audioUrl = AUDIO_BASE + "cuda-402.mp3",
                    visualContentUrl = "ai-hardware".liveTopicPage(),
                    isAutomotiveSafe = false, // profiler screenshots and raw metrics tables
                    audioSummary = "Reading a profile like a story: stalls, throughput ceilings, and the first three fixes to try.",
                ),
                Lesson(
                    id = "cuda-403",
                    title = "Memory Coalescing Explained (Audio Walkthrough)",
                    durationSeconds = 480,
                    audioUrl = AUDIO_BASE + "cuda-403.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "Why access patterns dominate kernel performance, told entirely through intuition and analogy.",
                ),
            ),
        ),

        // ── Pillar 3: Advanced LLM Tuning ──────────────────────────────────
        Course(
            id = "tune-vectors",
            title = "Vector Indexing & Retrieval",
            description = "Embeddings, ANN indexes, and evaluation for retrieval-augmented systems.",
            category = AiSchoolPillars.ADVANCED_TUNING,
            lessons = listOf(
                Lesson(
                    id = "vec-501",
                    title = "Embeddings and Semantic Distance",
                    durationSeconds = 540,
                    audioUrl = AUDIO_BASE + "vec-501.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "What an embedding space is, and why cosine similarity became the default ruler.",
                ),
                Lesson(
                    id = "vec-502",
                    title = "HNSW vs IVF: Choosing an Index",
                    durationSeconds = 600,
                    audioUrl = AUDIO_BASE + "vec-502.mp3",
                    visualContentUrl = "ai-models".liveTopicPage(),
                    isAutomotiveSafe = false, // raw benchmark JSON and recall tables
                    audioSummary = "Recall/latency/memory trade-offs between graph and clustering indexes, minus the tables.",
                ),
                Lesson(
                    id = "vec-503",
                    title = "RAG Evaluation Pipelines",
                    durationSeconds = 660,
                    audioUrl = AUDIO_BASE + "vec-503.mp3",
                    visualContentUrl = "ai-frameworks".liveTopicPage(),
                    isAutomotiveSafe = false, // interactive evaluation notebook
                    audioSummary = "Groundedness, answer relevance, and retrieval precision — how to score a RAG system end to end.",
                ),
            ),
        ),
        Course(
            id = "tune-finetune",
            title = "Fine-Tuning & Quantization",
            description = "Parameter-efficient tuning and precision engineering for deployable models.",
            category = AiSchoolPillars.ADVANCED_TUNING,
            lessons = listOf(
                Lesson(
                    id = "ft-601",
                    title = "LoRA and QLoRA in Plain English",
                    durationSeconds = 540,
                    audioUrl = AUDIO_BASE + "ft-601.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "Low-rank adapters demystified: what trains, what freezes, and why it is so cheap.",
                ),
                Lesson(
                    id = "ft-602",
                    title = "Quantization: INT8, FP8, and Beyond",
                    durationSeconds = 480,
                    audioUrl = AUDIO_BASE + "ft-602.mp3",
                    visualContentUrl = null,
                    isAutomotiveSafe = true,
                    audioSummary = "How fewer bits keep quality: calibration, outlier channels, and hardware-native formats.",
                ),
                Lesson(
                    id = "ft-603",
                    title = "Hands-On: Fine-Tune a 7B Model",
                    durationSeconds = 780,
                    audioUrl = AUDIO_BASE + "ft-603.mp3",
                    visualContentUrl = "ai-projects".liveTopicPage(),
                    isAutomotiveSafe = false, // full Python training scripts in a sandbox
                    audioSummary = "The full fine-tuning recipe — data prep, hyperparameters, evaluation — as a narrated walkthrough.",
                ),
            ),
        ),
    )
}
