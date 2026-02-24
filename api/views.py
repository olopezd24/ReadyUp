from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.db.models import Q
from .models import Game


@require_http_methods(["GET"])
def list_games(request):
    qs = Game.objects.all()

    q = request.GET.get("q")
    if q:
        qs = qs.filter(title__icontains=q)

    genres = request.GET.get("genres")
    if genres:
        qs = qs.filter(genre__icontains=genres)

    platforms = request.GET.get("platforms")
    if platforms:
        qs = qs.filter(platform__icontains=platforms)

    sort = request.GET.get("sort")
    if sort == "new":
        qs = qs.order_by("-release_date")
    elif sort == "top":
        qs = qs.order_by("-avg_rating")
    elif sort == "popular":
        qs = qs.order_by("-created_at")

    try:
        limit = int(request.GET.get("limit", 20))
        offset = int(request.GET.get("offset", 0))
    except ValueError:
        return JsonResponse({"error": "Invalid pagination params"}, status=400)

    count = qs.count()
    qs = qs[offset:offset + limit]

    results = []
    for game in qs:
        results.append({
            "id": game.id,
            "title": game.title,
            "coverUrl": game.cover_url,
            "releaseDate": game.release_date,
            "avgRating": game.avg_rating,
        })

    return JsonResponse({
        "count": count,
        "limit": limit,
        "offset": offset,
        "results": results
    })