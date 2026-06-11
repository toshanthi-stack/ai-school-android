import Foundation

/// Loads the syllabus: prefers the live `syllabus.json` feed, falls back to the
/// bundled `SeedSyllabus` when the network is unavailable or the response is not
/// the expected shape. Mirrors the Android client's resilience.
@MainActor
final class SyllabusStore: ObservableObject {
    @Published var courses: [Course] = []
    @Published var isLoading = true
    @Published var usingLiveFeed = false

    func load() async {
        isLoading = true
        defer { isLoading = false }
        do {
            guard let url = URL(string: Endpoints.syllabusJSON) else {
                courses = SeedSyllabus.courses; return
            }
            var request = URLRequest(url: url)
            request.setValue("visual", forHTTPHeaderField: "X-AISchool-Payload")
            request.timeoutInterval = 15
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse, http.statusCode == 200 else {
                throw URLError(.badServerResponse)
            }
            let decoded = try JSONDecoder().decode([Course].self, from: data)
            if decoded.isEmpty {
                courses = SeedSyllabus.courses
            } else {
                courses = decoded
                usingLiveFeed = true
            }
        } catch {
            // Offline, unreachable, or unexpected shape: use the contract-of-record.
            courses = SeedSyllabus.courses
        }
    }

    /// Courses grouped by pillar, in the canonical pillar order.
    var orderedCategories: [String] {
        let present = Set(courses.map(\.category))
        let known = Pillars.all.filter { present.contains($0) }
        let extra = present.subtracting(Pillars.all).sorted()
        return known + extra
    }

    func courses(in category: String) -> [Course] {
        courses.filter { $0.category == category }
    }
}
