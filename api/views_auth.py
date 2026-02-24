from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.views.decorators.csrf import csrf_exempt

from .utils import parse_json, error
from .auth import create_access_token, create_refresh_token, decode_token, jwt_required

@csrf_exempt
@require_http_methods(["POST"])
def register(request):
    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)

    username = (data.get("username") or "").strip()
    email = (data.get("email") or "").strip()
    password = data.get("password") or ""

    if not username or not email or not password:
        return error("Missing fields", 400)

    if User.objects.filter(username=username).exists():
        return error("Username already exists", 409)

    if User.objects.filter(email=email).exists():
        return error("Email already exists", 409)

    user = User.objects.create_user(username=username, email=email, password=password)

    return JsonResponse(
        {"id": user.id, "username": user.username, "email": user.email},
        status=201
    )


@csrf_exempt
@require_http_methods(["POST"])
def login(request):
    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)

    username = data.get("username")
    password = data.get("password")

    user = authenticate(request, username=username, password=password)
    if not user:
        return error("Invalid credentials", 401)

    access = create_access_token(user.id)
    refresh = create_refresh_token(user.id)

    return JsonResponse({"access": access, "refresh": refresh})


@csrf_exempt
@require_http_methods(["POST"])
def refresh(request):
    data = parse_json(request)
    if data is None:
        return error("Invalid JSON", 400)

    token = data.get("refresh")
    if not token:
        return error("Missing refresh token", 400)

    import jwt
    try:
        payload = decode_token(token)
    except jwt.ExpiredSignatureError:
        return error("Refresh token expired", 401)
    except jwt.InvalidTokenError:
        return error("Invalid token", 401)

    if payload.get("type") != "refresh":
        return error("Invalid token type", 401)

    user_id = int(payload.get("sub"))
    access = create_access_token(user_id)

    return JsonResponse({"access": access})


@require_http_methods(["GET"])
@jwt_required
def me(request):
    u = request.user
    return JsonResponse({
        "id": u.id,
        "username": u.username,
        "email": u.email
    })