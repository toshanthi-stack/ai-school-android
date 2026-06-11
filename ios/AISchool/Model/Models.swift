import Foundation

/// A single unit of learning content. Audio-first: every lesson has a narration
/// adapted from the source web tutorial by the content pipeline. `visualContentUrl`
/// is the real web lesson for the "read the full lesson" view (code, steps).
struct Lesson: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let durationSeconds: Int
    let audioUrl: String
    let visualContentUrl: String?
    let isAutomotiveSafe: Bool
    let audioSummary: String
    /// "conceptual" (audio is the whole lesson) or "code" (audio is an overview,
    /// the real code/steps live in the read view). Defaults so older feeds decode.
    var contentType: String = "conceptual"

    /// A readable web lesson exists (the "read the full lesson" view).
    var hasReadView: Bool { !(visualContentUrl?.isEmpty ?? true) }

    /// Code/steps heavy: reading is recommended on a phone; the car gets audio only.
    var isCodeHeavy: Bool { contentType == "code" }

    /// Rounded-up minutes, at least 1 (so short real lessons do not show "0 min").
    var durationMinutes: Int { max(1, Int((Double(durationSeconds) / 60).rounded(.up))) }

    enum CodingKeys: String, CodingKey {
        case id, title, durationSeconds, audioUrl, visualContentUrl
        case isAutomotiveSafe, audioSummary, contentType
    }

    init(id: String, title: String, durationSeconds: Int, audioUrl: String,
         visualContentUrl: String?, isAutomotiveSafe: Bool, audioSummary: String,
         contentType: String = "conceptual") {
        self.id = id; self.title = title; self.durationSeconds = durationSeconds
        self.audioUrl = audioUrl; self.visualContentUrl = visualContentUrl
        self.isAutomotiveSafe = isAutomotiveSafe; self.audioSummary = audioSummary
        self.contentType = contentType
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        title = try c.decode(String.self, forKey: .title)
        durationSeconds = try c.decode(Int.self, forKey: .durationSeconds)
        audioUrl = try c.decode(String.self, forKey: .audioUrl)
        visualContentUrl = try c.decodeIfPresent(String.self, forKey: .visualContentUrl)
        isAutomotiveSafe = try c.decode(Bool.self, forKey: .isAutomotiveSafe)
        audioSummary = try c.decode(String.self, forKey: .audioSummary)
        contentType = try c.decodeIfPresent(String.self, forKey: .contentType) ?? "conceptual"
    }
}

/// A course: an ordered set of lessons grouped under one pillar.
struct Course: Codable, Identifiable, Hashable {
    let id: String
    let title: String
    let description: String
    let category: String
    let lessons: [Lesson]

    var totalDurationSeconds: Int { lessons.reduce(0) { $0 + $1.durationSeconds } }
    var totalDurationMinutes: Int { max(1, Int((Double(totalDurationSeconds) / 60).rounded(.up))) }
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
