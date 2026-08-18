import { useCallback, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import type { FeedItem, GameEndedMessage } from './types';

export type Phase = 'waiting' | 'playing' | 'ended';

const POINTS_PER_ROUND = 10;

interface Options {
  playerId: string | null;
  roomCode: string | null;
  /** PLAYER_JOINED geldiginde oda bilgisini tazelemek icin cagirilir. */
  onRosterChanged: () => void;
}

/**
 * Backend ile tum gercek zamanli iletisim burada toplaniyor: baglanti yasam
 * dongusu, abonelikler ve protokol cozumleme. Ekranlar sadece hazir state
 * aliyor, hicbiri STOMP bilmiyor.
 */
export function useGameSocket({ playerId, roomCode, onRosterChanged }: Options) {
  const [connected, setConnected] = useState(false);
  const [phase, setPhase] = useState<Phase>('waiting');
  const [round, setRound] = useState(0);
  const [word, setWord] = useState<string | null>(null);
  const [roundOver, setRoundOver] = useState(false);
  const [scores, setScores] = useState<Record<string, number>>({});
  const [feed, setFeed] = useState<FeedItem[]>([]);

  const clientRef = useRef<Client | null>(null);

  // Abonelik geri cagirimlari icinde guncel kalmasi gereken degerler. State
  // yerine ref kullaniyoruz, yoksa her degisiklikte baglanti yeniden kurulur.
  const rosterRef = useRef(onRosterChanged);
  rosterRef.current = onRosterChanged;

  const push = useCallback((text: string, tone: FeedItem['tone'] = 'info') => {
    setFeed((prev) => [{ id: Date.now() + Math.random(), text, tone }, ...prev].slice(0, 40));
  }, []);

  useEffect(() => {
    if (!playerId || !roomCode) return;

    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';

    const client = new Client({
      brokerURL: `${protocol}//${location.host}/ws`,
      // Sunucu bu header'i okuyup STOMP oturumuyla esliyor; kisiye ozel
      // mesajlarin dogru sekmeye ulasmasi buna bagli.
      connectHeaders: { playerId },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        setConnected(true);

        client.subscribe(`/topic/room/${roomCode}`, (message) => {
          handleRoomMessage(message.body);
        });

        // Yalnizca bu oyuncuya gonderilen mesajlar (su an: WRONG_ANSWER).
        client.subscribe(`/user/queue/answer/${roomCode}`, () => {
          push('Olmadı — yanlış cevap ya da rakip daha hızlıydı', 'bad');
        });
      },

      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
      onStompError: (frame) => push(`Sunucu hatası: ${frame.headers.message ?? ''}`, 'bad'),
    });

    function handleRoomMessage(body: string) {
      // Oyun sonu tek JSON mesaj olarak geliyor, digerleri "ONEK:arg:arg" duz metin.
      if (body.startsWith('{')) {
        const message = JSON.parse(body) as GameEndedMessage;
        if (message.type === 'GAME_ENDED') {
          setScores(message.scores);
          setPhase('ended');
          setWord(null);
        }
        return;
      }

      const [prefix, ...args] = body.split(':');

      switch (prefix) {
        case 'PLAYER_JOINED':
          rosterRef.current();
          push('Yeni oyuncu katıldı');
          break;

        case 'GAME_STARTED':
          setPhase('playing');
          setScores({});
          setFeed([]);
          break;

        case 'ROUND_STARTED':
          setRound(Number(args[0]));
          setWord(args[1] ?? null);
          setRoundOver(false);
          break;

        case 'ROUND_TIMEOUT':
          setRoundOver(true);
          push(`Süre doldu — kimse bilemedi: ${args[1] ?? ''}`, 'bad');
          break;

        case 'CORRECT_ANSWER': {
          const winnerId = args[0];
          setRoundOver(true);
          // Canli skor tablosu icin yerel sayac; oyun sonunda sunucunun
          // gonderdigi kesin degerlerle degistiriliyor.
          setScores((prev) => ({ ...prev, [winnerId]: (prev[winnerId] ?? 0) + POINTS_PER_ROUND }));
          push(winnerId === playerId ? 'Doğru! +10' : 'Turu rakip aldı', winnerId === playerId ? 'good' : 'bad');
          break;
        }
      }
    }

    client.activate();
    clientRef.current = client;

    return () => {
      clientRef.current = null;
      void client.deactivate();
    };
  }, [playerId, roomCode, push]);

  const startGame = useCallback(() => {
    if (!clientRef.current?.connected || !roomCode || !playerId) return;
    clientRef.current.publish({
      destination: `/app/room/${roomCode}/start`,
      body: JSON.stringify({ playerId }),
    });
  }, [playerId, roomCode]);

  const sendAnswer = useCallback(
    (answer: string) => {
      const trimmed = answer.trim();
      if (!trimmed || !clientRef.current?.connected || !roomCode || !playerId) return;
      clientRef.current.publish({
        destination: `/app/room/${roomCode}/answer`,
        body: JSON.stringify({ playerId, answer: trimmed }),
      });
    },
    [playerId, roomCode],
  );

  return { connected, phase, round, word, roundOver, scores, feed, startGame, sendAnswer };
}
