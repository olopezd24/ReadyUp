from django.urls import path
from . import views_auth, views

urlpatterns = [
    path("auth/register", views_auth.register),
    path("auth/login", views_auth.login),
    path("auth/refresh", views_auth.refresh),

    path("me", views_auth.me),
    path("me/reviews", views.list_my_reviews),
    path("me/status", views.me_status_endpoint),
    path("me/status/<int:game_id>", views.me_status_game_endpoint),
    path("me/reports", views.list_my_reports),

    path("games", views.list_games),
    path("games/<int:game_id>", views.get_game_detail),
    path("games/<int:game_id>/reviews", views.list_game_reviews),
    path("games/<int:game_id>/review", views.my_review_endpoint),

    path("users/<int:user_id>/follow", views.follow_endpoint),
    path("users/<int:user_id>/followers", views.list_followers),
    path("users/<int:user_id>/following", views.list_following),

    path("feed", views.feed),

    path("reports", views.create_report),
]