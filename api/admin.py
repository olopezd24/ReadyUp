from django.contrib import admin
from .models import Game, Review, UserGameStatus

admin.site.register(Game)
admin.site.register(Review)
admin.site.register(UserGameStatus)