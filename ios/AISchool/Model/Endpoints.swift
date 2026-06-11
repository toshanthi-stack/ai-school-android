import Foundation

/// Endpoints of the live AI School production ecosystem
/// (lillytechsystems.com/ai-school).
enum Endpoints {
    static let baseURL = "https://www.lillytechsystems.com/ai-school/"
    static let indexPage = baseURL + "index.html"
    static let syllabusJSON = baseURL + "syllabus.json"
    static let audioBase = baseURL + "audio/"

    static var website: URL { URL(string: baseURL)! }

    /// Lilly Tech Systems (the company behind AI School).
    static var company: URL { URL(string: "https://www.lillytechsystems.com")! }

    /// Live topic page on the production site, e.g.
    /// `liveTopicPage("ai-prompts")` -> `.../ai-prompts/index.html`.
    static func liveTopicPage(_ slug: String) -> String {
        "https://www.lillytechsystems.com/\(slug)/index.html"
    }
}
