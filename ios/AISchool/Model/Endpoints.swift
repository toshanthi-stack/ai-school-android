import Foundation

/// Endpoints of the live AI School production ecosystem
/// (lillytechsystems.com/ai-school).
enum Endpoints {
    static let baseURL = "https://www.lillytechsystems.com/ai-school/"
    static let indexPage = baseURL + "index.html"
    /// Content feed (syllabus + audio) hosted on GitHub Pages. Lessons carry
    /// absolute audio URLs into this same host. The website links above stay on
    /// lillytechsystems.com.
    static let feedBase = "https://toshanthi-stack.github.io/ai-school-feed/"
    static let syllabusJSON = feedBase + "syllabus.json"
    static let audioBase = feedBase + "audio/"

    static var website: URL { URL(string: baseURL)! }

    /// Lilly Tech Systems (the company behind AI School).
    static var company: URL { URL(string: "https://www.lillytechsystems.com")! }

    /// Live topic page on the production site, e.g.
    /// `liveTopicPage("ai-prompts")` -> `.../ai-prompts/index.html`.
    static func liveTopicPage(_ slug: String) -> String {
        "https://www.lillytechsystems.com/\(slug)/index.html"
    }
}
