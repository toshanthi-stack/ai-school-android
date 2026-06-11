import SwiftUI

/// A lesson. Interactive lessons render the live web page; audio-only lessons
/// render a branded player with the bundled narration.
struct LessonView: View {
    let course: Course
    let lesson: Lesson
    @StateObject private var audio = AudioPlayer()

    var body: some View {
        Group {
            if lesson.hasVisualPayload, let url = URL(string: lesson.visualContentUrl ?? "") {
                LessonWebView(url: url)
                    .ignoresSafeArea(edges: .bottom)
            } else {
                audioBody
            }
        }
        .background(Brand.bg.ignoresSafeArea())
        .navigationTitle(lesson.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .onAppear { if !lesson.hasVisualPayload { audio.load(lesson: lesson) } }
        .onDisappear { audio.stop() }
    }

    private var accent: Color { Brand.accent(for: course.category) }

    private var audioBody: some View {
        VStack(spacing: 0) {
            RoundedRectangle(cornerRadius: 28)
                .fill(LinearGradient(colors: [Brand.primary, accent],
                                     startPoint: .topLeading, endPoint: .bottomTrailing))
                .frame(width: 200, height: 200)
                .overlay(
                    Image(systemName: "waveform")
                        .font(.system(size: 64))
                        .foregroundStyle(.white)
                )
                .padding(.top, 36)

            Text(course.category.uppercased())
                .font(.caption.weight(.bold))
                .foregroundStyle(accent)
                .padding(.top, 24)

            Text(lesson.title)
                .font(.title2.weight(.bold))
                .foregroundStyle(Brand.text)
                .multilineTextAlignment(.center)
                .padding(.top, 8)
                .padding(.horizontal, 24)

            Text("\(course.title) · \(lesson.durationMinutes) min")
                .font(.subheadline)
                .foregroundStyle(Brand.textDim)
                .padding(.top, 6)

            Text(lesson.audioSummary)
                .font(.callout)
                .foregroundStyle(Brand.textDim)
                .multilineTextAlignment(.center)
                .padding(.top, 20)
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
                    .font(.system(size: 76))
                    .foregroundStyle(accent)
            }
            .padding(.vertical, 28)
        }
        .frame(maxWidth: .infinity)
    }
}
