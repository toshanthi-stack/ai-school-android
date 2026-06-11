import SwiftUI

/// A lesson. Audio-first: the adapted narration plays for every lesson. When the
/// source lesson has code or steps, a "read the full lesson" view opens the real
/// web tutorial (a phone can show code; the car cannot, so it stays audio-only).
struct LessonView: View {
    let course: Course
    let lesson: Lesson
    @StateObject private var audio = AudioPlayer()
    @State private var showReader = false

    var body: some View {
        audioBody
            .background(Brand.bg.ignoresSafeArea())
            .navigationTitle(lesson.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbarColorScheme(.dark, for: .navigationBar)
            .onAppear { audio.load(lesson: lesson) }
            .onDisappear { audio.stop() }
            .sheet(isPresented: $showReader) { readerSheet }
    }

    private var accent: Color { Brand.accent(for: course.category) }

    private var audioBody: some View {
        VStack(spacing: 0) {
            RoundedRectangle(cornerRadius: 28)
                .fill(LinearGradient(colors: [Brand.primary, accent],
                                     startPoint: .topLeading, endPoint: .bottomTrailing))
                .frame(width: 180, height: 180)
                .overlay(
                    Image(systemName: "waveform")
                        .font(.system(size: 56))
                        .foregroundStyle(.white)
                )
                .padding(.top, 28)

            HStack(spacing: 8) {
                Text(course.category.uppercased())
                    .font(.caption.weight(.bold))
                    .foregroundStyle(accent)
                if lesson.isCodeHeavy {
                    Text("CODE")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(Brand.bg)
                        .padding(.horizontal, 6).padding(.vertical, 2)
                        .background(accent, in: Capsule())
                }
            }
            .padding(.top, 20)

            Text(lesson.title)
                .font(.title3.weight(.bold))
                .foregroundStyle(Brand.text)
                .multilineTextAlignment(.center)
                .padding(.top, 8)
                .padding(.horizontal, 24)

            Text("\(course.title) · \(lesson.durationMinutes) min · Audio")
                .font(.subheadline)
                .foregroundStyle(Brand.textDim)
                .padding(.top, 6)

            Text(lesson.audioSummary)
                .font(.callout)
                .foregroundStyle(Brand.textDim)
                .multilineTextAlignment(.center)
                .padding(.top, 16)
                .padding(.horizontal, 28)

            Spacer()

            VStack(spacing: 6) {
                ProgressView(value: audio.progress)
                    .tint(accent)
                HStack {
                    Text(timeString(audio.currentTime))
                    Spacer()
                    Text(timeString(audio.duration))
                }
                .font(.caption)
                .foregroundStyle(Brand.textDim)
            }
            .padding(.horizontal, 28)

            Button {
                audio.toggle()
            } label: {
                Image(systemName: audio.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                    .font(.system(size: 72))
                    .foregroundStyle(accent)
            }
            .padding(.top, 20)
            .padding(.bottom, lesson.hasReadView ? 12 : 28)

            if lesson.hasReadView {
                readButton
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
            }
        }
        .frame(maxWidth: .infinity)
    }

    private var readButton: some View {
        Button { showReader = true } label: {
            HStack(spacing: 8) {
                Image(systemName: lesson.isCodeHeavy
                      ? "chevron.left.forward.slash.chevron.right" : "doc.text")
                Text(lesson.isCodeHeavy ? "View the full lesson & code" : "Read the full lesson")
                    .fontWeight(.semibold)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .foregroundStyle(lesson.isCodeHeavy ? Brand.bg : accent)
            .background(
                lesson.isCodeHeavy
                ? AnyShapeStyle(accent)
                : AnyShapeStyle(accent.opacity(0.14)),
                in: RoundedRectangle(cornerRadius: 14, style: .continuous)
            )
        }
    }

    @ViewBuilder
    private var readerSheet: some View {
        if let url = URL(string: lesson.visualContentUrl ?? "") {
            NavigationStack {
                LessonWebView(url: url)
                    .ignoresSafeArea(edges: .bottom)
                    .navigationTitle("Full lesson")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .topBarTrailing) {
                            Button("Done") { showReader = false }
                        }
                    }
            }
            .preferredColorScheme(.dark)
        }
    }
}
