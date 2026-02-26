from django.contrib import admin
from .models import Game, Review, UserGameStatus, Report, Follow, Notification

admin.site.register(Game)
admin.site.register(Review)
admin.site.register(UserGameStatus)
admin.site.register(Follow)
admin.site.register(Notification)
admin.site.register(Report)