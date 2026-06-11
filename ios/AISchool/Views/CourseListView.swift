import SwiftUI

/// The catalog: courses grouped by pillar, with the AI School brand mark, a link
/// to the website, and Lilly Tech Systems attribution. Root of the navigation
/// stack.
struct CourseListView: View {
    @StateObject private var store = SyllabusStore()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    header
                    if store.isLoading {
                        ProgressView()
                            .tint(Brand.primary)
                            .frame(maxWidth: .infinity)
                            .padding(.top, 80)
                    } else {
                        ForEach(store.orderedCategories, id: \.self) { category in
                            pillarSection(category)
                        }
                        footer
                    }
                }
                .padding(.bottom, 24)
            }
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
                    Text("Live syllabus · lillytechsystems.com")
                        .font(.subheadline)
                    Image(systemName: "arrow.up.right.square")
                        .font(.system(size: 12))
                }
                .foregroundStyle(Brand.primary)
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 12)
        .padding(.bottom, 8)
    }

    private func pillarSection(_ category: String) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: Brand.icon(for: category))
                    .foregroundStyle(Brand.accent(for: category))
                Text(category)
                    .font(.headline)
                    .foregroundStyle(Brand.text)
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)

            ForEach(store.courses(in: category)) { course in
                NavigationLink(value: course) {
                    CourseCard(course: course)
                }
                .buttonStyle(.plain)
            }
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
        .padding(.top, 32)
        .padding(.bottom, 8)
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
            Text("\(course.lessons.count) lessons · \(course.totalDurationMinutes) min")
                .font(.caption)
                .foregroundStyle(Brand.textDim)
                .padding(.top, 2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Brand.card, in: RoundedCornerShape())
        .overlay(RoundedCornerShape().stroke(Brand.border, lineWidth: 1))
        .padding(.horizontal, 16)
    }
}

private struct RoundedCornerShape: InsettableShape {
    var inset: CGFloat = 0
    func path(in rect: CGRect) -> Path {
        Path(roundedRect: rect.insetBy(dx: inset, dy: inset), cornerRadius: 16)
    }
    func inset(by amount: CGFloat) -> some InsettableShape {
        var copy = self; copy.inset += amount; return copy
    }
}
