import type { Player, Room } from './types';

/**
 * Backend hata durumunda duz metin bir RuntimeException mesaji donuyor
 * ("Oda dolu", "Oyun zaten başladı" gibi). Govdeyi okuyup kullaniciya
 * oldugu gibi gosteriyoruz; yoksa HTTP kodu ile yetiniyoruz.
 */
async function request<T>(method: string, path: string): Promise<T> {
  const response = await fetch(path, { method });

  if (!response.ok) {
    const body = await response.text().catch(() => '');
    throw new Error(extractMessage(body) ?? `İstek başarısız (HTTP ${response.status})`);
  }

  return response.json() as Promise<T>;
}

function extractMessage(body: string): string | null {
  if (!body) return null;

  try {
    const parsed = JSON.parse(body);
    return parsed.message ?? parsed.error ?? null;
  } catch {
    return body.slice(0, 200);
  }
}

export const api = {
  createGuest: () => request<Player>('POST', '/api/players/guest'),

  getRoom: (roomCode: string) => request<Room>('GET', `/api/rooms/${roomCode}`),

  createRoom: (playerId: string) => request<Room>('POST', `/api/rooms/create/${playerId}`),

  joinRoom: (roomCode: string, playerId: string) =>
    request<Room>('POST', `/api/rooms/join/${roomCode}/${playerId}`),
};
