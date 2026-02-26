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
        verbose_name = "User game status"
        verbose_name_plural = "User game statuses"
        constraints = [
            models.UniqueConstraint(
                fields=["user", "game"],
                name="unique_status_per_user_game"
            )
        ]
        indexes = [
            models.Index(fields=["user", "status"]),
        ]

class Follow(models.Model):
    follower = models.ForeignKey(User, on_delete=models.CASCADE, related_name="following")
    following = models.ForeignKey(User, on_delete=models.CASCADE, related_name="followers")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["follower", "following"], name="uniq_follow")
        ]
        indexes = [
            models.Index(fields=["follower", "following"]),
        ]

    def _str__(self):
        return f"{self.follower.username} -> {self.following.username}"

class Notification(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="notifications")
    type = models.CharField(max_length=32)
    actor = models.ForeignKey(User, null=True, blank=True, on_delete=models.SET_NULL, related_name="notifications_sent")
    payload = models.JSONField(default=dict, blank=True)
    is_read = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        indexes = [
            models.Index(fields=["user","is_read", "created_at"]),
        ]

class Report(models.Model):
    TARGET_CHOICES = [
        ("review", "Review"),
        ("game", "Game"),
        ("user", "User"),
    ]

    STATUS_CHOICES = [
        ("open", "Open"),
        ("resolved", "Resolved"),
        ("rejected", "Rejected"),
    ]

    reporter = models.ForeignKey(User, on_delete=models.CASCADE, related_name="reports_made")
    target_type = models.CharField(max_length=16, choices=TARGET_CHOICES)
    target_id = models.PositiveIntegerField()
    reason = models.CharField(max_length=64)
    message = models.TextField(blank=True)
    status = models.CharField(max_length=16, choices=STATUS_CHOICES, default="open")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["reporter", "target_type", "target_id"], name="uniq_report_per_target")
        ]
        indexes = [
            models.Index(fields=["target_type", "target_id"]),
            models.Index(fields=["status", "created_at"]),
        ]
