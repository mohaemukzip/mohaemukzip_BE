import sys
import json
from youtube_transcript_api import YouTubeTranscriptApi, TranscriptsDisabled, NoTranscriptFound, VideoUnavailable
from youtube_transcript_api._errors import RequestBlocked, CouldNotRetrieveTranscript

def main():
    video_id = sys.argv[1]
    # 언어 우선순위: ko -> en (필요시 추가)
    languages = ["ko", "en"]

    try:
        transcript = YouTubeTranscriptApi.get_transcript(video_id, languages=languages)
        # transcript: [{"text": "...", "start": 12.3, "duration": 3.2}, ...]
        # 우리가 Gemini에게 줄 건 큰 텍스트 + 시작시간(초)
        items = [{
            "text": t.get("text", ""),
            "start": int(float(t.get("start", 0)))
        } for t in transcript]

        print(json.dumps({"ok": True, "items": items}, ensure_ascii=False))
    except (TranscriptsDisabled, NoTranscriptFound, VideoUnavailable) as e:
        print(json.dumps({"ok": False, "error": type(e).__name__, "message": str(e)}, ensure_ascii=False))
        sys.exit(2)
    except RequestBlocked as e:
        print(json.dumps({"ok": False, "error": "RequestBlocked", "message": str(e)}, ensure_ascii=False))
        sys.exit(3)
    except CouldNotRetrieveTranscript as e:
        print(json.dumps({"ok": False, "error": "CouldNotRetrieveTranscript", "message": str(e)}, ensure_ascii=False))
        sys.exit(4)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"ok": False, "error": "MissingArgument", "message": "videoId required"}, ensure_ascii=False))
        sys.exit(1)
    main()