import type { Player, Room } from '../types';

interface Props {
  room: Room;
  player: Player;
  scores: Record<string, number>;
  onPlayAgain: () => void;
}

export default function Result({ room, player, scores, onPlayAgain }: Props) {
  // Puan almayan oyuncular skor haritasinda yok; oda listesinden tamamliyoruz.
  const ranked = [...room.players]
    .map((p) => ({ player: p, points: scores[p.id] ?? 0 }))
    .sort((a, b) => b.points - a.points);

  const top = ranked[0];
  const isWinner = top?.player.id === player.id;
  const isTie = ranked.length > 1 && ranked[0].points === ranked[1].points;

  return (
    <div className="screen screen--center">
      <div className="brand brand--small">
        <h1>{isTie ? 'Berabere' : isWinner ? 'Kazandın' : 'Kaybettin'}</h1>
        {!isTie && top && <p>Kazanan: {top.player.username} — {top.points} puan</p>}
      </div>

      <div className="card card--narrow">
        <p className="label">Sonuç tablosu</p>
        <ol className="ranking">
          {ranked.map((row, index) => (
            <li key={row.player.id} className={row.player.id === player.id ? 'ranking__me' : undefined}>
              <span className="ranking__pos">{index + 1}</span>
              <span>{row.player.username}</span>
              <strong>{row.points}</strong>
            </li>
          ))}
        </ol>

        <button className="btn btn--primary btn--lg" onClick={onPlayAgain}>
          Yeni Oyun
        </button>
        <p className="muted">Skorlar kaydedildi. Yeni oyun için yeni bir oda kurulacak.</p>
      </div>
    </div>
  );
}
