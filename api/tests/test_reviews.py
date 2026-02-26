from django.test import TestCase, Client
from api.models import Game, Review
import json


class ReviewAPITests(TestCase):

    def setUp(self):
        self.client = Client()
        self.game = Game.objects.create(title="Zelda")

        # Registrar usuario
        self.client.post(
            "/api/auth/register",
            data=json.dumps({
                "username": "u1",
                "email": "u1@test.com",
                "password": "pass1234"
            }),
            content_type="application/json"
        )

        # Login
        login_resp = self.client.post(
            "/api/auth/login",
            data=json.dumps({
                "username": "u1",
                "password": "pass1234"
            }),
            content_type="application/json"
        )

        self.access = login_resp.json()["access"]

    def auth_header(self):
        return {"HTTP_AUTHORIZATION": f"Bearer {self.access}"}

    def test_create_review_ok(self):
        resp = self.client.post(
            f"/api/games/{self.game.id}/review",
            data=json.dumps({
                "rating": 8,
                "text": "Muy bueno"
            }),
            content_type="application/json",
            **self.auth_header()
        )

        self.assertEqual(resp.status_code, 201)
        self.assertEqual(Review.objects.count(), 1)

    def test_create_review_duplicate(self):
        # Primera review
        self.client.post(
            f"/api/games/{self.game.id}/review",
            data=json.dumps({"rating": 8}),
            content_type="application/json",
            **self.auth_header()
        )

        # Segunda (debe fallar)
        resp = self.client.post(
            f"/api/games/{self.game.id}/review",
            data=json.dumps({"rating": 9}),
            content_type="application/json",
            **self.auth_header()
        )

        self.assertEqual(resp.status_code, 409)
        self.assertEqual(Review.objects.count(), 1)

    def test_rating_out_of_range(self):
        resp = self.client.post(
            f"/api/games/{self.game.id}/review",
            data=json.dumps({"rating": 20}),
            content_type="application/json",
            **self.auth_header()
        )

        self.assertEqual(resp.status_code, 400)