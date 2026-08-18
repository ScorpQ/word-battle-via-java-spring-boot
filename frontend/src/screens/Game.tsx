import { useEffect, useRef, useState } from 'react';
import type { Player, Room } from '../types';
import type { useGameSocket } from '../useGameSocket';

interface Props {
  room: Room;
  player: Player;
  game: ReturnType<typeof useGameSocket>;
}

export default function Game({ room, player, game }: Props) {
  const [answer, setAnswer] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  // Yeni tur baslayinca kutuyu temizleyip odagi geri veriyoruz; oyuncu
  // klavyeden elini cekmeden oynayabilsin.
  useEffect(() => {
    setAnswer('');
    if (!game.roundOver) inputRef.current?.focus();
  }, [game.round, game.roundOver]);

  const ranked = [...room.players].sort(
    (a, b) => (game.scores[b.id] ?? 0) - (game.scores[a.id] ?? 0),
  );

  return (
    <div className="screen">
      <header className="topbar">
        <span className="label">Tur {game.round} / {room.totalRounds}</span>
        <span className="label">
          <span className={game.connected ? 'dot dot--on' : 'dot'} />
          {room.code}
        </span>
      </header>

      <div className="layout">
        <main className="stage">
          <p className="label">{game.roundOver ? 'Tur bitti' : 'Bu kelimeyi yaz'}</p>
          <p className={game.roundOver ? 'word word--dim' : 'word'}>{game.word ?? '…'}</p>

          <form
            className="field-row field-row--wide"
            onSubmit={(event) => {
              event.preventDefault();
              game.sendAnswer(answer);
              setAnswer('');
            }}
          >
            <input
              ref={inputRef}
              value={answer}
              onChange={(event) => setAnswer(event.target.value)}
              placeholder={game.roundOver ? 'Sonraki tur bekleniyor…' : 'cevabını yaz'}
              disabled={game.roundOver}
              autoComplete="off"
              spellCheck={false}
            />
            <button className="btn btn--primary" type="submit" disabled={game.roundOver || !answer.trim()}>
              Gönder
            </button>
          </form>
        </main>

        <aside className="side">
          <section className="card">
            <p className="label">Skor</p>
            <ul className="players">
              {ranked.map((p) => (
                <li key={p.id}>
                  <span>
                    {p.username}
                    {p.id === player.id && <em className="tag tag--me">sen</em>}
                  </span>
                  <strong>{game.scores[p.id] ?? 0}</strong>
                </li>
              ))}
            </ul>
          </section>

          <section className="card">
            <p className="label">Akış</p>
            <ul className="feed">
              {game.feed.length === 0 && <li className="feed__empty">henüz bir şey olmadı</li>}
              {game.feed.map((item) => (
                <li key={item.id} className={`feed__item feed__item--${item.tone}`}>
                  {item.text}
                </li>
              ))}
            </ul>
          </section>
        </aside>
      </div>
    </div>
  );
}
