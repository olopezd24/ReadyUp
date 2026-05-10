"""
Script de importación de juegos desde RAWG API → Django ReadyUp
================================================================
Uso:
    1. Pon tu API key de RAWG en la variable RAWG_API_KEY
    2. Ejecuta desde la carpeta raíz del proyecto Django:
       python import_games.py

Obtén tu API key gratuita en: https://rawg.io/apidocs
"""

import os
import sys
import django
import requests
import time

# ── Configuración ─────────────────────────────────────────────────────────────

RAWG_API_KEY = "a06455b6f9d24c4197d84d0a4145987a"
TOTAL_GAMES  = 500
PAGE_SIZE    = 40
SLEEP_BETWEEN_REQUESTS = 0.5

# Ordenación: relevance | released | added | created | updated | rating | metacritic
ORDERING = "-rating"

# Géneros permitidos — solo se importan juegos que pertenezcan a uno de estos
ALLOWED_GENRES = [
    "action", "adventure", "rpg", "shooter", "strategy", "puzzle",
    "racing", "sports", "simulation", "arcade", "platformer", "fighting",
    "family", "indie", "casual", "massively-multiplayer", "card", "board-games"
]

# ── Setup Django ──────────────────────────────────────────────────────────────

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "ReadyUp.settings")
django.setup()

from api.models import Game

# ── Helpers ───────────────────────────────────────────────────────────────────

def get_games_page(page: int) -> dict:
    params = {
        "key":       RAWG_API_KEY,
        "page":      page,
        "page_size": PAGE_SIZE,
        "ordering":  ORDERING,
        "mature":    False,
    }
    resp = requests.get("https://api.rawg.io/api/games", params=params, timeout=15)
    resp.raise_for_status()
    return resp.json()


def get_game_description(rawg_id: int) -> str:
    """Obtiene la descripción completa de un juego (requiere llamada extra)."""
    try:
        resp = requests.get(
            f"https://api.rawg.io/api/games/{rawg_id}",
            params={"key": RAWG_API_KEY},
            timeout=15
        )
        if resp.ok:
            data = resp.json()
            desc = data.get("description_raw") or data.get("description") or ""
            return desc[:2000]
    except Exception:
        pass
    return ""


def parse_genre(game_data: dict) -> str:
    genres = game_data.get("genres") or []
    if genres:
        return genres[0].get("name", "")
    return ""


def parse_genre_slug(game_data: dict) -> str:
    genres = game_data.get("genres") or []
    if genres:
        return genres[0].get("slug", "")
    return ""


def parse_platform(game_data: dict) -> str:
    platforms = game_data.get("platforms") or []
    names = []
    for p in platforms:
        name = p.get("platform", {}).get("name", "")
        if name:
            names.append(name)
    return ", ".join(names[:2])


def parse_cover(game_data: dict) -> str:
    return game_data.get("background_image") or ""


def parse_date(game_data: dict):
    date_str = game_data.get("released")
    if not date_str:
        return None
    try:
        from datetime import date
        parts = date_str.split("-")
        return date(int(parts[0]), int(parts[1]), int(parts[2]))
    except Exception:
        return None

# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    if RAWG_API_KEY == "PON_AQUI_TU_API_KEY":
        print("❌ ERROR: Pon tu API key de RAWG en la variable RAWG_API_KEY")
        print("   Obtén una gratis en: https://rawg.io/apidocs")
        sys.exit(1)

    print(f"🎮 Iniciando importación de hasta {TOTAL_GAMES} juegos desde RAWG...")
    print(f"   Ordenación: {ORDERING}")
    print(f"   Géneros permitidos: {', '.join(ALLOWED_GENRES)}")
    print()

    imported  = 0
    skipped   = 0
    filtered  = 0
    errors    = 0
    page      = 1

    while imported < TOTAL_GAMES:
        try:
            print(f"  📡 Página {page}...", end=" ", flush=True)
            data = get_games_page(page)
            results = data.get("results", [])

            if not results:
                print("sin más resultados.")
                break

            for g in results:
                if imported >= TOTAL_GAMES:
                    break

                title = g.get("name", "").strip()
                if not title:
                    continue

                # Filtrar por género permitido
                genre_slug = parse_genre_slug(g).lower()
                if not genre_slug or genre_slug not in ALLOWED_GENRES:
                    filtered += 1
                    continue

                # Evitar duplicados por título
                if Game.objects.filter(title=title).exists():
                    skipped += 1
                    continue

                # Descripción (llamada extra por juego)
                rawg_id = g.get("id")
                description = ""
                if rawg_id:
                    description = get_game_description(rawg_id)
                    time.sleep(SLEEP_BETWEEN_REQUESTS)

                try:
                    Game.objects.create(
                        title        = title,
                        description  = description,
                        release_date = parse_date(g),
                        cover_url    = parse_cover(g),
                        avg_rating   = 0,
                        genre        = parse_genre(g),
                        platform     = parse_platform(g),
                    )
                    imported += 1
                    print(f"✅ [{imported}] {title} ({parse_genre(g)})")
                except Exception as e:
                    errors += 1
                    print(f"⚠️  Error guardando '{title}': {e}")

            page += 1
            time.sleep(SLEEP_BETWEEN_REQUESTS)

            if not data.get("next"):
                break

        except requests.exceptions.RequestException as e:
            print(f"\n❌ Error de red en página {page}: {e}")
            print("   Esperando 5 segundos y reintentando...")
            time.sleep(5)
            continue

    print()
    print("=" * 50)
    print(f"✅ Importados:  {imported} juegos")
    print(f"⏭️  Omitidos:   {skipped} (ya existían)")
    print(f"🚫 Filtrados:  {filtered} (género no permitido)")
    print(f"❌ Errores:    {errors}")
    print(f"📦 Total en BD: {Game.objects.count()} juegos")


if __name__ == "__main__":
    main()