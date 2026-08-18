import { useState } from 'react';
import type { Player, Room } from '../types';

interface Props {
  room: Room;
  player: Player;
  connected: boolean;
  onStart: () => void;
  onLeave: () => void;
}

export default function Lobby({ room, player, connected, onStart, onLeave }: Props) {
  const [copied, setCopied] = useState(false);

  const isHost = room.host.id === player.id;
  const enoughPlayers = room.players.length >= 2;

  function copyCode() {
    navigator.clipboard.writeText(room.code).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }

  return (
    <div className="screen screen--center">
      <div className="brand brand--small">
        <h1>WordBattle</h1>
      </div>

      <div className="card card--narrow">
        <p className="label">Oda kodu</p>
        <button className="code" onClick={copyCode} title="Kopyalamak için tıkla">
          {room.code}
        </button>
        <p className="muted">{copied ? 'Kopyalandı' : 'Arkadaşına bu kodu gönder'}</p>

        <p className="label">Oyuncular ({room.players.length}/{room.maxPlayers})</p>
        <ul className="players">
          {room.players.map((p) => (
            <li key={p.id}>
              <span>{p.username}</span>
              <span className="tags">
                {p.id === room.host.id && <em className="tag">host</em>}
                {p.id === player.id && <em className="tag tag--me">sen</em>}
              </span>
            </li>
          ))}
        </ul>

        {isHost ? (
          <>
            <button className="btn btn--primary btn--lg" disabled={!connected || !enoughPlayers} onClick={onStart}>
              Oyunu Başlat
            </button>
            {!enoughPlayers && <p className="muted">Başlamak için en az 2 oyuncu gerekiyor.</p>}
          </>
        ) : (
          <p className="waiting">Host'un oyunu başlatması bekleniyor…</p>
        )}

        <div className="status">
          <span className={connected ? 'dot dot--on' : 'dot'} />
          {connected ? 'Bağlı' : 'Bağlanıyor…'}
        </div>
      </div>

      <button className="btn btn--ghost" onClick={onLeave}>
        Odadan çık
      </button>
    </div>
  );
}
