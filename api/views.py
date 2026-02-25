from django.contrib.staticfiles.storage import staticfiles_storage
from django.db import IntegrityError
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods
from django.db.models import Q, Avg

from .auth import jwt_required
from .models import Game, Review, UserGameStatus
from .utils import parse_json, error


def recalc_game_avg_rating(game_id: int) -> None:
    avg = Review.objects.filter(game_id=game_id).aggregate(a=Avg("rating"))["a"]
    Game.objects.filter(id=game_id).update(avg_rating=float(avg or 0))

VALID_STATUSES = {c[0] for c in UserGameStatus.Status.choices}

def validate_status(value):
    if value not in VALID_STATUSES:
        return False
    return True


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

@csrf_exempt
@require_http_methods(["POST"])
@jwt_required
def create_my_review(request, game_id):
    if not Game.objects.filter(id=game_id).exists():
        return JsonResponse({"error": "Game not found"}, status=404)

    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)

    rating = data.get("rating")
    text = data.get("text", "")

    if rating is None:
        return error("Missing field: rating", 400)

    try:
        rating = int(rating)
    except (TypeError, ValueError):
        return error("rating must be an integer", 400)

    if rating < 1 or rating > 10:
        return error("rating must be between 1 and 10", 400)

    try:
        review = Review.objects.create(
            user=request.user,
            game_id=game_id,
            rating=rating,
            text=text or "",
        )
    except IntegrityError:
        return JsonResponse({"error": "Review already exists"}, status=409)

    recalc_game_avg_rating(game_id)

    return JsonResponse({
        "id": review.id,
        "rating": review.rating,
        "text": review.text,
        "updated_at": review.updated_at.isoformat()
    }, status=201)

@csrf_exempt
@require_http_methods(["PUT"])
@jwt_required
def update_my_review(request, game_id):
    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)

    try:
        review = Review.objects.get(game_id=game_id, user=request.user)
    except Review.DoesNotExist:
        return JsonResponse({"error": "Review not found"}, status=404)

    if "rating" in data:
        try:
            rating = int(data["rating"])
        except (TypeError, ValueError):
            return error("rating must be an integer", 400)
        if rating < 1 or rating > 10:
            return error("rating must be between 1 and 10", 400)
        review.rating = rating

    if "text" in data:
        review.text = data["text"] or ""

    review.save()
    recalc_game_avg_rating(game_id)

    return JsonResponse({
        "id": review.id,
        "rating": review.rating,
        "text": review.text,
        "updated_at": review.updated_at.isoformat(),
    }, status=200)

@csrf_exempt
@require_http_methods(["DELETE"])
@jwt_required
def delete_my_review(request, game_id):
    try:
        review = Review.objects.get(game_id=game_id, user=request.user)
    except Review.DoesNotExist:
        return JsonResponse({"error": "Review not found"}, status=404)

    review.delete()
    recalc_game_avg_rating(game_id)

    return JsonResponse({}, status=204)

@require_http_methods(["GET"])
@jwt_required
def get_my_reviews(request, game_id):
    try:
        review = Review.objects.get(game_id=game_id, user=request.user)
    except Review.DoesNotExist:
        return JsonResponse({"error": "Review not found"}, status=404)

    return JsonResponse({
        "id": review.id,
        "rating": review.rating,
        "text": review.text,
        "updated_at": review.updated_at.isoformat(),
    }, status=200)

@csrf_exempt
@require_http_methods(["GET", "POST", "PUT", "DELETE"])
@jwt_required
def my_review_endpoint(request, game_id):
    if request.method == "GET":
        return get_my_reviews(request, game_id)
    if request.method == "POST":
        return create_my_review(request, game_id)
    if request.method == "PUT":
        return update_my_review(request, game_id)
    if request.method == "DELETE":
        return delete_my_review(request, game_id)

