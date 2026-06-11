import Foundation

/// A single unit of learning content. Mirrors the shared domain model used by
/// the Android flavors.
struct Lesson: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let durationSeconds: Int
    let audioUrl: String
    let visualContentUrl: String?
    let isAutomotiveSafe: Bool
    let audioSummary: String

    /// True when the lesson has a rich visual payload (interactive web lesson).
    var hasVisualPayload: Bool { !(visualContentUrl?.isEmpty ?? true) }

    var durationMinutes: Int { durationSeconds / 60 }
}

/// A course: an ordered set of lessons grouped under one pillar.
struct Course: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let description: String
    let category: String
    let lessons: [Lesson]

    var totalDurationSeconds: Int { lessons.reduce(0) { $0 + $1.durationSeconds } }
    var totalDurationMinutes: Int { totalDurationSeconds / 60 }
}

/// The three top-level pillars, in display order.
enum Pillars {
    static let generativeAI = "Generative AI Skills"
    static let infrastructure = "AI Infrastructure & Hardware"
    static let advancedTuning = "Advanced LLM Tuning"
    static let all = [generativeAI, infrastructure, advancedTuning]
}

/// Navigation value carrying both the lesson and its course (for accent color
/// and the "course title" subtitle on the lesson screen).
struct LessonRoute: Hashable {
    let course: Course
    let lesson: Lesson
}
