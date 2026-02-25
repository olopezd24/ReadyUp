from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.db.models import Q
from .models import Game, Review


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

@require_http_methods(["GET"])
def get_game_detail(request, game_id):
    try:
        game = Game.objects.get(id=game_id)
    except Game.DoesNotExist:
        return JsonResponse({"error": "Game not found"}, status=404)

    return JsonResponse({
        "id": game.id,
        "title": game.title,
        "description": game.description,
        "releaseDate": game.release_date.isoformat() if game.release_date else None,
        "genres": [game.genre] if game.genre else [],
        "platforms": [game.platform] if game.platform else [],
        "coverUrl": game.cover_url,
    })

@require_http_methods(["GET"])
def list_game_reviews(request, game_id):
    from .models import Game
    if not Game.objects.filter(id=game_id).exists():
        return JsonResponse({"error": "Game not found"}, status=404)

    qs = Review.objects.filter(game_id=game_id).select_related("user")

    sort = request.GET.get("sort")
    if sort == "top":
        qs = qs.order_by("-rating")
    else:
        qs = qs.order_by("-updated_at")

    try:
        limit = int(request.GET.get("limit", 20))
        offset = int(request.GET.get("offset", 0))
    except ValueError:
        return JsonResponse({"error": "Invalid pagination params"}, status=400)

    if limit < 1 or limit > 100 or offset < 0:
        return JsonResponse({"error": "Invalid limit/offset"}, status=400)

    count = qs.count()
    qs = qs[offset:offset + limit]

    results = []
    for r in qs:
        results.append({
            "id": r.id,
            "user": {
                "id": r.user.id,
                "username": r.user.username,
            },
            "rating": r.rating,
            "text": r.text,
            "updated_at": r.updated_at.isoformat()
        })
    return JsonResponse({
        "count": count,
        "results": results
    })