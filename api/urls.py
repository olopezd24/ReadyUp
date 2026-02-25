from django.urls import path
from . import views_auth, views

urlpatterns = [
    path("auth/register", views_auth.register),
    path("auth/login", views_auth.login),
    path("auth/refresh", views_auth.refresh),
    path("me", views_auth.me),
    path("games", views.list_games),
    path("games/<int:game_id>", views.get_game_detail),
    path("games/<int:game_id>/reviews", views.list_game_reviews),
    path("games/<int:game_id>/review", views.my_review_endpoint),
]