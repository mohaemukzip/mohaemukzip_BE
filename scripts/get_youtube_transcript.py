import os
import sys
import json
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api.proxies import GenericProxyConfig
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
        proxy_url = os.environ.get("HTTPS_PROXY")
        if proxy_url:
            api = YouTubeTranscriptApi(proxy_config=GenericProxyConfig(https_url=proxy_url))
        else:
            api = YouTubeTranscriptApi()
        result = api.fetch(video_id, languages=["ko", "en"])

        # FetchedTranscriptSnippet → dict 변환 (기존 JSON 포맷 유지)
        transcript = [
            {"text": s.text, "start": s.start, "duration": s.duration}
            for s in result
        ]

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
