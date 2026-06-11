import SwiftUI

/// A course: its description and the list of lessons. Each lesson pushes the
/// lesson screen.
struct CourseDetailView: View {
    let course: Course

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(course.description)
                    .font(.body)
                    .foregroundStyle(Brand.textDim)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                ForEach(course.lessons) { lesson in
                    NavigationLink(value: LessonRoute(course: course, lesson: lesson)) {
                        LessonRow(course: course, lesson: lesson)
                    }
                    .buttonStyle(.plain)
                }
                Spacer(minLength: 16)
            }
            .padding(.vertical, 8)
        }
        .background(Brand.bg.ignoresSafeArea())
        .navigationTitle(course.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
    }
}

private struct LessonRow: View {
    let course: Course
    let lesson: Lesson

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: lesson.hasVisualPayload ? "safari" : "waveform")
                .font(.system(size: 18))
                .foregroundStyle(Brand.accent(for: course.category))
                .frame(width: 44, height: 44)
                .background(Brand.accent(for: course.category).opacity(0.16), in: Circle())

            VStack(alignment: .leading, spacing: 3) {
                Text(lesson.title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(Brand.text)
                Text("\(lesson.hasVisualPayload ? "Interactive" : "Audio") · \(lesson.durationMinutes) min")
                    .font(.caption)
                    .foregroundStyle(Brand.textDim)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundStyle(Brand.textDim)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
    }
}
