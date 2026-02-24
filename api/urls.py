from django.urls import path
from . import views_auth

urlpatterns = [
    path("auth/register", views_auth.register),
    path("auth/login", views_auth.login),
    path("auth/refresh", views_auth.refresh),
    path("me", views_auth.me),
]