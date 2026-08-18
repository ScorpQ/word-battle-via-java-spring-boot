export interface Player {
  id: string;
  username: string;
  email: string | null;
  guest: boolean;
  createdAt: string;
}

export type RoomStatus = 'WAITING' | 'IN_GAME' | 'FINISHED';

export interface Room {
  id: string;
  code: string;
  status: RoomStatus;
  maxPlayers: number;
  currentRound: number;
  totalRounds: number;
  host: Player;
  players: Player[];
  createdAt: string;
}

/** Oyun sonunda /topic uzerinden gelen JSON mesaji. */
export interface GameEndedMessage {
  type: 'GAME_ENDED';
  scores: Record<string, number>;
}

/** Ekranda gosterilen olay akisi. */
export interface FeedItem {
  id: number;
  text: string;
  tone: 'info' | 'good' | 'bad';
}