@require_http_methods(["GET"])
@jwt_required
def list_my_reviews(request):
    qs = Review.objects.filter(user=request.user).select_related("game")

    try:
        limit = int(request.GET.get("limit", 20))
        offset = int(request.GET.get("offset", 0))
    except ValueError:
        return JsonResponse({"error": "Invalid parameters"}, status=400)

    if limit < 1 or limit > 100 or offset < 0:
        return JsonResponse({"error": "Invalid limit/offset"}, status=400)

    count = qs.count()
    qs = qs.order_by("-updated_at")[offset:offset+limit]

    results = []
    for r in qs:
        results.append({
            "game": {
                "id": r.game.id,
                "title": r.game.title,
            },
            "rating": r.rating,
            "text": r.text,
            "updated_at": r.updated_at.isoformat(),
        })

    return JsonResponse({
        "count": count,
        "results": results
    })

@require_http_methods(["GET"])
@jwt_required
def list_my_status(request):
    status = request.GET.get("status")
    if not status or not validate_status(status):
        return JsonResponse({"error": "Invalid status"}, status=400)

    qs = (
        UserGameStatus.objects
        .filter(user=request.user, status=status)
        .select_related("game")
        .order_by("-updated_at")
    )

    try:
        limit = int(request.GET.get("limit", 20))
        offset = int(request.GET.get("offset", 0))
    except ValueError:
        return JsonResponse({"error": "Invalid parameters"}, status=400)

    if limit < 1 or limit > 100 or offset < 0:
        return JsonResponse({"error": "Invalid limit/offset"}, status=400)

    count = qs.count()
    qs = qs[offset:offset+limit]

    results = []
    for s in qs:
        results.append({
            "game": {
                "id": s.game.id,
                "title": s.game.title,
                "cover_url": s.game.cover_url,
            },
            "status": s.status,
            "updatedAt": s.updated_at.isoformat(),
        })

    return JsonResponse({"count": count, "results": results}, status=200)

@csrf_exempt
@require_http_methods(["POST"])
@jwt_required
def add_my_status(request):
    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)
    game_id = data.get("gameId")
    status = data.get("status")

    if game_id is None or status is None:
        return error("Missing fields: gameId, status", 400)

    try:
        game_id = int(game_id)
    except (TypeError, ValueError):
        return error("gameId must be an integer", 400)

    if not validate_status(status):
        return JsonResponse({"error": "Invalid status"}, status=400)

    if not Game.objects.filter(id=game_id).exists():
        return JsonResponse({"error": "Game not found"}, status=404)

    try:
        obj = UserGameStatus.objects.create(
            user=request.user,
            game_id=game_id,
            status=status
        )
    except IntegrityError:
        return JsonResponse({"error": "Status already exists for this game"}, status=400)

    return JsonResponse({
        "gameId": obj.game_id,
        "status": obj.status,
        "updatedAt": obj.updated_at.isoformat(),
    }, status=201)

@csrf_exempt
@require_http_methods(["PUT"])
@jwt_required
def update_my_status(request, game_id):
    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)

    status = data.get("status")
    if status is None:
        return error("Missing fields: status", 400)

    if not validate_status(status):
        return JsonResponse({"error": "Invalid status"}, status=400)

    try:
        obj = UserGameStatus.objects.get(user=request.user, game_id=game_id)
    except UserGameStatus.DoesNotExist:
        return JsonResponse({"error": "Status not found"}, status=404)

    obj.status = status
    obj.save(update_fields=["status", "updated_at"])

    return JsonResponse({
        "gameId": obj.game_id,
        "status": obj.status,
        "updatedAt": obj.updated_at.isoformat(),
    }, status=200)

@csrf_exempt
@require_http_methods(["DELETE"])
@jwt_required
def delete_my_status(request, game_id):
    try:
        obj = UserGameStatus.objects.get(user=request.user, game_id=game_id)
    except UserGameStatus.DoesNotExist:
        return JsonResponse({"error": "Status not found"}, status=404)

    obj.delete()
    return JsonResponse({}, status=204)

@csrf_exempt
@require_http_methods(["GET", "POST"])
@jwt_required
def me_status_endpoint(request):
    if request.method == "GET":
        return list_my_status(request)
    return add_my_status(request)

@csrf_exempt
@require_http_methods(["PUT", "DELETE"])
@jwt_required
def me_status_game_endpoint(request, game_id):
    if request.method == "PUT":
        return update_my_status(request, game_id)
    return delete_my_status(request, game_id)
