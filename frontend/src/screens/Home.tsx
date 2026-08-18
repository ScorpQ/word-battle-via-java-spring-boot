import { useState } from 'react';
import { api } from '../api';
import type { Player, Room } from '../types';

interface Props {
  player: Player | null;
  onPlayerReady: (player: Player) => void;
  onRoomReady: (room: Room) => void;
}

export default function Home({ player, onPlayerReady, onRoomReady }: Props) {
  const [code, setCode] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /** Oyuncu henuz yoksa once guest olusturur; iki ekrana bolmeye gerek yok. */
  async function ensurePlayer(): Promise<Player> {
    if (player) return player;
    const created = await api.createGuest();
    onPlayerReady(created);
    return created;
  }

  async function run(action: (player: Player) => Promise<Room>) {
    setBusy(true);
    setError(null);
    try {
      onRoomReady(await action(await ensurePlayer()));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Bir şeyler ters gitti');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="screen screen--center">
      <div className="brand">
        <h1>WordBattle</h1>
        <p>Kelime ekranda belirir. En hızlı bilen puanı alır.</p>
      </div>

      <div className="card card--narrow">
        <button className="btn btn--primary btn--lg" disabled={busy} onClick={() => run((p) => api.createRoom(p.id))}>
          Yeni Oda Kur
        </button>

        <div className="divider"><span>veya</span></div>

        <form
          className="field-row"
          onSubmit={(event) => {
            event.preventDefault();
            if (code.trim()) run((p) => api.joinRoom(code.trim().toUpperCase(), p.id));
          }}
        >
          <input
            value={code}
            onChange={(event) => setCode(event.target.value.toUpperCase())}
            placeholder="ODA KODU"
            maxLength={6}
            autoComplete="off"
            spellCheck={false}
          />
          <button className="btn" type="submit" disabled={busy || !code.trim()}>
            Katıl
          </button>
        </form>

        {error && <p className="alert">{error}</p>}
      </div>

      {player && <p className="muted">Oyuncu: {player.username}</p>}
    </div>
  );
}
