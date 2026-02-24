from django.db import models
from django.contrib.auth.models import User

class Game(models.Model):
    title = models.CharField(max_length=200, db_index=True)
    description = models.TextField(blank=True)
    release_date = models.DateField(null=True, blank=True)
    cover_url = models.URLField(null=True, blank=True)
    avg_rating = models.FloatField(default=0)

    genre = models.CharField(max_length=100, blank=True, db_index=True)
    platform = models.CharField(max_length=100, blank=True, db_index=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

class Review(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="reviews")
    game = models.ForeignKey(Game, on_delete=models.CASCADE, related_name="reviews")
    rating = models.IntegerField()
    text = models.TextField(blank=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["user", "game"], name="unique_review_per_user_game")
        ]

class UserGameStatus(models.Model):
    class Status(models.TextChoices):
        PLAYING = "PLAYING"
        BACKLOG = "BACKLOG"
        COMPLETED = "COMPLETED"
        DROPPED = "DROPPED"

    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="statuses")
    game = models.ForeignKey(Game, on_delete=models.CASCADE, related_name="statuses")
    status = models.CharField(max_length=20, choices=Status.choices)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["user", "game"], name="unique_status_per_user_game")
        ]
        indexes = [
            models.Index(fields=["user", "status"]),
        ]