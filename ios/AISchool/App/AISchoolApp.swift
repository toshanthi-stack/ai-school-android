import SwiftUI

@main
struct AISchoolApp: App {
    var body: some Scene {
        WindowGroup {
            CourseListView()
                .preferredColorScheme(.dark)
        }
    }
}
