import json
from django.http import JsonResponse


def parse_json(request):
    try:
        if not request.body:
            return {}
        return json.loads(request.body.decode("utf-8"))
    except json.JSONDecodeError:
        return None


def error(message, status=400):
    return JsonResponse({"error": message}, status=status)