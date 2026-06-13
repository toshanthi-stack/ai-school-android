import SwiftUI

/// Home (catalog root): the top-level learning tracks, so the catalog opens
/// collapsed and the user picks a track first, then drills into its courses,
/// then lessons. The large, centered "ai school" lockup is pinned (stays while
/// the list scrolls); Lilly Tech Systems is shown with its flower logo footer.
struct CourseListView: View {
    @StateObject private var store = SyllabusStore()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                brandHeader

                if store.isLoading {
                    Spacer()
                    ProgressView().tint(Brand.primary)
                    Spacer()
                } else {
                    List {
                        Text("Choose a track to start learning.")
                            .font(.subheadline)
                            .foregroundStyle(Brand.textDim)
                            .plainRow(top: 4, bottom: 4)

                        ForEach(store.orderedCategories, id: \.self) { category in
                            NavigationLink(value: category) {
                                CategoryCard(category: category, courses: store.courses(in: category))
                            }
                            .plainRow(top: 6, bottom: 6)
                        }

                        footer
                            .plainRow(top: 32, bottom: 12)
                    }
                    .listStyle(.plain)
                    .scrollContentBackground(.hidden)
                }
            }
            .background(Brand.bg.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: String.self) { category in
                CategoryCoursesView(category: category, store: store)
            }
            .navigationDestination(for: Course.self) { CourseDetailView(course: $0) }
            .navigationDestination(for: LessonRoute.self) {
                LessonView(course: $0.course, lesson: $0.lesson)
            }
        }
        .tint(Brand.primary)
        .task { await store.load() }
    }

    /// Large, centered, pinned AI School lockup.
    private var brandHeader: some View {
        VStack(spacing: 6) {
            Image("BrandMark")
                .resizable()
                .frame(width: 70, height: 70)
                .clipShape(RoundedRectangle(cornerRadius: 17, style: .continuous))
            Text("ai school")
                .font(.system(size: 44, weight: .bold))
                .foregroundStyle(Brand.text)
            Link(destination: Endpoints.website) {
                HStack(spacing: 4) {
                    Text("Open AI School · lillytechsystems.com")
                        .font(.subheadline)
                    Image(systemName: "arrow.up.right.square")
                        .font(.system(size: 12))
                }
                .foregroundStyle(Brand.primary)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 8)
        .padding(.bottom, 14)
    }

    private var footer: some View {
        VStack(spacing: 10) {
            Text("An AI School product")
                .font(.caption)
                .foregroundStyle(Brand.textDim)
            Link(destination: Endpoints.company) {
                Image("LillyTechLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 96)
            }
        }
        .frame(maxWidth: .infinity)
    }
}

/// Courses within one track, reached from the home. Standard nav bar with the
/// track title and a back button.
struct CategoryCoursesView: View {
    let category: String
    @ObservedObject var store: SyllabusStore

    var body: some View {
        List {
            ForEach(store.courses(in: category)) { course in
                NavigationLink(value: course) {
                    CourseCard(course: course)
                }
                .plainRow(top: 6, bottom: 6)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .background(Brand.bg.ignoresSafeArea())
        .navigationTitle(category)
        .navigationBarTitleDisplayMode(.inline)
    }
}

/// Shared row styling: transparent background, no separators, edge-to-edge with
/// brand horizontal padding.
private extension View {
    func plainRow(top: CGFloat = 0, bottom: CGFloat = 0) -> some View {
        self
            .listRowBackground(Color.clear)
            .listRowSeparator(.hidden)
            .listRowInsets(EdgeInsets(top: top, leading: 16, bottom: bottom, trailing: 16))
    }
}

private struct CategoryCard: View {
    let category: String
    let courses: [Course]

    private var lessonCount: Int { courses.reduce(0) { $0 + $1.lessons.count } }

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: Brand.icon(for: category))
                .font(.title2)
                .foregroundStyle(Brand.accent(for: category))
                .frame(width: 32)
            VStack(alignment: .leading, spacing: 4) {
                Text(category)
                    .font(.headline)
                    .foregroundStyle(Brand.secondary)
                Text("\(courses.count) \(courses.count == 1 ? "course" : "courses") · \(lessonCount) lessons")
                    .font(.caption)
                    .foregroundStyle(Brand.textDim)
            }
            Spacer()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Brand.card, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Brand.border, lineWidth: 1)
        )
    }
}

private struct CourseCard: View {
    let course: Course

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(course.title)
                .font(.headline)
                .foregroundStyle(Brand.secondary)
            Text(course.description)
                .font(.subheadline)
                .foregroundStyle(Brand.text)
                .fixedSize(horizontal: false, vertical: true)
            Text("\(course.lessons.count) \(course.lessons.count == 1 ? "lesson" : "lessons") · \(course.totalDurationMinutes) min")
                .font(.caption)
                .foregroundStyle(Brand.textDim)
                .padding(.top, 2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Brand.card, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Brand.border, lineWidth: 1)
        )
    }
}
