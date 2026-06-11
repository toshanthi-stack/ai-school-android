import Foundation
import AVFoundation

/// Lightweight audio player for lesson narration. Prefers the bundled offline
/// narration (so playback works with no network, mirroring the Android demo
/// fallback) and falls back to the streamed `audioUrl`.
@MainActor
final class AudioPlayer: ObservableObject {
    @Published var isPlaying = false
    @Published var currentTime: Double = 0
    @Published var duration: Double = 0

    private var player: AVPlayer?
    private var timeObserver: Any?

    var progress: Double {
        guard duration > 0 else { return 0 }
        return min(currentTime / duration, 1)
    }

    /// Loads (and auto-plays) the narration for a lesson. `fallbackDuration` is
    /// the catalog duration, used until the asset reports its real length.
    func load(lesson: Lesson) {
        stop()
        duration = Double(lesson.durationSeconds)

        let url = Self.bundledNarrationURL(for: lesson.id) ?? URL(string: lesson.audioUrl)
        guard let url else { return }

        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio)
        try? AVAudioSession.sharedInstance().setActive(true)

        let item = AVPlayerItem(url: url)
        let avPlayer = AVPlayer(playerItem: item)
        player = avPlayer

        // Real asset duration once available.
        Task { [weak self] in
            if let assetDuration = try? await item.asset.load(.duration) {
                let seconds = CMTimeGetSeconds(assetDuration)
                if seconds.isFinite, seconds > 0 { self?.duration = seconds }
            }
        }

        let interval = CMTime(seconds: 0.25, preferredTimescale: 600)
        timeObserver = avPlayer.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            // Delivered on the main queue (queue: .main), so main-actor state is safe.
            MainActor.assumeIsolated {
                self?.currentTime = CMTimeGetSeconds(time)
            }
        }

        avPlayer.play()
        isPlaying = true
    }

    func toggle() {
        guard let player else { return }
        if isPlaying {
            player.pause()
        } else {
            if progress >= 1 { player.seek(to: .zero) }
            player.play()
        }
        isPlaying.toggle()
    }

    func stop() {
        if let timeObserver { player?.removeTimeObserver(timeObserver) }
        timeObserver = nil
        player?.pause()
        player = nil
        isPlaying = false
        currentTime = 0
        duration = 0
    }

    /// Bundled narration file for a lesson id (`genai-101` -> `genai_101.m4a`).
    static func bundledNarrationURL(for lessonId: String) -> URL? {
        let name = lessonId.replacingOccurrences(of: "-", with: "_")
        return Bundle.main.url(forResource: name, withExtension: "m4a")
    }
}
