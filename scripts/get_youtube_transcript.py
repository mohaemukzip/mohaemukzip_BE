import sys
import json
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api._errors import (
    TranscriptsDisabled,
    NoTranscriptFound,
    VideoUnavailable
)

def main():
    if len(sys.argv) < 2:
        print("videoId is required", file=sys.stderr)
        sys.exit(1)

    video_id = sys.argv[1]

    try:
        # 한국어 우선, 없으면 영어
        transcript = YouTubeTranscriptApi.get_transcript(
            video_id,
            languages=["ko", "en"]
        )

        # transcript item:
        # { "text": "...", "start": 12.34, "duration": 3.21 }
        print(json.dumps(transcript, ensure_ascii=False))

    except (TranscriptsDisabled, NoTranscriptFound):
        print("Transcript not available", file=sys.stderr)
        sys.exit(2)

    except VideoUnavailable:
        print("Video unavailable", file=sys.stderr)
        sys.exit(3)

    except Exception as e:
        import traceback
        print(f"Unknown error: {e!s}", file=sys.stderr)
        traceback.print_exc(file=sys.stderr)
        sys.exit(99)


if __name__ == "__main__":
    main()
