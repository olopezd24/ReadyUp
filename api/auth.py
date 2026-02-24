import jwt
from datetime import datetime, timedelta, timezone
from django.conf import settings
from django.contrib.auth.models import User
from django.http import JsonResponse


def _now():
    return datetime.now(timezone.utc)


def create_token(*, user_id: int, token_type: str, exp: datetime) -> str:
    payload = {
        "sub": str(user_id),
        "type": token_type,        # "access" o "refresh"
        "iat": int(_now().timestamp()),
        "exp": int(exp.timestamp()),
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALG)


def create_access_token(user_id: int) -> str:
    exp = _now() + timedelta(minutes=getattr(settings, "JWT_ACCESS_EXP_MINUTES", 30))
    return create_token(user_id=user_id, token_type="access", exp=exp)


def create_refresh_token(user_id: int) -> str:
    exp = _now() + timedelta(days=getattr(settings, "JWT_REFRESH_EXP_DAYS", 14))
    return create_token(user_id=user_id, token_type="refresh", exp=exp)


def decode_token(token: str) -> dict:
    return jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALG])


def get_bearer_token(request):
    auth = request.headers.get("Authorization", "")
    if not auth.startswith("Bearer "):
        return None
    return auth.split(" ", 1)[1].strip()


def jwt_required(view_func):
    def _wrapped(request, *args, **kwargs):
        token = get_bearer_token(request)
        if not token:
            return JsonResponse({"error": "Missing Bearer token"}, status=401)

        try:
            payload = decode_token(token)
        except jwt.ExpiredSignatureError:
            return JsonResponse({"error": "Access token expired"}, status=401)
        except jwt.InvalidTokenError:
            return JsonResponse({"error": "Invalid token"}, status=401)

        if payload.get("type") != "access":
            return JsonResponse({"error": "Invalid token type"}, status=401)

        user_id = payload.get("sub")
        try:
            user = User.objects.get(id=int(user_id))
        except (User.DoesNotExist, ValueError, TypeError):
            return JsonResponse({"error": "User not found"}, status=401)

        request.user = user
        return view_func(request, *args, **kwargs)

    return _wrapped