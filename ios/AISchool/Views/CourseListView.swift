import SwiftUI

/// The catalog: courses grouped by pillar, with the AI School brand mark, a link
/// to the website, and Lilly Tech Systems attribution. Root of the navigation
/// stack. Uses a List so scrolling is reliable with tappable navigation rows.
struct CourseListView: View {
    @StateObject private var store = SyllabusStore()

    var body: some View {
        NavigationStack {
            List {
                header
                    .plainRow(top: 12, bottom: 8)

                if store.isLoading {
                    ProgressView()
                        .tint(Brand.primary)
                        .frame(maxWidth: .infinity)
                        .padding(.top, 60)
                        .plainRow()
                } else {
                    ForEach(store.orderedCategories, id: \.self) { category in
                        pillarHeader(category)
                            .plainRow(top: 20, bottom: 4)
                        ForEach(store.courses(in: category)) { course in
                            NavigationLink(value: course) {
                                CourseCard(course: course)
                            }
                            .plainRow(top: 6, bottom: 6)
                        }
                    }
                    footer
                        .plainRow(top: 32, bottom: 12)
                }
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
            .background(Brand.bg.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: Course.self) { CourseDetailView(course: $0) }
            .navigationDestination(for: LessonRoute.self) {
                LessonView(course: $0.course, lesson: $0.lesson)
            }
        }
        .tint(Brand.primary)
        .task { await store.load() }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 12) {
                Image("BrandMark")
                    .resizable()
                    .frame(width: 42, height: 42)
                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                Text("AI School")
                    .font(.system(size: 30, weight: .bold))
                    .foregroundStyle(Brand.text)
            }
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
    }

    private func pillarHeader(_ category: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: Brand.icon(for: category))
                .foregroundStyle(Brand.accent(for: category))
            Text(category)
                .font(.headline)
                .foregroundStyle(Brand.text)
        }
    }

    private var footer: some View {
        VStack(spacing: 4) {
            Text("An AI School product")
                .font(.caption)
                .foregroundStyle(Brand.textDim)
            Link(destination: Endpoints.company) {
                Text("by Lilly Tech Systems")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(Brand.primary)
            }
        }
        .frame(maxWidth: .infinity)
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
