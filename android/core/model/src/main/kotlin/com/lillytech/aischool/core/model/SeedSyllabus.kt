package com.lillytech.aischool.core.model

/**
 * Structured mirror of the AI School catalog, generated from the content
 * pipeline's feed by `pipeline/bundle_into_apps.py`.
 *
 * The network layer prefers the live `syllabus.json` feed; this seed keeps
 * both app flavors fully functional offline (and is the bundled, no-hosting
 * content of record). Do not hand-edit; re-run the bundler to refresh.
 */
object SeedSyllabus {

    val courses: List<Course> = listOf(
        Course(
            id = "anthropic-claude-claude-models-overview",
            title = "Claude Models Overview",
            description = "An overview of the Claude model family and how its Opus, Sonnet, and Haiku tiers differ in strength, cost, and context.",
            category = "Anthropic Claude",
            lessons = listOf(
                Lesson(
                    id = "anthropic-claude-claude-models-overview",
                    title = "Claude Models Overview",
                    durationSeconds = 85,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/anthropic-claude/claude-models-overview/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of the Claude model family and how its Opus, Sonnet, and Haiku tiers differ in strength, cost, and context.",
                ),
            ),
        ),
        Course(
            id = "anthropic-claude-opus-deep-dive",
            title = "Opus Deep Dive",
            description = "An overview of when and why to reach for Claude Opus 4.7, covering its strengths, costs, and the trade-offs against Sonnet.",
            category = "Anthropic Claude",
            lessons = listOf(
                Lesson(
                    id = "anthropic-claude-opus-deep-dive",
                    title = "Opus Deep Dive",
                    durationSeconds = 89,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/anthropic-claude/opus-deep-dive/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of when and why to reach for Claude Opus 4.7, covering its strengths, costs, and the trade-offs against Sonnet.",
                ),
            ),
        ),
        Course(
            id = "anthropic-claude-sonnet-deep-dive",
            title = "Sonnet Deep Dive",
            description = "An overview of how to use Claude Sonnet 4.6 as a cost-effective production workhorse and how it fits alongside Opus.",
            category = "Anthropic Claude",
            lessons = listOf(
                Lesson(
                    id = "anthropic-claude-sonnet-deep-dive",
                    title = "Sonnet Deep Dive",
                    durationSeconds = 81,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/anthropic-claude/sonnet-deep-dive/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of how to use Claude Sonnet 4.6 as a cost-effective production workhorse and how it fits alongside Opus.",
                ),
            ),
        ),
        Course(
            id = "anthropic-claude-haiku-deep-dive",
            title = "Haiku Deep Dive",
            description = "How to use Claude Haiku 4.5 as your fast, low-cost default model and when to escalate to bigger models like Sonnet or Opus.",
            category = "Anthropic Claude",
            lessons = listOf(
                Lesson(
                    id = "anthropic-claude-haiku-deep-dive",
                    title = "Haiku Deep Dive",
                    durationSeconds = 78,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/anthropic-claude/haiku-deep-dive/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "How to use Claude Haiku 4.5 as your fast, low-cost default model and when to escalate to bigger models like Sonnet or Opus.",
                ),
            ),
        ),
        Course(
            id = "anthropic-claude-model-selection",
            title = "Model Selection",
            description = "A framework for choosing the right Claude model per use case by balancing latency, cost, quality, context, and tool-use complexity.",
            category = "Anthropic Claude",
            lessons = listOf(
                Lesson(
                    id = "anthropic-claude-model-selection",
                    title = "Model Selection",
                    durationSeconds = 83,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/anthropic-claude/model-selection/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "A framework for choosing the right Claude model per use case by balancing latency, cost, quality, context, and tool-use complexity.",
                ),
            ),
        ),
        Course(
            id = "anthropic-claude-claude-version-history",
            title = "Claude Version History",
            description = "An overview of Claude's version lineage, how its capabilities evolved across generations, and the discipline needed to migrate production systems forward safely.",
            category = "Anthropic Claude",
            lessons = listOf(
                Lesson(
                    id = "anthropic-claude-claude-version-history",
                    title = "Claude Version History",
                    durationSeconds = 85,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/anthropic-claude/claude-version-history/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Claude's version lineage, how its capabilities evolved across generations, and the discipline needed to migrate production systems forward safely.",
                ),
            ),
        ),
        Course(
            id = "ai-tools-claude-code",
            title = "Claude Code",
            description = "An overview of Anthropic's Claude Code, a terminal-native AI coding agent, and the six lessons that teach its core features.",
            category = "AI Tools",
            lessons = listOf(
                Lesson(
                    id = "ai-tools-claude-code",
                    title = "Claude Code",
                    durationSeconds = 82,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-tools/claude-code/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Anthropic's Claude Code, a terminal-native AI coding agent, and the six lessons that teach its core features.",
                ),
            ),
        ),
        Course(
            id = "ai-tools-cursor-editor",
            title = "Cursor",
            description = "An overview of Cursor, the AI-first code editor, and the six lessons that teach its core features.",
            category = "AI Tools",
            lessons = listOf(
                Lesson(
                    id = "ai-tools-cursor-editor",
                    title = "Cursor",
                    durationSeconds = 78,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-tools/cursor-editor/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Cursor, the AI-first code editor, and the six lessons that teach its core features.",
                ),
            ),
        ),
        Course(
            id = "ai-tools-github-copilot-tool",
            title = "GitHub Copilot",
            description = "An overview of GitHub Copilot's features and the six-lesson learning path that takes you from basics to enterprise use.",
            category = "AI Tools",
            lessons = listOf(
                Lesson(
                    id = "ai-tools-github-copilot-tool",
                    title = "GitHub Copilot",
                    durationSeconds = 80,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-tools/github-copilot-tool/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of GitHub Copilot's features and the six-lesson learning path that takes you from basics to enterprise use.",
                ),
            ),
        ),
        Course(
            id = "ai-tools-aider",
            title = "Aider",
            description = "An overview of Aider, the terminal-based AI pair programmer, and the six lessons that teach its key features.",
            category = "AI Tools",
            lessons = listOf(
                Lesson(
                    id = "ai-tools-aider",
                    title = "Aider",
                    durationSeconds = 77,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-tools/aider/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Aider, the terminal-based AI pair programmer, and the six lessons that teach its key features.",
                ),
            ),
        ),
        Course(
            id = "ai-tools-continue-dev",
            title = "Continue",
            description = "An overview of Continue, the open-source AI coding assistant, and the six lessons covering its setup, configuration, and advanced features.",
            category = "AI Tools",
            lessons = listOf(
                Lesson(
                    id = "ai-tools-continue-dev",
                    title = "Continue",
                    durationSeconds = 76,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-tools/continue-dev/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Continue, the open-source AI coding assistant, and the six lessons covering its setup, configuration, and advanced features.",
                ),
            ),
        ),
        Course(
            id = "ai-tools-cline-vscode",
            title = "Cline (VS Code)",
            description = "An overview of Cline, an autonomous AI coding agent for VS Code, and the six lessons that teach you to use it effectively.",
            category = "AI Tools",
            lessons = listOf(
                Lesson(
                    id = "ai-tools-cline-vscode",
                    title = "Cline (VS Code)",
                    durationSeconds = 76,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-tools/cline-vscode/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Cline, an autonomous AI coding agent for VS Code, and the six lessons that teach you to use it effectively.",
                ),
            ),
        ),
        Course(
            id = "ai-models-gpt-5",
            title = "GPT-5",
            description = "An overview of GPT-5, covering its capabilities, context window, multimodal inputs, tool use, pricing, and how to use it in production.",
            category = "AI Models",
            lessons = listOf(
                Lesson(
                    id = "ai-models-gpt-5",
                    title = "GPT-5",
                    durationSeconds = 82,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-models/gpt-5/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of GPT-5, covering its capabilities, context window, multimodal inputs, tool use, pricing, and how to use it in production.",
                ),
            ),
        ),
        Course(
            id = "ai-models-gpt-4o",
            title = "GPT-4o",
            description = "An overview of GPT-4o, OpenAI's omni-modal model, and the six lessons covering its capabilities from vision and audio to production deployment.",
            category = "AI Models",
            lessons = listOf(
                Lesson(
                    id = "ai-models-gpt-4o",
                    title = "GPT-4o",
                    durationSeconds = 87,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-models/gpt-4o/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of GPT-4o, OpenAI's omni-modal model, and the six lessons covering its capabilities from vision and audio to production deployment.",
                ),
            ),
        ),
        Course(
            id = "ai-models-claude-opus-4-7",
            title = "Claude Opus 4.7",
            description = "An overview of Claude Opus 4.7, Anthropic's flagship model, and the six lessons covering its reasoning, coding, agent, and production capabilities.",
            category = "AI Models",
            lessons = listOf(
                Lesson(
                    id = "ai-models-claude-opus-4-7",
                    title = "Claude Opus 4.7",
                    durationSeconds = 80,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-models/claude-opus-4-7/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Claude Opus 4.7, Anthropic's flagship model, and the six lessons covering its reasoning, coding, agent, and production capabilities.",
                ),
            ),
        ),
        Course(
            id = "ai-models-claude-sonnet-4-6",
            title = "Claude Sonnet 4.6",
            description = "An overview of Claude Sonnet 4.6 as Anthropic's balanced model and what its six-lesson learning path covers.",
            category = "AI Models",
            lessons = listOf(
                Lesson(
                    id = "ai-models-claude-sonnet-4-6",
                    title = "Claude Sonnet 4.6",
                    durationSeconds = 77,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-models/claude-sonnet-4-6/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Claude Sonnet 4.6 as Anthropic's balanced model and what its six-lesson learning path covers.",
                ),
            ),
        ),
        Course(
            id = "ai-models-claude-haiku-4-5",
            title = "Claude Haiku 4.5",
            description = "An overview of Claude Haiku 4.5, Anthropic's fast and inexpensive model, and how to deploy it for high-throughput, low-cost workloads.",
            category = "AI Models",
            lessons = listOf(
                Lesson(
                    id = "ai-models-claude-haiku-4-5",
                    title = "Claude Haiku 4.5",
                    durationSeconds = 80,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-models/claude-haiku-4-5/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Claude Haiku 4.5, Anthropic's fast and inexpensive model, and how to deploy it for high-throughput, low-cost workloads.",
                ),
            ),
        ),
        Course(
            id = "ai-models-gemini-2-5-pro",
            title = "Gemini 2.5 Pro",
            description = "An overview of Gemini 2.5 Pro, Google's flagship long-context, multimodal AI model, and the six lessons that teach how to use it.",
            category = "AI Models",
            lessons = listOf(
                Lesson(
                    id = "ai-models-gemini-2-5-pro",
                    title = "Gemini 2.5 Pro",
                    durationSeconds = 80,
                    audioUrl = "",
                    visualContentUrl = "https://www.lillytechsystems.com/ai-models/gemini-2-5-pro/index.html",
                    isAutomotiveSafe = true,
                    audioSummary = "An overview of Gemini 2.5 Pro, Google's flagship long-context, multimodal AI model, and the six lessons that teach how to use it.",
                ),
            ),
        ),
    )
}
