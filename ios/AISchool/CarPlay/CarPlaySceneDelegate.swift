import CarPlay

/// CarPlay entry point: the audio-only "in the car" surface, the iOS analog of
/// the Android Automotive media service. The driver browses pillars -> courses
/// -> lessons, every item is audio, and playback runs through
/// `CarPlayPlaybackController` and the system Now Playing template.
///
/// Wired in via the `CPTemplateApplicationSceneSessionRoleApplication` scene in
/// Info.plist; the SwiftUI app keeps serving the phone window scene.
@MainActor
final class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate {

    private var interfaceController: CPInterfaceController?
    private let store = SyllabusStore()

    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController
    ) {
        self.interfaceController = interfaceController
        CarPlayPlaybackController.shared.attach(interfaceController: interfaceController)

        // Show a placeholder while the syllabus loads (live feed -> bundled -> seed).
        let loading = CPListTemplate(
            title: "AI School",
            sections: [CPListSection(items: [CPListItem(text: "Loading lessons\u{2026}", detailText: nil)])]
        )
        interfaceController.setRootTemplate(loading, animated: false, completion: nil)

        Task {
            await store.load()
            let root = CarPlayCatalog.rootTemplate(store: store, interfaceController: interfaceController)
            interfaceController.setRootTemplate(root, animated: true, completion: nil)
        }
    }

    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didDisconnectInterfaceController interfaceController: CPInterfaceController
    ) {
        self.interfaceController = nil
        CarPlayPlaybackController.shared.detach()
    }
}

/// Builds the CarPlay browse hierarchy from the loaded syllabus. Audio-only:
/// only `isAutomotiveSafe` lessons are listed (code-heavy lessons live on the
/// phone), mirroring the Android `fetchAutomotiveSafeSyllabus` contract.
enum CarPlayCatalog {

    @MainActor
    static func rootTemplate(store: SyllabusStore, interfaceController: CPInterfaceController) -> CPListTemplate {
        var sections: [CPListSection] = []
        for category in store.orderedCategories {
            let items: [CPListItem] = store.courses(in: category).compactMap { course in
                let safe = course.lessons.filter(\.isAutomotiveSafe)
                guard !safe.isEmpty else { return nil }
                let item = CPListItem(
                    text: course.title,
                    detailText: "\(safe.count) audio lessons \u{00B7} \(course.totalDurationMinutes) min"
                )
                item.accessoryType = .disclosureIndicator
                item.handler = { _, completion in
                    let template = courseTemplate(course, interfaceController: interfaceController)
                    interfaceController.pushTemplate(template, animated: true, completion: nil)
                    completion()
                }
                return item
            }
            if !items.isEmpty {
                sections.append(CPListSection(items: items, header: category, sectionIndexTitle: nil))
            }
        }
        return CPListTemplate(title: "AI School", sections: sections)
    }

    @MainActor
    static func courseTemplate(_ course: Course, interfaceController: CPInterfaceController) -> CPListTemplate {
        let items: [CPListItem] = course.lessons.filter(\.isAutomotiveSafe).map { lesson in
            let item = CPListItem(
                text: lesson.title,
                detailText: "\(lesson.durationMinutes) min \u{00B7} \(lesson.audioSummary)"
            )
            item.handler = { _, completion in
                CarPlayPlaybackController.shared.play(lesson: lesson, in: course)
                completion()
            }
            return item
        }
        return CPListTemplate(title: course.title, sections: [CPListSection(items: items)])
    }
}
