import Foundation

/// Structured mirror of the AI School catalog published at
/// lillytechsystems.com/ai-school. The repository prefers the live
/// `syllabus.json` feed; this seed keeps the app fully functional offline and
/// is the contract-of-record for the three pillars.
enum SeedSyllabus {

    static let courses: [Course] = [

        // Pillar 1: Generative AI Skills
        Course(
            id: "genai-foundations",
            title: "Generative AI Foundations",
            description: "From tokens to transformers, the working mental model every AI practitioner needs.",
            category: Pillars.generativeAI,
            lessons: [
                Lesson(id: "genai-101", title: "How Large Language Models Actually Work",
                       durationSeconds: 540, audioUrl: Endpoints.audioBase + "genai-101.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "A plain-language tour of next-token prediction, attention, and why scale changes behavior."),
                Lesson(id: "genai-102", title: "Prompt Orchestration Patterns",
                       durationSeconds: 660, audioUrl: Endpoints.audioBase + "genai-102.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-prompts"), isAutomotiveSafe: false,
                       audioSummary: "Chaining, routing, and critic-loop prompt patterns, narrated without the on-screen playground."),
                Lesson(id: "genai-103", title: "Context Windows, Tokens, and Cost",
                       durationSeconds: 480, audioUrl: Endpoints.audioBase + "genai-103.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "How context length, tokenization, and caching drive latency and unit economics."),
                Lesson(id: "genai-104", title: "Building with the Claude & GPT APIs",
                       durationSeconds: 720, audioUrl: Endpoints.audioBase + "genai-104.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-apis"), isAutomotiveSafe: false,
                       audioSummary: "API design choices (system prompts, tools, streaming) explained as an audio walkthrough."),
            ]),
        Course(
            id: "genai-agents",
            title: "Agentic AI in Production",
            description: "Tool use, multi-agent orchestration, and the guardrails that make agents shippable.",
            category: Pillars.generativeAI,
            lessons: [
                Lesson(id: "agents-201", title: "Tool Use & Function Calling",
                       durationSeconds: 600, audioUrl: Endpoints.audioBase + "agents-201.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-agents"), isAutomotiveSafe: false,
                       audioSummary: "How models call tools: schemas, dispatch loops, and failure handling, summarized for listening."),
                Lesson(id: "agents-202", title: "Multi-Agent Orchestration",
                       durationSeconds: 540, audioUrl: Endpoints.audioBase + "agents-202.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "Planner-worker, debate, and pipeline topologies, when each wins and where they break."),
                Lesson(id: "agents-203", title: "Guardrails & Responsible AI",
                       durationSeconds: 480, audioUrl: Endpoints.audioBase + "agents-203.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "Evaluation harnesses, policy layers, and data-loss-prevention patterns for production agents."),
            ]),

        // Pillar 2: AI Infrastructure & Hardware
        Course(
            id: "infra-blackwell",
            title: "NVIDIA Blackwell Architecture Deep Dive",
            description: "What the B200 generation changes for training and inference economics.",
            category: Pillars.infrastructure,
            lessons: [
                Lesson(id: "bw-301", title: "Inside the Blackwell B200 Superchip",
                       durationSeconds: 600, audioUrl: Endpoints.audioBase + "bw-301.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "Dual-die design, the transformer engine, and what 'superchip' really means."),
                Lesson(id: "bw-302", title: "NVLink, HBM3e, and Memory Bandwidth",
                       durationSeconds: 660, audioUrl: Endpoints.audioBase + "bw-302.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-hardware"), isAutomotiveSafe: false,
                       audioSummary: "Why memory bandwidth, not FLOPs, bounds modern inference, narrated without the diagrams."),
                Lesson(id: "bw-303", title: "From Hopper to Blackwell: What Changed",
                       durationSeconds: 480, audioUrl: Endpoints.audioBase + "bw-303.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "A generational comparison: precision formats, power envelopes, and rack-scale design."),
            ]),
        Course(
            id: "infra-cuda",
            title: "CUDA Optimization Masterclass",
            description: "Hands-on GPU performance engineering, from execution model to profiler.",
            category: Pillars.infrastructure,
            lessons: [
                Lesson(id: "cuda-401", title: "The CUDA Execution Model",
                       durationSeconds: 720, audioUrl: Endpoints.audioBase + "cuda-401.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-architectures"), isAutomotiveSafe: false,
                       audioSummary: "Grids, blocks, warps, and occupancy, the execution model explained conversationally."),
                Lesson(id: "cuda-402", title: "Kernel Profiling with Nsight",
                       durationSeconds: 660, audioUrl: Endpoints.audioBase + "cuda-402.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-hardware"), isAutomotiveSafe: false,
                       audioSummary: "Reading a profile like a story: stalls, throughput ceilings, and the first three fixes to try."),
                Lesson(id: "cuda-403", title: "Memory Coalescing Explained (Audio Walkthrough)",
                       durationSeconds: 480, audioUrl: Endpoints.audioBase + "cuda-403.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "Why access patterns dominate kernel performance, told entirely through intuition and analogy."),
            ]),

        // Pillar 3: Advanced LLM Tuning
        Course(
            id: "tune-vectors",
            title: "Vector Indexing & Retrieval",
            description: "Embeddings, ANN indexes, and evaluation for retrieval-augmented systems.",
            category: Pillars.advancedTuning,
            lessons: [
                Lesson(id: "vec-501", title: "Embeddings and Semantic Distance",
                       durationSeconds: 540, audioUrl: Endpoints.audioBase + "vec-501.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "What an embedding space is, and why cosine similarity became the default ruler."),
                Lesson(id: "vec-502", title: "HNSW vs IVF: Choosing an Index",
                       durationSeconds: 600, audioUrl: Endpoints.audioBase + "vec-502.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-models"), isAutomotiveSafe: false,
                       audioSummary: "Recall/latency/memory trade-offs between graph and clustering indexes, minus the tables."),
                Lesson(id: "vec-503", title: "RAG Evaluation Pipelines",
                       durationSeconds: 660, audioUrl: Endpoints.audioBase + "vec-503.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-frameworks"), isAutomotiveSafe: false,
                       audioSummary: "Groundedness, answer relevance, and retrieval precision, how to score a RAG system end to end."),
            ]),
        Course(
            id: "tune-finetune",
            title: "Fine-Tuning & Quantization",
            description: "Parameter-efficient tuning and precision engineering for deployable models.",
            category: Pillars.advancedTuning,
            lessons: [
                Lesson(id: "ft-601", title: "LoRA and QLoRA in Plain English",
                       durationSeconds: 540, audioUrl: Endpoints.audioBase + "ft-601.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "Low-rank adapters demystified: what trains, what freezes, and why it is so cheap."),
                Lesson(id: "ft-602", title: "Quantization: INT8, FP8, and Beyond",
                       durationSeconds: 480, audioUrl: Endpoints.audioBase + "ft-602.mp3",
                       visualContentUrl: nil, isAutomotiveSafe: true,
                       audioSummary: "How fewer bits keep quality: calibration, outlier channels, and hardware-native formats."),
                Lesson(id: "ft-603", title: "Hands-On: Fine-Tune a 7B Model",
                       durationSeconds: 780, audioUrl: Endpoints.audioBase + "ft-603.mp3",
                       visualContentUrl: Endpoints.liveTopicPage("ai-projects"), isAutomotiveSafe: false,
                       audioSummary: "The full fine-tuning recipe (data prep, hyperparameters, evaluation) as a narrated walkthrough."),
            ]),
    ]
}
