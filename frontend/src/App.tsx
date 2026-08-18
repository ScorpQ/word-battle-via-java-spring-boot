import { useCallback, useState } from 'react';
import { api } from './api';
import { useGameSocket } from './useGameSocket';
import type { Player, Room } from './types';
import Home from './screens/Home';
import Lobby from './screens/Lobby';
import Game from './screens/Game';
import Result from './screens/Result';

export default function App() {
  const [player, setPlayer] = useState<Player | null>(null);
  const [room, setRoom] = useState<Room | null>(null);

  // Oyuncu listesi WebSocket'ten gelmiyor: PLAYER_JOINED sadece "birisi katildi"
  // diyor, kim oldugunu REST'ten tazeliyoruz.
  const refreshRoom = useCallback(() => {
    if (!room) return;
    api.getRoom(room.code).then(setRoom).catch(() => undefined);
  }, [room]);

  const game = useGameSocket({
    playerId: player?.id ?? null,
    roomCode: room?.code ?? null,
    onRosterChanged: refreshRoom,
  });

  const leaveRoom = () => setRoom(null);

  if (!room || !player) {
    return <Home player={player} onPlayerReady={setPlayer} onRoomReady={setRoom} />;
  }

  if (game.phase === 'ended') {
    return <Result room={room} player={player} scores={game.scores} onPlayAgain={leaveRoom} />;
  }

  if (game.phase === 'playing') {
    return <Game room={room} player={player} game={game} />;
  }

  return <Lobby room={room} player={player} connected={game.connected} onStart={game.startGame} onLeave={leaveRoom} />;
}
